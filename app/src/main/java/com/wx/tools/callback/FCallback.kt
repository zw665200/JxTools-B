package com.wx.tools.callback

import com.wx.tools.bean.FileStatus
import java.io.File

interface FCallback {
    fun onSuccess(step: Enum<FileStatus>)
    fun onProgress(step: Enum<FileStatus>, file: File)
    fun onFailed(step: Enum<FileStatus>, message: String)
}