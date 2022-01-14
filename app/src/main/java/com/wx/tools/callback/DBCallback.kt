package com.wx.tools.callback

import com.wx.tools.bean.FileStatus

interface DBCallback {
    fun onSuccess(step: Enum<FileStatus>)
    fun onProgress(step: Enum<FileStatus>, message: String)
    fun onProgress(step: Enum<FileStatus>, index: Int)
    fun onFailed(step: Enum<FileStatus>, message: String)
}