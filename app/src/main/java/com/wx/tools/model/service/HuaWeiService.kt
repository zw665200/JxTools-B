package com.wx.tools.model.service

import android.database.Cursor
import android.database.CursorWrapper
import android.database.sqlite.SQLiteDatabase
import com.wx.tools.bean.FileStatus
import com.wx.tools.callback.DBCallback
import com.wx.tools.utils.FileUtil
import com.wx.tools.utils.JLog
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

object HuaWeiService {

    fun openDatabase(dbPath: String, jxPath: String, callback: DBCallback): Boolean {
        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbPath, null)
            openFileInfo(db, jxPath, callback)
            db.close()
            return true
        } catch (e: Exception) {
            JLog.i("SQLiteDatabase error : + $e")
            db?.close()
            return false
        }
    }

    /**
     * 打开文件索引表
     */
    private fun openFileInfo(db: SQLiteDatabase, dbPath: String, callback: DBCallback) {
        val c: Cursor = db.rawQuery("select * from apk_file_info", null)
        val count = c.count
        JLog.i("fileInfo row count = $count")
        while (c.moveToNext()) {
            val index = c.getInt(c.getColumnIndex("file_index"))
            val path = c.getString(c.getColumnIndex("file_path"))

            JLog.i("file_index = $index")
            if (index == -1) {
                byteArrayTransformFile(dbPath, index, path, byteArrayOf())
            } else {
                if (path.endsWith(".cfg") || path.endsWith(".xml") || path.endsWith(".db") || path.endsWith(".acc") || path.endsWith(".jpg")) {
                    openFileData(db, dbPath, index, path, callback)
                }
            }

            //计算进度
            val percent = c.position * 100 / count
            callback.onProgress(FileStatus.UNZIP_WX, percent)
        }
        c.close()
    }

    /**
     * 打开文件数据表
     */
    private fun openFileData(
        db: SQLiteDatabase,
        dbPath: String,
        fileIndex: Int,
        filePath: String,
        callback: DBCallback
    ) {
        val map = HashMap<String, ByteArray>()
        val c: Cursor = db.rawQuery("select * from apk_file_data where file_index=?", arrayOf(fileIndex.toString()))
        JLog.i("fileData row count = ${c.count}")
        while (c.moveToNext()) {
            val data = c.getBlob(c.getColumnIndex("file_data"))

            if (map.containsKey(filePath)) {
                val d = map[filePath]
                if (d != null) {
                    val arr = ByteArray(d.size + data.size)
                    System.arraycopy(d, 0, arr, 0, d.size)
                    System.arraycopy(data, 0, arr, d.size, data.size)
                    map[filePath] = arr
                }
            } else {
                map[filePath] = data
            }
        }

        if (map.size > 0) {
            JLog.i("map size = ${map.size}")
            for (key in map.keys) {
                byteArrayTransformFile(dbPath, fileIndex, filePath, map[key]!!)
            }
        }
        c.close()

    }

    /**
     * 将字节数组转换成文件
     */
    private fun byteArrayTransformFile(jxPath: String, fileIndex: Int, filePath: String, data: ByteArray) {
        val file = File(jxPath)
        val path = jxPath + filePath.replace("/data/data", "")
        JLog.i("file_path = $path")

        if (fileIndex == -1) {
            //如果是目录则创建
            FileUtil.createFolder(path)
        } else {
            //如果是文件就写入
            val out = FileOutputStream(path)
            out.write(data)
            out.close()
        }
    }

    /**
     * 查询数据库所有的表名
     * @param db
     */
    private fun queryTableNames(db: SQLiteDatabase) {
        val cw = CursorWrapper(db.rawQuery("SELECT * FROM sqlite_master ORDER BY type DESC", null))
        val colCount: Int = cw.columnCount
        val mapList: MutableList<Map<String, Any>> = ArrayList(cw.count)
        while (cw.moveToNext()) {
            val map: MutableMap<String, Any> = HashMap()
            for (i in 0 until colCount) {
                val name: String = cw.getColumnName(i)
                val type: Int = cw.getType(i)
                if (type == Cursor.FIELD_TYPE_BLOB) {
                    map[name] = cw.getBlob(i)
                    JLog.i("blob name = ${map[name]}")
                } else {
                    map[name] = cw.getString(i)
                    JLog.i("table name = ${map[name]}")
                }
            }
            mapList.add(map)
        }
    }
}