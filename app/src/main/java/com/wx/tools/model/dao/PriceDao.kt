package com.wx.tools.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.wx.tools.bean.*

@Dao
interface PriceDao {

    @Query("SELECT * FROM price")
    fun getAll(): List<Price>?

    @Insert
    fun insert(data: Price)

    @Query("SELECT * FROM price WHERE id = (:key)")
    fun find(key: Int): Price?

    @Update
    fun update(vararg data: Price)

}