package com.wx.tools.model.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wx.tools.bean.Talker
import java.lang.reflect.Type
import java.util.*

class TalkerTypeConverter {
    var gson: Gson = Gson()

    @TypeConverter
    fun stringToSomeObjectList(data: String?): List<Talker?>? {
        if (data == null) {
            return Collections.emptyList()
        }
        val listType: Type = object : TypeToken<List<Talker?>?>() {}.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun someObjectListToString(someObjects: List<Talker?>?): String? {
        return gson.toJson(someObjects)
    }
}