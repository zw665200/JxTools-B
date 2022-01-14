package com.wx.tools.http.loader

import com.wx.tools.bean.AlipayParam
import com.wx.tools.controller.Constant
import com.wx.tools.controller.RetrofitServiceManager
import com.wx.tools.http.response.Response
import io.reactivex.Observable

object AliPayLoader {

    fun getOrderParam(serviceId: Int): Observable<Response<AlipayParam>> {
        return RetrofitServiceManager.getInstance().aliPayParam.getOrderParam(
            serviceId,
            Constant.CLIENT_TOKEN,
            Constant.PRODUCT_ID,
            Constant.CHANNEL_ID
        )
    }
}