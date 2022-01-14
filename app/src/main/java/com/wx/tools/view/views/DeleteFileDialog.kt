package com.wx.tools.view.views

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.wx.tools.R
import com.wx.tools.bean.FileStatus
import com.wx.tools.callback.FileCallback
import com.wx.tools.controller.WxManager
import com.wx.tools.utils.AppUtil
import kotlinx.coroutines.*


class DeleteFileDialog(context: Context) : Dialog(context, R.style.app_dialog), CoroutineScope by MainScope() {
    private val mContext: Context = context
    private lateinit var title: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var cancel: Button
    private var stop = false
    private var progress = 0

    init {
        initVew()
    }

    private fun initVew() {
        val dialogContent = LayoutInflater.from(mContext).inflate(R.layout.d_delete_files, null)
        setContentView(dialogContent)
        setCancelable(false)

        title = dialogContent.findViewById(R.id.delete_title)
        progressBar = dialogContent.findViewById(R.id.delete_progress)
        cancel = dialogContent.findViewById(R.id.delete_cancel)

        cancel.setOnClickListener { cancel() }

    }

    private fun beginExport() {


        launch(Dispatchers.IO) {

            WxManager.getInstance(mContext).deleteWxFile(stop, object : FileCallback {
                override fun onSuccess(step: Enum<FileStatus>) {
                    launch(Dispatchers.Main) {
                        title.text = "删除完成"
                        cancel.text = "确定"
                        progressBar.progress = progressBar.max
                    }
                }

                override fun onProgress(step: Enum<FileStatus>, index: Int) {
                    launch(Dispatchers.Main) {
                        progress++
                        progressBar.progress = progress
                    }
                }

                override fun onFailed(step: Enum<FileStatus>, message: String) {
                    launch(Dispatchers.Main) {
                        title.text = "导出失败"
                        cancel.text = "确定"
                    }
                }
            })
        }
    }

    override fun cancel() {
        stop = true
        super.cancel()
    }

    override fun onStart() {
        super.onStart()
    }


    override fun show() {
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.CENTER
            width = AppUtil.getScreenWidth(context) - 50
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        super.show()
        beginExport()
    }


}