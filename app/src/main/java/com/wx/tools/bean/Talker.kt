package com.wx.tools.bean

import android.os.Parcelable
import androidx.room.*
import com.wx.tools.model.db.MessageTypeConverter
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "talker")
@TypeConverters(MessageTypeConverter::class)
@Parcelize
data class Talker(
    @PrimaryKey var id: String,
    var userName: String,
    var nickName: String? = null,
    var alias: String? = null,
    var icon: String? = null,
    var conRemark: String? = null,
    var conversation: Conversation?,
    var type: Int = -1,
    var time: Long = 0L
) : Parcelable

