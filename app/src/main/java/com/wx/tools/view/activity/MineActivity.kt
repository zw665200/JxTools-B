package com.wx.tools.view.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tencent.mmkv.MMKV
import com.wx.tools.R
import com.wx.tools.adapter.DataAdapter
import com.wx.tools.bean.Resource
import com.wx.tools.controller.Constant
import com.wx.tools.controller.DBManager
import com.wx.tools.utils.AppUtil
import com.wx.tools.utils.Dict
import com.wx.tools.utils.FileUtil
import com.wx.tools.utils.ToastUtil
import com.wx.tools.view.base.BaseActivity
import kotlinx.android.synthetic.main.item_function.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MineActivity : BaseActivity() {
    private lateinit var title: TextView
    private lateinit var level: TextView
    private lateinit var phone: TextView
    private lateinit var back: ImageView
    private lateinit var customer: RecyclerView
    private var pay = false

    override fun setLayout(): Int {
        return R.layout.a_mine
    }


    override fun initView() {
        back = findViewById(R.id.iv_back)
        title = findViewById(R.id.tv_mine_nick)
        customer = findViewById(R.id.function)
        level = findViewById(R.id.tv_mine_vip)
        phone = findViewById(R.id.tv_mine_phone)

        back.setOnClickListener { finish() }

    }

    override fun initData() {
        val name = Constant.USER_NAME
        title.text = name
        loadFunction()
        loadUserInfo()
        loadDeviceInfo()
    }

    private fun loadFunction() {
        val list = arrayListOf<Resource>()
        list.add(Resource("website", R.drawable.mine_website, getString(R.string.mine_website)))
        list.add(Resource("service", R.drawable.mine_help, getString(R.string.mine_service)))
        list.add(Resource("privacy", R.drawable.mine_privacy, getString(R.string.mine_privacy)))
//        list.add(Resource("feedback", R.drawable.mine_feedback, getString(R.string.mine_feedback)))
        list.add(Resource("clear", R.drawable.clear_cache, getString(R.string.setting_clear_cache)))
        list.add(Resource("about", R.drawable.about_us, getString(R.string.setting_about_us)))

        val mAdapter = DataAdapter.Builder<Resource>()
            .setData(list)
            .setLayoutId(R.layout.item_function)
            .addBindView { itemView, itemData, position ->
                Glide.with(this).load(itemData.icon).into(itemView.function_icon)
                itemView.function_name.text = itemData.name

                itemView.setOnClickListener {
                    when (position) {
                        0 -> openWebsite()
                        1 -> openUserAgreement()
                        2 -> openPrivacyAgreement()
//                        3 -> openFeedback()
                        3 -> clearCache()
                        4 -> aboutUs()
                    }
                }
            }
            .create()

        customer.layoutManager = LinearLayoutManager(this)
        customer.adapter = mAdapter
        mAdapter.notifyDataSetChanged()
    }

    private fun loadUserInfo() {
        val mmkv = MMKV.defaultMMKV()
        when (mmkv?.decodeInt("recovery")) {
            110 -> {
                level.text = "等级：高级VIP会员"
                pay = true
            }

            111 -> {
                level.text = "等级：VIP会员"
                pay = true
            }

            else -> {
                level.text = "等级：普通用户"
                pay = false
            }
        }
    }

    private fun loadDeviceInfo() {
        if (Build.BRAND == "HUAWEI" || Build.BRAND == "HONOR") {
            val name = Dict.getHUAWEIName(Build.MODEL)
            if (name.isNullOrEmpty()) {
                val b = "手机型号: ${Build.BRAND} ${Build.MODEL}"
                phone.text = b
            } else {
                val b = "手机型号: $name"
                phone.text = b
            }
        } else {
            val b = "手机型号: ${Build.BRAND} ${Build.MODEL}"
            phone.text = b
        }
    }


    private fun openWebsite() {
        if (Constant.WEBSITE == "") return
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Constant.WEBSITE))
        startActivity(intent)
    }

    private fun openHelpCenter() {
    }

    private fun openFeedback() {
        val intent = Intent(this, FeedbackActivity::class.java)
        startActivity(intent)
    }

    private fun openUserAgreement() {
        val intent = Intent(this, AgreementActivity::class.java)
        intent.putExtra("index", 0)
        startActivity(intent)
    }

    private fun openPrivacyAgreement() {
        val intent = Intent(this, AgreementActivity::class.java)
        intent.putExtra("index", 1)
        startActivity(intent)
    }

    private fun openSetting() {
        val intent = Intent(this, SettingActivity::class.java)
        startActivity(intent)
    }

    private fun clearCache() {
        launch(Dispatchers.IO) {
            DBManager.deleteFiles(this@MineActivity)
            FileUtil.clearAllCache(this@MineActivity)
        }

        launch(Dispatchers.Main) {
            ToastUtil.showShort(this@MineActivity, "清除成功")
        }

    }

    private fun aboutUs() {
        val packName = AppUtil.getPackageVersionName(this, packageName)
        val appName = getString(R.string.app_name)
        ToastUtil.show(this, "$appName $packName")
    }

}