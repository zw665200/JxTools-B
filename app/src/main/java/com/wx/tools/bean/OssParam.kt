package com.wx.tools.bean

/**
 * @author Herr_Z
 * @description:
 * @date : 2021/3/26 15:49
 */
data class OssParam(
    var endpoint: String,
    var bucket: String,
    var objectKey: String,
    var AccessKeyId: String,
    var AccessKeySecret: String,
    var SecurityToken: String
)