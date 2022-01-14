package com.wx.tools.model.service

import android.content.Context
import android.database.Cursor
import android.database.CursorWrapper
import com.wx.tools.bean.*
import com.wx.tools.callback.DBCallback
import com.wx.tools.controller.Constant
import com.wx.tools.controller.DBManager
import com.wx.tools.controller.PwdManager
import com.wx.tools.utils.AppUtil
import com.wx.tools.utils.JLog
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteDatabaseHook
import java.io.File

/**
@author ZW
@description:
@date : 2020/11/25 14:31
 */
object MicroMsgService {

    fun readWeChatDatabase(context: Context, pwd: Password, srcCreateTime: Long, dbFile: File, index: Int, callback: DBCallback) {

        var db: SQLiteDatabase? = null
        try {

            SQLiteDatabase.loadLibs(context)

            val hook: SQLiteDatabaseHook = object : SQLiteDatabaseHook {
                override fun preKey(database: SQLiteDatabase?) {
                }

                override fun postKey(database: SQLiteDatabase?) {
                    database?.rawExecSQL("PRAGMA cipher_page_size = 1024;")
                    database?.rawExecSQL("PRAGMA cipher_hmac = NO;")
                    database?.rawExecSQL("PRAGMA kdf_iter = 4000;")
                    database?.rawExecSQL("PRAGMA cipher_kdf_algorithm = PBKDF2_HMAC_SHA1;")
                    database?.rawExecSQL("PRAGMA cipher_migrate;")
                }
            }

            db = SQLiteDatabase.openOrCreateDatabase(dbFile, pwd.microPwd, null, hook, { JLog.i("open db exception") })


//            queryTableNames(db)
//            queryColumnNames(db, "message")
//            queryColumnNames(db, "contact")
//            queryColumnNames(db, "voiceinfo")
//            queryColumnNames(db, "userinfo")

            val userInfo = openUserInfoTable(db, callback)
            val avatars = openAvatarTable(db)
            val contacts = openContactTable(db, index, callback)
            val conversations = openConversationTable(db, index, callback)

            if (userInfo.userName == "") {
                db.close()
                callback.onFailed(FileStatus.DECODE_ACCOUNT, Constant.NOT_FOUND_ACCOUNT)
                return
            }

            if (contacts.isEmpty()) {
                db.close()
                callback.onFailed(FileStatus.DECODE_CONTACT, Constant.NOT_FOUND_CONTACT)
                return
            }

            //处理Account
            val account = analyzedAccount(userInfo, srcCreateTime, avatars)
            DBManager.insert(context, account)
            callback.onProgress(FileStatus.DECODE_ACCOUNT, "正在处理账号：${account.nickName}")

            //处理Contacts
            val contactList = analyzedContact(userInfo, srcCreateTime, contacts, avatars)
            for (contact in contactList) {
                DBManager.insert(context, contact)
            }

            val path = dbFile.path.replace(dbFile.name, Constant.DB_FTS_NAME)
            JLog.i("microMsg path = ${dbFile.path}")
            JLog.i("ftsIndex path = $path")
            val md5 = PwdManager.getFTSPwd(pwd.imei, userInfo.userName, pwd.uin)
            pwd.FTSPwd = md5
            FTS5IndexMicroMsgService.readWeChatDatabase(context, account, md5, srcCreateTime, path)

            //处理Messages
            openMessageTable(context, db, account, srcCreateTime, contactList, conversations, callback)

            db.close()

        } catch (e: Exception) {
            db?.close()
            SQLiteDatabase.releaseMemory()
            JLog.i("sqlite error = ${e.message}")
        }
    }

    /**
     * 查询数据库所有的表名
     * @param db
     */
    private fun queryTableNames(db: SQLiteDatabase) {
        val cw = CursorWrapper(db.rawQuery("SELECT * FROM sqlite_master ORDER BY type DESC", null))
        val colCount: Int = cw.columnCount
        val mapList: MutableList<Map<String, Any>> = ArrayList(cw.count)
        while (cw.moveToNext()) {
            val map: MutableMap<String, Any> = HashMap()
            for (i in 0 until colCount) {
                val name: String = cw.getColumnName(i)
                val type: Int = cw.getType(i)
                if (type == Cursor.FIELD_TYPE_BLOB) {
                    map[name] = cw.getBlob(i)
                    JLog.i("blob name = ${map[name]}")
                } else {
                    map[name] = cw.getString(i)
                    JLog.i("table name = ${map[name]}")
                }
            }
            mapList.add(map)
        }
    }


    /**
     * 查询表里所有的列名
     * @param
     */
    private fun queryColumnNames(db: SQLiteDatabase, tableName: String) {
        val c: Cursor = db.query(tableName, null, null, null, null, null, null)
        while (c.moveToNext()) {
            val id = c.getInt(c.getColumnIndex("id"))
            val type = c.getInt(c.getColumnIndex("type"))
            val value = c.getString(c.getColumnIndex("value"))
            JLog.i("id = $id")
            JLog.i("type = $type")
            JLog.i("value = $value")
        }

        c.close()
    }

    /**
     * 获得账户信息
     */
    private fun openUserInfoTable(db: SQLiteDatabase, callback: DBCallback): Account {
        var userName = ""
        var nickName: String? = null
        var alias: String? = null
        var mail: String? = null
        var qq: String? = null
        var phone: String? = null
        var region = ""
        val c: Cursor = db.rawQuery("select * from userinfo", null)
        JLog.i("find userInfo rows = ${c.count}")
        while (c.moveToNext()) {
            val id: Int = c.getInt(c.getColumnIndex("id"))
            val value: String = c.getString(c.getColumnIndex("value"))
            when (id) {
                2 -> userName = value
                4 -> nickName = value
                5 -> mail = value
                6 -> phone = value
                9 -> qq = value
                42 -> alias = value
                12293 -> region = value
            }
        }

        if (userName.isNotEmpty()) {
            callback.onSuccess(FileStatus.DECODE_ACCOUNT)
        }

        return Account(userName, userName, nickName, alias, null, null, -1, mail, phone, qq, region, 0L)
    }

    /**
     * 打开聊天记录表
     * @param
     * type =
     * 1000 系统消息
     * 1：文本消息 包含小表情
     * 图片消息，相机中的照片和配置有不同，从相册中发送的消息中会保留一个 MMAsset，如同 PAAset：3
     * 48：位置消息
     * 34：语音消息 字段imagepath字段就代表该语音文件的文件名字，格式为msg_151806100918b4cf11fefe0106.amr，存储当前路径的
     * 改目录以用户的id为规则生成和上面数据库的名字规则一样就可以区分了，里面详细的目录
     * 42：名片消息，公众号名片和普通名片用的是同一种类型
     * 大表情：47
     * 分享消息，这种消息会含有多种类型，比如分享的收藏，分享的小程序，微信红包等等。这种消息类型可以避免不断添加多种消息类型，像这种预先定义一种消息类型，预留一些字段，这样产品添加消息类型的时候，UI
     * 可以任意组合：49
     */
    private fun openMessageTable(
        context: Context,
        db: SQLiteDatabase,
        account: Account,
        time: Long,
        contacts: MutableList<Contact>,
        conversations: MutableList<Conversation>,
        callback: DBCallback
    ) {

        callback.onProgress(FileStatus.DECODE_MESSAGE, "")

        var position = 0
        for (contact in contacts) {
            val username = contact.userName
            val c = db.rawQuery("SELECT * from message where talker='$username' order by msgId", null)
            val count = c.count

            val messageInfo = arrayListOf<Message>()
            while (c.moveToNext()) {
                val msgId: Int = c.getInt(c.getColumnIndex("msgId"))
                val talker: String = c.getString(c.getColumnIndex("talker"))
                val isSend: Int = c.getInt(c.getColumnIndex("isSend"))
                val type: Int = c.getInt(c.getColumnIndex("type"))
                val content: String? = c.getString(c.getColumnIndex("content"))
                val createTimeStamp: String = c.getString(c.getColumnIndex("createTime"))
                val date: String = AppUtil.timeStamp2Date(createTimeStamp, null)
                val imgPath: String? = c.getString(c.getColumnIndex("imgPath"))

                val message = Message("${account.userName}:$msgId", account.userName, talker, null, type, isSend, content, date, imgPath)
                messageInfo.add(message)
            }

            if (count != 0) {
                analyzedData(context, account, time, messageInfo, contacts, contact, conversations, callback)
            }

            position++
            val percent = position * 100 / contacts.size
            callback.onProgress(FileStatus.DECODE_MESSAGE, percent)
            messageInfo.clear()
            c.close()
        }
    }


    /**
     * 打开联系人列表
     * @param
     * verifyFlag = 0 是好友加群组加关注的公众号
     * type:type=4微信群里非好友，type=2微信群 type=0公众号 type=33官方账号 type=8可能为删除的好友
     * alias为修改后的微信号，username是原始微信号，如果没有自定义微信号，那么alias为空
     */
    private fun openContactTable(db: SQLiteDatabase, percent: Int, callback: DBCallback): MutableList<Contact> {

        val contacts = mutableListOf<Contact>()

        val c: Cursor = db.rawQuery(
            "SELECT * from rcontact WHERE verifyFlag=0 AND type!=33 " +
                    "AND type!=35 and nickname!='' AND nickname != '该账号已注销'  order by pyInitial",
            null
        )

        val count = c.count
        JLog.i("find contacts rows = ${c.count}")

        while (c.moveToNext()) {

            val alias: String = c.getString(c.getColumnIndex("alias"))
            val type: Int = c.getInt(c.getColumnIndex("type"))
            val username: String = c.getString(c.getColumnIndex("username"))
            val nickname: String = c.getString(c.getColumnIndex("nickname"))
            val conRemark: String = c.getString(c.getColumnIndex("conRemark"))

//            JLog.i("type = $type")
//            JLog.i("username = $username")
//            JLog.i("nickname = $nickname")
//            JLog.i("alias = $alias")
//            JLog.i("verifyFlag = $verifyFlag")
//            JLog.i("deleteFlag = $deleteFlag")

            contacts.add(Contact(username, username, nickname, alias, "", conRemark, type))

//            val d = c.position * percent / count
//            callback.onProgress(FileStatus.DECODE_CONTACT, d)

        }

        c.close()
        return contacts
    }

    /**
     * 打开头像列表
     * @param
     * imgflag:3为用户
     */
    private fun openAvatarTable(db: SQLiteDatabase): MutableList<Avatar> {

        val avatars = mutableListOf<Avatar>()

        val c: Cursor = db.rawQuery("select * from img_flag", null)

        JLog.i("find avatar rows = ${c.count}")

        while (c.moveToNext()) {

            val imgflag: Int = c.getInt(c.getColumnIndex("imgflag"))
            val lastupdatetime: Int = c.getInt(c.getColumnIndex("lastupdatetime"))
            val username: String = c.getString(c.getColumnIndex("username"))
            val reserved1: String = c.getString(c.getColumnIndex("reserved1"))
            val reserved2: String = c.getString(c.getColumnIndex("reserved2"))

//            JLog.i("imgflag = $imgflag")
//            JLog.i("username = $username")
//            JLog.i("lastupdatetime = $lastupdatetime")
//            JLog.i("reserved1 = $reserved1")
//            JLog.i("reserved2 = $reserved2")

            avatars.add(Avatar(username, imgflag, lastupdatetime, reserved1, reserved2))

        }

        c.close()
        return avatars
    }

    /**
     * 打开会话列表
     * @param
     * imgflag:3为用户
     */
    private fun openConversationTable(db: SQLiteDatabase, percent: Int, callback: DBCallback): MutableList<Conversation> {

        val conversation = mutableListOf<Conversation>()

        val c: Cursor = db.rawQuery(
            "select * from rconversation where msgType=1",
            null
        )

        val count = c.count
        JLog.i("find conversation rows = ${c.count}")

        while (c.moveToNext()) {

            val msgCount: Int = c.getInt(c.getColumnIndex("msgCount"))
            val conversationTime: Int = c.getInt(c.getColumnIndex("conversationTime"))
            val username: String = c.getString(c.getColumnIndex("username"))
            val content: String = c.getString(c.getColumnIndex("content"))
            val unReadCount: Int = c.getInt(c.getColumnIndex("unReadCount"))

//            JLog.i("imgflag = $imgflag")
//            JLog.i("username = $username")
//            JLog.i("lastupdatetime = $lastupdatetime")
//            JLog.i("reserved1 = $reserved1")
//            JLog.i("reserved2 = $reserved2")

            conversation.add(Conversation(msgCount, conversationTime, username, content, unReadCount))

            val d = c.position * percent / count
            callback.onProgress(FileStatus.DECODE_CONTACT, d)

        }

        c.close()
        return conversation
    }

    private fun openContactTableExt(db: SQLiteDatabase) {
        val c: Cursor = db.query("contact_ext", null, null, null, null, null, null)
        while (c.moveToNext()) {

            val uin: Int = c.getInt(c.getColumnIndex("uin"))
            JLog.i("1")
            val username: String = c.getString(c.getColumnIndex("username"))
            JLog.i("2")
            val email: String = c.getString(c.getColumnIndex("Email"))
            JLog.i("3")
            val mobile: String = c.getString(c.getColumnIndex("Mobile"))

            JLog.i("---chat begin---")
            JLog.i("uin = $uin")
            JLog.i("username = $username")
            JLog.i("mobile = $mobile")
            JLog.i("---chat end---")
            JLog.i("  ")
        }

        c.close()
    }

    /**
     * 整理数据
     */
    private fun analyzedData(
        context: Context,
        account: Account,
        time: Long,
        messageInfo: ArrayList<Message>,
        contacts: MutableList<Contact>,
        contact: Contact,
        conversations: MutableList<Conversation>,
        callback: DBCallback
    ) {

        //得到最后一次会话列表
        val conversation = findConversation(contact.userName, conversations)

        for (message in messageInfo) {

            //群消息需要转化
            val messageTransform = analyzedMessage(contacts, message)

            //保存聊天记录
            DBManager.insert(context, messageTransform)
        }

        val talker = Talker(
            account.userName + ":" + contact.userName,
            contact.userName,
            contact.nickName,
            contact.alias,
            contact.icon,
            contact.conRemark,
            conversation,
            contact.type,
            time
        )

        //保存聊天名单列表
        DBManager.insert(context, talker)

    }

    /**
     * 匹配用户头像
     */
    private fun analyzedContact(userInfo: Account, time: Long, contacts: MutableList<Contact>, avatars: MutableList<Avatar>): MutableList<Contact> {
        val parent = arrayListOf<Contact>()
        parent.addAll(contacts)
        for ((index, child) in parent.withIndex()) {
            val username = child.userName
            //id为账户名:用户名
            contacts[index].id = userInfo.userName + ":" + contacts[index].userName + ":" + time
            contacts[index].time = time
            for (avatar in avatars) {
                if (username == avatar.userName) {
                    contacts[index].icon = avatar.reserved2
                }
            }
        }
        return contacts
    }

    /**
     * 匹配用户头像
     */

    private fun analyzedAccount(account: Account, time: Long, avatars: MutableList<Avatar>): Account {
        val username = account.userName
        for (child in avatars) {
            account.id = account.userName + ":" + time
            account.time = time
            if (username == child.userName) {
                account.icon = child.reserved2
            }
        }
        return account
    }

    /**
     * 消息转换
     */
    private fun analyzedMessage(contacts: MutableList<Contact>, message: Message): Message {
        if (message.content.isNullOrEmpty()) return message
        val m = message.content!!.split(":")
        if (m.isNullOrEmpty()) {
            JLog.i("empty message")
            return message
        } else {
            val accountName = m[0]
            for (contact in contacts) {
                if (accountName == contact.userName) {
                    message.content = contact.nickName + message.content!!.replace(m[0], "")
                    message.icon = contact.icon
                }
            }
        }
        return message
    }

    /**
     * 匹配用户头像
     */
    private fun findConversation(username: String, conversations: MutableList<Conversation>): Conversation? {
        val parent = mutableListOf<Conversation>()
        parent.addAll(conversations)
        for ((index, child) in conversations.withIndex()) {
            if (username == child.username) {
                return conversations[index]
            }
        }
        return null
    }


}