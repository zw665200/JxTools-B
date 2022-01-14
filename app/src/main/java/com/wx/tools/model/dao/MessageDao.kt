package com.wx.tools.model.dao

import androidx.room.*
import com.wx.tools.bean.Message

@Dao
interface MessageDao {

    @Query("SELECT * FROM message")
    fun getAll(): List<Message>?

    @Insert
    fun insert(message: Message)

    @Query("SELECT * FROM message WHERE accountName = :accountName AND talkerName = :userName LIMIT :limit OFFSET :offset")
    fun findMessagesByName(accountName: String, userName: String, limit: Int, offset: Int): List<Message>?

    @Query("SELECT * FROM message WHERE id = :id")
    fun findMessageById(id: String): Message?

    @Update
    fun update(vararg user: Message)

    @Delete
    fun delete(message: Message)

}