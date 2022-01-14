package com.wx.tools.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.wx.tools.bean.PayData

@Dao
interface PayDao {

    @Query("SELECT * FROM payData")
    fun getAll(): List<PayData>?

    @Insert
    fun insert(payData: PayData)

    @Query("SELECT * FROM payData WHERE name = (:key)")
    fun find(key: String): PayData?

    @Query("SELECT * FROM payData WHERE name = (:key) AND isMenu = (:isMenu)")
    fun find(key: String, isMenu: Boolean): PayData?

    @Update
    fun update(vararg payData: PayData)

}