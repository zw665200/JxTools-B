package com.wx.tools.view.fragment

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wx.tools.R
import com.wx.tools.adapter.DataAdapter
import com.wx.tools.bean.Talker
import com.wx.tools.controller.Constant
import com.wx.tools.controller.DBManager
import com.wx.tools.utils.JLog
import com.wx.tools.view.activity.WeChatActivity
import com.wx.tools.view.base.BaseFragment
import kotlinx.android.synthetic.main.item_wechat.view.*
import kotlinx.android.synthetic.main.rv_choose_account_item.view.tv_item_nickname
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FChat : BaseFragment() {
    private lateinit var rv: RecyclerView
    private lateinit var mAdapter: DataAdapter<Talker>
    private var pageIndex = 0
    private var pageSize = 15
    private var accountName = ""
    private var lastItemPosition = 0
    private var talkerList = arrayListOf<Talker>()

    override fun initView(inflater: LayoutInflater): View {
        val rootView = inflater.inflate(R.layout.f_chat, null)
        rv = rootView.findViewById(R.id.rv_wx_chat_list)
        rv.layoutManager = LinearLayoutManager(activity)

        initRecyclerView()
        return rootView
    }

    override fun initData() {
        accountName = activity!!.intent.getStringExtra("account").toString()
        JLog.i("accountName = $accountName")
        getNextPageData()
    }

    override fun click(v: View?) {
    }

    private fun initRecyclerView() {
        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == SCROLL_STATE_IDLE &&
                    lastItemPosition == mAdapter.itemCount
                ) {
                    pageIndex++
                    getNextPageData()

                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager
                if (layoutManager is LinearLayoutManager) {
                    val firstVisionItem = layoutManager.findFirstVisibleItemPosition()
                    val lastCompleteItem = layoutManager.findLastCompletelyVisibleItemPosition()
                    lastItemPosition = firstVisionItem + (lastCompleteItem - firstVisionItem) + 1
                }
            }
        })

        initAdapter()
    }

    private fun getNextPageData() {
        launch(Dispatchers.IO) {
            val talker = DBManager.getTalkers(activity!!, accountName, Constant.CURRENT_BACKUP_TIME, pageIndex * pageSize)
            if (talker.isNotEmpty()) {
                launch(Dispatchers.Main) {
                    if (pageIndex == 0) {
                        talkerList.clear()
                        talkerList.addAll(talker)
                        mAdapter.notifyDataSetChanged()
                    } else {
                        talkerList.addAll(talker)
                        mAdapter.notifyDataSetChanged()
                    }
                }
            } else {
                JLog.i("talkerList is empty")
            }
        }
    }

    private fun initAdapter() {
        mAdapter = DataAdapter.Builder<Talker>()
            .setData(talkerList)
            .setLayoutId(R.layout.item_wechat)
            .addBindView { itemView, itemData ->
                if (!itemData.conRemark.isNullOrEmpty()) {
                    itemView.tv_item_nickname.text = itemData.conRemark
                } else if (!itemData.nickName.isNullOrEmpty()) {
                    itemView.tv_item_nickname.text = itemData.nickName
                } else {
                    itemView.tv_item_nickname.text = itemData.userName
                }

                if (itemData.conversation != null) {
                    itemView.tv_item_wxId.text = itemData.conversation!!.content
                } else {
                    itemView.tv_item_wxId.text = "[其它消息]"
                }

                if (itemData.type == -1) {
                    itemView.tv_item_nickname.setTextColor(ContextCompat.getColor(activity!!, R.color.color_yellow))
                    itemView.tv_item_wxId.setTextColor(ContextCompat.getColor(activity!!, R.color.color_yellow))
                } else {
                    itemView.tv_item_nickname.setTextColor(ContextCompat.getColor(activity!!, R.color.color_black))
                    itemView.tv_item_wxId.setTextColor(ContextCompat.getColor(activity!!, R.color.color_black))
                }

                if (itemData.icon.isNullOrEmpty()) {
                    Glide.with(this).load(R.drawable.ic_address_head_unknown_def).into(itemView.user_icon)
                } else {
                    Glide.with(this).load(itemData.icon).error(R.drawable.ic_address_head_unknown_def).into(itemView.user_icon)
                }

                itemView.setOnClickListener {
                    val intent = Intent()
                    intent.setClass(activity!!, WeChatActivity::class.java)
                    intent.putExtra("accountName", accountName)
                    intent.putExtra("talkerId", itemData.userName)
                    intent.putExtra("talkerName", if (!itemData.conRemark.isNullOrEmpty()) itemData.conRemark else itemData.nickName)
                    startActivity(intent)
                }
            }

            .create()
        rv.adapter = mAdapter
        mAdapter.notifyDataSetChanged()
    }
}