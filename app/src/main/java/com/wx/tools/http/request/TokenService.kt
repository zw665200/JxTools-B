package com.wx.tools.http.request

import com.wx.tools.bean.GetToken
import com.wx.tools.bean.Token
import com.wx.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.POST

interface TokenService {

    @POST("getQuestToken")
    fun getToken(@Body getToken: GetToken): Observable<Response<Token>>
}