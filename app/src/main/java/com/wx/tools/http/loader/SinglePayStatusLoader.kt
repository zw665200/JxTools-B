package com.wx.tools.http.loader

import com.wx.tools.bean.PayStatus
import com.wx.tools.controller.RetrofitServiceManager
import com.wx.tools.http.response.Response
import io.reactivex.Observable

object SinglePayStatusLoader {

    fun getPayStatus(serviceId: Int, token: String): Observable<Response<List<PayStatus>>> {
        return RetrofitServiceManager.getInstance().singlePayStatus.getPayStatus(serviceId, token)
    }
}