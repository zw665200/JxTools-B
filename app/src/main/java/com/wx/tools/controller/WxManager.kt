package com.wx.tools.controller

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import com.wx.tools.bean.*
import com.wx.tools.callback.*
import com.wx.tools.utils.Dict
import com.wx.tools.utils.AppUtil
import com.wx.tools.utils.FileUtil
import com.wx.tools.utils.JLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*


class WxManager private constructor(context: Context) {
    private var mContext = context

    companion object {
        private var localPath = ""
        private var backupPath = ""
        private var jxBackupPath = ""
        private var exPortPath = ""

        @Volatile
        private var instance: WxManager? = null

        fun getInstance(c: Context): WxManager {
            if (instance == null) {
                synchronized(WxManager::class) {
                    if (instance == null) {
                        instance = WxManager(c)
                    }
                }
            }

            //初始化路径
            localPath = FileUtil.getSDPath(c)
            jxBackupPath = c.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)!!.absolutePath + Constant.JX_BACKUP_PATH
            exPortPath = localPath + Constant.EXPORT_PATH

            FileUtil.createFolder(backupPath)
            FileUtil.createFolder(jxBackupPath)
            FileUtil.createFolder(exPortPath)

            return instance!!
        }
    }


    fun checkBackupPath(): Boolean {
        if (Constant.ROM == "") {
            return false
        } else {

            when (Constant.ROM) {
                Constant.ROM_EMUI -> backupPath = localPath + Constant.BACKUP_PATH
                Constant.ROM_MIUI -> backupPath = localPath + Constant.XM_BACKUP_PATH
                Constant.ROM_FLYME -> backupPath = localPath + Constant.FLYME_BACKUP_PATH
                Constant.ROM_OPPO -> backupPath = localPath + Constant.OPPO_BACKUP_PATH
                Constant.ROM_VIVO -> backupPath = localPath + Constant.BACKUP_PATH
            }

            JLog.i("localPath = $localPath")
            JLog.i("backupPath = $backupPath")
            JLog.i("jxBackupPath = $jxBackupPath")

            return true
        }
    }


    /**
     * 检查是否有备份文件存在,如果有弹出对话框
     */
    fun getBackupFiles(): MutableList<FileBean> {
        var backupFiles = mutableListOf<FileBean>()
        if (checkBackupPath()) {
            when (Constant.ROM) {
                Constant.ROM_EMUI -> backupFiles = getHuaWeiBackupFiles(backupPath)

                Constant.ROM_MIUI -> backupFiles = getMiBackupFiles(backupPath)

                Constant.ROM_FLYME -> backupFiles = getMeiZuBackUpFiles(backupPath)

                Constant.ROM_OPPO -> backupFiles = getOPPOBackupFiles(backupPath)

                Constant.ROM_VIVO -> backupFiles = getVIVOBackupFiles(backupPath)
            }
        }

        return backupFiles
    }

    /**
     * 获得魅族的备份文件
     * @param backupPath 备份文件路径
     */
    private fun getMeiZuBackUpFiles(backupPath: String): MutableList<FileBean> {
        val currentFiles = FileUtil.getFileList(backupPath)
        val backupFilesName = mutableListOf<FileBean>()
        if (currentFiles != null) {
            JLog.i("file size = ${currentFiles.size}")

            for (child in currentFiles) {
                if (child.name.endsWith(".zip") && child.name.contains("-")) {
                    val length = (child.length() / 1024 / 1024).toString()
                    val name = AppUtil.timeStamp2Date(child.lastModified().toString(), null)
                    val backupFile = FileBean(name, child.absolutePath, length, child.lastModified())
                    backupFilesName.add(backupFile)
                }
            }
        }

        return backupFilesName
    }


    /**
     * 获得华为的备份文件
     */
    private fun getHuaWeiBackupFiles(backupPath: String): MutableList<FileBean> {
        val backupFilesName = mutableListOf<FileBean>()
        var currentBackupFiles = FileUtil.searchFolder(backupPath, Constant.HW_BACKUP_APP_DATA_TAR)
        val currentXmlFiles = FileUtil.searchFolder(backupPath, Constant.HW_BACKUP_NAME_XML)
        if (currentBackupFiles.isNullOrEmpty() || currentXmlFiles.isNullOrEmpty() || currentBackupFiles.size != 1 || currentXmlFiles.size != 1) {
            currentBackupFiles = FileUtil.searchFiles(backupPath, Constant.HW_BACKUP_NAME_TAR)
            if (currentBackupFiles.isNullOrEmpty()) {
                return arrayListOf()
            } else {
                val child = currentBackupFiles[0]
                val length = (child.length() / 1024 / 1024).toString()
                val name = AppUtil.timeStamp2Date(child.lastModified().toString(), null)
                val backupFile = FileBean(name, child.absolutePath, length, child.lastModified())
                backupFilesName.add(backupFile)
            }
        } else {
            val file = currentBackupFiles[0]
            val info = currentXmlFiles[0]
            val length = (FileUtil.getTotalSize(file) / 1024 / 1024).toString()
            val name = AppUtil.timeStamp2Date(info.lastModified().toString(), null)
            backupFilesName.add(FileBean(name, file.absolutePath, length, info.lastModified()))
        }

        return backupFilesName
    }

    /**
     * 获得小米的备份文件
     */
    private fun getMiBackupFiles(backupPath: String): MutableList<FileBean> {
        val currentFiles = FileUtil.searchFiles(backupPath, Constant.XM_BACKUP_NAME_BAK)
        val backupFilesName = mutableListOf<FileBean>()
        if (currentFiles != null) {
            JLog.i("file size = ${currentFiles.size}")

            for (child in currentFiles) {
                //找到备份文件列表
                if (child.name == Constant.XM_BACKUP_NAME_BAK) {
                    val length = (child.length() / 1024 / 1024).toString()
                    val name = AppUtil.timeStamp2Date(child.lastModified().toString(), null)
                    backupFilesName.add(FileBean(name, child.absolutePath, length, child.lastModified()))
                }
            }
        }

        return backupFilesName
    }

    /**
     * 获得OPPO的备份文件
     */
    private fun getOPPOBackupFiles(backupPath: String): MutableList<FileBean> {
        val currentFiles = FileUtil.getFileList(backupPath)
        val backupFilesName = mutableListOf<FileBean>()
        if (currentFiles != null) {

            //查找备份的tar包
            val files = FileUtil.searchFiles(backupPath, Constant.OPPO_BACKUP_NAME_TAR)
            if (files.size > 0) {
                for (index in currentFiles) {
                    //找到文件名字是今天的备份文件，可能会有多个
                    if (index.name.endsWith(".conf")) {
                        val length = (files[0].length() / 1024 / 1024).toString()
                        backupFilesName.add(FileBean(index.name.replace(".conf", ""), files[0].absolutePath, length, index.lastModified()))
                    }
                }
            }
        }

        return backupFilesName
    }

    /**
     * 获得VIVO的备份文件
     */
    private fun getVIVOBackupFiles(backupPath: String): MutableList<FileBean> {
        val currentFiles = FileUtil.searchFiles(backupPath, Constant.VIVO_BACKUP_NAME_TAR)
        val backupFilesName = mutableListOf<FileBean>()
        if (currentFiles.isNotEmpty()) {
            for (child in currentFiles) {
                val length = (child.length() / 1024 / 1024).toString()
                val name = AppUtil.timeStamp2Date(child.lastModified().toString(), null)
                backupFilesName.add(FileBean(name, child.absolutePath, length, child.lastModified()))
            }
        }

        return backupFilesName
    }


    suspend fun getWxMessage(backupPath: String, date: Long, callback: DBCallback, isPrepared: Boolean) {
        getWxMessage(backupPath, date, "", callback, isPrepared)
    }

    /**
     * 带密码解析备份文件以获得文件资料
     */
    suspend fun getWxMessage(backupPath: String, date: Long, pwd: String, callback: DBCallback, isPrepared: Boolean) {

        when (Constant.ROM) {
            Constant.ROM_EMUI -> {
                HuaWeiWechatManager.getWxMessage(mContext, jxBackupPath, backupPath, date, pwd, callback, isPrepared)
            }
            Constant.ROM_FLYME -> {
                MeiZuWeChatManager.getWxMessage(mContext, jxBackupPath, backupPath, callback, isPrepared)
            }
            Constant.ROM_MIUI -> {
                MiWeChatManager.getWxMessage(mContext, jxBackupPath, backupPath, callback, isPrepared)
            }
            Constant.ROM_OPPO -> {
                OPPOWeChatManager.getWxMessage(mContext, jxBackupPath, backupPath, callback, isPrepared)
            }
            Constant.ROM_VIVO -> {
                VIVOWechatManager.getWxMessage(mContext, jxBackupPath, backupPath, callback, isPrepared)
            }
        }
    }

    /**
     * 获得微信图片
     */
    fun getWxPics(callback: PicCallback) {
        Constant.stopScanPic = false

        //查找Pictures路径下的聊天图片
        val picPath = localPath + Constant.PICTURE_PATH

        val chatList = arrayListOf<FileWithType>()
        FileUtil.searchWxPics(picPath, object : FCallback {
            override fun onSuccess(step: Enum<FileStatus>) {
            }

            override fun onProgress(step: Enum<FileStatus>, file: File) {
                val name = file.name.toLowerCase(Locale.ROOT)
                if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".gif")) {
                    val date = file.lastModified()
                    val fileWithType = FileWithType(file.name, file.path, file.length().toInt(), date, "wx_chat")
                    callback.onProgress(FileStatus.PIC, fileWithType)
                    chatList.add(fileWithType)
                    DBManager.insert(mContext, fileWithType)
                }
            }

            override fun onFailed(step: Enum<FileStatus>, message: String) {
            }
        })


        val chatPicList = arrayListOf<String>()
        chatPicList.add(localPath + Constant.WX_RESOURCE_PATH)
        chatPicList.add(localPath + Constant.WX_HIGN_VERSION_PATH)

        for (chatPicPath in chatPicList) {
            val folder = chatPicPath + "MicroMsg/"
            val files = FileUtil.getChildFolders(folder)
            for (child in files) {
                if (Constant.stopScanPic) break
                if (child.name.length == 32) {
                    val account = child.name
                    val folder1 = "$folder$account/image/"
                    FileUtil.searchWxPics(folder1, object : FCallback {
                        override fun onSuccess(step: Enum<FileStatus>) {
                        }

                        override fun onProgress(step: Enum<FileStatus>, file: File) {
                            val name = file.name.toLowerCase(Locale.ROOT)
                            val path = file.path
                            val date = file.lastModified()

                            //聊天图片
                            if (name.startsWith("fts_") || name.startsWith("reader_") || path.contains("/luckymoney/")) {
                                val fileWithType = FileWithType(file.name, path, file.length().toInt(), date, "wx_other")
                                callback.onProgress(FileStatus.PIC, fileWithType)
                                chatList.add(fileWithType)
                                DBManager.insert(mContext, fileWithType)
                                return
                            }
                        }

                        override fun onFailed(step: Enum<FileStatus>, message: String) {
                        }
                    })

                    val folder2 = "$folder$account/image2/"
                    FileUtil.searchWxPics(folder2, object : FCallback {
                        override fun onSuccess(step: Enum<FileStatus>) {
                        }

                        override fun onProgress(step: Enum<FileStatus>, file: File) {
                            val name = file.name.toLowerCase(Locale.ROOT)
                            val path = file.path
                            val date = file.lastModified()

                            //聊天图片
                            if (name.startsWith("th_") || name.endsWith("jpg")) {
                                JLog.i("find image create time = $date")
                                val fileWithType = FileWithType(file.name, path, file.length().toInt(), date, "wx_chat")
                                callback.onProgress(FileStatus.PIC, fileWithType)
                                chatList.add(fileWithType)
                                DBManager.insert(mContext, fileWithType)
                            }
                        }

                        override fun onFailed(step: Enum<FileStatus>, message: String) {
                        }
                    })

                    val folder3 = "$folder$account/emoji/"
                    FileUtil.searchWxPics(folder3, object : FCallback {
                        override fun onSuccess(step: Enum<FileStatus>) {
                        }

                        override fun onProgress(step: Enum<FileStatus>, file: File) {
                            val name = file.name.toLowerCase(Locale.ROOT)
                            val path = file.path
                            val date = file.lastModified()

                            //emoji
                            if (name.endsWith("_cover")) {
                                val fileWithType = FileWithType(file.name, path, file.length().toInt(), date, "wx_emoji")
                                callback.onProgress(FileStatus.PIC, fileWithType)
                                chatList.add(fileWithType)
                                DBManager.insert(mContext, fileWithType)
                                return
                            }
                        }

                        override fun onFailed(step: Enum<FileStatus>, message: String) {
                        }
                    })

                    val folder4 = "$folder$account/scanner/"
                    FileUtil.searchWxPics(folder4, object : FCallback {
                        override fun onSuccess(step: Enum<FileStatus>) {
                        }

                        override fun onProgress(step: Enum<FileStatus>, file: File) {
                            val name = file.name.toLowerCase(Locale.ROOT)
                            val path = file.path
                            val date = file.lastModified()

                            //other
                            if (name.endsWith(".jpg")) {
                                val fileWithType = FileWithType(file.name, path, file.length().toInt(), date, "wx_other")
                                callback.onProgress(FileStatus.PIC, fileWithType)
                                chatList.add(fileWithType)
                                DBManager.insert(mContext, fileWithType)
                                return
                            }
                        }

                        override fun onFailed(step: Enum<FileStatus>, message: String) {
                        }
                    })

                    val folder5 = "$folder$account/favorite/"
                    FileUtil.searchWxPics(folder5, object : FCallback {
                        override fun onSuccess(step: Enum<FileStatus>) {
                        }

                        override fun onProgress(step: Enum<FileStatus>, file: File) {
                            val name = file.name.toLowerCase(Locale.ROOT)
                            val path = file.path
                            val date = file.lastModified()

                            //other
                            if (name.endsWith(".jpg") || name.contains("_t") || name.contains("jpg_bigthumb")) {
                                val fileWithType = FileWithType(file.name, path, file.length().toInt(), date, "wx_other")
                                callback.onProgress(FileStatus.PIC, fileWithType)
                                chatList.add(fileWithType)
                                DBManager.insert(mContext, fileWithType)
                                return
                            }
                        }

                        override fun onFailed(step: Enum<FileStatus>, message: String) {
                        }
                    })

                }
            }

            //缓存目录
            val folder1 = chatPicPath + "cache/"
            val files1 = FileUtil.getChildFolders(folder1)
            for (child in files1) {
                if (Constant.stopScanPic) break
                if (child.name.length == 32) {
                    val account = child.name
                    val folder2 = "$folder1$account/finder/avatar/"
                    JLog.i("find cache folder path = $folder2")
                    FileUtil.searchWxPics(folder2, object : FCallback {
                        override fun onSuccess(step: Enum<FileStatus>) {
                        }

                        override fun onProgress(step: Enum<FileStatus>, file: File) {
                            val name = file.name.toLowerCase(Locale.ROOT)
                            val path = file.path
                            val date = file.lastModified()


                            //头像
                            if (name.startsWith("finder_avatar")) {
                                JLog.i("find cache avatar")
                                JLog.i("find cache avatar create time = $date")
                                val fileWithType = FileWithType(file.name, path, file.length().toInt(), date, "wx_avatar")
                                callback.onProgress(FileStatus.PIC, fileWithType)
                                chatList.add(fileWithType)
                                DBManager.insert(mContext, fileWithType)
                                return
                            }
                        }

                        override fun onFailed(step: Enum<FileStatus>, message: String) {
                        }
                    })

                    val folder3 = "$folder1$account/finder/image/"
                    FileUtil.searchWxPics(folder3, object : FCallback {
                        override fun onSuccess(step: Enum<FileStatus>) {
                        }

                        override fun onProgress(step: Enum<FileStatus>, file: File) {
                            val name = file.name.toLowerCase(Locale.ROOT)
                            val path = file.path
                            val date = file.lastModified()

                            //公众号或者聊天图片
                            if (name.startsWith("finder_image") || name.startsWith("full_image")) {
                                val fileWithType = FileWithType(file.name, path, file.length().toInt(), date, "wx_chat")
                                callback.onProgress(FileStatus.PIC, fileWithType)
                                chatList.add(fileWithType)
                                DBManager.insert(mContext, fileWithType)
                                return
                            }
                        }

                        override fun onFailed(step: Enum<FileStatus>, message: String) {
                        }
                    })

                    val folder4 = "$folder1$account/sns/"
                    FileUtil.searchWxPics(folder4, object : FCallback {
                        override fun onSuccess(step: Enum<FileStatus>) {
                        }

                        override fun onProgress(step: Enum<FileStatus>, file: File) {
                            val name = file.name.toLowerCase(Locale.ROOT)
                            val path = file.path
                            val date = file.lastModified()

                            //朋友圈图片
                            if (name.startsWith("snsu_")) {
                                val fileWithType = FileWithType(file.name, path, file.length().toInt(), date, "wx_sns")
                                callback.onProgress(FileStatus.PIC, fileWithType)
                                chatList.add(fileWithType)
                                DBManager.insert(mContext, fileWithType)
                                return
                            }
                        }

                        override fun onFailed(step: Enum<FileStatus>, message: String) {
                        }
                    })

                    val folder5 = "${folder1}sns_ad_landingpages/"
                    JLog.i("folder = $folder5")
                    FileUtil.searchWxPics(folder5, object : FCallback {
                        override fun onSuccess(step: Enum<FileStatus>) {
                        }

                        override fun onProgress(step: Enum<FileStatus>, file: File) {
                            val name = file.name.toLowerCase(Locale.ROOT)
                            val path = file.path
                            val date = file.lastModified()

                            //朋友圈图片
                            if (name.startsWith("adid_img")) {
                                val fileWithType = FileWithType(file.name, path, file.length().toInt(), date, "wx_sns")
                                callback.onProgress(FileStatus.PIC, fileWithType)
                                chatList.add(fileWithType)
                                DBManager.insert(mContext, fileWithType)
                                return
                            }
                        }

                        override fun onFailed(step: Enum<FileStatus>, message: String) {
                        }
                    })
                }
            }
        }

        //获得QQ图片
        chatList.addAll(getQQPics(callback))

        //获取陌陌图片
        chatList.addAll(getMoMoPics(callback))

        if (chatList.isEmpty()) {
            callback.onFailed(FileStatus.PIC, "没有找到图片")
        } else {
            //扫描完成
            callback.onSuccess(FileStatus.PIC)
        }
    }

    /**
     * 获取微信的视频文件
     */
    fun getWxVideos(callback: VideoCallback) {
        Constant.stopScanVideo = false

        //查找聊天视频文件
        val videoPath = localPath + Constant.WX_PICTURE_PATH

        val chatList = arrayListOf<FileWithType>()
        FileUtil.getVideoFiles(videoPath, object : FCallback {
            override fun onSuccess(step: Enum<FileStatus>) {
            }

            override fun onProgress(step: Enum<FileStatus>, file: File) {
                val name = file.name.toLowerCase(Locale.ROOT)
                val path = file.path

                if (name.endsWith(".mp4") || name.endsWith(".3gp") || name.endsWith(".avi") || name.endsWith(".rm")
                    || name.endsWith(".rmvb") || name.endsWith(".wmv") || name.endsWith(".mov")
                ) {
                    val date = file.lastModified()
                    val fileWithType = FileWithType(file.name, path, file.length().toInt(), date, "video")
                    callback.onProgress(FileStatus.VIDEO, fileWithType)
                    DBManager.insert(mContext, fileWithType)
                    chatList.add(fileWithType)
                }
            }

            override fun onFailed(step: Enum<FileStatus>, message: String) {
            }
        })


        val pathOld = localPath + Constant.WX_RESOURCE_PATH
        val pathNew = localPath + Constant.WX_HIGN_VERSION_PATH
        val list = arrayListOf<String>()
        list.add(pathOld)
        list.add(pathNew)

        for (item in list) {
            val folder1 = item + "cache/"
            val files = FileUtil.getChildFolders(folder1)
            for (child in files) {
                if (Constant.stopScanVideo) break
                if (child.name.length == 32) {
                    val account = child.name
                    val folder2 = "$folder1$account/finder/video/"
                    FileUtil.getVideoFiles(folder2, object : FCallback {
                        override fun onSuccess(step: Enum<FileStatus>) {
                        }

                        override fun onProgress(step: Enum<FileStatus>, file: File) {
                            val name = file.name.toLowerCase(Locale.ROOT)
                            val path = file.path
                            if (name.startsWith("finder_video")) {
                                val date = file.lastModified()
                                val fileWithType = FileWithType(file.name, path, file.length().toInt(), date, "video")
                                callback.onProgress(FileStatus.VIDEO, fileWithType)
                                DBManager.insert(mContext, fileWithType)
                                chatList.add(fileWithType)
                            }
                        }

                        override fun onFailed(step: Enum<FileStatus>, message: String) {
                        }
                    })

                    val folder3 = "$folder1$account/sns/"
                    FileUtil.getVideoFiles(folder3, object : FCallback {
                        override fun onSuccess(step: Enum<FileStatus>) {
                        }

                        override fun onProgress(step: Enum<FileStatus>, file: File) {
                            val name = file.name.toLowerCase(Locale.ROOT)
                            val path = file.path
                            if (name.startsWith("sight_")) {
                                val date = file.lastModified()
                                val fileWithType = FileWithType(file.name, path, file.length().toInt(), date, "video")
                                callback.onProgress(FileStatus.VIDEO, fileWithType)
                                DBManager.insert(mContext, fileWithType)
                                chatList.add(fileWithType)
                            }
                        }

                        override fun onFailed(step: Enum<FileStatus>, message: String) {
                        }
                    })
                }
            }

            val folder2 = item + "MicroMsg/"
            val files2 = FileUtil.getChildFolders(folder2)
            for (child in files2) {
                if (child.name.length == 32) {
                    val account = child.name
                    val folder3 = "$folder2$account/video/"
                    FileUtil.getVideoFiles(folder3, object : FCallback {
                        override fun onSuccess(step: Enum<FileStatus>) {
                        }

                        override fun onProgress(step: Enum<FileStatus>, file: File) {
                            val name = file.name.toLowerCase(Locale.ROOT)
                            val path = file.path
                            if (name.endsWith(".mp4") || name.endsWith(".3gp") || name.endsWith(".avi") || name.endsWith(".rm")
                                || name.endsWith(".rmvb") || name.endsWith(".wmv") || name.endsWith(".mov")
                            ) {
                                val date = file.lastModified()
                                val fileWithType = FileWithType(file.name, path, file.length().toInt(), date, "video")
                                callback.onProgress(FileStatus.VIDEO, fileWithType)
                                DBManager.insert(mContext, fileWithType)
                                chatList.add(fileWithType)
                            }
                        }

                        override fun onFailed(step: Enum<FileStatus>, message: String) {
                        }
                    })
                }
            }

            val folder3 = item + "MicroMsg/hbstoryvideo/"
            FileUtil.getVideoFiles(folder3, object : FCallback {
                override fun onSuccess(step: Enum<FileStatus>) {
                }

                override fun onProgress(step: Enum<FileStatus>, file: File) {
                    val name = file.name.toLowerCase(Locale.ROOT)
                    val path = file.path
                    if (name.endsWith(".mp4")) {
                        val date = file.lastModified()
                        val fileWithType = FileWithType(file.name, path, file.length().toInt(), date, "video")
                        callback.onProgress(FileStatus.VIDEO, fileWithType)
                        DBManager.insert(mContext, fileWithType)
                        chatList.add(fileWithType)
                    }
                }

                override fun onFailed(step: Enum<FileStatus>, message: String) {
                }
            })

            val folder4 = item + "MicroMsg/favorite/"
            FileUtil.getVideoFiles(folder4, object : FCallback {
                override fun onSuccess(step: Enum<FileStatus>) {
                }

                override fun onProgress(step: Enum<FileStatus>, file: File) {
                    val name = file.name.toLowerCase(Locale.ROOT)
                    val path = file.path
                    if (name.endsWith(".mp4")) {
                        val date = file.lastModified()
                        val fileWithType = FileWithType(file.name, path, file.length().toInt(), date, "video")
                        callback.onProgress(FileStatus.VIDEO, fileWithType)
                        DBManager.insert(mContext, fileWithType)
                        chatList.add(fileWithType)
                    }
                }

                override fun onFailed(step: Enum<FileStatus>, message: String) {
                }
            })
        }

        if (chatList.isEmpty()) {
            callback.onFailed(FileStatus.VIDEO, "没有找到视频文件")
        } else {
            callback.onSuccess(FileStatus.VIDEO)
        }
    }

    /**
     * 获取微信的音频文件
     */
    fun getWxVoices(callback: VoiceCallback) {
        Constant.stopScanVoice = false

        val pathOld = localPath + Constant.WX_RESOURCE_PATH + "MicroMsg/"
        val pathNew = localPath + Constant.WX_HIGN_VERSION_PATH + "MicroMsg/"
        val list = arrayListOf<String>()
        list.add(pathOld)
        list.add(pathNew)

        val voicesFiles = arrayListOf<FileWithType>()

        for (item in list) {
            val files = FileUtil.getChildFolders(item)
            for (child in files) {
                if (child.name.length == 32) {
                    val account = child.name
                    val folder = "$item$account/voice2/"
                    FileUtil.getVoiceFiles(folder, object : FCallback {
                        override fun onSuccess(step: Enum<FileStatus>) {
                        }

                        override fun onProgress(step: Enum<FileStatus>, file: File) {
                            val name = file.name.toLowerCase(Locale.ROOT)
                            val path = file.path

                            if (name.startsWith("msg_") && name.endsWith(".amr")) {
                                val date = file.lastModified()
                                val fileWithType = FileWithType(file.name, path, file.length().toInt(), date, "voice")
                                callback.onProgress(FileStatus.VOICE, fileWithType)
                                DBManager.insert(mContext, fileWithType)
                                voicesFiles.add(fileWithType)
                            }
                        }

                        override fun onFailed(step: Enum<FileStatus>, message: String) {
                        }
                    })
                }
            }
        }

        if (voicesFiles.size > 0) {
            callback.onSuccess(FileStatus.VOICE)
        } else {
            callback.onFailed(FileStatus.VOICE, "没有找到音频文件")
        }
    }

    /**
     * 获取微信的文档文件
     */
    fun getWxDocs(callback: DocCallback) {
        Constant.stopScanDoc = false

        //在系统下载目录里找文档
        val downloadPath = localPath + Constant.WX_DOWNLOAD_PATH

        val docList = arrayListOf<FileWithType>()
        FileUtil.searchDocs(downloadPath, object : FCallback {
            override fun onSuccess(step: Enum<FileStatus>) {
            }

            override fun onProgress(step: Enum<FileStatus>, file: File) {
                val path = file.path
                val date = file.lastModified()
                val fileWithType = FileWithType(file.name, path, file.length().toInt(), date, "wx_doc")
                callback.onProgress(FileStatus.DOC, fileWithType)
                DBManager.insert(mContext, fileWithType)
                docList.add(fileWithType)
            }

            override fun onFailed(step: Enum<FileStatus>, message: String) {
            }
        })


        val docPathOld = localPath + Constant.WX_RESOURCE_PATH + "MicroMsg/"
        val docPathNew = localPath + Constant.WX_HIGN_VERSION_PATH + "MicroMsg/"
        val list = arrayListOf<String>()
        list.add(docPathOld)
        list.add(docPathNew)

        for (item in list) {
            val files = FileUtil.getChildFolders(item)
            for (child in files) {
                if (child.name.length == 32) {
                    val account = child.name
                    val folder = "$item$account"
                    FileUtil.searchDocs(folder, object : FCallback {
                        override fun onSuccess(step: Enum<FileStatus>) {
                        }

                        override fun onProgress(step: Enum<FileStatus>, file: File) {
                            val name = file.name
                            if (name.endsWith(".txt") || name.endsWith(".pdf") || name.endsWith(".csv")
                                || name.endsWith(".xls") || name.endsWith(".xlsx") || name.endsWith(".doc")
                                || name.endsWith(".docx") || name.endsWith(".ppt") || name.endsWith(".pptx")
                                || name.endsWith(".rtf") || name.endsWith(".zip") || name.endsWith(".rar")
                                || name.endsWith(".xml") || name.endsWith(".psd") || name.endsWith(".tar")
                                || name.endsWith(".db")
                            ) {
                                val path = file.path
                                val date = file.lastModified()
                                val fileWithType = FileWithType(file.name, path, file.length().toInt(), date, "doc")
                                callback.onProgress(FileStatus.DOC, fileWithType)
                                DBManager.insert(mContext, fileWithType)
                                docList.add(fileWithType)
                            }
                        }

                        override fun onFailed(step: Enum<FileStatus>, message: String) {
                        }
                    })
                }
            }

            val folder = "$item/Download/"
            FileUtil.searchDocs(folder, object : FCallback {
                override fun onSuccess(step: Enum<FileStatus>) {
                }

                override fun onProgress(step: Enum<FileStatus>, file: File) {
                    val path = file.path
                    val date = file.lastModified()
                    val fileWithType = FileWithType(file.name, path, file.length().toInt(), date, "doc")
                    callback.onProgress(FileStatus.DOC, fileWithType)
                    DBManager.insert(mContext, fileWithType)
                    docList.add(fileWithType)
                }

                override fun onFailed(step: Enum<FileStatus>, message: String) {
                }
            })
        }

        if (docList.isEmpty()) {
            callback.onFailed(FileStatus.VOICE, "没有找到文档文件")
        } else {
            callback.onSuccess(FileStatus.DOC)
        }
    }

    /**
     * 获得QQ图片
     */
    fun getQQPics(callback: PicCallback): ArrayList<FileWithType> {
        val pathOld = localPath + Constant.QQ_RESOURCE_PATH
        val pathNew = localPath + Constant.QQ_HIGN_VERSION_PATH

        val list = arrayListOf<String>()
        list.add(pathOld)
        list.add(pathNew)

        val picFiles = arrayListOf<FileWithType>()
        for (item in list) {
            FileUtil.searchQQPics(item, object : FCallback {
                override fun onSuccess(step: Enum<FileStatus>) {
                }

                override fun onProgress(step: Enum<FileStatus>, file: File) {
                    val name = file.name.toLowerCase(Locale.ROOT)
                    val path = file.path
                    val date = file.lastModified()

                    //表情包
                    if (path.contains("/emoji/") && name.endsWith("_cover")) {
                        val fileWithType = FileWithType(file.name, path, file.length().toInt(), date, "wx_emoji")
                        callback.onProgress(FileStatus.PIC, fileWithType)
                        picFiles.add(fileWithType)
                        DBManager.insert(mContext, fileWithType)
                        return
                    }

                    //聊天图片
                    if (path.contains("/image/") || name.contains("/image2/") || name.contains("/record/")) {
                        val fileWithType = FileWithType(file.name, path, file.length().toInt(), date, "wx_chat")
                        callback.onProgress(FileStatus.PIC, fileWithType)
                        picFiles.add(fileWithType)
                        DBManager.insert(mContext, fileWithType)
                        return
                    }

                    //缓存聊天图片(包括公众号和好友)
                    if (path.contains("cache") && path.contains("/image/")) {
                        //聊天图片
                        if (path.contains("/gif")) {
                            val fileWithType = FileWithType(file.name, path, file.length().toInt(), date, "wx_emoji")
                            callback.onProgress(FileStatus.PIC, fileWithType)
                            picFiles.add(fileWithType)
                            DBManager.insert(mContext, fileWithType)
                            return
                        }

                        if (path.contains("/photo/")) {
                            val fileWithType = FileWithType(file.name, path, file.length().toInt(), date, "qq_chat")
                            callback.onProgress(FileStatus.PIC, fileWithType)
                            picFiles.add(fileWithType)
                            DBManager.insert(mContext, fileWithType)
                            return
                        }

                        if (!path.contains("QWallet") && !path.contains(".apollo") && !path.contains("newpoke")) {
                            val fileWithType = FileWithType(file.name, path, file.length().toInt(), date, "wx_other")
                            callback.onProgress(FileStatus.PIC, fileWithType)
                            picFiles.add(fileWithType)
                            DBManager.insert(mContext, fileWithType)
                            return
                        }
                    }

                    if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".gif")) {
                        if (!name.contains("_wm")) {
                            val fileWithType = FileWithType(file.name, path, file.length().toInt(), date, "wx_other")
                            callback.onProgress(FileStatus.PIC, fileWithType)
                            picFiles.add(fileWithType)
                            DBManager.insert(mContext, fileWithType)
                            return
                        }
                    }
                }

                override fun onFailed(step: Enum<FileStatus>, message: String) {
                }
            })
        }

        return picFiles

//        if (picFiles.isEmpty()) {
//            callback.onFailed(FileStatus.PIC, "没有找到图片")
//        } else {
//            callback.onSuccess(FileStatus.PIC)
//        }

    }

    /**
     * 获得momo图片
     */
    fun getMoMoPics(callback: PicCallback): ArrayList<FileWithType> {
        val path = localPath + Constant.MM_RESOURCE_PATH
        val list = arrayListOf<String>()
        list.add(path)

        val picFiles = arrayListOf<FileWithType>()
        for (item in list) {
            if (Constant.stopScanPic) break
            FileUtil.searchQQPics(item, object : FCallback {
                override fun onSuccess(step: Enum<FileStatus>) {
                }

                override fun onProgress(step: Enum<FileStatus>, file: File) {
                    val name = file.name.toLowerCase(Locale.ROOT)
                    val path = file.path
                    val date = file.lastModified()

                    //表情包
                    if (path.contains("/imgcache/") && name.endsWith(".0")) {
                        val fileWithType = FileWithType(file.name, path, file.length().toInt(), date, "wx_other")
                        callback.onProgress(FileStatus.PIC, fileWithType)
                        picFiles.add(fileWithType)
                        DBManager.insert(mContext, fileWithType)
                        return
                    }

                }

                override fun onFailed(step: Enum<FileStatus>, message: String) {
                }
            })
        }

        return picFiles

//        if (picFiles.isEmpty()) {
//            callback.onFailed(FileStatus.PIC, "没有找到图片")
//        } else {
//            callback.onSuccess(FileStatus.PIC)
//        }

    }

    /**
     * 获取QQ的视频文件
     */
    fun getQQVideos(stop: Boolean, callback: VideoCallback) {
        var chatVideoPath = localPath + Constant.QQ_RESOURCE_PATH
        if (Build.VERSION.SDK_INT > 28) {
            chatVideoPath = localPath + Constant.QQ_HIGN_VERSION_PATH
        }

        //查找聊天视频文件
//        val videoPath = localPath + Constant.QQ_PICTURE_PATH

        val chatList = arrayListOf<FileWithType>()
        FileUtil.searchVideos(chatVideoPath, object : FCallback {

            override fun onSuccess(step: Enum<FileStatus>) {
            }

            override fun onProgress(step: Enum<FileStatus>, file: File) {
                val name = file.name.toLowerCase(Locale.ROOT)
                if (name.endsWith(".mp4") || name.endsWith(".3gp") || name.endsWith(".avi") || name.endsWith(".rm")
                    || name.endsWith(".rmvb") || name.endsWith(".wmv") || name.endsWith(".mov")
                ) {
                    val date = file.lastModified()
                    val fileWithType = FileWithType(file.name, file.path, file.length().toInt(), date, "wx_video")
                    callback.onProgress(FileStatus.PIC, fileWithType)
                    chatList.add(fileWithType)
                }
            }

            override fun onFailed(step: Enum<FileStatus>, message: String) {
            }
        })


//        val videoFiles = arrayListOf<FileWithType>()
//        for ((index, child) in fileList2.withIndex()) {
//            FileUtil.searchVideos(child, object : File2Callback {
//                override fun onSuccess(step: Enum<FileStatus>) {
//                }
//
//                override fun onProgress(step: Enum<FileStatus>, file: File) {
//                    val name = file.name
//                    val path = file.path
//                    JLog.i("child name = $name")
//                    val date = file.lastModified()
//                    val fileWithType = FileWithType(file.name, path, file.length().toInt(), date, "wx_video")
//                    callback.onProgress(FileStatus.VIDEO, fileWithType)
//                    videoFiles.add(fileWithType)
//                }
//
//                override fun onFailed(step: Enum<FileStatus>, message: String) {
//                }
//            })
//
//            callback.onProgress(FileStatus.VIDEO, fileList1.size + index + 1)
//        }

        if (chatList.isEmpty()) {
            callback.onFailed(FileStatus.VIDEO, "没有找到视频文件")
        } else {
            callback.onSuccess(FileStatus.VIDEO)
        }
    }

    /**
     * 保存支付数据到数据库
     */
    fun savePayData(context: Context, serviceId: String, srcTime: String, isPayed: Boolean, isMenu: Boolean) {
        val payData = PayData(srcTime, Constant.USER_NAME, serviceId, System.currentTimeMillis(), isPayed, isMenu)
        DBManager.insert(context, payData)
    }

    /**
     * 获得支付数据到数据库
     */
    fun checkPay(context: Context, srcTime: String): Boolean {
        val payData = DBManager.getPayDataByKey(context, srcTime)
        if (payData != null) {
            return true
        }

        return false
    }

    /**
     * 获得支付数据到数据库
     */
    fun checkPay(context: Context, srcTime: String, isMenu: Boolean): Boolean {
        val payData = DBManager.getPayDataByKey(context, srcTime, isMenu)
        if (payData != null) {
            return true
        }

        return false
    }

    /**
     * 导出文档文件
     */
    fun exportDocFile(type: String, fileList: MutableList<FileWithType>, callback: FileCallback) {
        val path = exPortPath + type
        val result = FileUtil.createFolder(path)
        if (result) {
            val size = fileList.size
            for ((index, child) in fileList.withIndex()) {
                FileUtil.copyFile(child.path, path, callback)
                callback.onProgress(FileStatus.COPY, (index + 1) * 100 / size)
            }
            callback.onSuccess(FileStatus.COPY)
        }
    }

    /**
     * 导出语音文件
     */
    fun exportVoiceFile(type: String, fileList: MutableList<FileWithType>, callback: FileCallback) {
        if (exPortPath == "") return
        FileUtil.createFolder(exPortPath)
        val path = exPortPath + type
        val result = FileUtil.createFolder(path)
        if (result) {
            val size = fileList.size
            for ((index, child) in fileList.withIndex()) {
                val filePath = AudioManager.getTransformedAudio(child.path)
                if (filePath.isNullOrEmpty()) continue
                JLog.i("export child file path = $filePath")
                FileUtil.copyFile(filePath, path, callback)
                callback.onProgress(FileStatus.COPY, (index + 1) * 100 / size)
            }
            callback.onSuccess(FileStatus.COPY)
        } else {
            callback.onFailed(FileStatus.COPY, "创建目录失败")
        }
    }

    /**
     * 删除文件
     */
    fun deleteFile(fileList: MutableList<FileWithType>, callback: FileWithTypeCallback) {
        Constant.stopScanPic = false
        Constant.stopScanVideo = false
        Constant.stopScanVoice = false
        Constant.stopScanDoc = false

        if (fileList.isNotEmpty()) {
            for (child in fileList) {
                if (!Constant.stopScanPic && !Constant.stopScanVideo && !Constant.stopScanVoice && !Constant.stopScanDoc) {
                    FileUtil.deleteFile(child.path)
                    callback.onProgress(FileStatus.DELETE, child)
                }
            }
            callback.onSuccess(FileStatus.DELETE)
        }
    }


    /**
     * 删除微信数据
     */
    fun deleteWxFile(stop: Boolean, callback: FileCallback) {
        if (localPath == "") return
        val wxOldPath = localPath + Constant.WX_RESOURCE_PATH
        val wxNewPath = localPath + Constant.WX_HIGN_VERSION_PATH
        val wxDownloadPath = localPath + Constant.WX_PICTURE_PATH
        val list = arrayListOf<String>()
        list.add(wxNewPath)
        list.add(wxOldPath)
        list.add(wxDownloadPath)

        for (path in list) {
            val parent = FileUtil.searchFiles(path)
            if (parent.isNotEmpty()) {
                for ((index, child) in parent.withIndex()) {
                    if (stop) break
                    val bl = FileUtil.deleteFile(child.absolutePath)
                    JLog.i("boolean = $bl")
                    callback.onProgress(FileStatus.DELETE, index)
                }
            }
        }

        callback.onSuccess(FileStatus.DELETE)
    }

    fun deleteUnzipBackupFiles() {
        if (jxBackupPath.isEmpty()) return
        FileUtil.deleteDirection(File(jxBackupPath))
    }

    fun getRecoveryUser(): String {
        val list = arrayListOf<String>()
        list.add("成功恢复微信聊天记录")
        list.add("成功恢复微信通讯录")
        list.add("成功恢复微信文档")
        list.add("成功恢复微信图片")
        list.add("成功恢复微信视频")
        list.add("成功恢复微信语音")
        list.add("成功恢复了账单")
        list.add("成功删除了微信记录")
        list.add("成功删除微信文档")
        list.add("成功删除微信视频")
        list.add("成功删除微信语音")
        list.add("成功删除微信图片")

        val map = Dict.getPhoneModel()

        val indexL = Random().nextInt(list.size - 1)
        val indexM = Random().nextInt(map.size - 1)
        return "恭喜" + map[map.keyAt(indexM)] + "用户" + list[indexL] + "  " + Random().nextInt(50) + "分钟前"
    }

    fun savePriceList(context: Context, list: List<Price>) {
        if (list.isEmpty()) return
        for (child in list) {
            DBManager.insert(context, child)
        }
    }

    fun getDBFiles(backupPath: String, type: String, callback: DocCallback) {
        if (backupPath == "") return

        if (type == "local_pic") {
            val pics = arrayListOf<FileWithType>()

            FileUtil.searchWxPics(backupPath, object : FCallback {
                override fun onSuccess(step: Enum<FileStatus>) {
                    callback.onSuccess(FileStatus.PIC)
                }

                override fun onProgress(step: Enum<FileStatus>, file: File) {
                    val name = file.name.toLowerCase(Locale.ROOT)
                    val path = file.path
                    val date = file.lastModified()
                    //公众号或者聊天图片
                    if (name.startsWith("finder_image")) {
                        val fileWithType = FileWithType(file.name, path, file.length().toInt(), date, "wx_in_pic")
                        callback.onProgress(FileStatus.PIC, fileWithType)
                        pics.add(fileWithType)
                        return
                    }

                    //朋友圈图片
                    if (path.contains("/sns/") && name.startsWith("snsu_")) {
                        val fileWithType = FileWithType(file.name, path, file.length().toInt(), date, "wx_in_pic")
                        callback.onProgress(FileStatus.PIC, fileWithType)
                        pics.add(fileWithType)
                        return
                    }


                    //头像
                    if (path.contains("/avatar/") || name.startsWith("finder_avatar")) {
                        val fileWithType = FileWithType(file.name, path, file.length().toInt(), date, "wx_in_pic")
                        callback.onProgress(FileStatus.PIC, fileWithType)
                        pics.add(fileWithType)
                        return
                    }

                    //emoji
                    if (path.endsWith("_cover")) {
                        val fileWithType = FileWithType(file.name, path, file.length().toInt(), date, "wx_in_pic")
                        callback.onProgress(FileStatus.PIC, fileWithType)
                        pics.add(fileWithType)
                        return
                    }

                    //聊天图片
                    if (path.contains("/image2/")) {
                        if (name.startsWith("th_") || name.endsWith("jpg")) {
                            val fileWithType = FileWithType(file.name, path, file.length().toInt(), date, "wx_in_pic")
                            callback.onProgress(FileStatus.PIC, fileWithType)
                            pics.add(fileWithType)
                        }
                        return
                    }

                    //聊天图片
                    if (path.contains("/image/")) {
                        if (name.startsWith("fts_") || name.startsWith("reader_") || path.contains("/luckymoney/")) {
                            val fileWithType = FileWithType(file.name, path, file.length().toInt(), date, "wx_in_pic")
                            callback.onProgress(FileStatus.PIC, fileWithType)
                            pics.add(fileWithType)
                            return
                        }
                    }

                    if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".gif")) {
                        val fileWithType = FileWithType(file.name, file.path, file.length().toInt(), date, "wx_in_pic")
                        callback.onProgress(FileStatus.PIC, fileWithType)
                        pics.add(fileWithType)
                    }

                }

                override fun onFailed(step: Enum<FileStatus>, message: String) {
                    callback.onFailed(FileStatus.PIC, message)
                }
            })


            if (pics.isEmpty()) {
                callback.onFailed(FileStatus.PIC, "没有找到图片")
            } else {
                callback.onSuccess(FileStatus.PIC)
            }
        }


        if (type == "local_doc") {
            val docs = arrayListOf<FileWithType>()
            FileUtil.searchDocs(backupPath, object : FCallback {
                override fun onSuccess(step: Enum<FileStatus>) {
                }

                override fun onProgress(step: Enum<FileStatus>, file: File) {
                    val name = file.name
                    val path = file.path
                    val date = file.lastModified()

                    if (name.endsWith(".txt") || name.endsWith(".pdf") || name.endsWith(".csv")
                        || name.endsWith(".xls") || name.endsWith(".xlsx") || name.endsWith(".doc")
                        || name.endsWith(".docx") || name.endsWith(".ppt") || name.endsWith(".pptx")
                        || name.endsWith(".rtf") || name.endsWith(".zip") || name.endsWith(".rar")
                        || name.endsWith(".xml") || name.endsWith(".psd") || name.endsWith(".tar")
                        || name.endsWith(".db")
                    ) {
                        val fileWithType = FileWithType(file.name, path, file.length().toInt(), date, "wx_in_doc")
                        callback.onProgress(FileStatus.DOC, fileWithType)
                        docs.add(fileWithType)
                    }
                }

                override fun onFailed(step: Enum<FileStatus>, message: String) {
                }
            })


            if (docs.isEmpty()) {
                callback.onFailed(FileStatus.DOC, "没有找到文件")
            } else {
                callback.onSuccess(FileStatus.DOC)
            }
        }

        if (type == "local_voice") {
            val voices = arrayListOf<FileWithType>()
            FileUtil.searchVoices(backupPath, false, object : FCallback {
                override fun onSuccess(step: Enum<FileStatus>) {
                }

                override fun onProgress(step: Enum<FileStatus>, file: File) {
                    val name = file.name
                    val path = file.path
                    JLog.i("child name = $name")
                    val date = file.lastModified()
                    if (name.endsWith(".amr")) {
                        val fileWithType = FileWithType(file.name, path, file.length().toInt(), date, "wx_in_voice")
                        callback.onProgress(FileStatus.VOICE, fileWithType)
                        voices.add(fileWithType)
                    }

                }

                override fun onFailed(step: Enum<FileStatus>, message: String) {
                }
            })

            if (voices.isEmpty()) {
                callback.onFailed(FileStatus.VOICE, "没有找到语音")
            } else {
                callback.onSuccess(FileStatus.VOICE)
            }
        }
    }

    fun openFileInNative(context: Context, FILE_NAME: String, type: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        if (Build.VERSION.SDK_INT <= 23) {
            val file = File(context.externalCacheDir, FILE_NAME)
            val uri = Uri.parse(file.path)
            intent.setDataAndType(uri, "application/$type")
        } else {
            val file = File(FILE_NAME)
            val uri: Uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
            intent.setDataAndType(uri, "application/$type")
        }

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            JLog.i("Activity was not found for intent, $intent")
        }
    }

    fun savePicsOrVideoToAlbum(context: Context, type: String, files: MutableList<FileWithType>, callback: FileCallback) {
        if (files.isEmpty()) return
        val size = files.size
        when (type) {
            "pic" -> {
                for ((index, file) in files.withIndex()) {
                    FileUtil.saveImage(context, File(file.path))
                    callback.onProgress(FileStatus.SAVE, index / size)
                }
                callback.onSuccess(FileStatus.SAVE)
            }

            "video" -> {
                for ((index, file) in files.withIndex()) {
                    FileUtil.saveVideo(context, File(file.path))
                    callback.onProgress(FileStatus.SAVE, index / size)
                }
                callback.onSuccess(FileStatus.SAVE)
            }
        }
    }

    fun replaceBackupAPkForHuawei(context: Activity) {
        CoroutineScope(Dispatchers.IO).launch {
            val packName = "huaweibackup.apk"
            val path = jxBackupPath + packName
            val file = File(path)
            if (!file.exists()) {
                AppUtil.copyApkFromAssets(context, packName, jxBackupPath)
            }
        }
    }

    fun installKoBackupApk(activity: Activity) {
        val packName = "huaweibackup.apk"
        val path = jxBackupPath + packName
        val file = File(path)
        AppUtil.installApk(file, activity)
    }

    fun clearCache() {

    }
}