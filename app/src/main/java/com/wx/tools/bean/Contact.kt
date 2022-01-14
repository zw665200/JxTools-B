package com.wx.tools.bean

import android.os.Parcelable
import androidx.room.*
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "contact")
@Parcelize
data class Contact(
    @PrimaryKey var id: String,
    var userName: String,
    var nickName: String? = null,
    var alias: String? = null,
    var icon: String? = null,
    var conRemark: String? = null,
    var type: Int = -1,
    var time: Long = 0L
) : Parcelable

