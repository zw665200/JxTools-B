package com.wx.tools.controller

import android.app.Activity
import android.os.Build
import com.hyphenate.chat.ChatClient
import com.hyphenate.chat.ChatClient.ConnectionListener
import com.hyphenate.chat.ChatManager.MessageListener
import com.hyphenate.chat.Message
import com.hyphenate.helpdesk.Error
import com.hyphenate.helpdesk.callback.Callback
import com.hyphenate.helpdesk.easeui.util.IntentBuilder
import com.hyphenate.helpdesk.model.VisitorInfo
import com.wx.tools.bean.UserInfo
import com.wx.tools.utils.AppUtil
import com.wx.tools.utils.JLog
import com.wx.tools.utils.ToastUtil
import com.tencent.mmkv.MMKV


/**
 * @author Herr_Z
 * @description:
 * @date : 2021/3/8 16:06
 */
object IMManager {
    private var messageListener: MessageListener? = null

    fun register(name: String, success: () -> Unit, failed: (str: String?) -> Unit) {
        ChatClient.getInstance().register(name, Constant.SDK_DEFAULT_PASSWORD, object : Callback {
            override fun onSuccess() {
                success()
                JLog.i("register success")
            }

            override fun onError(code: Int, error: String?) {
                when (code) {
                    Error.USER_ALREADY_EXIST -> success()
                    Error.USER_ALREADY_LOGIN -> success()
                    else -> failed(error)
                }
            }

            override fun onProgress(progress: Int, status: String?) {
            }
        })
    }

    private fun checkLogin(): Boolean {
        return ChatClient.getInstance().isLoggedInBefore
    }

    /**
     * IM login
     * @param activity
     * @param name
     */
    fun login(activity: Activity, name: String, success: () -> Unit) {
        ChatClient.getInstance().login(name, Constant.SDK_DEFAULT_PASSWORD, object : Callback {
            override fun onSuccess() {
                JLog.i("login success")
                success()
            }

            override fun onError(code: Int, error: String?) {
                ToastUtil.show(activity, "登录客服服务器失败")
            }

            override fun onProgress(progress: Int, status: String?) {
            }
        })
    }


    /**
     * start conversation
     */
    fun startConversation(activity: Activity, name: String, menuDes: String) {
        if (checkLogin()) {
            val value = MMKV.defaultMMKV()?.decodeString("report") ?: ""
            val refund = if (value == "success") {
                "已提交退款申请"
            } else {
                "还未提交退款申请"
            }

            val userInfo = MMKV.defaultMMKV()?.decodeParcelable("userInfo", UserInfo::class.java)
            if (userInfo != null) {
                val date = userInfo.addtime * 1000

                val visitorInfo = VisitorInfo()
                visitorInfo.phone("${Build.BRAND}|${Build.MODEL}")
                visitorInfo.email("app版本" + activity.packageManager.getPackageInfo(activity.packageName, 0).versionName)
                visitorInfo.companyName("安卓" + Build.VERSION.RELEASE)
                visitorInfo.nickName(userInfo.nickname)
                visitorInfo.qq(userInfo.city)
                visitorInfo.description("$menuDes||$refund||${AppUtil.timeStamp2Date(date.toString(), null)}")

                val intent = IntentBuilder(activity)
                    .setServiceIMNumber(Constant.SDK_SERVICE_ID)
                    .setTitleName("在线客服")
                    .setVisitorInfo(visitorInfo)
                    .build()
                activity.startActivity(intent)
            } else {
                JLog.i("userInfo is empty")
            }

        } else {
            login(activity, name) { startConversation(activity, name, menuDes) }
        }
    }

    fun setConnectionListener() {
        ChatClient.getInstance().addConnectionListener(object : ConnectionListener {
            override fun onConnected() {
                //成功连接到服务器
            }

            override fun onDisconnected(errorcode: Int) {
                //errorcode的值
                //Error.USER_REMOVED 账号移除
                //Error.USER_LOGIN_ANOTHER_DEVICE 账号在其他地方登录
                //Error.USER_AUTHENTICATION_FAILED 账号密码错误
                //Error.USER_NOT_FOUND  账号找不到
            }
        })
    }

    fun setMessageListener(method: (() -> Unit)) {
        if (checkLogin()) {
            if (messageListener == null) {
                messageListener = object : MessageListener {
                    override fun onMessage(list: List<Message?>?) {
                        //收到普通消息
                        method()
                    }

                    override fun onCmdMessage(list: List<Message?>?) {
                        //收到命令消息，命令消息不存数据库，一般用来作为系统通知，例如留言评论更新，
                        //会话被客服接入，被转接，被关闭提醒
                    }

                    override fun onMessageStatusUpdate() {
                        //消息的状态修改，一般可以用来刷新列表，显示最新的状态
                    }

                    override fun onMessageSent() {
                        //发送消息后，会调用，可以在此刷新列表，显示最新的消息
                    }
                }
            }

            ChatClient.getInstance().chatManager().addMessageListener(messageListener)
        }
    }

    fun removeMessageListener() {
        if (checkLogin() && messageListener != null) {
            ChatClient.getInstance().chatManager().removeMessageListener(messageListener)
        }
    }


    /**
     * IM logout
     */
    fun logout() {
        if (checkLogin()) {
            ChatClient.getInstance().logout(true, object : Callback {
                override fun onSuccess() {

                }

                override fun onError(code: Int, error: String?) {
                }

                override fun onProgress(progress: Int, status: String?) {
                }
            })
        }
    }
}