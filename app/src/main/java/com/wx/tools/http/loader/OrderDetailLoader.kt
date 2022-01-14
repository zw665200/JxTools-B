package com.wx.tools.http.loader

import com.wx.tools.bean.OrderDetail
import com.wx.tools.controller.Constant
import com.wx.tools.controller.RetrofitServiceManager
import com.wx.tools.http.response.Response
import io.reactivex.Observable

object OrderDetailLoader {

    fun getOrderStatus(orderSn: String): Observable<Response<OrderDetail>> {
        return RetrofitServiceManager.getInstance().orderDetail.getOrderDetail(orderSn, Constant.CLIENT_TOKEN)
    }
}