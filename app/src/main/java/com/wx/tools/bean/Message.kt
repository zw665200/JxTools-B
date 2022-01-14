package com.wx.tools.bean

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "message")
@Parcelize
data class Message(
    @PrimaryKey var id: String,
    var accountName: String,
    var talkerName: String,
    var icon: String?,
    var type: Int = 0,
    var isSend: Int = 0,
    var content: String?,
    var date: String?,
    var imgPath: String?
) : Parcelable