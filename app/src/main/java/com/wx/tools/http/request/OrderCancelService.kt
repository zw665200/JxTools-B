package com.wx.tools.http.request

import com.wx.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface OrderCancelService {

    @POST("orderCancel")
    @FormUrlEncoded
    fun orderCancel(@Field("orderSn") orderSn: String, @Field("clientToken") token: String): Observable<Response<String?>>
}