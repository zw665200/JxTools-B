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
import com.wx.tools.bean.FileWithType
import com.wx.tools.callback.FileCallback
import com.wx.tools.controller.Constant
import com.wx.tools.controller.WxManager
import com.wx.tools.utils.AppUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class ExportFileDialog(context: Context, list: MutableList<FileWithType>, type: String) : Dialog(context, R.style.app_dialog),
    CoroutineScope by MainScope() {
    private val mContext: Context = context
    private var mList = mutableListOf<FileWithType>()
    private lateinit var title: TextView
    private lateinit var export: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var cancel: Button
    private var mType = type

    init {
        initVew()
        mList.addAll(list)
    }

    private fun initVew() {
        val dialogContent = LayoutInflater.from(mContext).inflate(R.layout.d_export_files, null)
        setContentView(dialogContent)
        setCancelable(false)

        title = dialogContent.findViewById(R.id.export_title)
        export = dialogContent.findViewById(R.id.export_path)
        progressBar = dialogContent.findViewById(R.id.export_progress)

        cancel = dialogContent.findViewById(R.id.dialog_cancel)
        cancel.setOnClickListener { cancel() }

    }

    private fun beginExport() {

        when (mType) {
            "export_voice" -> {
                val path = "位置：文件管理" + Constant.EXPORT_PATH + mType
                export.text = path

                launch(Dispatchers.IO) {
                    WxManager.getInstance(mContext).exportVoiceFile(mType, mList, object : FileCallback {
                        override fun onSuccess(step: Enum<FileStatus>) {
                            launch(Dispatchers.Main) {
                                title.text = "导出完成"
                                cancel.text = "确定"
                            }
                        }

                        override fun onProgress(step: Enum<FileStatus>, index: Int) {
                            launch(Dispatchers.Main) {
                                progressBar.progress = index
                            }
                        }

                        override fun onFailed(step: Enum<FileStatus>, message: String) {
                            launch(Dispatchers.Main) {
                                cancel.text = "确定"
                            }
                        }
                    })
                }
            }

            "export_doc" -> {
                val path = "位置：文件管理" + Constant.EXPORT_PATH + mType
                export.text = path

                launch(Dispatchers.IO) {
                    WxManager.getInstance(mContext).exportDocFile(mType, mList, object : FileCallback {
                        override fun onSuccess(step: Enum<FileStatus>) {
                            launch(Dispatchers.Main) {
                                title.text = "导出完成"
                                cancel.text = "确定"
                            }
                        }

                        override fun onProgress(step: Enum<FileStatus>, index: Int) {
                            launch(Dispatchers.Main) {
                                progressBar.progress = index
                            }
                        }

                        override fun onFailed(step: Enum<FileStatus>, message: String) {
                            launch(Dispatchers.Main) {
                                cancel.text = "确定"
                            }
                        }
                    })
                }

            }


            "recovery_pic" -> {
                export.text = "导出到相册"
                launch(Dispatchers.IO) {
                    WxManager.getInstance(mContext).savePicsOrVideoToAlbum(mContext, "pic", mList, object : FileCallback {
                        override fun onSuccess(step: Enum<FileStatus>) {
                            launch(Dispatchers.Main) {
                                progressBar.progress = 100
                                title.text = "导出完成"
                                cancel.text = "确定"
                            }
                        }

                        override fun onProgress(step: Enum<FileStatus>, index: Int) {
                            launch(Dispatchers.Main) {
                                progressBar.progress = index
                            }
                        }

                        override fun onFailed(step: Enum<FileStatus>, message: String) {
                            launch(Dispatchers.Main) {
                                cancel.text = "确定"
                            }
                        }
                    })
                }
            }

            "recovery_video" -> {
                export.text = "导出到相册"
                launch(Dispatchers.IO) {
                    WxManager.getInstance(mContext).savePicsOrVideoToAlbum(mContext, "video", mList, object : FileCallback {
                        override fun onSuccess(step: Enum<FileStatus>) {
                            launch(Dispatchers.Main) {
                                progressBar.progress = 100
                                title.text = "导出完成"
                                cancel.text = "确定"
                            }
                        }

                        override fun onProgress(step: Enum<FileStatus>, index: Int) {
                            launch(Dispatchers.Main) {
                                progressBar.progress = index
                            }
                        }

                        override fun onFailed(step: Enum<FileStatus>, message: String) {
                            launch(Dispatchers.Main) {
                                cancel.text = "确定"
                            }
                        }
                    })
                }

            }

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
        beginExport()
    }


}