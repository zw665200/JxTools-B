package com.wx.tools.model.service

import android.content.Context
import android.database.Cursor
import android.database.CursorWrapper
import com.wx.tools.bean.*
import com.wx.tools.callback.FileCallback
import com.wx.tools.controller.DBManager
import com.wx.tools.utils.ArithmeticUtil
import com.wx.tools.utils.Binascii
import com.wx.tools.utils.JLog
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteDatabaseHook
import java.io.*

/**
@author ZW
@description:
@date : 2020/11/25 14:31
 */
object FTS5IndexMicroMsgService {

    fun readWeChatDatabase(context: Context, account: Account, pwd: String, time: Long, dbPath: String): ArrayList<FTSMetaMessage>? {

        var db: SQLiteDatabase? = null
        try {

            val hook: SQLiteDatabaseHook = object : SQLiteDatabaseHook {
                override fun preKey(database: SQLiteDatabase?) {
                }

                override fun postKey(database: SQLiteDatabase?) {
                    database?.rawExecSQL("PRAGMA cipher_hmac = NO;")
                    database?.rawExecSQL("PRAGMA cipher_page_size = 4096;")
                    database?.rawExecSQL("PRAGMA kdf_iter = 64000;")
                    database?.rawExecSQL("PRAGMA cipher_hmac_algorithm = HMAC_SHA1;")
                    database?.rawExecSQL("PRAGMA cipher_kdf_algorithm = PBKDF2_HMAC_SHA1;")
                    database?.rawExecSQL("PRAGMA cipher_migrate;")
                }

            }

            db = SQLiteDatabase.openOrCreateDatabase(dbPath, pwd, null, hook, { JLog.i("open db exception") })

            detachDatabase(context, db, account, time, dbPath)

//            queryTableNames(db)

//            val metaMessages = openMetaMessageTable(db)
//            val messages = openMessageContentTable(db)


//            if (metaMessages.isEmpty() || messages.isEmpty()) {
//                db.close()
//                return null
//            }

            db?.close()

            //解析数据
//            return analyzedData(metaMessages, messages) as ArrayList<FTSMetaMessage>

        } catch (e: Exception) {
            db?.close()
            SQLiteDatabase.releaseMemory()
            JLog.i("ftsIndex db sqlite error = ${e.message}")
        }

        return null
    }


    /**
     * 解密并分离数据库
     */
    private fun detachDatabase(context: Context, database: SQLiteDatabase, account: Account, time: Long, dbPath: String) {
        val file = File(dbPath)
        val path = file.absolutePath.replace(file.name, "FTS5IndexMicroMsg_decrypt.db")
        JLog.i("new db filePath = $path")
        database.rawExecSQL("ATTACH DATABASE '$path' AS fts5indexDecrypt KEY '';")
        database.rawExecSQL("SELECT sqlcipher_export( 'fts5indexDecrypt' );")
        database.rawExecSQL("DETACH DATABASE fts5indexDecrypt;")
        val decryptedFile = File(path)
        if (decryptedFile.exists()) {
            analyseDeletedMessage(context, account, time, decryptedFile)
        }
    }

    /**
     * 查找聊天记录碎片信息
     */
    private fun analyseDeletedMessage(context: Context, account: Account, time: Long, dbFile: File) {
        val talker = Talker(
            "${account.userName}:default",
            "default",
            "未整理的丢失的消息",
            "",
            null,
            null,
            null,
            -1,
            time
        )
        DBManager.insert(context, talker)

        val from = RandomAccessFile(dbFile, "r")
        val onebyte = ByteArray(1)
        val twobytes = ByteArray(2)
        val fourbytes = ByteArray(4)
        var offset = 0L
        while (from.read(onebyte) != -1) {
            //calculate read size
            offset += 1

            //find out the key data
            val hex = Binascii.hexlify(onebyte)
            if (hex == "03" || hex == "04") {

                from.read(onebyte)
                val nextHex = Binascii.hexlify(onebyte)
                if (nextHex != "00") continue

                //取0300或者0400前第4个字节
                from.seek(offset - 6)
                from.read(onebyte)
                val left = Binascii.hexlify(onebyte)

                //找到区域开始的位置
                if (left == "00") {
                    from.seek(offset - 4)
                    //读取key,决定这块区域是否有删除的标识
                    if (from.read(onebyte) != -1) {
                        val key = Binascii.hexlify(onebyte)

                        JLog.i("key = $key")

                        //往前移动2格
                        from.seek(offset - 2)
                        offset -= 2

                        while (from.read(twobytes) != -1) {
                            offset++

                            val value = Binascii.hexlify(twobytes)

                            //find short message
                            if (value == "0300") {
                                from.read(onebyte)
                                val lengthHex = Binascii.hexlify(onebyte)
                                val leftDecimal = Binascii.hexToDecimal(lengthHex)
                                if (leftDecimal % 2 == 0) {
                                    continue
                                }
                                val length = (leftDecimal - 13) / 2
                                if (length <= 0) {
                                    continue
                                }

                                JLog.i("0003 value length = $length")

                                val byte = ByteArray(length)
                                if (from.read(byte) != -1) {
                                    val msgHex = Binascii.hexlify(byte)
                                    val text = ArithmeticUtil.hex2Str(msgHex)
                                    if (text != "") {
                                        val message = Message(
                                            "${account.userName}:${System.currentTimeMillis()}",
                                            account.userName,
                                            "default",
                                            null,
                                            1,
                                            0,
                                            text,
                                            null,
                                            null
                                        )

                                        DBManager.insert(context, message)
                                    }

                                    JLog.i("text = $text")
                                }
                            }

                            //find long message
                            if (value == "0400") {
                                from.read(onebyte)
                                val leftHex = Binascii.hexlify(onebyte)
                                val leftBinary = Binascii.hexToBinary(leftHex)
                                val leftChars = leftBinary.toCharArray()
                                val charList = arrayListOf<Char>()

                                if (leftHex == "00") {
                                    continue
                                }

                                JLog.i("leftHex = $leftHex")
                                JLog.i("leftBinary = $leftBinary")

                                for ((position, char) in leftChars.withIndex()) {
                                    if (position == 0) {
                                        charList.add(char)
                                        continue
                                    }

                                    if (char.toString() == "0") {
                                        charList.add(char)
                                    } else {
                                        break
                                    }
                                }

                                val leftChar = leftBinary.substring(charList.size)
                                JLog.i("leftChar = $leftChar")

                                from.read(onebyte)
                                val rightHex = Binascii.hexlify(onebyte)
                                val rightChar = Binascii.hexToBinary(rightHex)

                                JLog.i("rightHex = $rightHex")
                                JLog.i("rightChar = $rightChar")

                                val char = leftChar + rightChar
                                val rightDecimal = Binascii.binaryToDecimal(char)
                                if (rightDecimal % 2 == 0) {
                                    continue
                                }
                                val length = (rightDecimal - 13) / 2
                                if (length <= 0) {
                                    continue
                                }

                                JLog.i("0004 value length = $length")

                                val byte = ByteArray(length)
                                if (from.read(byte) != -1) {
                                    val msgHex = Binascii.hexlify(byte)
                                    val text = ArithmeticUtil.hex2Str(msgHex)
                                    if (text != "") {

                                        val message = Message(
                                            "${account.userName}:default",
                                            account.userName,
                                            "default",
                                            null,
                                            1,
                                            0,
                                            text,
                                            null,
                                            null
                                        )

                                        DBManager.insert(context, message)
                                    }

                                    JLog.i("text = $text")
                                }
                            }

                            //区域标识终结
                            if (value == "0000") {
                                break
                            }
                        }
                    }
                }

            }
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
    private fun openMetaMessageTable(db: SQLiteDatabase): ArrayList<FTSMetaMessage> {
        val metaMessageList = arrayListOf<FTSMetaMessage>()
        val c: Cursor = db.rawQuery("select * from FTS5MetaMessage", null)
        JLog.i("find FTS5MetaMessage rows = ${c.count}")
        while (c.moveToNext()) {
            val docid: Int = c.getInt(c.getColumnIndex("docid"))
            val type: Int = c.getInt(c.getColumnIndex("type"))
            val subtype: Int = c.getInt(c.getColumnIndex("subtype"))
            val entity_id: Int = c.getInt(c.getColumnIndex("entity_id"))
            val status: Int = c.getInt(c.getColumnIndex("status"))
            val aux_index: String = c.getString(c.getColumnIndex("aux_index"))
            val talker: String = c.getString(c.getColumnIndex("talker"))
            val timestamp: Long = c.getLong(c.getColumnIndex("timestamp"))

//            JLog.i("meta docid = $docid")
//            JLog.i("meta entity_id = $entity_id")
//            JLog.i("meta aux_index = $aux_index")
//            JLog.i("meta talker = $talker")

            val metaMessage = FTSMetaMessage(docid, type, subtype, entity_id, aux_index, timestamp, status, talker, "")
            metaMessageList.add(metaMessage)
        }

        return metaMessageList
    }

    /**
     * 打开聊天记录索引表
     * @param
     * type =
     */
    private fun openMessageContentTable(db: SQLiteDatabase): ArrayList<FTSMessage> {
        val messages = arrayListOf<FTSMessage>()
        val c: Cursor = db.rawQuery("SELECT * from FTS5IndexMessage_content", null)
        val count = c.count
        JLog.i("find messages rows = $count")
        while (c.moveToNext()) {
            val id: Int = c.getInt(c.getColumnIndex("id"))
            val c0: String = c.getString(c.getColumnIndex("c0"))

//            JLog.i("content id = $id")
//            JLog.i("content c0 = $c0")

            val message = FTSMessage(id, c0)
            messages.add(message)
        }

        c.close()
        return messages
    }

    /**
     * 打开联系人列表
     * @param
     * verifyFlag = 0 是好友加群组加关注的公众号
     * type:type=4微信群里非好友，type=2微信群 type=0公众号 type=33官方账号 type=8可能为删除的好友
     * alias为修改后的微信号，username是原始微信号，如果没有自定义微信号，那么alias为空
     */
    private fun openContactTable(db: SQLiteDatabase, percent: Int, callback: FileCallback): MutableList<Contact> {

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
    private fun openConversationTable(db: SQLiteDatabase, percent: Int, callback: FileCallback): MutableList<Conversation> {

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
    private fun analyzedData(metaMessages: ArrayList<FTSMetaMessage>, messages: ArrayList<FTSMessage>): MutableList<FTSMetaMessage> {

        val messageList = mutableListOf<FTSMetaMessage>()

        //处理message
        for (meta in metaMessages) {
            val id = meta.docid
            for (message in messages) {
                if (id == message.id) {
                    val metaMessage = FTSMetaMessage(
                        id,
                        meta.type,
                        meta.subtype,
                        meta.entity_id,
                        meta.aux_index,
                        meta.timestamp,
                        meta.status,
                        meta.talker,
                        message.C0
                    )

                    messageList.add(metaMessage)
                }
            }
        }

        return messageList
    }


}