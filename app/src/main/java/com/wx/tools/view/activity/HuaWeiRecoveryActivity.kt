package com.wx.tools.view.activity

import android.app.AlertDialog
import android.content.Intent
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wx.tools.R
import com.wx.tools.adapter.DataAdapter
import com.wx.tools.bean.*
import com.wx.tools.callback.DBCallback
import com.wx.tools.controller.WxManager
import com.wx.tools.utils.JLog
import com.wx.tools.view.base.BaseActivity
import com.kofigyan.stateprogressbar.StateProgressBar
import kotlinx.android.synthetic.main.a_wechat_recovery.*
import kotlinx.android.synthetic.main.d_huawei_pwd.*
import kotlinx.android.synthetic.main.item_steps.view.*
import kotlinx.coroutines.*

class HuaWeiRecoveryActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var steps: RecyclerView
    private var descriptionData = arrayListOf<String>()
    private lateinit var stateProgressBar: StateProgressBar
    private lateinit var mAdapter: DataAdapter<Step>
    private var stepList = mutableListOf<Step>()
    private var filePath: String? = null
    private var fileDate: Long = 0
    private var serviceId: Int = 0
    private var percent = 0


    override fun setLayout(): Int {
        return R.layout.a_wechat_recovery
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        steps = findViewById(R.id.steps)

        back.setOnClickListener { quit() }

        steps.layoutManager = LinearLayoutManager(this)

        stateProgressBar = findViewById(R.id.your_state_progress_bar_id)

    }

    override fun initData() {
        filePath = intent.getStringExtra("filePath")
        fileDate = intent.getLongExtra("fileDate", 0L)
        serviceId = intent.getIntExtra("serviceId", 0)
        val pwd = intent.getStringExtra("pwd")
        if (filePath != null && serviceId != 0 && pwd != null && fileDate != 0L) {
            initStep(pwd)
            getMessage(filePath!!, pwd)

            descriptionData.add("开始")
            descriptionData.add("解压文件")
            descriptionData.add("解析数据")
            descriptionData.add("解析完成")
            stateProgressBar.stateSize = 12f
            stateProgressBar.stateNumberTextSize = 12f
            stateProgressBar.stateLineThickness = 4f

            stateProgressBar.descriptionTopSpaceIncrementer = 10f
            stateProgressBar.stateDescriptionSize = 12f
            stateProgressBar.setStateDescriptionData(descriptionData)
        }
    }


    private fun initStep(pwd: String) {
        stepList.clear()
        stepList.add(Step("输入备份密码：$pwd", 0))
        stepList.add(Step(getString(R.string.recovery_step1), 0))
        stepList.add(Step("", 0))

        mAdapter = DataAdapter.Builder<Step>()
            .setData(stepList)
            .setLayoutId(R.layout.item_steps)
            .addBindView { itemView, itemData ->
                itemView.step_text.text = itemData.content
                if (itemData.type == 0) {
                    itemView.complete.visibility = View.GONE
                } else {
                    itemView.complete.visibility = View.VISIBLE
                }
            }
            .create()
        steps.adapter = mAdapter
        mAdapter.notifyDataSetChanged()
    }

    private fun getMessage(filePath: String, pwd: String) {
        val callback = object : DBCallback {
            override fun onSuccess(step: Enum<FileStatus>) {

                launch(Dispatchers.Main) {
                    when (step) {
                        FileStatus.UNZIP_BACKUP -> {
                            //添加下一步
                            stepList.add(Step(getString(R.string.recovery_step2), 1))
                            stepList.add(Step(getString(R.string.recovery_step3), 0))
                            mAdapter.notifyDataSetChanged()
                            percent = 0
                        }

                        FileStatus.UNZIP_WX -> {
                            stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.THREE)
                            stepList.add(Step(getString(R.string.recovery_step4), 1))
                            stepList.add(Step(getString(R.string.recovery_step5), 0))
                            mAdapter.notifyDataSetChanged()
                        }

                        FileStatus.DECODE_ACCOUNT -> {
                        }

                        FileStatus.DECODE_MESSAGE -> {
                            mAdapter.notifyDataSetChanged()
                        }

                        FileStatus.DECODE_CONTACT -> {
                            stepList.add(Step(getString(R.string.recovery_step6), 1))
                            stepList.add(Step(getString(R.string.recovery_step8), 1))
                            stepList.add(Step(getString(R.string.recovery_step9), 0))
                            mAdapter.notifyDataSetChanged()
                        }

                        FileStatus.ANALYZE -> {
                            stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.FOUR)
                            stepList.add(Step(getString(R.string.recovery_step10), 1))
                            mAdapter.notifyDataSetChanged()
                            stateProgressBar.setAllStatesCompleted(true)
                            nextStep()
                        }
                    }
                }
            }

            override fun onProgress(step: Enum<FileStatus>, message: String) {
                launch(Dispatchers.Main) {
                    when (step) {
                        FileStatus.UNZIP_BACKUP -> {
                            stepList.removeAt(stepList.size - 1)
                            stepList.add(Step("已完成 $message", 0))
                            mAdapter.notifyItemChanged(stepList.size - 1)
                        }

                        FileStatus.DECODE_ACCOUNT -> {
                            stepList.add(Step(message, 0))
                            mAdapter.notifyItemInserted(stepList.size - 1)
                        }

                        FileStatus.DECODE_MESSAGE -> {
                            stepList.add(Step("已完成 $percent%", 0))
                            mAdapter.notifyItemInserted(stepList.size - 1)
                        }
                    }

                }
            }

            override fun onProgress(step: Enum<FileStatus>, index: Int) {
                launch(Dispatchers.Main) {

                    when (step) {
                        FileStatus.UNZIP_BACKUP -> {
//                            percent = index
//                            stepList.removeAt(stepList.size - 1)
//                            stepList.add(Step("已完成 $percent%", 0))
//                            mAdapter.notifyItemChanged(stepList.size - 1)
                        }

                        FileStatus.UNZIP_WX -> {

                        }

                        FileStatus.DECODE_MESSAGE -> {
                            percent = index
                            stepList.removeAt(stepList.size - 1)
                            stepList.add(Step("已完成 $percent%", 0))
                            mAdapter.notifyItemChanged(stepList.size - 1)
                        }

                        FileStatus.DECODE_CONTACT -> {

                        }
                        FileStatus.ANALYZE -> {

                        }
                    }
                }
            }

            override fun onFailed(step: Enum<FileStatus>, message: String) {
                launch(Dispatchers.Main) {
                    JLog.i(message)
                    stepList.add(Step(message, 0))
                    mAdapter.notifyDataSetChanged()
                }
            }
        }


        launch(Dispatchers.IO) { WxManager.getInstance(this@HuaWeiRecoveryActivity).getWxMessage(filePath, fileDate, pwd, callback, false) }

    }

    private fun nextStep() {
        val intent = Intent()
        intent.setClass(this, ChooseAccountActivity::class.java)
        intent.putExtra("filePath", filePath)
        intent.putExtra("serviceId", serviceId)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onBackPressed() {
        super.onBackPressed()
        quit()
    }


    private fun quit() {
        AlertDialog.Builder(this)
            .setMessage("确认要退出吗？")
            .setPositiveButton("确定") { _, _ ->
                finish()
            }
            .setNegativeButton("取消") { _, _ ->

            }
            .show()
    }
}