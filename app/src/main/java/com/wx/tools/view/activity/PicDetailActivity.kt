package com.wx.tools.view.activity

import android.content.Intent
import android.graphics.BitmapFactory
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.wx.tools.R
import com.wx.tools.bean.FileStatus
import com.wx.tools.bean.FileWithType
import com.wx.tools.callback.FileWithTypeCallback
import com.wx.tools.controller.PayManager
import com.wx.tools.controller.WxManager
import com.wx.tools.utils.AppUtil
import com.wx.tools.utils.JLog
import com.wx.tools.view.base.BaseActivity
import com.wx.tools.view.views.ExportFileDialog
import com.tencent.mmkv.MMKV
import com.wx.tools.controller.Constant
import kotlinx.coroutines.*

class PicDetailActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var img: ImageView
    private lateinit var shuiYin: ImageView
    private lateinit var name: TextView
    private lateinit var description: TextView
    private lateinit var export: Button
    private lateinit var delete: Button
    private var file: FileWithType? = null
    private var mainList = arrayListOf<FileWithType>()
    private var payed = false
    private var serviceId: Int = 0


    override fun setLayout(): Int {
        return R.layout.pics_detail
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        back.setOnClickListener { finish() }
        img = findViewById(R.id.iv_img)
        shuiYin = findViewById(R.id.shuiyin)
        name = findViewById(R.id.pic_name)
        description = findViewById(R.id.pic_detail)
        export = findViewById(R.id.recovery)
        delete = findViewById(R.id.delete)

        export.setOnClickListener { nextStep() }
        delete.setOnClickListener { deletePics() }
    }

    override fun initData() {
        file = intent.getParcelableExtra("file")
        serviceId = intent.getIntExtra("serviceId", 0)
        if (file != null) {
            JLog.i("file path = ${file!!.path}")
            val date = AppUtil.timeStamp2Date(file!!.date.toString(), null)
            val dText = "创建时间: $date"
            name.text = dText

            val size = file!!.size / 1024

            val bitmap = BitmapFactory.decodeFile(file!!.path)
            if (bitmap != null) {
                val width = bitmap.width
                val height = bitmap.height
                val text = "大小: $size KB    分辨率: $width * $height"
                description.text = text
                Glide.with(this).load(file!!.path).into(img)
                mainList.add(file!!)
            }

        }

    }

    override fun onResume() {
        super.onResume()
        checkPay()
    }


    private fun checkPay() {
        PayManager.getInstance().checkRecoveryPay(this, serviceId) {
            if (it) {
                payed = true
                shuiYin.visibility = View.GONE
            } else {
                payed = false
            }
        }
    }

    private fun nextStep() {
        if (mainList.isEmpty()) return
        if (payed) {
            //pay success , do something
            ExportFileDialog(this, mainList, "recovery_pic").show()
        } else {
            toPayPage()
        }
    }

    private fun deletePics() {
        PayManager.getInstance().checkDeletePay(this) {
            if (it) {
                launch(Dispatchers.IO) {
                    WxManager.getInstance(this@PicDetailActivity).deleteFile(mainList,  object : FileWithTypeCallback {
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
            } else {
                toSinglePayPage()
            }
        }
    }

    private fun toPayPage() {
        val intent = Intent()
        intent.setClass(this, PayActivity::class.java)
        intent.putExtra("serviceId", serviceId)
        intent.putExtra("title", "微信图片恢复")
        startActivity(intent)
    }

    private fun toSinglePayPage() {
        val intent = Intent()
        intent.setClass(this, SinglePayActivity::class.java)
        intent.putExtra("serviceCode", Constant.DELETE)
        startActivity(intent)
    }


}