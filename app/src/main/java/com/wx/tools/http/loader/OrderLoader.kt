package com.wx.tools.http.loader

import com.wx.tools.bean.Order
import com.wx.tools.controller.Constant
import com.wx.tools.controller.RetrofitServiceManager
import com.wx.tools.http.response.Response
import io.reactivex.Observable

object OrderLoader {

    fun getOrders(): Observable<Response<List<Order>>> {
        return RetrofitServiceManager.getInstance().orders.getOrders(Constant.CLIENT_TOKEN)
    }
}