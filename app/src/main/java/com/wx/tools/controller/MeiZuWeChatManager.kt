package com.wx.tools.controller

import android.content.Context
import com.wx.tools.bean.FileStatus
import com.wx.tools.callback.DBCallback
import com.wx.tools.model.service.MicroMsgService
import com.wx.tools.utils.FileUtil
import com.wx.tools.utils.JLog
import com.wx.tools.utils.ZipUtils
import com.tencent.mmkv.MMKV
import java.io.File


object MeiZuWeChatManager {

    /**
     * 获取微信备份文件
     */
    suspend fun getWxMessage(
        mContext: Context,
        jxBackupPath: String,
        backupPath: String,
        callback: DBCallback,
        isPrepared: Boolean
    ) {


        JLog.i("MeiZu:isPrepared = $isPrepared")
        JLog.i("MeiZu:jxBackupPath = $jxBackupPath")
        JLog.i("MeiZu:backupPath = $backupPath")

        if (!isPrepared) {
            unzipBackupFile(mContext, backupPath, jxBackupPath, callback)
            return
        }

        val file = FileUtil.getFile(backupPath)
        val realJxPath = jxBackupPath + file.name.replace(".zip", "")
        val value = MMKV.defaultMMKV()
        value?.encode("backup_path", realJxPath)
        value?.encode("backup_time", file.lastModified())
        Constant.CURRENT_BACKUP_TIME = file.lastModified()
        Constant.CURRENT_BACKUP_PATH = realJxPath

        //查找包里所有的MicroMsg文件
        val dbFiles = FileUtil.searchDbFiles(realJxPath, Constant.DB_NAME)
        if (dbFiles.isNullOrEmpty()) {
            callback.onFailed(FileStatus.DECODE_ACCOUNT, "没有找到微信账号数据，请按以下失败原因检查：")
            callback.onFailed(FileStatus.DECODE_ACCOUNT, "1. 备份微信只备份了程序，没有备份数据；")
            callback.onFailed(FileStatus.DECODE_ACCOUNT, "2. 微信卸载过，重新安装后没有再登录；")
            callback.onFailed(FileStatus.DECODE_ACCOUNT, "3. 在手机设置里清除了应用数据；")
            callback.onFailed(FileStatus.DECODE_ACCOUNT, "4. 存储空间不够，导致备份不完全或者解压不完全。")
            return
        }

        val iMeiCfgFiles = FileUtil.searchFiles(realJxPath, Constant.COMPATIBLE_INFO_NAME)
        val iMeiMetaFiles = FileUtil.searchFiles(realJxPath, Constant.DENGTA_META_NAME)
        val uinHistoryFiles = FileUtil.searchFiles(realJxPath, Constant.HISTORY_INFO_NAME)
        val uinInfoFiles = FileUtil.searchDetailFiles(realJxPath, Constant.AUTH_INFO_KEY_NAME)
        val uinCfgFiles = FileUtil.searchDetailFiles(realJxPath, Constant.UIN_CONFIG_NAME)

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
        if (uinList.isEmpty() && iMeiList.isEmpty()) {
            callback.onFailed(FileStatus.DECODE_MESSAGE, "参数缺失")
            return
        }


        //得到MicroMsg密码列表
        val pwdList = PwdManager.getMircoMsgPwd(iMeiList, uinList, dbFiles)

        if (pwdList.size > 0) {
            for ((index, child) in pwdList.withIndex()) {
                val path = child.name
                val dbFile = File(path)

                //用得到的组合密码挨个去破解
                MicroMsgService.readWeChatDatabase(mContext, child, file.lastModified(), dbFile, index + 1, callback)
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
     * 先判断JX备份目录是否已经存在解压后的文件
     * 如果有，则直接去找微信备份压缩包，然后解压，解压后再查找数据
     * @param
     */
    private suspend fun unzipBackupFile(context: Context, backupPath: String, jxBackupPath: String, callback: DBCallback) {
        val file = FileUtil.getFile(backupPath)
        if (file != null) {
            val realJxPath = jxBackupPath + file.name.replace(".zip", "")
            JLog.i("realJxPath = $realJxPath")

            //如果JX备份目录已经存在解压后的备份文件,继续找到微信压缩文件
            if (FileUtil.isFileExist(realJxPath)) {
                JLog.i("unzip file exist")
                callback.onSuccess(FileStatus.UNZIP_BACKUP)
                callback.onProgress(FileStatus.UNZIP_BACKUP, 100)

                //搜索微信压缩文件
                val filesPath = FileUtil.searchFiles(realJxPath, Constant.MZ_BACKUP_NAME_TAR)
                if (filesPath.size > 0) {
                    for (index in filesPath) {
                        val name = index.name
                        val path = index.path
                        if (name.endsWith(".zip")) {

                            //找到微信文件并且解压到文件所在目录
                            val b = FileUtil.unZipFile(path, path.replace(name, ""), FileStatus.UNZIP_WX, callback)
                            if (b) {
                                getWxMessage(context, jxBackupPath, backupPath, callback, true)
                            } else {
                                callback.onFailed(FileStatus.UNZIP_WX, "解压微信失败")
                            }
                        }
                    }
                }
            } else {
                JLog.i("unzip file is not exist")
                //如果JX备份目录不存在，说明还未解压，需要从手机备份目录解压到JX目录
                val result = FileUtil.unZipFileWith7Zip(backupPath, jxBackupPath)
                JLog.i("result = $result")
                when (result!!) {
                    ZipUtils.ResultType.RESULT_SUCCESS -> {
                    }
                    ZipUtils.ResultType.RESULT_WARNING -> {
                    }
                    ZipUtils.ResultType.RESULT_FAULT -> {
                    }
                    ZipUtils.ResultType.RESULT_COMMAND -> {
                    }
                    ZipUtils.ResultType.RESULT_MEMORY -> {
                    }
                    ZipUtils.ResultType.RESULT_SUER_STOP -> {
                    }
                }

                //二次检查是否已经解压(因为压缩包里存在压缩包，可能已经解压完成但是返回解压错误)
                if (FileUtil.isFileExist(realJxPath)) {
                    JLog.i("unzip again")

                    //解压完成后再次递归
                    unzipBackupFile(context, backupPath, jxBackupPath, callback)

                } else {
                    callback.onFailed(FileStatus.UNZIP_BACKUP, "文件解压失败")
                }

            }

        }

    }


}