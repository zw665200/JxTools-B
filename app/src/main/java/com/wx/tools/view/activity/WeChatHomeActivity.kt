package com.wx.tools.view.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.wx.tools.R
import com.wx.tools.utils.JLog
import com.wx.tools.view.base.BaseFragment
import com.wx.tools.view.base.BaseFragmentActivity
import com.wx.tools.view.fragment.FChat
import com.wx.tools.view.fragment.FContact

class WeChatHomeActivity : BaseFragmentActivity() {
    companion object {
        private const val FRAGMENT_HOME = 0
        private const val FRAGMENT_CONTACT = 1
        private const val DEFAULT_INDEX = FRAGMENT_HOME

        val BOTTOM_ICON_UNCHECKED = arrayOf(
            R.drawable.ic_global_friend_normal,
            R.drawable.ic_global_contacts_normel
        )

        val BOTTOM_ICON_CHECKED = arrayOf(
            R.drawable.ic_global_friend_select,
            R.drawable.ic_global_contacts_select
        )

        val BOTTOM_TEXT_ARRAY = arrayOf("聊天记录", "通讯录")
        const val BOTTOM_CHECKED_COLOR: Int = 0xff16aef9.toInt()
        const val BOTTOM_UNCHECKED_COLOR: Int = 0xffc0c0c0.toInt()

        val FRAGMENT_CLASS_ARRAY: Array<Class<out BaseFragment>> = arrayOf(
            FChat::class.java,
            FContact::class.java
        )

    }

    private var mCheckedFragmentID: Int = DEFAULT_INDEX
    private lateinit var back: ImageView
    private lateinit var title: TextView
    private var serviceId: Int = 0
    private lateinit var bottom: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.a_wechat_list)
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        title = findViewById(R.id.wechat_list_title)
        bottom = findViewById(R.id.ll_home_bottom)
        back.setOnClickListener { finish() }

        initData()
    }


    override fun setTabSel(item: View?, index: Int) {
//        super.setTabSel(bottomLayout.getChildAt(mCheckedFragmentID), mCheckedFragmentID)
        super.setTabSel(item, index)
    }

    override fun onItemClick(item: View?, index: Int) {
        mCheckedFragmentID = index
        when (index) {
            FRAGMENT_HOME -> title.text = "微信聊天记录"
            FRAGMENT_CONTACT -> title.text = "通讯录"
        }

    }

    override fun getBottomLayoutInflater(): LayoutInflater {
        return super.getBottomLayoutInflater()
    }

    override fun putFragments(): Array<Class<out BaseFragment>> {
        return FRAGMENT_CLASS_ARRAY
    }

    private val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f)
    override fun getBottomItemView(index: Int): View {
        val bottomView = bottomLayoutInflater.inflate(R.layout.l_home_bottom, null)
        val bottomLayout = bottomView.findViewById<LinearLayout>(R.id.home_page_bottom_layout)
        bottomLayout.layoutParams = params

        val bottomImage = bottomView.findViewById<ImageView>(R.id.home_page_bottom_image)
        bottomImage.setImageResource(BOTTOM_ICON_UNCHECKED[index])
        val buttonName = bottomView.findViewById<TextView>(R.id.home_page_bottom_btn_name)
        buttonName.text = BOTTOM_TEXT_ARRAY[index]
        return bottomView
    }

    override fun getFLid(): Int {
        return R.id.fl_home_body
    }

    override fun getBottomLayout(): LinearLayout {
        return this.findViewById(R.id.ll_home_bottom)
    }

    override fun checkAllBottomItem(item: View?, position: Int, isChecked: Boolean) {
        (item?.findViewById<ImageView>(R.id.home_page_bottom_image))?.setImageResource(if (isChecked) BOTTOM_ICON_CHECKED[position] else BOTTOM_ICON_UNCHECKED[position])
        (item?.findViewById<TextView>(R.id.home_page_bottom_btn_name))?.setTextColor(if (isChecked) BOTTOM_CHECKED_COLOR else BOTTOM_UNCHECKED_COLOR)
    }

    private fun changeFragment(index: Int) {
        setTabSel(bottomLayout.getChildAt(index), index)
    }


    override fun onResume() {
        super.onResume()
    }

    private fun initData() {
        serviceId = intent.getIntExtra("serviceId", 0)
        JLog.i("serviceID = $serviceId")
        when (serviceId) {
            1 -> {
                mCheckedFragmentID = FRAGMENT_HOME
                bottom.isEnabled = false
            }
            2 -> {
                mCheckedFragmentID = FRAGMENT_CONTACT
                bottom.isEnabled = false
            }
        }
    }

}