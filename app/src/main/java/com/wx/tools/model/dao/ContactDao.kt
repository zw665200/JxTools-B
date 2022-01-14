package com.wx.tools.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.wx.tools.bean.Contact

@Dao
interface ContactDao {

    @Query("SELECT * FROM contact")
    fun getAll(): List<Contact>?

    @Insert
    fun insert(talker: Contact)

    @Query("SELECT * FROM contact WHERE id LIKE :accountName||':%' AND time = :srcTime AND type != 4 AND userName NOT LIKE '%chatroom' AND userName NOT LIKE 'gh_%'")
    fun findContacts(accountName: String, srcTime: Long): List<Contact>?

    @Query("SELECT * FROM contact WHERE id LIKE :accountName||':%' AND time = :srcTime AND userName LIKE '%chatroom'")
    fun findGroupContacts(accountName: String, srcTime: Long): List<Contact>?

    @Query("SELECT * FROM contact WHERE id LIKE :accountName||':%' AND time = :srcTime AND userName LIKE 'gh_%'")
    fun findGhContacts(accountName: String, srcTime: Long): List<Contact>?

    @Query("SELECT * FROM contact WHERE id LIKE :accountName||':%' AND time = :srcTime AND type = 4")
    fun findNotFriendInGroupContacts(accountName: String, srcTime: Long): List<Contact>?

    @Query("SELECT * FROM contact WHERE id = (:id)")
    fun findContact(id: String): Contact?

    @Update
    fun update(vararg user: Contact)

}