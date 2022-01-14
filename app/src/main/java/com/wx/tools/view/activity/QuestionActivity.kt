package com.wx.tools.view.activity

import android.view.View
import android.widget.Adapter
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tencent.smtt.sdk.QbSdk
import com.wx.tools.R
import com.wx.tools.adapter.MutableDataAdapter
import com.wx.tools.bean.FileWithType
import com.wx.tools.bean.Message
import com.wx.tools.bean.Resource
import com.wx.tools.view.base.BaseActivity
import kotlinx.android.synthetic.main.item_question_content.view.*
import kotlinx.android.synthetic.main.item_question_title.view.*
import kotlinx.android.synthetic.main.item_question_title.view.question_title

class QuestionActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var recyclerView: RecyclerView
    private val mainList = arrayListOf<Resource>()
    private lateinit var adapter: MutableDataAdapter<Resource>

    override fun setLayout(): Int {
        return R.layout.a_question
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        recyclerView = findViewById(R.id.question_list)
        recyclerView.layoutManager = LinearLayoutManager(this)

        back.setOnClickListener { finish() }
    }

    override fun initData() {
        mainList.add(Resource("", 0, "新手教程"))
        mainList.add(Resource("微信聊天记录怎么恢复？", 1, "点击首页的【微信聊天恢复】，根据教程提示操作（部分机型需要配合电脑端的官方备份助手）"))
        mainList.add(Resource("如何找回删掉的微信好友？", 1, "点击首页的【微信好友恢复】，根据教程提示操作（部分机型需要配合电脑的官方备份助手）"))
        mainList.add(Resource("手机照片/微信图片怎么恢复？", 1, "点击首页的【微信图片恢复】，软件会对微信的目录深层扫描（提示：手机内的文件碎片越多，扫描的时间就会越长，请耐心等待）"))
        mainList.add(Resource("微信语音删除了怎么恢复？", 1, "点击首页的【微信语音恢复】，等待软件扫描微信数据碎片，待分析完成之后，便会跳转到恢复结果页面。然后可以看到恢复的语音文件。"))
        mainList.add(Resource("微信聊天记录和通讯录怎么恢复不到微信里面？", 1, "本软件是对微信备份文件进行数据分析的，和微信本身也没有任何的交互。恢复的聊天记录只能查看，恢复的好友需要通过恢复的微信号去微信上添加"))
        mainList.add(Resource("", 0, "常见问题"))
        mainList.add(Resource("几个月前删除的信息还能恢复吗？", 1, "本软件能恢复出来多少的聊天记录和文件，和用户删除之后的具体操作有很大关系。新的数据会不断覆盖掉之前被删除的数据。时间越久或者数据写入频繁，恢复的几率越低。"))
        mainList.add(Resource("没有微信密码怎么找回聊天记录？", 1, "软件扫描数据，和是否登录微信账户或者是否打开微信没有关系。软件是通过扫描备份文件中的数据碎片来查找记录的。"))
        mainList.add(Resource("微信卸载有机会恢复什么？", 1, "微信卸载之后，文字记录恢复几率相对很低，有机会找到卸载之前的图片、语音、视频，文档等文件，具体可以体验首页各个功能。"))
        mainList.add(Resource("没有微信密码怎么找回聊天记录？", 1, "软件扫描数据，和是否登录微信账户或者是否打开微信没有关系。软件是通过扫描备份文件中的数据碎片来查找记录的。"))
        mainList.add(Resource("手机丢失以后，能找到丢失手机上的记录吗？", 1, "手机丢失之后，是无法找到丢失手机上的记录的。"))
        mainList.add(Resource("人工客服问题？", 1, "人工客服需要购买vip套餐后才能使用，我们的人工客服在线时间是10：00-22：00"))

        initRecyclerView()
    }

    private fun initRecyclerView() {
        adapter = MutableDataAdapter.Builder<Resource>()
            .setData(mainList)
            .setLayoutId(R.layout.item_question_title, R.layout.item_question_content)
            .setViewType { position -> mainList[position].icon }
            .addBindView { itemView, itemData ->
                when (itemData.icon) {
                    0 -> {
                        itemView.question_title.text = itemData.name
                    }

                    1 -> {
                        itemView.question_content_title.text = itemData.type
                        itemView.question_content.text = itemData.name
                        itemView.question_content_title.setOnClickListener {
                            when (itemView.question_content.visibility) {
                                View.VISIBLE -> itemView.question_content.visibility = View.GONE
                                View.GONE -> itemView.question_content.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }.create()

        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}