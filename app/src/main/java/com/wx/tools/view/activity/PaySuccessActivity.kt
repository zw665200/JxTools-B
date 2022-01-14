package com.wx.tools.view.activity

import android.content.Intent
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wx.tools.R
import com.wx.tools.adapter.DataAdapter
import com.wx.tools.bean.Resource
import com.wx.tools.view.base.BaseActivity
import kotlinx.android.synthetic.main.item_function.view.*

class PaySuccessActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var have: RecyclerView
    private lateinit var notHave: RecyclerView
    private lateinit var pay: Button
    private lateinit var cancel: Button
    private var serviceId = 0

    override fun setLayout(): Int {
        return R.layout.a_pay_success
    }


    override fun initView() {
        back = findViewById(R.id.iv_back)
        have = findViewById(R.id.function_have)
        notHave = findViewById(R.id.function_not_have)
        pay = findViewById(R.id.pay_btn)
        cancel = findViewById(R.id.cancel_btn)

        back.setOnClickListener { finish() }
        pay.setOnClickListener { toPay() }
        cancel.setOnClickListener { finish() }

    }

    override fun initData() {
        loadFunctionHave()
        loadFunctionNotHave()

        serviceId = intent.getIntExtra("serviceId", 0)

    }

    private fun loadFunctionHave() {
        val list = arrayListOf<Resource>()
        list.add(Resource("audio", R.drawable.ico_faction03, "微信视频"))
        list.add(Resource("doc", R.drawable.ico_faction04, "微信图片"))
        list.add(Resource("doc", R.drawable.ico_faction05, "微信语音"))
        list.add(Resource("doc", R.drawable.ico_faction06, "微信文档"))

        val mAdapter = DataAdapter.Builder<Resource>()
            .setData(list)
            .setLayoutId(R.layout.item_pay_success_function)
            .addBindView { itemView, itemData ->
                Glide.with(this).load(itemData.icon).into(itemView.function_icon)
                itemView.function_name.text = itemData.name
            }
            .create()

        have.layoutManager = GridLayoutManager(this, 4)
        have.adapter = mAdapter
        mAdapter.notifyDataSetChanged()
    }

    private fun loadFunctionNotHave() {
        val list = arrayListOf<Resource>()
        list.add(Resource("pic", R.drawable.ico_faction01, "微信聊天"))
        list.add(Resource("video", R.drawable.ico_faction02, "微信好友"))

        val mAdapter = DataAdapter.Builder<Resource>()
            .setData(list)
            .setLayoutId(R.layout.item_pay_success_function)
            .addBindView { itemView, itemData ->
                Glide.with(this).load(itemData.icon).into(itemView.function_icon)
                itemView.function_name.text = itemData.name
            }
            .create()

        notHave.layoutManager = GridLayoutManager(this, 2)
        notHave.adapter = mAdapter
        mAdapter.notifyDataSetChanged()
    }

    private fun toPay() {
        val intent = Intent(this, PayActivity::class.java)
        intent.putExtra("serviceId", serviceId)
        startActivity(intent)
        finish()
    }

}