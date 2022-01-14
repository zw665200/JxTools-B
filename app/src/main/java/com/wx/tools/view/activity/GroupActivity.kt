package com.wx.tools.view.activity

import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wx.tools.R
import com.wx.tools.adapter.DataAdapter
import com.wx.tools.bean.Contact
import com.wx.tools.controller.Constant
import com.wx.tools.controller.DBManager
import com.wx.tools.view.base.BaseActivity
import kotlinx.android.synthetic.main.item_contact.view.*
import kotlinx.coroutines.*

class GroupActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var userRv: RecyclerView
    private lateinit var groupName: TextView
    private var list = arrayListOf<Contact>()
    private var isPayed = true
    private var name: String? = null
    private var accountName: String? = null
    private var type: Int = -1
    private lateinit var mAdapter: DataAdapter<Contact>

    override fun setLayout(): Int {
        return R.layout.a_wechat_group
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        userRv = findViewById(R.id.rv_group)
        groupName = findViewById(R.id.group_name)
        back.setOnClickListener { finish() }
        initAccountList()
    }

    override fun initData() {
        accountName = intent.getStringExtra("accountName")
        name = intent.getStringExtra("name")
        type = intent.getIntExtra("type", -1)

        if (accountName == null || name == null) return

        groupName.text = name!!

        launch(Dispatchers.IO) {
            when (type) {
                -1 -> {
                    val accountList = DBManager.getGroupContacts(this@GroupActivity, accountName!!, Constant.CURRENT_BACKUP_TIME)
                    if (accountList.isNotEmpty()) {
                        launch(Dispatchers.Main) {
                            list.clear()
                            list.addAll(accountList)
                            mAdapter.notifyDataSetChanged()
                        }
                    }
                }

                -2 -> {
                    val accountList = DBManager.getGhContacts(this@GroupActivity, accountName!!, Constant.CURRENT_BACKUP_TIME)
                    if (accountList.isNotEmpty()) {
                        launch(Dispatchers.Main) {
                            list.clear()
                            list.addAll(accountList)
                            mAdapter.notifyDataSetChanged()
                        }
                    }
                }

                -3 -> {
                    val accountList = DBManager.getNotFriendInGroupContacts(this@GroupActivity, accountName!!, Constant.CURRENT_BACKUP_TIME)
                    if (accountList.isNotEmpty()) {
                        launch(Dispatchers.Main) {
                            list.clear()
                            list.addAll(accountList)
                            mAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
        }
    }

    private fun initAccountList() {
        mAdapter = DataAdapter.Builder<Contact>()
            .setData(list)
            .setLayoutId(R.layout.item_contact)
            .addBindView { itemView, itemData ->
                itemView.tv_item_nickname.text = itemData.nickName ?: "未知群"

                val name = "微信号：" + if (itemData.alias.isNullOrEmpty()) itemData.userName else itemData.alias
                itemView.tv_item_wxId.text = name


                if (itemData.icon.isNullOrEmpty()) {
                    Glide.with(this).load(R.drawable.ic_address_head_unknown_def).into(itemView.user_icon)
                } else {
//                    Glide.with(this).load(itemData.icon).error(R.drawable.ic_address_head_unknown_def).into(itemView.user_icon)
                    Glide.with(this).load(itemData.icon).into(itemView.user_icon)
                }
                itemView.setOnClickListener {

                }
            }
            .create()

        userRv.layoutManager = LinearLayoutManager(this)
        userRv.adapter = mAdapter
        mAdapter.notifyDataSetChanged()
    }

}