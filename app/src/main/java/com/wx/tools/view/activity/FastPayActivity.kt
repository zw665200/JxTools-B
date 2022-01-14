package com.wx.tools.view.activity

import android.annotation.SuppressLint
import android.text.TextUtils
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import com.alipay.sdk.app.PayTask
import com.wx.tools.R
import com.wx.tools.utils.JLog
import com.wx.tools.view.base.BaseActivity

class FastPayActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var webview: WebView
    private lateinit var title: TextView

    override fun setLayout(): Int {
        return R.layout.a_agreement
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        back.setOnClickListener { finish() }

        title = findViewById(R.id.agreement_title)
        webview = findViewById(R.id.webview)
    }

    override fun initData() {
        title.text = "发起支付"
        val page = intent.getStringExtra("page")
        if (page != null) {
            JLog.i("page = $page")
            initWebView(page)
        }

    }

    @SuppressLint("setJavaScriptEnabled")
    private fun initWebView(url: String) {
        webview.settings.apply {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            domStorageEnabled = true
        }

        webview.setVerticalScrollbarOverlay(true)
        webview.webViewClient = MyWebViewClient()
        webview.loadUrl(url)
    }

    private inner class MyWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            if (!(url.startsWith("http") || url.startsWith("https"))) {
                return true
            }
            /**
             * 推荐采用的新的二合一接口(payInterceptorWithUrl),只需调用一次
             */
            val task = PayTask(this@FastPayActivity)
            val isIntercepted = task.payInterceptorWithUrl(url, true) { result ->
                val realUrl = result.returnUrl
                if (!TextUtils.isEmpty(url)) {
                    this@FastPayActivity.runOnUiThread { view.loadUrl(realUrl) }
                }
            }
            /**
             * 判断是否成功拦截
             * 若成功拦截，则无需继续加载该URL；否则继续加载
             */
            if (!isIntercepted) {
                view.loadUrl(url)
            }
            return true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (webview != null) {
            webview.removeAllViews()
            try {
                webview.destroy()
            } catch (t: Throwable) {
            }
        }
    }

}