package com.wx.tools.view.activity

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatSpinner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tencent.mmkv.MMKV
import com.wx.tools.R
import com.wx.tools.adapter.DataAdapter
import com.wx.tools.bean.FileBean
import com.wx.tools.bean.FileStatus
import com.wx.tools.bean.FileWithType
import com.wx.tools.callback.DialogCallback
import com.wx.tools.callback.FileWithTypeCallback
import com.wx.tools.callback.VideoCallback
import com.wx.tools.controller.Constant
import com.wx.tools.controller.DBManager
import com.wx.tools.controller.PayManager
import com.wx.tools.controller.WxManager
import com.wx.tools.utils.AppUtil
import com.wx.tools.utils.JLog
import com.wx.tools.utils.ToastUtil
import com.wx.tools.view.base.BaseActivity
import com.wx.tools.view.views.ExportFileDialog
import com.wx.tools.view.views.QuitDialog
import kotlinx.android.synthetic.main.item_pic.view.*
import kotlinx.android.synthetic.main.item_video.view.*
import kotlinx.android.synthetic.main.item_voice.view.*
import kotlinx.coroutines.*
import java.io.File
import kotlin.concurrent.thread

class WeChatVideoRecoveryActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var recovery: Button
    private lateinit var picRv: RecyclerView
    private lateinit var from: AppCompatSpinner
    private lateinit var size: AppCompatSpinner
    private lateinit var time: AppCompatSpinner
    private lateinit var mAdapter: DataAdapter<FileWithType>
    private var mainVideos = mutableListOf<FileWithType>()
    private var sortVideos = mutableListOf<FileWithType>()
    private var checkedVideos = mutableListOf<FileWithType>()
    private lateinit var noData: ImageView
    private var prepared = false
    private var pay = false
    private var serviceId: Int = 0
    private lateinit var title: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var searchStatus: TextView
    private lateinit var progressBarLayout: LinearLayout
    private lateinit var sortView: LinearLayout
    private lateinit var searchPause: ImageView
    private lateinit var delete: Button
    private lateinit var option: ImageView
    private var isPause = false
    private lateinit var desc: TextView

    private var type = "default"
    private var minSize: Long = 0L
    private var maxSize: Long = 1024 * 1024 * 1024L
    private var minDate: Long = 0L
    private var maxDate = System.currentTimeMillis()
    private var initSpinnerSort = false
    private var initSpinnerSize = false
    private var initSpinnerTime = false
    private var chooseAll = false
    private var bitmapMap = HashMap<String, Bitmap>()

    override fun setLayout(): Int {
        return R.layout.a_wechat_video_recovery
    }


    override fun initView() {
        back = findViewById(R.id.iv_back)
        noData = findViewById(R.id.no_data)
        picRv = findViewById(R.id.rv_video)
        title = findViewById(R.id.wx_name)
        recovery = findViewById(R.id.recovery)
        progressBar = findViewById(R.id.progress)
        searchStatus = findViewById(R.id.search_status)
        progressBarLayout = findViewById(R.id.ll_progressbar)
        sortView = findViewById(R.id.ll_1)
        searchPause = findViewById(R.id.pause)
        delete = findViewById(R.id.delete)
        option = findViewById(R.id.option)
        desc = findViewById(R.id.progress_des)
        from = findViewById(R.id.spinner_from)
        size = findViewById(R.id.spinner_size)
        time = findViewById(R.id.spinner_time)

        back.setOnClickListener { onBackPressed() }
        option.setOnClickListener { checkOption() }
        searchPause.setOnClickListener { checkPause() }
        recovery.setOnClickListener { nextStep(this) }
        delete.setOnClickListener { deleteVideos() }

        title.text = getString(R.string.video_list_title)
        desc.text = getString(R.string.progress_des)
        loadVideos()
        spinnerListener()
    }

    override fun initData() {
        serviceId = intent.getIntExtra("serviceId", 0)
        searchVideos()
    }

    override fun onResume() {
        super.onResume()
        checkPay()
    }

    private fun searchVideos() {

        val callback = object : VideoCallback {
            override fun onSuccess(step: Enum<FileStatus>) {
                launch(Dispatchers.Main) {
                    prepared = true
                    if (!isPause) {
                        progressBar.progress = progressBar.max
                        searchStatus.text = getString(R.string.search_status_finish)
                        desc.text = getString(R.string.progress_des)
                        searchPause.setImageResource(R.drawable.play)
                        progressBarLayout.visibility = View.GONE
                        sortView.visibility = View.VISIBLE
                    }
                }
            }

            override fun onProgress(step: Enum<FileStatus>, index: Int) {
                if (!isPause) {
                    progressBar.progress = index
                }
            }

            override fun onProgress(step: Enum<FileStatus>, file: FileWithType) {
                launch(Dispatchers.Main) {
                    mainVideos.add(file)
                    sortVideos.add(file)

                    mAdapter.notifyItemInserted(mainVideos.size - 1)

                    title.apply {
                        val text = getString(R.string.video_list_title) + "(${sortVideos.size})"
                        this.text = text
                    }

                    //如果没有按下了暂停键，继续刷新界面
                    if (!isPause) {
                        if (progressBar.progress >= 9000) {
                            progressBar.progress = 9000
                        } else {
                            progressBar.progress = progressBar.progress + 1
                        }

                    }
                }
            }

            override fun onFailed(step: Enum<FileStatus>, message: String) {
                launch(Dispatchers.Main) {
                    prepared = true
                    searchStatus.text = getString(R.string.search_status_finish)
                    desc.text = getString(R.string.progress_des)
                    progressBar.progress = progressBar.max
                    noData.visibility = View.VISIBLE
                    searchPause.setImageResource(R.drawable.play)
                    ToastUtil.showShort(this@WeChatVideoRecoveryActivity, message)
                }
            }
        }

        thread {
            WxManager.getInstance(this@WeChatVideoRecoveryActivity).getWxVideos(callback)
        }
    }

    private fun spinnerListener() {
        from.setPopupBackgroundResource(R.color.color_white)
        size.setPopupBackgroundResource(R.color.color_white)
        time.setPopupBackgroundResource(R.color.color_white)

        from.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!initSpinnerSort) {
                    initSpinnerSort = true
                    return
                }

                when (position) {
                    0 -> {
                        type = "default"
                        sortAllSelectedList()
                    }

                    1 -> {
                        type = "date_desc"
                        sortAllSelectedList()
                    }

                    2 -> {
                        type = "date_asc"
                        sortAllSelectedList()
                    }

                    3 -> {
                        type = "size_desc"
                        sortAllSelectedList()
                    }

                    4 -> {
                        type = "size_asc"
                        sortAllSelectedList()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        size.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!initSpinnerSize) {
                    initSpinnerSize = true
                    return
                }
                when (position) {
                    0 -> {
                        minSize = 0
                        maxSize = 1024 * 1024 * 1024L
                        sortAllSelectedList()
                    }

                    1 -> {
                        minSize = 0
                        maxSize = 10 * 1024 * 1024L
                        sortAllSelectedList()
                    }

                    2 -> {
                        minSize = 10 * 1024 * 1024L
                        maxSize = 100 * 1024 * 1024L
                        sortAllSelectedList()
                    }

                    3 -> {
                        minSize = 100 * 1024 * 1024L
                        maxSize = 1024 * 1024 * 1024L
                        sortAllSelectedList()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        time.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!initSpinnerTime) {
                    initSpinnerTime = true
                    return
                }
                when (position) {
                    0 -> {
                        minDate = 0
                        maxDate = System.currentTimeMillis()
                        sortAllSelectedList()
                    }

                    1 -> {
                        minDate = System.currentTimeMillis() - 7 * 86400000L
                        maxDate = System.currentTimeMillis()
                        sortAllSelectedList()
                    }

                    2 -> {
                        minDate = System.currentTimeMillis() - 30 * 86400000L
                        maxDate = System.currentTimeMillis() - 7 * 86400000L
                        sortAllSelectedList()
                    }

                    3 -> {
                        minDate = 0
                        maxDate = System.currentTimeMillis() - 30 * 86400000L
                        sortAllSelectedList()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun sortAllSelectedList() {
        if (!prepared && sortVideos.isEmpty()) return
        launch(Dispatchers.IO) {
            sortVideos = DBManager.getVideoByKey(this@WeChatVideoRecoveryActivity, type, minSize, maxSize, minDate, maxDate)
            launch(Dispatchers.Main) {
                mainVideos.clear()
                if (sortVideos.size > 0) {
                    mainVideos.addAll(sortVideos)

                    val tText = getString(R.string.voice_list_title) + "(${sortVideos.size})"
                    title.text = tText
                } else {
                    title.text = getString(R.string.voice_list_title)
                }
                mAdapter.notifyDataSetChanged()
            }
        }
    }


    private fun loadVideos() {
        val height = AppUtil.getScreenHeight(this)
        mAdapter = DataAdapter.Builder<FileWithType>()
            .setData(mainVideos)
            .setLayoutId(R.layout.item_video)
            .addBindView { itemView, itemData, position ->
                val layoutParam = itemView.layoutParams
                layoutParam.height = height / 7
                itemView.layoutParams = layoutParam

                val date = AppUtil.timeStamp2Date(itemData.date.toString(), "yyyy/MM/dd HH:mm")
                val size = itemData.size / 1024
                if (size > 1024) {
                    val f = size.toFloat() / 1024
                    val d = String.format("%.1f", f) + " MB"
                    itemView.video_description.text = "$date $d"
                } else {
                    val f = "$size KB"
                    itemView.video_description.text = "$date $f"
                }

                getVideoBackground(itemData.path, itemView.video, position)

                //检查选中状态
                if (itemData.check) {
                    itemView.video_select.visibility = View.VISIBLE
                } else {
                    itemView.video_select.visibility = View.GONE
                }

                itemView.setOnLongClickListener {
                    toVideoDetailPage(itemData)
                    return@setOnLongClickListener true
                }

                itemView.setOnClickListener {
                    if (!itemData.check) {
                        itemView.video_select.visibility = View.VISIBLE
                        checkedVideos.add(itemData)
                        itemData.check = true
                        recovery.text = getString(R.string.pics_list_button) + "(选中${checkedVideos.size})"
                        delete.text = getString(R.string.list_delete_button) + "(选中${checkedVideos.size})"
                    } else {
                        itemView.video_select.visibility = View.GONE
                        checkedVideos.remove(itemData)
                        itemData.check = false
                        if (checkedVideos.isEmpty()) {
                            recovery.text = getString(R.string.pics_list_button)
                            delete.text = getString(R.string.list_delete_button)
                        } else {
                            recovery.text = getString(R.string.pics_list_button) + "(选中${checkedVideos.size})"
                            delete.text = getString(R.string.list_delete_button) + "(选中${checkedVideos.size})"
                        }
                    }
                }
            }
            .create()

        picRv.adapter = mAdapter
        picRv.layoutManager = GridLayoutManager(this, 2)
        mAdapter.notifyDataSetChanged()
    }

    /**
     * 读视频第一帧
     */
    private fun getVideoBackground(path: String, itemView: ImageView, position: Int) {

        if (bitmapMap[path] != null) {
            Glide.with(this@WeChatVideoRecoveryActivity).load(bitmapMap[path]).placeholder(R.drawable.default_page_bg_no_shipin_d).into(itemView)
            return
        }

        //防止itemview复用图片闪烁，先重置背景
        Glide.with(this@WeChatVideoRecoveryActivity).load(R.drawable.default_page_bg_no_shipin_d).into(itemView)

        launch(Dispatchers.IO) {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(this@WeChatVideoRecoveryActivity, Uri.fromFile(File(path)))
                val bitmap = retriever.getFrameAtTime(0)
                if (bitmap != null) {
                    launch(Dispatchers.Main) {
                        bitmapMap[path] = bitmap
                        Glide.with(this@WeChatVideoRecoveryActivity).load(bitmap).placeholder(R.drawable.default_page_bg_no_shipin_d).into(itemView)
                    }
                }

            } catch (ex: Exception) {
            }

        }
    }

    private fun checkPay() {
        PayManager.getInstance().checkRecoveryPay(this, serviceId) {
            pay = it
        }
    }

    private fun toPayPage() {
        val intent = Intent()
        intent.setClass(this, PayActivity::class.java)
        intent.putExtra("serviceId", serviceId)
        intent.putExtra("title", "微信视频恢复")
        startActivity(intent)
    }

    private fun toSinglePayPage() {
        val intent = Intent()
        intent.setClass(this, SinglePayActivity::class.java)
        intent.putExtra("serviceCode", Constant.DELETE)
        startActivity(intent)
    }

    private fun toVideoDetailPage(itemData: FileWithType) {
        if (pay) {
            val intent = Intent()
            intent.setClass(this, VideoDetailActivity::class.java)
            intent.putExtra("file", itemData)
            startActivityForResult(intent, 0x104)
        } else {
            toPayPage()
        }
    }

    private fun nextStep(context: Context) {
        if (checkedVideos.isEmpty()) {
            ToastUtil.show(this, "未选中视频")
            return
        }

        if (pay) {
            //pay success , do something
            ExportFileDialog(context, checkedVideos, "recovery_video").show()
        } else {
            toPayPage()
        }
    }

    private fun deleteVideos() {
        if (checkedVideos.isEmpty()) {
            ToastUtil.show(this, "未选中视频")
            return
        }

        PayManager.getInstance().checkDeletePay(this) {
            if (it) {
                launch(Dispatchers.IO) {
                    val deleteList = arrayListOf<FileWithType>()
                    deleteList.addAll(checkedVideos)
                    WxManager.getInstance(this@WeChatVideoRecoveryActivity).deleteFile(deleteList, object : FileWithTypeCallback {
                        override fun onSuccess(step: Enum<FileStatus>) {

                        }

                        override fun onProgress(step: Enum<FileStatus>, file: FileWithType) {
                            launch(Dispatchers.Main) {
                                notify(file)
                            }
                        }

                        override fun onFailed(step: Enum<FileStatus>, message: String) {

                        }
                    })
                }
            } else {
                toSinglePayPage()
            }
        }
    }

    private fun checkPause() {
        if (!isPause) {
            searchPause.setImageResource(R.drawable.play)
            isPause = true
        } else {
            searchPause.setImageResource(R.drawable.pause)
            isPause = false
            mAdapter.notifyDataSetChanged()
            title.text = getString(R.string.video_list_title) + "(${sortVideos.size})"

            if (prepared) {
                //初始化控件的状态
                isPause = false
                mainVideos.clear()
                sortVideos.clear()
                checkedVideos.clear()
                progressBar.progress = 0
                mAdapter.notifyDataSetChanged()
                searchVideos()
            }
        }
    }

    private fun checkOption() {
        if (!chooseAll) {
            chooseAll = true
            if (sortVideos.isNotEmpty()) {
                checkedVideos.clear()
                checkedVideos.addAll(sortVideos)
                recovery.text = getString(R.string.pics_list_button) + "(选中${checkedVideos.size})"
                delete.text = getString(R.string.list_delete_button) + "(选中${checkedVideos.size})"
                mainVideos.clear()
                for (child in sortVideos) {
                    mainVideos.add(FileWithType(child.name, child.path, child.size, child.date, child.type, true))
                }
                mAdapter.notifyDataSetChanged()
            }
        } else {
            chooseAll = false
            if (sortVideos.isNotEmpty()) {
                recovery.text = getString(R.string.pics_list_button)
                delete.text = getString(R.string.list_delete_button)
                checkedVideos.clear()
                mainVideos.clear()
                for (child in sortVideos) {
                    mainVideos.add(FileWithType(child.name, child.path, child.size, child.date, child.type, false))
                }
                mAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun notify(file: FileWithType) {
        if (Constant.stopScanVideo) return

        mAdapter.notifyItemRemoved(mainVideos.indexOf(file))
        mainVideos.remove(file)
        sortVideos.remove(file)
        checkedVideos.remove(file)
        if (checkedVideos.isEmpty()) {
            recovery.text = getString(R.string.pics_list_button)
            delete.text = getString(R.string.list_delete_button)
        } else {
            recovery.text = getString(R.string.pics_list_button) + "(选中${checkedVideos.size})"
            delete.text = getString(R.string.list_delete_button) + "(选中${checkedVideos.size})"
        }
    }

    override fun onBackPressed() {
        if (prepared) {
            finish()
        } else {
            QuitDialog(this, "正在扫描中，确定要退出吗？", object : DialogCallback {
                override fun onSuccess(file: FileBean) {
                    Constant.stopScanVideo = true
                    finish()
                }

                override fun onCancel() {
                }
            }).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0x104 && resultCode == 0x301) {
            //delete success
            if (data != null) {
                val file = data.getParcelableExtra<FileWithType>("file")
                if (file != null) {
                    notify(file)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Constant.stopScanVideo = true
        bitmapMap.clear()
        mainVideos.clear()
        sortVideos.clear()
        checkedVideos.clear()
    }

}