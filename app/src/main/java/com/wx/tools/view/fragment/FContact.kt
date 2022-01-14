package com.wx.tools.view.fragment

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wx.tools.R
import com.wx.tools.adapter.DataAdapter
import com.wx.tools.bean.Contact
import com.wx.tools.controller.Constant
import com.wx.tools.controller.DBManager
import com.wx.tools.utils.JLog
import com.wx.tools.view.activity.GroupActivity
import com.wx.tools.view.activity.WeChatActivity
import com.wx.tools.view.base.BaseFragment
import kotlinx.android.synthetic.main.item_wechat.view.*
import kotlinx.android.synthetic.main.item_wechat.view.user_icon
import kotlinx.android.synthetic.main.rv_choose_account_item.view.tv_item_nickname
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FContact : BaseFragment() {
    private lateinit var rv: RecyclerView
    private var contactList = arrayListOf<Contact>()
    private lateinit var tips: LinearLayout
    private lateinit var deleteTips: ImageView

    override fun initView(inflater: LayoutInflater): View {
        val rootView = inflater.inflate(R.layout.f_contact, null)
        rv = rootView.findViewById(R.id.rv_wx_chat_list)
        tips = rootView.findViewById(R.id.ll_tips)
        deleteTips = rootView.findViewById(R.id.delete_tips)
        rv.layoutManager = LinearLayoutManager(activity)
        deleteTips.setOnClickListener { tips.visibility = View.GONE }
        return rootView
    }

    override fun initData() {
        val accountName = activity!!.intent.getStringExtra("account")
        if (accountName != null) {
            launch(Dispatchers.IO) {
                val list = DBManager.getContacts(activity!!, accountName, Constant.CURRENT_BACKUP_TIME)
                if (list.isNotEmpty()) {
                    launch(Dispatchers.Main) {
                        val contact = list[0]
                        contactList.clear()
                        contactList.add(Contact(contact.id, "群组", "群组", "Group", "", "群组", -1))
                        contactList.add(Contact(contact.id, "公众号", "公众号", "Official Account", "", "公众号", -2))
                        contactList.add(Contact(contact.id, "群聊好友", "群聊好友", "Group Friends", "", "公众号", -3))
                        contactList.addAll(list)
                        loadContactList(contactList, accountName)
                    }
                } else {
                    JLog.i("talkerList is empty")
                }
            }
        }
    }

    override fun click(v: View?) {
    }

    private fun loadContactList(contactList: ArrayList<Contact>, accountName: String) {
        val adapter = DataAdapter.Builder<Contact>()
            .setData(contactList)
            .setLayoutId(R.layout.item_contact)
            .addBindView { itemView, itemData ->
                JLog.i("username = ${itemData.userName}")

                when (itemData.type) {
                    -1 -> {
                        itemView.tv_item_nickname.text = itemData.nickName
                        itemView.tv_item_wxId.text = itemData.alias
                        Glide.with(this).load(R.drawable.ic_address_icon_group).into(itemView.user_icon)
                        itemView.tv_item_nickname.setTextColor(getColor(activity!!, R.color.color_black))
                    }

                    -2 -> {
                        itemView.tv_item_nickname.text = itemData.nickName
                        itemView.tv_item_wxId.text = itemData.alias
                        Glide.with(this).load(R.drawable.ic_address_icon_official_accounts).into(itemView.user_icon)
                        itemView.tv_item_nickname.setTextColor(getColor(activity!!, R.color.color_black))
                    }

                    -3 -> {
                        itemView.tv_item_nickname.text = itemData.nickName
                        itemView.tv_item_wxId.text = itemData.alias
                        Glide.with(this).load(R.drawable.ic_address_icon_group).into(itemView.user_icon)
                        itemView.tv_item_nickname.setTextColor(getColor(activity!!, R.color.color_black))
                    }

                    else -> {
                        itemView.tv_item_nickname.text = if (!itemData.conRemark.isNullOrEmpty()) "${itemData.conRemark}" else itemData.nickName
                        itemView.tv_item_wxId.text = if (itemData.alias.isNullOrEmpty()) "微信号：${itemData.userName}" else "微信号：${itemData.alias}"
                        if (itemData.icon.isNullOrEmpty()) {
                            Glide.with(this).load(R.drawable.ic_address_head_unknown_def).into(itemView.user_icon)
                        } else {
                            Glide.with(this).load(itemData.icon).error(R.drawable.ic_address_head_unknown_def)
                                .fallback(R.drawable.ic_address_head_unknown_def).into(itemView.user_icon)
                        }

                        if (itemData.type == 0) {
                            //偶数
                            itemView.tv_item_nickname.setTextColor(getColor(activity!!, R.color.color_yellow))
                        } else {
                            //偶数都是删除的好友
                            val n = itemData.type % 2
                            if (n == 0) {
                                //偶数
                                itemView.tv_item_nickname.setTextColor(getColor(activity!!, R.color.color_yellow))
                                itemView.tv_item_wxId.setTextColor(getColor(activity!!, R.color.color_yellow))
                            } else {
                                //奇数
                                itemView.tv_item_nickname.setTextColor(getColor(activity!!, R.color.color_black))
                                itemView.tv_item_wxId.setTextColor(getColor(activity!!, R.color.color_black))
                            }
                        }

                    }
                }

                itemView.setOnClickListener {
                    when (itemData.type) {
                        -1 -> {
                            val intent = Intent()
                            intent.setClass(activity!!, GroupActivity::class.java)
                            intent.putExtra("accountName", accountName)
                            intent.putExtra("type", -1)
                            intent.putExtra("name", itemData.nickName)
                            startActivity(intent)
                        }
                        -2 -> {
                            val intent = Intent()
                            intent.setClass(activity!!, GroupActivity::class.java)
                            intent.putExtra("accountName", accountName)
                            intent.putExtra("type", -2)
                            intent.putExtra("name", itemData.nickName)
                            startActivity(intent)
                        }
                        -3 -> {
                            val intent = Intent()
                            intent.setClass(activity!!, GroupActivity::class.java)
                            intent.putExtra("accountName", accountName)
                            intent.putExtra("type", -3)
                            intent.putExtra("name", itemData.nickName)
                            startActivity(intent)
                        }
                        else -> {
                            val intent = Intent()
                            intent.setClass(activity!!, WeChatActivity::class.java)
                            intent.putExtra("accountName", accountName)
                            intent.putExtra("talkerId", itemData.userName)
                            intent.putExtra("talkerName", itemData.nickName)
                            startActivity(intent)
                        }
                    }

                }
            }
            .create()
        rv.adapter = adapter
        adapter.notifyDataSetChanged()
    }
}