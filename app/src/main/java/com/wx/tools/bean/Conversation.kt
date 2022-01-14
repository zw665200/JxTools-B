package com.wx.tools.bean

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Conversation(
    val msgCount: Int,
    val conversationTime: Int,
    val username: String,
    val content: String,
    val unReadCount: Int
) : Parcelable
