package com.wx.tools.bean

data class Order(
    var order_id: Int,
    var order_sn: String,
    var uid: Int,
    var nickname: String,
    var phone: String,
    var status: String,
    var server_name: String,
    var server_code: String,
    var server_price: String,
    var pay_price: String,
    var createtime: Long,
    var updatetime: Long,
    var social_number: String,
    var is_del: Int,
    var pay_ordersn: String
)
