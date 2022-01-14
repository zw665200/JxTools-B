package com.wx.tools.view.activity

import android.content.Context
import android.content.Intent
import android.widget.*
import com.bumptech.glide.Glide
import com.wx.tools.R
import com.wx.tools.bean.FileStatus
import com.wx.tools.bean.FileWithType
import com.wx.tools.callback.FileWithTypeCallback
import com.wx.tools.controller.WxManager
import com.wx.tools.utils.AppUtil
import com.wx.tools.view.base.BaseActivity
import com.wx.tools.view.views.ExportFileDialog
import com.tencent.smtt.sdk.QbSdk
import com.wx.tools.controller.Constant
import com.wx.tools.controller.PayManager
import kotlinx.android.synthetic.main.item_doc.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DocDetailActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var style: ImageView
    private lateinit var docName: TextView
    private lateinit var docDesc: TextView
    private lateinit var export: Button
    private lateinit var delete: Button
    private val mainList = arrayListOf<FileWithType>()
    private var file: FileWithType? = null

    override fun setLayout(): Int {
        return R.layout.a_doc_detail
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        style = findViewById(R.id.style)
        back.setOnClickListener { finish() }

        docName = findViewById(R.id.doc_name)
        docDesc = findViewById(R.id.doc_description)

        export = findViewById(R.id.recovery)
        delete = findViewById(R.id.delete)

        export.setOnClickListener { nextStep(this) }
        delete.setOnClickListener { deleteDocs() }
        style.setOnClickListener { openFile(file!!) }
    }

    override fun initData() {
        file = intent.getParcelableExtra("file")
        if (file != null) {
            mainList.add(file!!)
            val date = AppUtil.timeStamp2Date(file!!.date.toString(), null)
            docName.text = file!!.name
            var size = file!!.size
            if (size > 1024) {
                size /= 1024
                docDesc.text = date + " / " + size + "KB"
            } else {
                docDesc.text = date + " / " + size + "B"
            }

            if (file!!.name.endsWith(".doc") || file!!.name.endsWith(".docx")) {
                Glide.with(this).load(R.drawable.file_word).into(style)
                return
            }

            if (file!!.name.endsWith(".txt")) {
                Glide.with(this).load(R.drawable.file_txt).into(style)
                return
            }

            if (file!!.name.endsWith(".ppt") || file!!.name.endsWith(".pptx")) {
                Glide.with(this).load(R.drawable.file_ppt).into(style)
                return
            }

            if (file!!.name.endsWith(".xls") || file!!.name.endsWith(".xlsx") || file!!.name.endsWith(".csv")) {
                Glide.with(this).load(R.drawable.file_excel).into(style)
                return
            }

            if (file!!.name.endsWith(".pdf")) {
                Glide.with(this).load(R.drawable.file_pdf).into(style)
                return
            }

            if (file!!.name.endsWith(".html") || file!!.name.endsWith(".xhtml")) {
                Glide.with(this).load(R.drawable.file_html).into(style)
                return
            }

            if (file!!.name.endsWith(".ai")) {
                Glide.with(this).load(R.drawable.file_ai).into(style)
                return
            }

            if (file!!.name.endsWith(".cad")) {
                Glide.with(this).load(R.drawable.file_cad).into(style)
                return
            }

            if (file!!.name.endsWith(".psd")) {
                Glide.with(this).load(R.drawable.file_ps).into(style)
                return
            }

            if (file!!.name.endsWith(".java") || file!!.name.endsWith(".php") || file!!.name.endsWith(".exe")
                || file!!.name.endsWith(".python") || file!!.name.endsWith(".class") || file!!.name.endsWith(".config")
                || file!!.name.endsWith(".json") || file!!.name.endsWith(".xml") || file!!.name.endsWith(".kt")
            ) {
                Glide.with(this).load(R.drawable.file_html).into(style)
                return
            }

            if (file!!.name.endsWith(".zip") || file!!.name.endsWith(".rar") || file!!.name.endsWith(".tar")) {
                Glide.with(this).load(R.drawable.file_rar).into(style)
            } else {
                Glide.with(this).load(R.drawable.file_default).into(style)
            }
        }
    }


    private fun nextStep(context: Context) {
        if (mainList.isEmpty()) return
        //pay success , do something
        ExportFileDialog(context, mainList, "wx_doc").show()
    }

    private fun openFile(file: FileWithType) {
//        val intent = Intent()
//        intent.setClass(this, FileReaderActivity::class.java)
//        intent.putExtra("title", file.name)
//        intent.putExtra("page", file.path)
//        startActivity(intent)

        val params = HashMap<String, String>()
        params["style"] = "2"
        params["local"] = "true"
        QbSdk.openFileReader(this, file.path, params, { })
    }

    private fun deleteDocs() {

        PayManager.getInstance().checkDeletePay(this) {
            if (it) {
                launch(Dispatchers.IO) {
                    WxManager.getInstance(this@DocDetailActivity).deleteFile(mainList,  object : FileWithTypeCallback {
                        override fun onSuccess(step: Enum<FileStatus>) {
                            launch(Dispatchers.Main) {
                            }
                        }

                        override fun onProgress(step: Enum<FileStatus>, file: FileWithType) {
                            launch(Dispatchers.Main) {
                                val intent = Intent()
                                intent.putExtra("file", file)
                                setResult(0x301, intent)
                                finish()
                            }
                        }

                        override fun onFailed(step: Enum<FileStatus>, message: String) {
                            launch(Dispatchers.Main) {
                            }
                        }
                    })
                }
            }else{
                toSinglePayPage()
            }
        }
    }

    private fun toSinglePayPage() {
        val intent = Intent()
        intent.setClass(this, SinglePayActivity::class.java)
        intent.putExtra("serviceCode", Constant.DELETE)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        QbSdk.closeFileReader(this)
    }

}