package com.wx.tools.callback

import com.wx.tools.bean.FileStatus
import com.wx.tools.bean.FileWithType

interface PicCallback {
    fun onSuccess(step: Enum<FileStatus>)
    fun onProgress(step: Enum<FileStatus>, index: Int)
    fun onProgress(step: Enum<FileStatus>, file: FileWithType)
    fun onFailed(step: Enum<FileStatus>, message: String)
}