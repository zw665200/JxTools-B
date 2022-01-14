package com.wx.tools.model.db

import android.content.Context
import androidx.annotation.NonNull
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.wx.tools.bean.*
import com.wx.tools.controller.Constant
import com.wx.tools.model.dao.*


@Database(
    entities = [Account::class, Talker::class, Message::class, Contact::class, FileWithType::class, PayData::class, Price::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    //获取数据表操作实例
    abstract fun accountDao(): AccountDao
    abstract fun talkerDao(): TalkerDao
    abstract fun contactDao(): ContactDao
    abstract fun fileDao(): PicDao
    abstract fun payDao(): PayDao
    abstract fun priceDao(): PriceDao
    abstract fun messageDao(): MessageDao

    //单例模式
    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java, Constant.ROOM_DB_NAME
                ).addMigrations(migration).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                return instance
            }
        }

        //数据库升级用的
        var migration: Migration = object : Migration(2, 3) {
            override fun migrate(@NonNull database: SupportSQLiteDatabase) {
            }
        }
    }
}