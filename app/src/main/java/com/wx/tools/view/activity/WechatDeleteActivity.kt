package com.wx.tools.view.activity

import android.content.Intent
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.wx.tools.R
import com.wx.tools.adapter.DataAdapter
import com.wx.tools.view.base.BaseActivity
import com.wx.tools.view.views.DeleteFileDialog
import com.wx.tools.view.views.ScaleInTransformer
import kotlinx.android.synthetic.main.banner.view.*

class WechatDeleteActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var deleteInner: Button
    private lateinit var deleteOutter: Button
    private var mainPics = mutableListOf<Int>()
    private lateinit var mAdapter: DataAdapter<Int>
    private lateinit var vp: ViewPager2
    private var serviceId: Int = 0


    override fun setLayout(): Int {
        return R.layout.a_delete_files
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        deleteInner = findViewById(R.id.delete_inner)
        deleteOutter = findViewById(R.id.delete_outter)
        vp = findViewById(R.id.vp_banner)

        back.setOnClickListener { finish() }
        deleteInner.setOnClickListener { deleteInner() }
        deleteOutter.setOnClickListener { deleteOutter() }
    }

    override fun initData() {
        serviceId = intent.getIntExtra("serviceId", 0)
        mainPics.clear()
        mainPics.add(R.mipmap.delete_01)
        mainPics.add(R.mipmap.delete_02)
        initBannerViewPager()
    }

    private fun initBannerViewPager() {
        vp.orientation = ViewPager2.ORIENTATION_HORIZONTAL

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

    private fun deleteInner() {
        val intent = Intent()
        intent.action = "android.intent.action.MAIN"
        intent.setClassName("com.android.settings", "com.android.settings.ManageApplications")
        startActivity(intent)
    }

    private fun deleteOutter() {
//        val am: ActivityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//
//        val bl = am::clearApplicationUserData
//        bl.invoke()
//        JLog.i("delete result = $bl")


        DeleteFileDialog(this).show()

    }


}