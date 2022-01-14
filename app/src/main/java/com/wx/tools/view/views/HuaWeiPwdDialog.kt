package com.wx.tools.view.views

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.wx.tools.R
import com.wx.tools.callback.FileDialogCallback
import com.wx.tools.utils.AppUtil
import com.wx.tools.utils.ToastUtil


class HuaWeiPwdDialog(context: Context, callback: FileDialogCallback) : Dialog(context, R.style.app_dialog) {
    private val mContext: Context = context
    private val mCallback: FileDialogCallback = callback
    private lateinit var title: TextView
    private lateinit var pwd: EditText
    private lateinit var ok: Button


    init {
        initVew()
    }

    private fun initVew() {
        val dialogContent = LayoutInflater.from(mContext).inflate(R.layout.d_huawei_pwd, null)
        setContentView(dialogContent)
        setCancelable(true)

        title = dialogContent.findViewById(R.id.pwd_title)
        pwd = dialogContent.findViewById(R.id.pwd)
        ok = dialogContent.findViewById(R.id.ok)

        ok.setOnClickListener {
            if (pwd.text.isNotEmpty()) {
                mCallback.onSuccess(pwd.text.toString())
            } else {
                ToastUtil.showShort(mContext, "密码错误，请重新输入")
            }
        }

        show()
    }


    override fun show() {
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.CENTER
            width = AppUtil.getScreenWidth(context) - 50
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        super.show()
    }


}