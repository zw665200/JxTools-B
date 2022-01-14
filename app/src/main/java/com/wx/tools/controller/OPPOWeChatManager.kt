package com.wx.tools.controller

import android.content.Context
import com.wx.tools.bean.FileStatus
import com.wx.tools.callback.DBCallback
import com.wx.tools.model.service.MicroMsgService
import com.wx.tools.utils.FileUtil
import com.wx.tools.utils.JLog
import com.wx.tools.utils.ZipUtils.ResultType
import com.tencent.mmkv.MMKV
import java.io.File


object OPPOWeChatManager {

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

        if (!isPrepared) {
            unzipBackupFile(mContext, jxBackupPath, backupPath, callback)
            return
        }

        val file = File(backupPath)
        val rootPath = jxBackupPath + file.name.replace(".tar", "") + File.separator
        val value = MMKV.defaultMMKV()
        value?.encode("backup_path", rootPath)
        value?.encode("backup_time", file.lastModified())
        Constant.CURRENT_BACKUP_TIME = file.lastModified()
        Constant.CURRENT_BACKUP_PATH = rootPath

        JLog.i("rootPath = $rootPath")

        //查找包里所有的MicroMsg文件
        val dbFiles = FileUtil.searchDbFiles(rootPath, Constant.DB_NAME)
        if (dbFiles.isNullOrEmpty()) {
            callback.onFailed(FileStatus.DECODE_ACCOUNT, "没有找到微信账号数据，请按以下失败原因检查：")
            callback.onFailed(FileStatus.DECODE_ACCOUNT, "1. 备份微信只备份了程序，没有备份数据；")
            callback.onFailed(FileStatus.DECODE_ACCOUNT, "2. 微信卸载过，重新安装后没有再登录；")
            callback.onFailed(FileStatus.DECODE_ACCOUNT, "3. 在手机设置里清除了应用数据；")
            callback.onFailed(FileStatus.DECODE_ACCOUNT, "4. 存储空间不够，导致备份不完全或者解压不完全。")
            return
        }

        val iMeiCfgFiles = FileUtil.searchFiles(rootPath, Constant.COMPATIBLE_INFO_NAME)
        val iMeiMetaFiles = FileUtil.searchFiles(rootPath, Constant.DENGTA_META_NAME)
        val uinHistoryFiles = FileUtil.searchFiles(rootPath, Constant.HISTORY_INFO_NAME)
        val uinInfoFiles = FileUtil.searchDetailFiles(rootPath, Constant.AUTH_INFO_KEY_NAME)
        val uinCfgFiles = FileUtil.searchDetailFiles(rootPath, Constant.UIN_CONFIG_NAME)

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
    private suspend fun unzipBackupFile(context: Context, jxBackupPath: String, backupPath: String, callback: DBCallback) {
        val file = File(backupPath)
        val jxPath = jxBackupPath + file.name.replace(".tar", "")
        JLog.i("jxPath = $jxPath")

        //从手机备份目录解压到JX目录
        val result = FileUtil.unZipFileWith7Zip(backupPath, jxPath)

        JLog.i("result = $result")
        when (result!!) {
            ResultType.RESULT_SUCCESS -> {
                JLog.i("unzip success")
                openDatabase(context, jxBackupPath, backupPath, callback)
            }

            ResultType.RESULT_FAULT -> {
                callback.onFailed(FileStatus.UNZIP_BACKUP, "解压失败")
            }

            else -> {
                callback.onFailed(FileStatus.UNZIP_BACKUP, "解压失败")
            }
        }
    }

    private suspend fun openDatabase(context: Context, jxPath: String, backupPath: String, callback: DBCallback) {

        callback.onSuccess(FileStatus.UNZIP_BACKUP)
        callback.onProgress(FileStatus.UNZIP_BACKUP, 100)

        //搜索微信压缩文件
        val filesPath = FileUtil.searchFiles(jxPath, Constant.WX_BACKUP_NAME)
        if (filesPath.size > 0) {
            callback.onProgress(FileStatus.UNZIP_WX, 100)
            callback.onSuccess(FileStatus.UNZIP_WX)

            MiWeChatManager.getWxMessage(context, jxPath, backupPath, callback, true)
        }
    }


}