package com.wx.tools.controller

import android.content.Context
import android.util.Xml
import com.wx.tools.bean.Password
import com.wx.tools.utils.AppUtil
import com.wx.tools.utils.DeviceUtil
import com.wx.tools.utils.JLog
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.ObjectInputStream

object PwdManager {

    /**
     * 获取WX数据库密码
     */
    fun getMircoMsgPwd(iMeiList: ArrayList<String>, uinList: ArrayList<String>, dbFiles: MutableList<File>): ArrayList<Password> {
        val pwdList = arrayListOf<Password>()

        if (iMeiList.isNotEmpty() && uinList.isNotEmpty()) {
            for (iMei in iMeiList) {
                for (uin in uinList) {
                    val md5 = AppUtil.MD5Encode("mm$uin")
                    for (child in dbFiles) {
                        if (child.path.contains(md5)) {
                            val pwd = AppUtil.MD5Encode("$iMei$uin").substring(0, 7)
                            val password = Password(uin, iMei, "", pwd, "", child.path)
                            pwdList.add(password)

                            JLog.i("pwd = $iMei$uin , md5 = $pwd , account = $md5 ")
                        }
                    }
                }
            }
        }

        return pwdList
    }

    fun getFTSPwd(iMei: String, wxId: String, uin: String): String {
        var u = uin.toLong()
        if (u < 0) {
            u += 4294967296
        }

        //密码为三者的md5的前7位
        val md5 = AppUtil.MD5Encode(u.toString() + iMei + wxId)
        JLog.i("pwd value = ${u.toString() + iMei + wxId}")
        JLog.i("md5 = $md5")
        return if (md5.length > 7) {
            return md5.substring(0, 7)
        } else ""
    }


    private fun getMrcroMsgPwd(iMei: String, uin: String): String {
        JLog.i("pwd value = ${iMei + uin}")
        //密码为两者的md5的前7位
        val md5 = AppUtil.MD5Encode(iMei + uin)
        return if (md5.length > 7) {
            return md5.substring(0, 7)
        } else ""
    }


    /**
     * 获取配置文件中的IMEI
     */
    fun getImeiFromXml(context: Context, cfgFiles: List<File>, iMeiFiles: List<File>): ArrayList<String> {
        var fis: FileInputStream? = null
        val imei = arrayListOf<String>()
        var value = ""

        if (cfgFiles.isNotEmpty()) {
            try {
                fis = FileInputStream(cfgFiles[0])
                val ois = ObjectInputStream(fis)
                val maps = ois.readObject() as Map<*, *>
                for (key in maps.keys) {
                    if (key == 258) {
                        value = maps[key].toString()
                    }
                }

                fis.close()
                ois.close()

            } catch (e: Exception) {
                fis?.close()
            }
        }


        if (iMeiFiles.isNotEmpty()) {
            val result = getIMeiFromMetaXml(iMeiFiles[0])
            if (result.isNotEmpty()) {
                imei.add(result)
            } else {
                imei.add(value)
            }
        } else {
            //添加默认密码
            imei.add("1234567890ABCDEF")
            if (value != "") {
                imei.add(value)
            }
            imei.addAll(DeviceUtil.getIMEIs(context))
        }

        return imei
    }

    /**
     * 获取配置文件中的UIN
     */
    fun getUinsFromHistoryXml(cfgFile: File): ArrayList<String> {
        val list = arrayListOf<String>()
        try {
            val fis = ByteArrayInputStream(cfgFile.readBytes())
            val pullParser = Xml.newPullParser()
            pullParser.setInput(fis, "UTF-8")
            var event = pullParser.eventType
            val depth = pullParser.depth
            val type = pullParser.next()
            while ((type != XmlPullParser.END_TAG || pullParser
                    .depth > depth) && event != XmlPullParser.END_DOCUMENT
            ) {
                when (event) {
                    XmlPullParser.START_DOCUMENT -> {

                    }

                    XmlPullParser.START_TAG -> {
                        if ("string" == pullParser.name) {
                            val text = pullParser.nextText()
                            JLog.i(text)
                            list.add(text)
                        }
                    }

                    XmlPullParser.END_TAG -> {
                    }
                }
                event = pullParser.next()
            }

            fis.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return list
    }

    /**
     * 获取配置文件中的UIN
     */
    fun getUinsFromSystemConfigXml(cfgFile: File): ArrayList<String> {

        val list = arrayListOf<String>()
        try {
            val fis = ByteArrayInputStream(cfgFile.readBytes())
            val pullParser = Xml.newPullParser()
            pullParser.setInput(fis, "UTF-8")
            var event = pullParser.eventType
            val depth = pullParser.depth
            val type = pullParser.next()
            while ((type != XmlPullParser.END_TAG || pullParser
                    .depth > depth) && event != XmlPullParser.END_DOCUMENT
            ) {
                when (event) {
                    XmlPullParser.START_DOCUMENT -> {

                    }

                    XmlPullParser.START_TAG -> {
                        if ("int" == pullParser.name) {
                            val name = pullParser.getAttributeValue(0)
                            if (name == "default_uin") {
                                val uin = pullParser.getAttributeValue(1)
                                list.add(uin)
                                JLog.i("uin = $uin")
                            }
                        }
                    }

                    XmlPullParser.END_TAG -> {
                    }
                }
                event = pullParser.next()
            }

            fis.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return list
    }

    /**
     * 获取配置文件中的UIN
     */
    fun getUinsFromAuthInfoXml(cfgFile: File): ArrayList<String> {
        val list = arrayListOf<String>()
        var fis: ByteArrayInputStream? = null
        try {
            fis = ByteArrayInputStream(cfgFile.readBytes())
            val pullParser = Xml.newPullParser()
            pullParser.setInput(fis, "UTF-8")
            var event = pullParser.eventType
            val depth = pullParser.depth
            val type = pullParser.next()
            while ((type != XmlPullParser.END_TAG || pullParser
                    .depth > depth) && event != XmlPullParser.END_DOCUMENT
            ) {
                when (event) {
                    XmlPullParser.START_DOCUMENT -> {

                    }

                    XmlPullParser.START_TAG -> {
                        if ("int" == pullParser.name) {
                            val name = pullParser.getAttributeValue(0)
                            if (name == "_auth_uin") {
                                val uin = pullParser.getAttributeValue(1)
                                list.add(uin)
                            }
                        }
                    }

                    XmlPullParser.END_TAG -> {
                    }
                }
                event = pullParser.next()
            }

            fis.close()
        } catch (e: Exception) {
            fis?.close()
            e.printStackTrace()
        }

        return list
    }


    /**
     * 获取DENTA_META文件里的imei
     */
    private fun getIMeiFromMetaXml(cfgFile: File): String {
        var result = ""
        try {
            val fis = ByteArrayInputStream(cfgFile.readBytes())
            val pullParser = Xml.newPullParser()
            pullParser.setInput(fis, "UTF-8")
            var event = pullParser.eventType
            val depth = pullParser.depth
            val type = pullParser.next()
            while ((type != XmlPullParser.END_TAG || pullParser
                    .depth > depth) && event != XmlPullParser.END_DOCUMENT
            ) {
                when (event) {
                    XmlPullParser.START_DOCUMENT -> {

                    }

                    XmlPullParser.START_TAG -> {
                        if ("string" == pullParser.name) {
                            val name = pullParser.getAttributeValue(0)
                            if (name == "IMEI_DENGTA") {
                                result = pullParser.nextText()
                            }
                        }
                    }

                    XmlPullParser.END_TAG -> {
                    }
                }
                event = pullParser.next()
            }

            fis.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return result
    }
}