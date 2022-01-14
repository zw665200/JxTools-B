package com.wx.tools.bean

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "fileWithType")
@Parcelize
data class FileWithType(
    var name: String,
    @PrimaryKey var path: String,
    var size: Int,
    var date: Long,
    var type: String,
    var check: Boolean = false
) : Parcelable