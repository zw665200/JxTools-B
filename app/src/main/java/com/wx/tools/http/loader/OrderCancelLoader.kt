package com.wx.tools.http.loader

import com.wx.tools.controller.Constant
import com.wx.tools.controller.RetrofitServiceManager
import com.wx.tools.http.response.Response
import io.reactivex.Observable

object OrderCancelLoader {

    fun orderCancel(orderSn: String): Observable<Response<String?>> {
        return RetrofitServiceManager.getInstance().orderCancel().orderCancel(orderSn, Constant.CLIENT_TOKEN)
    }
}