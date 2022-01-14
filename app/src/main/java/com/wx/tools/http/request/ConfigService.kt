package com.wx.tools.http.request

import com.wx.tools.bean.Config
import com.wx.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.*

interface ConfigService {

    @GET("siteInfo")
    fun getConfig(): Observable<Response<Config>>
}