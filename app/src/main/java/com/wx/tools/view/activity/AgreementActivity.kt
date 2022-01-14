package com.wx.tools.view.activity

import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.tencent.mmkv.MMKV
import com.wx.tools.R
import com.wx.tools.view.base.BaseActivity

class AgreementActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var privacyImg: ImageView
    private lateinit var userImg: ImageView
    private lateinit var webview: WebView
    private lateinit var privacyTitle: TextView
    private lateinit var userTitle: TextView
    private lateinit var agree: Button
    private lateinit var refuse: Button
    private lateinit var bottomView: LinearLayout
    private val customerUrl = "file:///android_asset/customer_agreement.html"
    private val privacyUrl = "file:///android_asset/privacy_agreement.html"
    private var kv = MMKV.defaultMMKV()


    override fun setLayout(): Int {
        return R.layout.a_agreement
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        back.setOnClickListener { finish() }

        privacyTitle = findViewById(R.id.privacy_agreement_title)
        userTitle = findViewById(R.id.user_agreement_title)
        privacyImg = findViewById(R.id.privacy_indictor)
        userImg = findViewById(R.id.user_indictor)
        refuse = findViewById(R.id.refuse)
        agree = findViewById(R.id.agree)

        webview = findViewById(R.id.webview)

        bottomView = findViewById(R.id.bottom_view)

        privacyTitle.setOnClickListener { loadPrivacyPage() }
        userTitle.setOnClickListener { loadUserPage() }
        refuse.setOnClickListener { refuse() }
        agree.setOnClickListener { agree() }

    }

    override fun initData() {
        initWebView()
        webview.loadUrl(privacyUrl)
        checkAgreementPermission()
    }

    private fun initWebView() {
        webview.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
//                view?.loadUrl(url)
                return true
            }
        }
    }

    private fun checkAgreementPermission() {
        val value = kv?.decodeBool("service_agree")
        if (value != null && value) {
            back.visibility = View.VISIBLE
            bottomView.visibility = View.GONE
        }
    }

    private fun loadPrivacyPage() {
        webview.loadUrl(privacyUrl)
        privacyImg.visibility = View.VISIBLE
        userImg.visibility = View.INVISIBLE
    }

    private fun loadUserPage() {
        webview.loadUrl(customerUrl)
        privacyImg.visibility = View.INVISIBLE
        userImg.visibility = View.VISIBLE
    }

    private fun refuse() {
        setResult(0x2)
        finish()
    }

    private fun agree() {
        setResult(0x1)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}