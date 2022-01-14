package com.wx.tools.http.request

import com.wx.tools.bean.OrderDetail
import com.wx.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.*

interface OrderDetailService {

    @POST("orderDetail")
    @FormUrlEncoded
    fun getOrderDetail(@Field("orderSn") orderSn: String, @Field("clientToken") token: String): Observable<Response<OrderDetail>>
}