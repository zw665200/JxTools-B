package com.wx.tools.controller

import android.util.Xml
import com.wx.tools.bean.Uin
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream

class UinXmlSerializer {

    companion object {
        public fun getUin(steam: InputStream): Uin {
            var mList: List<Uin>
            var mUin: Uin = Uin(0)

            //创建XmlPullParser
            var parser: XmlPullParser = Xml.newPullParser();
            //解析文件输入流
            parser.setInput(steam, "utf-8")
            //得到当前的解析对象
            var eventType = parser.eventType


            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_DOCUMENT -> {

                    }

                    XmlPullParser.START_TAG -> {
                        var xppName = parser.name
                        if (xppName == "default_uin") {
                            val uin = Integer.parseInt(parser.getAttributeValue(0))
                            mUin = Uin(uin)
                        }
                    }

                    XmlPullParser.END_DOCUMENT -> {
                    }
                }
                eventType = parser.next()
            }

            return mUin
        }

    }
}