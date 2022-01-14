package com.wx.tools.bean

data class PayStatus(
    var serverId: Int,
    var serverExpire: Int,
    var serverName: String,
    var discountFee: Float,
    var packDetail: List<PackDetail>
)
