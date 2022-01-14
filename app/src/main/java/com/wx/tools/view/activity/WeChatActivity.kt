package com.wx.tools.view.activity

import android.widget.AbsListView
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wx.tools.R
import com.wx.tools.adapter.MutableDataAdapter
import com.wx.tools.bean.Message
import com.wx.tools.controller.DBManager
import com.wx.tools.utils.AppUtil
import com.wx.tools.utils.JLog
import com.wx.tools.view.base.BaseActivity
import kotlinx.android.synthetic.main.item_chatlist_left.view.*
import kotlinx.android.synthetic.main.item_chatlist_right.view.*
import kotlinx.android.synthetic.main.item_wechat.view.*
import kotlinx.android.synthetic.main.rv_choose_account_item.view.*
import kotlinx.coroutines.*

class WeChatActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var talkerTitle: TextView
    private lateinit var mAdapter: MutableDataAdapter<Message>
    private var messageList = arrayListOf<Message>()
    private var pageIndex = 0
    private var pageSize = 15
    private var accountName = ""
    private var talkerName = ""
    private var lastItemPosition = 0

    override fun setLayout(): Int {
        return R.layout.a_wechat
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        back.setOnClickListener { finish() }

        talkerTitle = findViewById(R.id.talker_name)

        recyclerView = findViewById(R.id.rv_wx_chat_list)
        recyclerView.layoutManager = LinearLayoutManager(this)

        initRecyclerView()
    }

    override fun initData() {
        accountName = intent.getStringExtra("accountName").toString()
        talkerName = intent.getStringExtra("talkerId").toString()
        val name = intent.getStringExtra("talkerName")
        if (name != null) {
            talkerTitle.text = name
        }

        getNextPageData()
    }

    private fun getNextPageData() {
        launch(Dispatchers.IO) {
            val messages = DBManager.getMessages(this@WeChatActivity, accountName, talkerName, pageSize, pageIndex * pageSize)
            JLog.i("messages size = ${messages.size}")
            if (messages.isNotEmpty()) {
                launch(Dispatchers.Main) {
                    if (pageIndex == 0) {
                        messageList.clear()
                        messageList.addAll(messages)
                        mAdapter.notifyDataSetChanged()
                    } else {
                        messageList.addAll(messages)
                        mAdapter.notifyDataSetChanged()
                    }
                }
            } else {
                JLog.i("messageList is empty")
            }
        }
    }

    private fun initRecyclerView() {

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE &&
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

    /**
     * 加载会话列表
     */
    private fun initAdapter() {
        val screenWidth = AppUtil.getScreenWidth(this)
        mAdapter = MutableDataAdapter.Builder<Message>()
            .setData(messageList)
            .setLayoutId(R.layout.item_chatlist_left, R.layout.item_chatlist_right)
            .setViewType { position -> messageList[position].isSend }
            .addBindView { itemView, itemData ->
                if (itemData.isSend == 0) {
                    Glide.with(this).load(itemData.icon).error(R.drawable.ic_address_head_unknown_def).into(itemView.icon_left)
                    itemView.create_date_left.text = itemData.date
                    itemView.chat_list_content_left.maxWidth = screenWidth * 3 / 4

                    when (itemData.type) {
                        1 -> itemView.chat_list_content_left.text = itemData.content
                        3 -> itemView.chat_list_content_left.text = "[图片]"
                        34 -> itemView.chat_list_content_left.text = "[语音]"
                        43 -> itemView.chat_list_content_left.text = "[视频]"
                        47 -> itemView.chat_list_content_left.text = "[表情]"
                        48 -> itemView.chat_list_content_left.text = "[位置]"
                        49 -> itemView.chat_list_content_left.text = "[截图]"
                        50 -> itemView.chat_list_content_left.text = "[语音视频通话]"
                        1000 -> itemView.chat_list_content_left.text = "[已撤回的消息]"
                        else -> itemView.chat_list_content_left.text = "[分享]"
                    }
                } else {
                    Glide.with(this).load(itemData.icon).error(R.drawable.ic_address_head_unknown_def).into(itemView.icon_right)
                    itemView.create_date_right.text = itemData.date
                    itemView.chat_list_content_right.maxWidth = screenWidth * 3 / 4

                    when (itemData.type) {
                        1 -> itemView.chat_list_content_right.text = itemData.content
                        3 -> itemView.chat_list_content_right.text = "[图片]"
                        34 -> itemView.chat_list_content_right.text = "[语音]"
                        43 -> itemView.chat_list_content_right.text = "[视频]"
                        47 -> itemView.chat_list_content_right.text = "[表情]"
                        48 -> itemView.chat_list_content_right.text = "[位置]"
                        49 -> itemView.chat_list_content_right.text = "[截图]"
                        50 -> itemView.chat_list_content_right.text = "[语音视频通话]"
                        1000 -> itemView.chat_list_content_right.text = "[已撤回的消息]"
                        else -> itemView.chat_list_content_right.text = "[分享]"
                    }
                }
            }
            .create()

        recyclerView.adapter = mAdapter
    }
}