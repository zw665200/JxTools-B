package com.wx.tools.controller

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.alipay.sdk.app.PayTask
import com.tencent.mmkv.MMKV
import com.wx.tools.bean.*
import com.wx.tools.callback.PayCallback
import com.wx.tools.http.loader.*
import com.wx.tools.http.response.ResponseTransformer
import com.wx.tools.http.schedulers.SchedulerProvider
import com.wx.tools.utils.JLog
import com.wx.tools.utils.ToastUtil
import com.wx.tools.view.activity.FastPayActivity
import kotlinx.coroutines.*
import kotlin.concurrent.thread

class PayManager private constructor() : CoroutineScope by MainScope() {

    companion object {

        @Volatile
        private var instance: PayManager? = null

        fun getInstance(): PayManager {
            if (instance == null) {
                synchronized(PayManager::class) {
                    if (instance == null) {
                        instance = PayManager()
                    }
                }
            }

            return instance!!
        }
    }

    fun checkBillPay(context: Context, result: (Boolean) -> Unit) {
        val mmkv = MMKV.defaultMMKV()
        val value = mmkv?.decodeBool("bill", false)
        if (value == true) {
            result(true)
        } else {
            val service = MMKV.defaultMMKV()?.decodeParcelable(Constant.BILL, Price::class.java)
            if (service != null) {
                getSinglePayStatus(context, service.id) {
                    when (it.serverExpire) {
                        0 -> {
                            result(true)
                            mmkv?.encode("bill", true)
                        }

                        else -> {
                            result(false)
                            mmkv?.encode("bill", false)
                        }
                    }
                }
            }
        }
    }

    fun checkDeletePay(context: Context, result: (Boolean) -> Unit) {
        val mmkv = MMKV.defaultMMKV()
        val value = mmkv?.decodeBool("deletePay", false)
        if (value == true) {
            result(true)
        } else {
            val service = MMKV.defaultMMKV()?.decodeParcelable(Constant.DELETE, Price::class.java)
            if (service != null) {
                JLog.i("id = ${service.id}")
                getSinglePayStatus(context, service.id) {
                    when (it.serverExpire) {
                        0 -> {
                            result(true)
                            mmkv?.encode("deletePay", true)
                        }

                        else -> {
                            result(false)
                            mmkv?.encode("deletePay", false)
                        }
                    }
                }
            }
        }
    }

    /**
     * 检查恢复套餐
     * @param context
     * @param serviceId
     * @param result
     */
    fun checkRecoveryPay(context: Context, serviceId: Int, result: (Boolean) -> Unit) {
        val mmkv = MMKV.defaultMMKV()
        when (mmkv?.decodeInt("recovery")) {
            110 -> result(true)

            111 -> {
                if (serviceId == 1 || serviceId == 2) {
                    result(false)
                } else {
                    result(true)
                }
            }

            else -> {
                getPayStatus(context, Constant.COM) {
                    when (it.serverExpire) {
                        0 -> {
                            val pack = it.packDetail
                            if (pack.isEmpty()) {
                                result(true)
                                mmkv?.encode("recovery", 110)
                                return@getPayStatus
                            }

                            if (pack.size == 1) {
                                mmkv?.encode("recovery", 111)
                                if (serviceId == 1 || serviceId == 2) {
                                    result(false)
                                } else {
                                    result(true)
                                }
                                return@getPayStatus
                            }
                        }

                        else -> {
                            result(false)
                            mmkv?.encode("recovery", 0)
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    fun getPayStatus(context: Context, serviceCode: String, success: (PayStatus) -> Unit) {
        if (Constant.CLIENT_TOKEN == "") {
            val mmkv = MMKV.defaultMMKV()
            val userInfo = mmkv?.decodeParcelable("userInfo", UserInfo::class.java)
            if (userInfo != null) {
                Constant.CLIENT_TOKEN = userInfo.client_token
                Constant.USER_NAME = userInfo.nickname
            } else {
                return
            }
        }

        thread {
            val service = MMKV.defaultMMKV()?.decodeParcelable(serviceCode, Price::class.java)
            if (service != null) {

                PayStatusLoader.getPayStatus(service.id, Constant.CLIENT_TOKEN)
                    .compose(ResponseTransformer.handleResult())
                    .compose(SchedulerProvider.getInstance().applySchedulers())
                    .subscribe({
                        if (it.isEmpty()) return@subscribe
                        success(it[0])
                    }, {
                        ToastUtil.showShort(context, "获取支付状态失败")
                    })

            } else {
                ToastUtil.show(context, "获取套餐列表失败")
            }
        }
    }

    fun getSinglePayStatus(context: Context, serviceId: Int, payStatus: (PayStatus) -> Unit) {
        if (Constant.CLIENT_TOKEN == "") {
            val mmkv = MMKV.defaultMMKV()
            val userInfo = mmkv?.decodeParcelable("userInfo", UserInfo::class.java)
            if (userInfo != null) {
                Constant.CLIENT_TOKEN = userInfo.client_token
                Constant.USER_NAME = userInfo.nickname
            } else {
                return
            }
        }

        launch(Dispatchers.IO) {
            SinglePayStatusLoader.getPayStatus(serviceId, Constant.CLIENT_TOKEN)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    if (it.isEmpty()) return@subscribe
                    payStatus(it[0])
                }, {
                    ToastUtil.showShort(context, "获取支付状态失败")
                })
        }
    }


    /**
     * 支付宝支付
     */
    @SuppressLint("CheckResult")
    fun doAliPay(activity: Activity, serviceId: Int, callback: PayCallback) {
        thread {
            AliPayLoader.getOrderParam(serviceId)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    checkOrderStatus(activity, it, callback)
                }, {
                    ToastUtil.show(activity, "发起支付请求失败")
                })
        }
    }

    @SuppressLint("CheckResult")
    fun doFastPay(activity: Activity, serviceId: Int, callback: PayCallback) {
        thread {
            FastPayParamLoader.getOrderParam(serviceId)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    checkFastPay(activity, it, callback)
                }, {
                    ToastUtil.show(activity, "发起支付请求失败")
                })
        }
    }

    private fun checkOrderStatus(activity: Activity, order: AlipayParam, callback: PayCallback) {
        thread {
            JLog.i("param = ${order.body}")
            JLog.i("orderSn = ${order.orderSn}")

            val task = PayTask(activity)
            val result = task.payV2(order.body, true)
            val res = PayResult(result)
            val resultStatus = res.resultStatus

            if (resultStatus == "9000") {
                JLog.i("alipay success")

                callback.progress(order.orderSn)
                callback.success()

//                OrderDetailLoader.getOrderStatus(order.orderSn)
////                OrderDetailLoader.getOrderStatus("method=alipay.trade.app.pay&app_id=2021002116609108×tamp=2021-04-26+11%3A25%3A03&format=json&version=1.0&alipay_sdk=alipay-easysdk-php-2.0.0&charset=UTF-8&sign_type=RSA2&biz_content=%7B%22subject%22%3A%22%E6%95%B0%E6%8D%AE%E6%9C%8D%E5%8A%A1%22%2C%22out_trade_no%22%3A%22QL2021042611250382487%22%2C%22total_amount%22%3A0.16000000000000003%7D¬ify_url=http%3A%2F%2Fapi.ql-recovery.com%2FpayNotify&sign=dDQtJrD1QDoKwxk9%2FXUssYvWO15kGRFU5LjfBeO40sCDpfnYBhJ0QaLmetHVZ6p10vBgF5e0%2FMMg5PFvui6mD8J%2Ff7y7G2RNB5wCo5z0dWAZr8WvyftE6paWfluQ0%2Fs%2FrFfvvkasIhSf37c6MQysUfdcGbJUvZzjgLO8h39PMPp0cSQK5zT26IYwzLcVudSg7EEU4Csm%2FvThVONVcBBXt4G4N1hU1ZjICxkC3YOidS2WTllnKOELe7tY4ApDhxwbo0eQ4Xh5%2Bqqj4oeMd6ZVPbHYcJ%2Bb4WlUak5HVEvozExiMLRUB%2FgCVYw5PGuqEEWrqfSbOzy%2Fu1o9sNYuSbw7mQ%3D%3D")
//                    .compose(ResponseTransformer.handleResult())
//                    .compose(SchedulerProvider.getInstance().applySchedulers())
//                    .subscribe({
//                        if (it.order_sn != order.orderSn) {
//                            return@subscribe
//                        }
//
//                        when (it.status) {
////                            "1" -> callback.success()
////                            "0" -> callback.failed("未支付")
////                            "2" -> callback.failed("退款中")
////                            "3" -> callback.failed("已退款")
////                            "4" -> callback.failed("已取消")
//                        }
//                    }, {
//                        JLog.i("${it.message}")
//                    })

            } else {
                //支付失败，也需要发起服务端校验
                JLog.i("alipay failed")

                callback.failed("已取消")

//                launch(Dispatchers.IO) {
//                    OrderCancelLoader.orderCancel(order.orderSn)
//                        .compose(ResponseTransformer.handleResult())
//                        .compose(SchedulerProvider.getInstance().applySchedulers())
//                        .subscribe({}, {
//                            JLog.i("${it.message}")
//                        })
//                }
            }
        }

    }

    private fun checkFastPay(activity: Activity, order: FastPayParam, callback: PayCallback) {
        callback.progress(order.orderSn)

        val page = order.body.url
//        JLog.i("cd = ${page.orderCd}")
//        JLog.i("sign = ${page.sign}")


        //alipay
//        val intent = Intent()
//        intent.setClass(activity, FastPayActivity::class.java)
//        intent.putExtra("title", "支付")
//        intent.putExtra("page", page)
//        activity.startActivity(intent)

        val url = "alipayqr://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode=$page"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        activity.startActivity(intent)

        //wechat pay
//        val sb = StringBuffer()
//        sb.append("orderCd=")
//        sb.append(page.orderCd)
//        sb.append("&")
//        sb.append("sign=")
//        sb.append(page.sign)
//        sb.append("&")
//        val api = WXAPIFactory.createWXAPI(activity, Constant.TENCENT_APP_ID)
//        val req = WXLaunchMiniProgram.Req()
//        req.userName = Constant.TENCENT_MINI_PROGRAM_APP_ID
//        req.path = "pages/index/index?$sb"
//        req.miniprogramType = WXLaunchMiniProgram.Req.MINIPTOGRAM_TYPE_RELEASE
//        api.sendReq(req)

    }

}