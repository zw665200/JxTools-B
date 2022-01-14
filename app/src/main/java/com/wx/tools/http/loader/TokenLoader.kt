package com.wx.tools.http.loader

import android.content.Context
import android.os.Build
import com.wx.tools.bean.GetToken
import com.wx.tools.bean.Token
import com.wx.tools.controller.Constant
import com.wx.tools.controller.RetrofitServiceManager
import com.wx.tools.http.response.Response
import com.wx.tools.utils.AES
import com.wx.tools.utils.AppUtil
import com.wx.tools.utils.DeviceUtil
import com.wx.tools.utils.JLog
import io.reactivex.Observable

object TokenLoader {

    fun getToken(context: Context): Observable<Response<Token>> {
        val brand = Build.BRAND
        val mode = Build.MODEL
        val deviceId = DeviceUtil.getDeviceId(context)
        val device = Build.DEVICE
        val time = System.currentTimeMillis()
        val questTime = AppUtil.timeStamp2Date(time.toString(), null)
        val questToken = AES.encrypt(Constant.CHANNEL_ID, AppUtil.MD5Encode((time / 1000).toString()) + Constant.CHANNEL_ID)
        val questFrom = Constant.CHANNEL_ID
        val productId = Constant.PRODUCT_ID

        val token = GetToken(questTime, questToken, questFrom, device, deviceId, brand, mode, productId)

        return RetrofitServiceManager.getInstance().token.getToken(token)
    }
}