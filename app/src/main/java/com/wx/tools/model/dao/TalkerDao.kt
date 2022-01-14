package com.wx.tools.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.wx.tools.bean.Talker

@Dao
interface TalkerDao {

    @Query("SELECT * FROM talker")
    fun getAll(): List<Talker>?

    @Insert
    fun insert(talker: Talker)

    @Query("SELECT * FROM talker WHERE id LIKE :accountName||':%' AND userName = (:userName) AND time = :srcTime LIMIT 15 OFFSET :offset")
    fun findTalkersByAccount(accountName: String, userName: String, srcTime: Long, offset: Int): Talker?

    @Query("SELECT talker.id,talker.userName,talker.nickName,talker.alias,talker.icon,talker.conRemark,talker.conversation,talker.type,talker.time FROM talker WHERE id LIKE :accountName||':%' AND time = :srcTime LIMIT 15 OFFSET :offset")
    fun findTalkersByAccount(accountName: String, srcTime: Long, offset: Int): List<Talker>?

    @Query("SELECT * FROM talker WHERE id = :id")
    fun findTalkersById(id: String): List<Talker>?

    @Update
    fun update(vararg user: Talker)

}