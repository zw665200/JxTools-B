package com.wx.tools.http.request

import com.wx.tools.bean.Price
import com.wx.tools.controller.Constant
import com.wx.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.*

interface ServiceListService {

    @GET("serverList/${Constant.PRODUCT_ID}")
    fun getServiceList(): Observable<Response<List<Price>>>
}