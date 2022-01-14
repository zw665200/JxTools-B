package com.wx.tools.bean

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserInfo(
    var id: Int,
    var nickname: String,
    var user_type: Int,
    var addtime: Long,
    var last_logintime: Long,
    var login_ip: String,
    var popularize_id: Int,
    var pop_name: String,
    var client_token: String,
    var city: String
) : Parcelable
