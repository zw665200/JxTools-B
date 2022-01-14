//package com.jxtools.wx.view.activity
//
//import android.content.Intent
//import android.view.View
//import android.widget.Button
//import android.widget.ImageView
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.jxtools.wx.R
//import com.jxtools.wx.adapter.DataAdapter
//import com.jxtools.wx.bean.Resource
//import com.jxtools.wx.controller.Constant
//import com.jxtools.wx.http.loader.ConfigLoader
//import com.jxtools.wx.http.response.ResponseTransformer
//import com.jxtools.wx.http.loader.PayStatusLoader
//import com.jxtools.wx.http.schedulers.SchedulerProvider
//import com.jxtools.wx.utils.AppUtil
//import com.jxtools.wx.utils.JLog
//import com.jxtools.wx.utils.ToastUtil
//import com.jxtools.wx.view.base.BaseActivity
//import com.tencent.imsdk.v2.V2TIMConversation
//import com.tencent.mmkv.MMKV
//import com.tencent.qcloud.tim.uikit.base.ITitleBarLayout
//import com.tencent.qcloud.tim.uikit.component.TitleBarLayout
//import com.tencent.qcloud.tim.uikit.modules.chat.ChatLayout
//import com.tencent.qcloud.tim.uikit.modules.chat.base.ChatInfo
//import kotlinx.android.synthetic.main.item_customer.view.*
//import kotlinx.coroutines.*
//
//class ChatActivity : BaseActivity() {
//    private lateinit var customer: RecyclerView
//    private lateinit var back: ImageView
//    private lateinit var charge: Button
//    private lateinit var chatLayout: ChatLayout
//    private lateinit var titleBarLayout: TitleBarLayout
//    private var payed = false
//
//    override fun setLayout(): Int {
//        return R.layout.a_customer_service
//    }
//
//    override fun initView() {
//        chatLayout = findViewById(R.id.chat_layout)
//        titleBarLayout = findViewById(R.id.start_c2c_chat_title)
//
//        titleBarLayout.setTitle("客服", ITitleBarLayout.POSITION.MIDDLE)
//    }
//
//    override fun initData() {
//        toChat()
//    }
//
//    private fun toChat() {
//        chatLayout.initDefault()
//        val chatInfo = ChatInfo()
//        chatInfo.chatName = "客服"
//        chatInfo.type = V2TIMConversation.V2TIM_C2C
//        chatInfo.id = "0"
//
//    }
//}