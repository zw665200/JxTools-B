package com.wx.tools.view.views

import android.app.Activity
import android.app.Dialog
import android.text.SpannableString
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.TextView
import com.wx.tools.R
import com.wx.tools.bean.FileBean
import com.wx.tools.callback.DialogCallback
import com.wx.tools.utils.AppUtil


class AuthDialog(private val activity: Activity, callback: DialogCallback) : Dialog(activity, R.style.app_dialog) {
    private lateinit var content: TextView
    private lateinit var refuse: TextView
    private lateinit var agree: TextView
    private var mCallback = callback


    init {
        initVew()
    }

    private fun initVew() {
        val dialogContent = LayoutInflater.from(activity).inflate(R.layout.d_service_auth, null)
        setContentView(dialogContent)
        setCancelable(false)

        content = dialogContent.findViewById(R.id.service_content)
        refuse = dialogContent.findViewById(R.id.service_refuse)
        agree = dialogContent.findViewById(R.id.service_agree)

        content.text = SpannableString(activity.getString(R.string.auth_service_content))

        refuse.setOnClickListener {
            mCallback.onCancel()
        }

        agree.setOnClickListener {
            mCallback.onSuccess(FileBean("", "", "", 0))
        }

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