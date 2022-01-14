package com.wx.tools.bean

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "price")
@Parcelize
data class Price(
    @PrimaryKey var id: Int,
    var server_name: String,
    var server_type: String,
    var server_code: String,
    var server_price: String,
    var sale_price: String,
    var desc: String,
    var sort: Int,
    var createtime: Long,
    var updatetime: Long,
    var is_del: Int
): Parcelable
