package com.wx.tools.http.loader

import com.wx.tools.bean.UserInfo
import com.wx.tools.controller.RetrofitServiceManager
import com.wx.tools.http.response.Response
import io.reactivex.Observable

object UserInfoLoader : ObjectLoader() {

    fun getUser(token: String): Observable<Response<List<UserInfo>>> {
        return RetrofitServiceManager.getInstance().userInfo.getUser(token)
    }
}