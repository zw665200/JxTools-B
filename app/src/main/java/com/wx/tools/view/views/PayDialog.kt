package com.wx.tools.view.views

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import com.wx.tools.R
import com.wx.tools.callback.DCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope


class PayDialog(context: Context, callback: DCallback) : Dialog(context, R.style.app_dialog),
    CoroutineScope by MainScope() {
    private val mContext: Context = context
    private lateinit var cancel: Button
    private lateinit var pay: Button
    private var mCallback = callback

    init {
        initVew()
    }

    private fun initVew() {
        val dialogContent = LayoutInflater.from(mContext).inflate(R.layout.d_pay, null)
        setContentView(dialogContent)
        setCancelable(false)


    }


    override fun cancel() {
        super.cancel()
        mCallback.onCancel()
    }


    override fun show() {
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.CENTER
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        super.show()
    }


}