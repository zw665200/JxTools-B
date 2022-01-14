package com.wx.tools

import android.app.Application
import com.baidu.mobads.action.BaiduAction
import com.hyphenate.chat.ChatClient
import com.hyphenate.helpdesk.easeui.UIProvider
import com.tencent.bugly.Bugly
import com.tencent.mmkv.MMKV
import com.tencent.smtt.sdk.QbSdk
import com.wx.tools.controller.Constant
import com.wx.tools.controller.RetrofitServiceManager
import com.wx.tools.utils.AppUtil
import com.wx.tools.utils.JLog
import com.wx.tools.utils.RomUtil


class BaseApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initData()
        initRom()
        initHttpRequest()
        initMMKV()
        initBaiduAction()
        initIM()
        initBugly()
        initX5Environment()
    }

    private fun initData() {
        if (AppUtil.isDebugger(this)) {
            Constant.isDebug = true
        }
    }

    /**
     * 读取设备信息
     *
     */
    private fun initRom() {
        val name = RomUtil.getName()
        JLog.i("name = $name")
        if (name != "") {
            Constant.ROM = name
        }
    }

    private fun initHttpRequest() {
        RetrofitServiceManager.getInstance().initRetrofitService()
    }


    /**
     * init mmkv
     */
    private fun initMMKV() {
        MMKV.initialize(this)
    }

    private fun initBaiduAction() {
        //OPPO用户不激活OCPC
        if (!RomUtil.isOppo()) {
            BaiduAction.init(this, Constant.USER_ACTION_SET_ID, Constant.APP_SECRET_KEY)
            BaiduAction.setActivateInterval(this, 7)
            BaiduAction.setPrintLog(false)
        }
    }

    private fun initIM() {
        val option = ChatClient.Options()
        option.setAppkey(Constant.SDK_APP_KEY)
        option.setTenantId(Constant.SDK_APP_ID)

        if (!ChatClient.getInstance().init(this, option)) {
            return
        }

        UIProvider.getInstance().init(this)
    }

    private fun initBugly() {
        Bugly.init(applicationContext, Constant.BUGLY_APPID, false)
    }

    private fun initX5Environment() {
        val cb = object : QbSdk.PreInitCallback {
            override fun onCoreInitFinished() {
            }

            override fun onViewInitFinished(p0: Boolean) {
            }
        }

        QbSdk.initX5Environment(this, cb)
    }

}