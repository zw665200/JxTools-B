package com.wx.tools.view.activity

import android.content.Intent
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.wx.tools.R
import com.wx.tools.bean.FileStatus
import com.wx.tools.bean.FileWithType
import com.wx.tools.callback.FileWithTypeCallback
import com.wx.tools.controller.AudioManager
import com.wx.tools.controller.Constant
import com.wx.tools.controller.PayManager
import com.wx.tools.controller.WxManager
import com.wx.tools.utils.AppUtil
import com.wx.tools.utils.AudioTracker.AudioPlayListener
import com.wx.tools.utils.ToastUtil
import com.wx.tools.view.base.BaseActivity
import com.wx.tools.view.views.ExportFileDialog
import kotlinx.coroutines.*

class VoiceDetailActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var img: ImageView
    private lateinit var description: TextView
    private lateinit var export: Button
    private lateinit var delete: Button
    private var file: FileWithType? = null
    private lateinit var play: ImageView
    private var mainPics = arrayListOf<FileWithType>()
    private var quit = false
    private var status = Status.START
    private var callback: AudioPlayListener?=null

    override fun setLayout(): Int {
        return R.layout.voice_detail
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        description = findViewById(R.id.voice_description)
        export = findViewById(R.id.recovery)
        delete = findViewById(R.id.delete)
        play = findViewById(R.id.play)

        back.setOnClickListener { finish() }
        export.setOnClickListener { saveVoices() }
        delete.setOnClickListener { deleteVoices() }
        play.setOnClickListener { startPlay() }

    }

    override fun initData() {
        file = intent.getParcelableExtra("file")
        if (file != null) {
            mainPics.add(file!!)

            val date = AppUtil.timeStamp2Date(file!!.date.toString(), null) + " / " + file!!.size / 1024 + "KB"
            description.text = date

            callback = object : AudioPlayListener {
                override fun onStart() {
                    if (quit) return
                    launch(Dispatchers.Main) { Glide.with(this@VoiceDetailActivity).load(R.drawable.pause).into(play) }
                }

                override fun onStop() {
                    if (quit) return
                    launch(Dispatchers.Main) {
                        status = Status.PAUSE
                        Glide.with(this@VoiceDetailActivity).load(R.drawable.play).into(play)
                    }
                }

                override fun onError(message: String) {
                    if (quit) return
                    launch(Dispatchers.Main) {
                        status = Status.STOP
                        ToastUtil.showShort(this@VoiceDetailActivity, message)
                    }
                }
            }
        }
    }

    private fun startPlay() {
        if (callback != null){
            AudioManager.play(file!!.path, callback!!)
        }
    }

    private fun saveVoices() {
        if (mainPics.isEmpty()) return

        //pay success , do something
        ExportFileDialog(this, mainPics, "export_voice").show()
    }

    private fun deleteVoices() {
        if (mainPics.isEmpty()) return

        launch(Dispatchers.IO) {
            WxManager.getInstance(this@VoiceDetailActivity).deleteFile(mainPics,  object : FileWithTypeCallback {
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
                        ToastUtil.showShort(this@VoiceDetailActivity,message)
                    }
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        quit = true

        AudioManager.release()
    }

    enum class Status {
        START, PLAY, PAUSE, STOP
    }
}