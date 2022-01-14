package com.wx.tools.view.base

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.wx.tools.R
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

abstract class BaseActivity : AppCompatActivity(), CoroutineScope by MainScope() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        super.setContentView(R.layout.activity_base)
        LayoutInflater.from(this).inflate(setLayout(), fl_base)
        initView()
        initData()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = this.resources.getColor(R.color.color_blue, null)
        }
    }

    protected abstract fun setLayout(): Int
    protected abstract fun initView()
    protected abstract fun initData()

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

}