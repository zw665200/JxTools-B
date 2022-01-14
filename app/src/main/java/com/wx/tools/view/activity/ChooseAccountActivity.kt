package com.wx.tools.view.activity

import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wx.tools.R
import com.wx.tools.adapter.DataAdapter
import com.wx.tools.bean.Account
import com.wx.tools.controller.Constant
import com.wx.tools.controller.DBManager
import com.wx.tools.view.base.BaseActivity
import kotlinx.android.synthetic.main.rv_choose_account_item.view.*
import kotlinx.coroutines.*

class ChooseAccountActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var userRv: RecyclerView
    private var list = arrayListOf<Account>()
    private var serviceId: Int = 0
    private lateinit var mAdapter: DataAdapter<Account>
    private lateinit var title: TextView


    override fun setLayout(): Int {
        return R.layout.a_wechat_recovery_choose_account
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        userRv = findViewById(R.id.rv_user)
        title = findViewById(R.id.tv_choose_account_title)
        back.setOnClickListener { finish() }

        initAccountList()
    }

    override fun initData() {
        serviceId = intent.getIntExtra("serviceId", 0)
        when (serviceId) {
            1 -> title.text = getString(R.string.recovery_tips_title)
            2 -> title.text = getString(R.string.recovery_tips_title_1)
        }

        launch(Dispatchers.IO) {
            val accountList = DBManager.getAccountsBySrcTime(this@ChooseAccountActivity, Constant.CURRENT_BACKUP_TIME)
            if (accountList.isNullOrEmpty()) {
                return@launch
            }

            list.clear()
            list.addAll(accountList)
            mAdapter.notifyDataSetChanged()
        }
    }

    private fun initAccountList() {
        mAdapter = DataAdapter.Builder<Account>()
            .setData(list)
            .setLayoutId(R.layout.rv_choose_account_item)
            .addBindView { itemView, itemData ->
                itemView.tv_item_nickname.text = itemData.nickName ?: "未命名"
                itemView.tv_item_phone.text = "手机号：${itemData.phone}"
                itemView.tv_item_wx_id.text = "微信号：${itemData.alias ?: itemData.userName}"
                itemView.tv_item_region.text = "地区：${itemData.region}"
                Glide.with(this).load(itemData.icon).into(itemView.user_icon)
                itemView.setOnClickListener {
                    toWechatListPage(itemData.userName)
                }
            }
            .create()

        userRv.layoutManager = LinearLayoutManager(this)
        userRv.adapter = mAdapter
        mAdapter.notifyDataSetChanged()
    }

    private fun toWechatListPage(userName: String) {
        launch(Dispatchers.Main) {
            intent.setClass(this@ChooseAccountActivity, WeChatHomeActivity::class.java)
            intent.putExtra("account", userName)
            intent.putExtra("serviceId", serviceId)
            startActivity(intent)
        }
    }

}