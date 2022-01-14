package com.wx.tools.http.loader

import com.wx.tools.controller.Constant
import com.wx.tools.controller.RetrofitServiceManager
import com.wx.tools.http.response.Response
import io.reactivex.Observable

object ComplaintLoader : ObjectLoader() {

    fun reportComplaint(
        uid: String,
        complaintAType: String,
        phone: String,
        payAccount: String,
        problemDesc: String,
        pic: String
    ): Observable<Response<List<String?>?>> {
        return RetrofitServiceManager.getInstance().reportComplaint().reportComplaint(
            Constant.PRODUCT_ID,
            uid,
            Constant.USER_NAME,
            Constant.CLIENT_TOKEN,
            complaintAType,
            phone,
            payAccount,
            problemDesc,
            pic
        )
    }
}