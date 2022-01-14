package com.wx.tools.callback

import com.wx.tools.bean.FileBean

interface DialogCallback {
    fun onSuccess(file: FileBean)
    fun onCancel()
}