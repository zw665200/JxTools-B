package com.wx.tools.http.request

import com.wx.tools.bean.UserInfo
import com.wx.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.*

interface UserInfoService {

    @POST("visit")
    @FormUrlEncoded
    fun getUser(@Field("questToken") token: String): Observable<Response<List<UserInfo>>>
}