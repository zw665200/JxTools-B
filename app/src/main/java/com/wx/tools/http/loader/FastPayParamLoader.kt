package com.wx.tools.http.loader

import com.wx.tools.bean.FastPayParam
import com.wx.tools.controller.Constant
import com.wx.tools.controller.RetrofitServiceManager
import com.wx.tools.http.response.Response
import io.reactivex.Observable

object FastPayParamLoader {

    fun getOrderParam(serviceId: Int): Observable<Response<FastPayParam>> {
        return RetrofitServiceManager.getInstance().fastPayParam.getOrderParam(
            serviceId,
            Constant.CLIENT_TOKEN,
            Constant.PRODUCT_ID,
            Constant.CHANNEL_ID
        )
    }
}