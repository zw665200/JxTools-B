package com.wx.tools.view.views

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wx.tools.R
import com.wx.tools.adapter.DataAdapter
import com.wx.tools.bean.FileBean
import com.wx.tools.callback.DialogCallback
import com.wx.tools.utils.AppUtil
import kotlinx.android.synthetic.main.d_backup_files_item.view.*


class BackupFilesDialog(context: Context, callback: DialogCallback) : Dialog(context, R.style.app_dialog) {
    private val mContext: Context = context
    private var mCallback: DialogCallback? = callback
    private lateinit var recyclerView: RecyclerView
    private lateinit var cancel: TextView
    private lateinit var mAdapter: DataAdapter<FileBean>
    private var mList = arrayListOf<FileBean>()


    init {
        initVew(context)
    }

    private fun initVew(context: Context) {
        val dialogContent = LayoutInflater.from(mContext).inflate(R.layout.d_backup_files, null)
        setContentView(dialogContent)
        setCancelable(true)

        recyclerView = dialogContent.findViewById(R.id.rv_backup)
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager

        cancel = dialogContent.findViewById(R.id.dialog_cancel)
        cancel.setOnClickListener { cancel() }

        mAdapter = DataAdapter.Builder<FileBean>()
            .setData(mList)
            .setLayoutId(R.layout.d_backup_files_item)
            .addBindView { itemView, itemData ->
                itemView.tv_item_file_name.text = if (itemData.name.isNotEmpty()) itemData.name else "未命名"
                itemView.tv_item_file_size.text = if (itemData.size.isNotEmpty()) "${itemData.size} MB" else "未知大小"
                itemView.setOnClickListener {
                    if (mCallback != null) {
                        mCallback!!.onSuccess(itemData)
                    }
                }
            }
            .create()
        recyclerView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

    }

    fun setData(list: MutableList<FileBean>) {
        if (list.isNotEmpty()) {
            mList.clear()
            mList.addAll(list)
            mAdapter.notifyDataSetChanged()
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
    }

    private fun goBackup() {
        val intent = Intent(Settings.ACTION_PRIVACY_SETTINGS)
        mContext.startActivity(intent)
//        if (filePath != null) {
//            GlobalScope.launch {
////                val payed = WxManager.getInstance(this@WeChatRecoveryActivity).checkPay(this@WeChatRecoveryActivity, filePath!!)
//                val payed = true
//                GlobalScope.launch(Dispatchers.Main) {
//                    if (payed) {
//                        dialog.show()
//                    } else {
//                        initStep()
//                        getMessage(filePath!!)
//                    }
//                }
//            }
//        }
    }


}