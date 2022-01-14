package com.wx.tools.http.loader

import com.wx.tools.bean.OssParam
import com.wx.tools.controller.RetrofitServiceManager
import com.wx.tools.http.response.Response
import io.reactivex.Observable

object OssLoader : ObjectLoader() {

    fun getOssToken(token: String): Observable<Response<OssParam>> {
        return RetrofitServiceManager.getInstance().ossToken.getOssToken(token)
    }
}