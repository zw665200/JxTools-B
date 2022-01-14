package com.wx.tools.http.request

import com.wx.tools.bean.AlipayParam
import com.wx.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.*

interface AliPayService {

    @POST("orderPay")
    @FormUrlEncoded
    fun getOrderParam(
        @Field("serviceId") serviceId: Int,
        @Field("clientToken") clientToken: String,
        @Field("productId") productId: String,
        @Field("channelCode") channelCode: String
    ): Observable<Response<AlipayParam>>
}