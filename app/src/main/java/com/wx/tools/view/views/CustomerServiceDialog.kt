package com.wx.tools.view.views

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.wx.tools.R
import com.wx.tools.bean.FileBean
import com.wx.tools.callback.DialogCallback
import com.wx.tools.utils.AppUtil
import com.wx.tools.view.activity.AgreementActivity


class CustomerServiceDialog(private val activity: Activity, callback: DialogCallback) : Dialog(activity, R.style.app_dialog) {
    private lateinit var content: TextView
    private lateinit var refuse: TextView
    private lateinit var agree: TextView
    private var mCallback = callback


    init {
        initVew()
    }

    private fun initVew() {
        val dialogContent = LayoutInflater.from(activity).inflate(R.layout.d_customer_service, null)
        setContentView(dialogContent)
        setCancelable(false)

        content = dialogContent.findViewById(R.id.service_content)
        refuse = dialogContent.findViewById(R.id.service_refuse)
        agree = dialogContent.findViewById(R.id.service_agree)

        val span = SpannableString(activity.getString(R.string.customer_service_agreement))
        span.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent()
                intent.setClass(activity, AgreementActivity::class.java)
                intent.putExtra("index", 0)
                activity.startActivity(intent)
            }
        }, 13, 19, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        span.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent()
                intent.setClass(activity, AgreementActivity::class.java)
                intent.putExtra("index", 1)
                activity.startActivity(intent)
            }
        }, 20, 26, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        span.setSpan(ForegroundColorSpan(ContextCompat.getColor(activity,R.color.color_blue)), 13, 19, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        span.setSpan(ForegroundColorSpan(ContextCompat.getColor(activity,R.color.color_blue)), 20, 26, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        content.text = span
        content.movementMethod = LinkMovementMethod.getInstance()

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
            width = AppUtil.getScreenWidth(context) * 5 / 6
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        super.show()
    }


}