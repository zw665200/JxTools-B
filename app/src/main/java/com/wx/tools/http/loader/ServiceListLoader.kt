package com.wx.tools.http.loader

import com.wx.tools.bean.Price
import com.wx.tools.controller.RetrofitServiceManager
import com.wx.tools.http.response.Response
import io.reactivex.Observable

object ServiceListLoader {

    fun getServiceList(): Observable<Response<List<Price>>> {
        return RetrofitServiceManager.getInstance().price.getServiceList()
    }
}