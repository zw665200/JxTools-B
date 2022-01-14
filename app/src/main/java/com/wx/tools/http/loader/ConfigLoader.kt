package com.wx.tools.http.loader

import com.wx.tools.bean.Config
import com.wx.tools.controller.RetrofitServiceManager
import com.wx.tools.http.response.Response
import io.reactivex.Observable

object ConfigLoader {

    fun getConfig(): Observable<Response<Config>> {
        return RetrofitServiceManager.getInstance().config.getConfig()
    }
}