package com.wx.tools.view.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.wx.tools.R
import com.wx.tools.controller.Constant
import com.wx.tools.utils.JLog
import com.wx.tools.view.base.BaseFragment
import com.wx.tools.view.base.BaseFragmentActivity
import com.wx.tools.view.fragment.FHome
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory

class MainActivity : BaseFragmentActivity(), View.OnClickListener {

    companion object {
        private const val FRAGMENT_HOME = 0

        //        private const val FRAGMENT_BOX = 1
//        private const val FRAGMENT_BILL = 2
        private const val DEFAULT_INDEX = FRAGMENT_HOME

        val BOTTOM_ICON_UNCHECKED = arrayOf(
            R.drawable.ic_global_contacts_select
        )

        val BOTTOM_ICON_CHECKED = arrayOf(
            R.drawable.ic_global_contacts_select
        )
        val BOTTOM_TEXT_ARRAY = arrayOf("首页", "价格", "账单", "我的")
        const val BOTTOM_CHECKED_COLOR: Int = 0xff1ece8d.toInt()
        const val BOTTOM_UNCHECKED_COLOR: Int = 0xffc0c0c0.toInt()

        val FRAGMENT_CLASS_ARRAY: Array<Class<out BaseFragment>> = arrayOf(
            FHome::class.java
        )

    }

    private var mCheckedFragmentID: Int = DEFAULT_INDEX


    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.a_home)
        super.onCreate(savedInstanceState)
        regToWx()
    }

    override fun initView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.color_blue)
        }
    }


    override fun onItemClick(item: View?, index: Int) {
        mCheckedFragmentID = index
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

    override fun getBottomLayout(): LinearLayout? {
        return this@MainActivity.findViewById(R.id.ll_home_bottom)
    }

    override fun checkAllBottomItem(item: View?, position: Int, isChecked: Boolean) {
        (item?.findViewById<ImageView>(R.id.home_page_bottom_image))?.setImageResource(if (isChecked) BOTTOM_ICON_CHECKED[position] else BOTTOM_ICON_UNCHECKED[position])
        (item?.findViewById<TextView>(R.id.home_page_bottom_btn_name))?.setTextColor(if (isChecked) BOTTOM_CHECKED_COLOR else BOTTOM_UNCHECKED_COLOR)
    }

    override fun setTabSel(item: View?, index: Int) {
        super.setTabSel(item, index)

    }

    private fun changeFragment(index: Int) {
        setTabSel(bottomLayout?.getChildAt(index), index)
    }


    private var mLastClick: Long = 0L
    override fun onBackPressed() {
        if (System.currentTimeMillis() - mLastClick < 2000) {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                addCategory(Intent.CATEGORY_HOME)
            }
            startActivity(homeIntent)
        } else {
            Toast.makeText(this@MainActivity, "再按一下后退键退出程序", Toast.LENGTH_SHORT).show()
            mLastClick = System.currentTimeMillis()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.hook -> {

            }
        }
    }

    private fun regToWx() {
        val api = WXAPIFactory.createWXAPI(this, Constant.TENCENT_APP_ID, true)
        api.registerApp(Constant.TENCENT_APP_ID)

        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                api.registerApp(Constant.TENCENT_APP_ID)
            }
        }, IntentFilter(ConstantsAPI.ACTION_REFRESH_WXAPP))
    }

    private fun foo2() {
        val widthLeft = 3.333
        val intervalWidth = 1.429
        val width = 14.285
        val height = 3.367
        val heightTop = 2.357
        val intervalHeight = 1.01


        //横向距离
        JLog.i("画竖向参考线：$widthLeft%")
        for (index in 1..6) {
            val w1 = widthLeft + width * (index) + intervalWidth * (index - 1)
            val w2 = widthLeft + width * (index) + intervalWidth * (index)
            JLog.i("画竖向参考线：$w1%")
            JLog.i("画竖向参考线：$w2%")
        }


        //竖向距离
        JLog.i("画竖向参考线：$heightTop%")
        for (index in 1..22) {
            val h1 = heightTop + height * (index) + intervalHeight * (index - 1)
            val h2 = heightTop + height * (index) + intervalHeight * (index)
            JLog.i("画横向参考线：$h1%")
            JLog.i("画横向参考线：$h2%")
        }

    }
}