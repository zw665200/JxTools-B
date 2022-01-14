package com.wx.tools.model.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wx.tools.bean.Conversation
import com.wx.tools.bean.Message
import java.lang.reflect.Type
import java.util.*

class MessageTypeConverter {
    var gson: Gson = Gson()

    @TypeConverter
    fun stringToSomeObjectList(data: String?): List<Message?>? {
        if (data == null) {
            return Collections.emptyList()
        }
        val listType: Type = object : TypeToken<List<Message?>?>() {}.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun someObjectListToString(someObjects: List<Message?>?): String? {
        return gson.toJson(someObjects)
    }

    @TypeConverter
    fun stringToSomeObject(data: String?): Conversation? {
        if (data == null) {
            return null
        }
        val type: Type = object : TypeToken<Conversation?>() {}.type
        return gson.fromJson(data, type)
    }

    @TypeConverter
    fun someObjectToString(someObject: Conversation?): String? {
        return gson.toJson(someObject)
    }

}