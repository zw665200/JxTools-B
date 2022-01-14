package com.wx.tools.bean

data class PackDetail(
    var id: Int,
    var server_name: String,
    var server_code: String,
    var server_type: String,
    var server_price: String,
    var sale_price: String,
    var desc: String,
    var sort: Int,
    var createtime: Int,
    var updatetime: Int,
    var is_del: Int,
    var packid: Int,
    var pack_name:String
)