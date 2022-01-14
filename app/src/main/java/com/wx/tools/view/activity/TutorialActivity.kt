package com.wx.tools.view.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import com.wx.tools.R
import com.wx.tools.view.base.BaseActivity

class TutorialActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var webview: WebView
    private val url1 = "http://down.ql-recovery.com/step1.html"
    private val url2 = "http://down.ql-recovery.com/step2.html"
    private var step = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun setLayout(): Int {
        return R.layout.a_tutorial
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        back.setOnClickListener { finish() }

        webview = findViewById(R.id.webview)
    }

    override fun initData() {
        step = intent.getIntExtra("step", 1)
        when (step) {
            1 -> webview.loadUrl(url1)
            2 -> webview.loadUrl(url2)
        }

        initWebview()
    }

    @SuppressLint("setJavaScriptEnabled")
    private fun initWebview() {
        webview.settings.apply {
            javaScriptEnabled = true
        }
        webview.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
//                view?.loadUrl(url)
                return true
            }
        }
    }


}