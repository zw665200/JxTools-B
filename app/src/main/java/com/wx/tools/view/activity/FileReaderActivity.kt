package com.wx.tools.view.activity

import android.widget.ImageView
import android.widget.TextView
import com.wx.tools.utils.JLog
import com.wx.tools.view.base.BaseActivity
import com.tencent.smtt.export.external.interfaces.WebResourceRequest
import com.tencent.smtt.sdk.QbSdk
import com.tencent.smtt.sdk.WebSettings
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient
import com.wx.tools.R

class FileReaderActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var webview: WebView
    private lateinit var title: TextView


    override fun setLayout(): Int {
        return R.layout.a_file_reader
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        back.setOnClickListener { finish() }

        title = findViewById(R.id.agreement_title)
        webview = findViewById(R.id.webview)

        val setting = webview.settings
        setting.javaScriptEnabled = true
        setting.allowFileAccess = true
        setting.builtInZoomControls = true
        setting.cacheMode = WebSettings.LOAD_NO_CACHE
        setting.domStorageEnabled = true
        setting.setGeolocationEnabled(true)
    }

    override fun initData() {

        val titleName = intent.getStringExtra("title")
        val page = intent.getStringExtra("page")
        if (titleName != null && page != null) {
            title.text = titleName

            JLog.i("page = $page")

            webview.loadUrl(page)

//            val params = HashMap<String, String>()
//            params["style"] = "2"
//            params["local"] = "true"
//            QbSdk.openFileReader(this, page, params, { })


//            val url = "https://view.officeapps.live.com/op/view.aspx?src=$page"
//            initWebView(url)
//            webview.loadUrl(url)
//            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//            startActivity(intent)
        }
    }

    private fun initWebView(url: String) {

        webview.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                JLog.i("url = " + request?.url)
//                val returnUrl = request?.url
//                try {
//                    JLog.i("received page = $url")
//                    val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
//                    intent.addCategory(Intent.CATEGORY_BROWSABLE)
//                    intent.component = null
//                    startActivity(intent)
//                } catch (ex: Exception) {
//                    return false
//                }

//                webview.loadUrl(url)

                return true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webview.removeAllViews()
        QbSdk.closeFileReader(this)
    }

}