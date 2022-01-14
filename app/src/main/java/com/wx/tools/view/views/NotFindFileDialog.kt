package com.wx.tools.view.views

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import com.wx.tools.R
import com.wx.tools.utils.AppUtil


class NotFindFileDialog(context: Context) : Dialog(context, R.style.app_dialog) {
    private val mContext: Context = context
    private lateinit var ok: Button

    init {
        initVew()
    }

    private fun initVew() {
        val dialogContent = LayoutInflater.from(mContext).inflate(R.layout.d_not_find_file, null)
        setContentView(dialogContent)
        setCancelable(true)

        ok = dialogContent.findViewById(R.id.ok)

        ok.setOnClickListener {
            cancel()
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