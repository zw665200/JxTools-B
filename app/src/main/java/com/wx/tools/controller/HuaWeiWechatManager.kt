package com.wx.tools.controller

import android.content.Context
import android.util.Xml
import com.wx.tools.bean.FileStatus
import com.wx.tools.callback.DBCallback
import com.wx.tools.callback.FileCallback
import com.wx.tools.model.service.MicroMsgService
import com.wx.tools.utils.*
import com.tencent.mmkv.MMKV
import org.xmlpull.v1.XmlPullParser
import java.io.*
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.xml.bind.DatatypeConverter

/**
 *
 */

object HuaWeiWechatManager {

    /**
     * 获取微信备份文件
     * @param jxPath 程序的实际备份文件地址
     * @param backupPath 系统的实际备份文件地址
     */
    fun getWxMessage(
        mContext: Context,
        jxPath: String,
        backupPath: String,
        date: Long,
        pwd: String,
        callback: DBCallback,
        isPrepared: Boolean
    ) {

        JLog.i("backupPath = $backupPath")
        JLog.i("jxPath = $jxPath")

        if (!isPrepared) {
            unZipBackupFile(mContext, pwd, jxPath, backupPath, date, callback)
            return
        }

        //save decrypted files info
        val value = MMKV.defaultMMKV()
        value?.encode("backup_path", backupPath)
        value?.encode("backup_time", date)
        Constant.CURRENT_BACKUP_TIME = date
        Constant.CURRENT_BACKUP_PATH = backupPath

        //查找包里所有的MicroMsg.db文件
        val dbFiles = FileUtil.searchDbFiles(jxPath, Constant.DB_NAME)
        if (dbFiles.isNullOrEmpty()) {
            callback.onFailed(FileStatus.DECODE_ACCOUNT, "没有找到微信账号数据，请按以下失败原因检查：")
            callback.onFailed(FileStatus.DECODE_ACCOUNT, "1. 备份密码输入有误；")
            callback.onFailed(FileStatus.DECODE_ACCOUNT, "2. 备份微信只备份了程序，没有备份数据；")
            callback.onFailed(FileStatus.DECODE_ACCOUNT, "3. 微信卸载过，重新安装后没有再登录；")
            callback.onFailed(FileStatus.DECODE_ACCOUNT, "4. 在手机设置里清除了应用数据；")
            callback.onFailed(FileStatus.DECODE_ACCOUNT, "5. 存储空间不够，导致备份不完全或者解压不完全。")
            return
        }
        
        val iMeiCfgFiles = FileUtil.searchDetailFiles(jxPath, Constant.COMPATIBLE_INFO_NAME)
        val iMeiMetaFiles = FileUtil.searchDetailFiles(jxPath, Constant.DENGTA_META_NAME)
        val uinHistoryFiles = FileUtil.searchDetailFiles(jxPath, Constant.HISTORY_INFO_NAME)
        val uinInfoFiles = FileUtil.searchDetailFiles(jxPath, Constant.AUTH_INFO_KEY_NAME)
        val uinCfgFiles = FileUtil.searchDetailFiles(jxPath, Constant.UIN_CONFIG_NAME)

        val uinList = if (uinHistoryFiles.isEmpty()) {
            if (uinInfoFiles.isEmpty()) {
                if (uinCfgFiles.isEmpty()) {
                    callback.onFailed(FileStatus.UNZIP_WX, "文件缺失")
                    callback.onFailed(FileStatus.UNZIP_WX, "可能原因：备份包不完整，没有找到解析必备的文件")
                    return
                } else {
                    PwdManager.getUinsFromSystemConfigXml(uinCfgFiles[0])
                }
            } else {
                PwdManager.getUinsFromAuthInfoXml(uinInfoFiles[0])
            }
        } else {
            PwdManager.getUinsFromHistoryXml(uinHistoryFiles[0])
        }

        val iMeiList = PwdManager.getImeiFromXml(mContext, iMeiCfgFiles, iMeiMetaFiles)

        //得到MicroMsg密码列表
        val pwdList = PwdManager.getMircoMsgPwd(iMeiList, uinList, dbFiles)

        if (pwdList.size > 0) {

            for ((index, child) in pwdList.withIndex()) {
                val path = child.name
                val dbFile = File(path)

                //用得到的组合密码去破解
                MicroMsgService.readWeChatDatabase(mContext, child, date, dbFile, index + 1, callback)
            }

            val accountList = DBManager.getAccountsBySrcTime(mContext, Constant.CURRENT_BACKUP_TIME)
            if (accountList.isNullOrEmpty()) {
                callback.onFailed(FileStatus.DECODE_ACCOUNT, "解析失败")
            } else {
                callback.onSuccess(FileStatus.DECODE_MESSAGE)
                callback.onSuccess(FileStatus.DECODE_CONTACT)
                callback.onSuccess(FileStatus.ANALYZE)
                WxManager.getInstance(mContext).deleteUnzipBackupFiles()
            }


        } else {
            JLog.i("can't find db file")
            callback.onFailed(FileStatus.DECODE_ACCOUNT, "没有找到账号信息")
        }

    }


    /**
     * 解压备份文件
     */
    private fun unZipBackupFile(context: Context, pwd: String, jxPath: String, backupPath: String, date: Long, callback: DBCallback) {
        val parent = FileUtil.getFile(backupPath).parentFile
        if (parent == null) {
            callback.onFailed(FileStatus.UNZIP_BACKUP, "备份文件缺失")
            return
        }

        JLog.i("unZipBackupFile inPath = $backupPath")
        JLog.i("unZipBackupFile outPath = $backupPath")

        //读取备份文件列表
        val backupFiles = FileUtil.searchFiles(backupPath, "com.tencent.mm", "tar")

        val infoPath = parent.absolutePath + File.separator + "info.xml"
        JLog.i("infoPath = $infoPath")
        val tarFiles = arrayListOf<File>()
        if (backupPath.endsWith(".tar")) {
            tarFiles.add(File(backupPath))
        } else {
            for (child in backupFiles) {
                if (!child.name.contains("decrypt")) {
                    tarFiles.add(child)
                }
            }
        }

        val hex = getDecodeMsg(infoPath)
        JLog.i("hex = $hex")
        if (hex.length == 96) {
            //得到的是一个48位的ascii数据
            val ascii = DatatypeConverter.parseHexBinary(hex)
            if (ascii.size == 48) {
                val salt = ByteArray(32)
                val counterIv = ByteArray(16)
                for ((index, byte) in ascii.withIndex()) {
                    if (index < 32) {
                        salt[index] = byte
                    } else {
                        counterIv[index - 32] = byte
                    }
                }

                //获得密钥
                val key = PBKDF2().getEncryptedPassword(pwd, salt)
                JLog.i("key size = ${key.size}")

                if (key.size != 32) {
                    callback.onFailed(FileStatus.UNZIP_BACKUP, "密码错误")
                    return
                }

                //破解
                decryptPackage(context, pwd, date, key, counterIv, jxPath, tarFiles, backupPath, callback)

            }
        } else {
            callback.onFailed(FileStatus.UNZIP_BACKUP, "密码错误")
            return
        }
    }

    private fun decryptPackage(
        context: Context,
        pwd: String,
        date: Long,
        key: ByteArray,
        counterIv: ByteArray,
        jxPath: String,
        tarFiles: ArrayList<File>,
        parentPath: String,
        callback: DBCallback
    ) {

        //定义线程池批量解密数据块
        val corePoolSize = Runtime.getRuntime().availableProcessors() * 2 + 1
        val executor = ThreadPoolExecutor(
            corePoolSize, corePoolSize, 1, TimeUnit.SECONDS,
            LinkedBlockingQueue(),
            Executors.defaultThreadFactory(),
            ThreadPoolExecutor.DiscardOldestPolicy()
        )

        //预启动所有核心线程
        executor.prestartAllCoreThreads()

        JLog.i("corePoolSize = $corePoolSize")

        val count = tarFiles.size
        var backupIndex = 0
        var wxIndex = 0
        for (tar in tarFiles) {
            var outPath = parentPath + File.separator + "decrypt_" + tar.name
            if (parentPath.endsWith(".tar")) {
                outPath = parentPath.replace(Constant.HW_BACKUP_NAME_TAR, "") + "decrypt_" + tar.name
            }

            if (FileUtil.isFileExist(outPath)) {
                FileUtil.deleteFile(outPath)
            }


            JLog.i("fromPath = ${tar.absolutePath}")
            JLog.i("outPath = $outPath")

            //获得解包密码
            AES.decrypt(executor, key, counterIv, tar.absolutePath, outPath, object : FileCallback {
                override fun onSuccess(step: Enum<FileStatus>) {

                    backupIndex++
                    callback.onProgress(FileStatus.UNZIP_BACKUP, "$backupIndex/$count")
                    if (backupIndex == count) {
                        //破解成功后回调
                        callback.onSuccess(FileStatus.UNZIP_BACKUP)
                    }

                    //解压到文件所在目录
                    val result = FileUtil.unZipFileWith7Zip(outPath, jxPath)
                    JLog.i("result = $result")
                    when (result!!) {
                        ZipUtils.ResultType.RESULT_SUCCESS -> {
                            wxIndex++
                            FileUtil.deleteFile(outPath)
                            if (wxIndex == count) {
                                //破解成功后回调
                                callback.onSuccess(FileStatus.UNZIP_WX)
                                getWxMessage(context, jxPath, tar.absolutePath, date, pwd, callback, true)
                            }
                        }

                        ZipUtils.ResultType.RESULT_FAULT -> {
                            wxIndex++
                            FileUtil.deleteFile(outPath)
                            if (wxIndex == count) {
                                //破解成功后回调
                                callback.onSuccess(FileStatus.UNZIP_WX)
                                getWxMessage(context, jxPath, tar.absolutePath, date, pwd, callback, true)
                            }
                        }

                        else -> {
                        }
                    }
                }

                override fun onProgress(step: Enum<FileStatus>, index: Int) {
//                    callback.onProgress(FileStatus.UNZIP_BACKUP, index)
                }

                override fun onFailed(step: Enum<FileStatus>, message: String) {
                    callback.onFailed(FileStatus.UNZIP_BACKUP, message)
                }
            })
        }
    }


    private fun getDecodeMsg(path: String): String {
        val file = File(path)
        var result = ""
        try {
            JLog.i("开始解析xml")
            val fis = ByteArrayInputStream(file.readBytes())
            val pullParser = Xml.newPullParser()
            pullParser.setInput(fis, "UTF-8")
            var event = pullParser.eventType
            val depth = pullParser.depth
            val type = pullParser.next()
            loop@ while ((type != XmlPullParser.END_TAG || pullParser
                    .depth > depth) && event != XmlPullParser.END_DOCUMENT
            ) {
                when (event) {
                    XmlPullParser.START_DOCUMENT -> {
                    }

                    XmlPullParser.START_TAG -> {
                        val name = pullParser.name
                        if (name == "value") {
                            val value = pullParser.getAttributeValue(0)
                            if (value != null && value.length == 96) {
                                result = value
                                break@loop
                            }
                        }
                    }

                    XmlPullParser.END_TAG -> {
                    }
                }
                event = pullParser.next()
            }

            fis.close()
        } catch (e: Exception) {
            JLog.i("entry error : $e")
        }

        return result
    }

}