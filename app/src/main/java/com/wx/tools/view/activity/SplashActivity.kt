package com.wx.tools.view.activity

import android.content.Intent
import android.os.*
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bytedance.sdk.openadsdk.*
import com.wx.tools.R
import com.wx.tools.bean.FileBean
import com.wx.tools.callback.DialogCallback
import com.wx.tools.controller.Constant
import com.wx.tools.controller.DBManager
import com.wx.tools.http.loader.ConfigLoader
import com.wx.tools.http.response.ResponseTransformer
import com.wx.tools.http.schedulers.SchedulerProvider
import com.wx.tools.utils.ToastUtil
import com.wx.tools.view.base.BaseActivity
import com.wx.tools.view.views.CustomerServiceDialog
import com.tencent.mmkv.MMKV
import com.tencent.smtt.export.external.TbsCoreSettings
import com.tencent.smtt.sdk.QbSdk
import com.wx.tools.bean.UserInfo
import com.wx.tools.http.loader.ServiceListLoader
import com.wx.tools.utils.JLog
import kotlinx.android.synthetic.main.d_pics.*
import kotlinx.coroutines.*

/**
@author ZW
@description:
@date : 2020/11/25 10:31
 */
class SplashActivity : BaseActivity() {
    private lateinit var textView: TextView
    private lateinit var splashBg: ImageView
    private lateinit var timer: CountDownTimer
    private var kv = MMKV.defaultMMKV()
    private var show = false

    override fun setLayout(): Int {
        return R.layout.activity_splash
    }

    override fun initView() {
        textView = findViewById(R.id.splash_start)
        splashBg = findViewById(R.id.splash_bg)

        initUserInfo()
        initTimer()
        initTBS()
        clearDatabase()
        getConfig()
        getServiceList()

    }


    override fun initData() {

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus && !show) {
            val value = kv?.decodeBool("service_agree")
            if (value == null || !value) {
                val intent = Intent(this, AgreementActivity::class.java)
                startActivityForResult(intent, 0x1)
                show = true
            } else {
                timer.start()
            }
        }
        super.onWindowFocusChanged(hasFocus)
    }

    private fun initUserInfo() {
        val userInfo = kv?.decodeParcelable("userInfo", UserInfo::class.java)
        if (userInfo != null) {
            Constant.CLIENT_TOKEN = userInfo.client_token
            Constant.USER_NAME = userInfo.nickname
            Constant.USER_ID = userInfo.id.toString()
        }
    }

    private fun initTimer() {
        timer = object : CountDownTimer(2 * 1000L, 1000) {
            override fun onFinish() {
                jumpTo()
            }

            override fun onTick(millisUntilFinished: Long) {
//                textView.text = "${millisUntilFinished / 1000}"
            }
        }
    }

    private fun initTBS() {
        val map = HashMap<String, Any>()
        map[TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER] = true
        map[TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE] = true
        QbSdk.initTbsSettings(map)
    }

    private fun jumpTo() {
        val intent = Intent()
        intent.setClass(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun clearDatabase() {
        launch(Dispatchers.IO) {
            DBManager.deleteFiles(this@SplashActivity)
        }
    }


    private fun getConfig() {
        launch {
            ConfigLoader.getConfig()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    Constant.WEBSITE = it.offcialSite
                }, {
                    ToastUtil.show(this@SplashActivity, "获取配置文件失败")
                })
        }
    }

    private fun getServiceList() {
        launch {
            ServiceListLoader.getServiceList()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    if (it.isNotEmpty()) {
                        for (child in it) {
                            //save service list
                            JLog.i("code = ${child.server_code}")
                            MMKV.defaultMMKV()?.encode(child.server_code, child)
                        }
                    }
                }, {
                    ToastUtil.show(this@SplashActivity, "获取服务列表失败")
                })
        }
    }

//    private fun openAd() {
//        val mTTAdNative = TTAdSdk.getAdManager().createAdNative(this)
//        val adSlot = AdSlot.Builder().setCodeId("887589004").setImageAcceptedSize(1080, 1920)
//            .setAdLoadType(TTAdLoadType.LOAD)
//            .build()
//
//        mTTAdNative.loadSplashAd(adSlot, object : TTAdNative.SplashAdListener {
//            override fun onError(code: Int, message: String?) {
//                JLog.i("error code = $code")
//                jumpTo()
//            }
//
//            override fun onTimeout() {
//                JLog.i("on timeout")
//                jumpTo()
//            }
//
//            override fun onSplashAdLoad(ad: TTSplashAd?) {
//
//                if (ad == null) {
//                    JLog.i("ad is null")
//                    return
//                }
//
//                JLog.i("ad is not null")
//
//                val view = ad.splashView
//                if (view != null) {
//                    (splashBg.parent as ViewGroup).addView(view)
//                }
//
//                ad.setSplashInteractionListener(object : TTSplashAd.AdInteractionListener {
//                    override fun onAdClicked(p0: View?, p1: Int) {
//                        timer.cancel()
//                    }
//
//                    override fun onAdShow(p0: View?, p1: Int) {
//                        if (p0 != null) {
//                            JLog.i("ad is show")
//                            timer.start()
//                        }
//                    }
//
//                    override fun onAdSkip() {
//                        timer.cancel()
//                        jumpTo()
//                    }
//
//                    override fun onAdTimeOver() {
////                        jumpTo()
//                    }
//                })
//            }
//        })
//
//    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x1) {
            if (resultCode == 0x1) {
                kv?.encode("service_agree", true)
                timer.start()
            }

            if (resultCode == 0x2) {
                kv?.encode("service_agree", false)
                timer.start()
            }
        }
    }

}