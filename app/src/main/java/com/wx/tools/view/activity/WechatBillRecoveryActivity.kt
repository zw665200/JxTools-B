package com.wx.tools.view.activity

import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.wx.tools.R
import com.wx.tools.adapter.DataAdapter
import com.wx.tools.controller.WxManager
import com.wx.tools.utils.AppUtil
import com.wx.tools.utils.JLog
import com.wx.tools.view.base.BaseActivity
import com.wx.tools.view.views.ScaleInTransformer
import kotlinx.android.synthetic.main.banner.view.*

class WechatBillRecoveryActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var service: ImageView
    private var mainPics = mutableListOf<Int>()
    private lateinit var mAdapter: DataAdapter<Int>
    private lateinit var vp: ViewPager2
    private lateinit var title: TextView
    private lateinit var description: TextView
    private var steps = arrayListOf<String>()

    override fun setLayout(): Int {
        return R.layout.a_wechat_bill_recovery
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        service = findViewById(R.id.iv_service)
        vp = findViewById(R.id.vp_banner)
        title = findViewById(R.id.tv_tip_title)
        description = findViewById(R.id.recovery_tips_description)

        service.setOnClickListener { startCustomerService() }
        back.setOnClickListener { finish() }
    }

    override fun initData() {
        title.text = getString(R.string.bill_recovery_title)

        mainPics.clear()
        mainPics.add(R.mipmap.ic_bill_1)
        mainPics.add(R.mipmap.ic_bill_2)
        mainPics.add(R.mipmap.ic_bill_3)
        mainPics.add(R.mipmap.ic_bill_4)
        mainPics.add(R.mipmap.ic_bill_5)
        mainPics.add(R.mipmap.ic_bill_6)
        mainPics.add(R.mipmap.ic_bill_7)
        mainPics.add(R.mipmap.ic_bill_8)
        mainPics.add(R.mipmap.ic_bill_9)
        mainPics.add(R.mipmap.ic_bill_10)
        mainPics.add(R.mipmap.ic_bill_11)
        mainPics.add(R.mipmap.ic_bill_12)
        mainPics.add(R.mipmap.ic_bill_13)

        steps.clear()
        steps.add(getString(R.string.bill_recovery_step1))
        steps.add(getString(R.string.bill_recovery_step2))
        steps.add(getString(R.string.bill_recovery_step3))
        steps.add(getString(R.string.bill_recovery_step4))
        steps.add(getString(R.string.bill_recovery_step5))
        steps.add(getString(R.string.bill_recovery_step6))
        steps.add(getString(R.string.bill_recovery_step7))
        steps.add(getString(R.string.bill_recovery_step8))
        steps.add(getString(R.string.bill_recovery_step9))
        steps.add(getString(R.string.bill_recovery_step10))
        steps.add(getString(R.string.bill_recovery_step11))
        steps.add(getString(R.string.bill_recovery_step12))
        steps.add(getString(R.string.bill_recovery_step13))
        description.text = steps[0]

        initBannerViewPager()
    }


    private fun initBannerViewPager() {
        vp.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        vp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                description.text = steps[position]
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }
        })

        mAdapter = DataAdapter.Builder<Int>()
            .setData(mainPics)
            .setLayoutId(R.layout.banner)
            .addBindView { itemView, itemData ->
                Glide.with(this).load(itemData).into(itemView.iv_banner)
            }
            .create()

        vp.apply {
            offscreenPageLimit = 2
            (getChildAt(0) as RecyclerView).apply {
                val padding = resources.getDimensionPixelOffset(R.dimen.dp_5)
                // setting padding on inner RecyclerView puts overscroll effect in the right place
                setPadding(padding, 0, padding, 0)
                clipToPadding = false
            }
        }

        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(ScaleInTransformer())
        compositePageTransformer.addTransformer(MarginPageTransformer(resources.getDimension(R.dimen.dp_5).toInt()))
        vp.setPageTransformer(compositePageTransformer)
        vp.adapter = mAdapter
        mAdapter.notifyDataSetChanged()
        vp.setCurrentItem(0, false)

    }

    private fun startCustomerService() {
        val intent = Intent()
        intent.setClass(this, CustomerServiceActivity::class.java)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x1) {
            JLog.i("卸载回调")
            val packName = "com.huawei.KoBackup"
            if (AppUtil.checkPackageInfo(this, packName)) {
                val version = AppUtil.getPackageVersionCode(this, packName)
                JLog.i("version = $version")
                if (version == 80002301) {
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.addCategory(Intent.CATEGORY_LAUNCHER)
                    intent.component = ComponentName(packName, "com.huawei.KoBackup.InitializeActivity")
                    startActivity(intent)
                }
            } else {
                WxManager.getInstance(this).installKoBackupApk(this)
            }
        }

        if (requestCode == 0x2) {
            JLog.i("卸载回调")
            val packName = "com.coloros.backuprestore"
            if (AppUtil.checkPackageInfo(this, packName)) {
                val version = AppUtil.getPackageVersionCode(this, packName)
                JLog.i("version = $version")
                if (version == 165) {
                    val intent = packageManager.getLaunchIntentForPackage(packName)
                    if (intent != null) {
                        startActivity(intent)
                    }
                }
            }
        }

        if (requestCode == 0x10086) {
            JLog.i("安装未知应用权限回调")
            if (Build.VERSION.SDK_INT >= 26 && packageManager.canRequestPackageInstalls()) {
                WxManager.getInstance(this).installKoBackupApk(this)
            }
        }
    }

}