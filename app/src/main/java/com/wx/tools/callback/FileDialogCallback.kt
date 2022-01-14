package com.wx.tools.callback

interface FileDialogCallback {
    fun onSuccess(str: String)
    fun onCancel()
}