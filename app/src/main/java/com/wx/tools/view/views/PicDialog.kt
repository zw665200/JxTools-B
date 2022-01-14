package com.wx.tools.view.views

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.wx.tools.R


class PicDialog : Dialog {
    private val mContext: Context
    private var resourceId: Int
    private lateinit var pic: ImageView
    private lateinit var cancel: ImageView


    constructor(context: Context, resourceId: Int) : super(context, R.style.app_dialog) {
        this.mContext = context
        this.resourceId = resourceId
        initVew(context)
    }

    private fun initVew(context: Context) {
        val dialogContent = LayoutInflater.from(mContext).inflate(R.layout.d_pics, null)
        setContentView(dialogContent)
        setCancelable(false)

        pic = dialogContent.findViewById(R.id.full_size_pic)
        cancel = dialogContent.findViewById(R.id.dialog_cancel)
        cancel.setOnClickListener { cancel() }

    }

    override fun onStart() {
        super.onStart()
    }

    override fun show() {
        val outMetrics = mContext.resources.displayMetrics
        val widthMetrics = outMetrics.widthPixels
        val heightMetrics = outMetrics.heightPixels

        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.CENTER
            width = widthMetrics
            height = heightMetrics
        }

        //load pic
        Glide.with(mContext).load(resourceId).into(pic)

        super.show()
    }


}