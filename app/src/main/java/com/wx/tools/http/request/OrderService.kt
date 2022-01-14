package com.wx.tools.http.request

import com.wx.tools.bean.Order
import com.wx.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.*

interface OrderService {

    @POST("orderList")
    @FormUrlEncoded
    fun getOrders(@Field("clientToken") token: String): Observable<Response<List<Order>>>
}