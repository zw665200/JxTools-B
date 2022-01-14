package com.wx.tools.bean

data class FTSMetaMessage(
    var docid: Int,
    var type: Int,
    var subtype: Int = 0,
    var entity_id: Int,
    var aux_index: String,
    var timestamp: Long,
    var status: Int = 0,
    var talker: String,
    var message: String
)
