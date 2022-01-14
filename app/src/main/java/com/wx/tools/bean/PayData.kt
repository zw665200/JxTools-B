package com.wx.tools.bean

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payData")
data class PayData(
    @PrimaryKey var name: String,
    var userName: String,
    var type: String,
    var date: Long,
    var isPayed: Boolean,
    var isMenu: Boolean = false
)
