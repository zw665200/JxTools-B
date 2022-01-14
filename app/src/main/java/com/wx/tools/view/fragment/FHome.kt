package com.wx.tools.view.fragment

import android.content.Context
import android.content.Intent
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baidu.mobads.action.ActionParam
import com.baidu.mobads.action.ActionType
import com.baidu.mobads.action.BaiduAction
import com.baidu.mobads.action.PrivacyStatus
import com.bumptech.glide.Glide
import com.tencent.mmkv.MMKV
import com.wx.tools.R
import com.wx.tools.adapter.DataAdapter
import com.wx.tools.bean.Resource
import com.wx.tools.bean.UserInfo
import com.wx.tools.controller.Constant
import com.wx.tools.controller.IMManager
import com.wx.tools.controller.PayManager
import com.wx.tools.controller.WxManager
import com.wx.tools.http.loader.TokenLoader
import com.wx.tools.http.loader.UserInfoLoader
import com.wx.tools.http.response.ResponseTransformer
import com.wx.tools.http.schedulers.SchedulerProvider
import com.wx.tools.utils.*
import com.wx.tools.view.activity.*
import com.wx.tools.view.base.BaseFragment
import com.wx.tools.view.views.AutoTextView
import com.wx.tools.view.views.WaveProgressView
import com.wx.tools.view.views.WaveView
import kotlinx.android.synthetic.main.item_heart.view.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.util.*

open class FHome : BaseFragment() {
    private lateinit var rv: RecyclerView
    private lateinit var notice: AutoTextView
    private lateinit var rVAdapter: DataAdapter<Resource>
    private var mainPics = mutableListOf<Resource>()
    private lateinit var timer: CountDownTimer
    private lateinit var progressTimer: CountDownTimer
    private var position = 0
    private lateinit var waveProgressView: WaveView
    private lateinit var userService: ImageView
    private lateinit var mine: ImageView
    private lateinit var robot: ImageView
    private lateinit var phone: TextView
    private lateinit var brand: TextView
    private var lastClickTime = 0L
    private var prepared = true
    private var mmkv = MMKV.defaultMMKV()

    override fun initView(inflater: LayoutInflater): View {
        val rootView = inflater.inflate(R.layout.f_home, null)
        rv = rootView.findViewById(R.id.ry_billboard)
        notice = rootView.findViewById(R.id.tv_notice)
        waveProgressView = rootView.findViewById(R.id.waveView)
        mine = rootView.findViewById(R.id.mine)
        userService = rootView.findViewById(R.id.user_service)
        phone = rootView.findViewById(R.id.phone_status)
        brand = rootView.findViewById(R.id.phone_brand)

        mine.setOnClickListener { toMinePage() }
        userService.setOnClickListener { toCustomerServicePage() }
        initNotice()
        initMainRecycleView()

        return rootView
    }

    override fun initData() {

        mainPics.clear()
        mainPics.add(Resource("wechat", R.drawable.wechat, "微信聊天恢复"))
        mainPics.add(Resource("friends", R.drawable.friends, "微信好友恢复"))
        mainPics.add(Resource("doc", R.drawable.doc, "微信文档恢复"))
        mainPics.add(Resource("video", R.drawable.video, "微信视频恢复"))
        mainPics.add(Resource("pic", R.drawable.pic, "微信图片恢复"))
        mainPics.add(Resource("audio", R.drawable.voice, "微信语音恢复"))
        mainPics.add(Resource("bill", R.drawable.bill, "微信账单恢复"))
        mainPics.add(Resource("delete", R.drawable.delete, "微信彻底删除"))

        checkPermission()
    }

    override fun onResume() {
        super.onResume()
        initCustomerService()
        initProgressBar()
        initCurrentStatus()
        checkPay(activity!!)
    }


    override fun click(v: View?) {
    }

    private fun initNotice() {
        timer = object : CountDownTimer(4000 * 1000L, 4000) {
            override fun onFinish() {

            }

            override fun onTick(millisUntilFinished: Long) {
                val str = WxManager.getInstance(activity!!).getRecoveryUser()
                notice.setText(str, ContextCompat.getColor(activity!!, R.color.color_dark_grey))
                if (position == 2) {
                    position = 0
                } else {
                    position++
                }
            }
        }

        timer.start()
    }

    private fun initCustomerService() {
        IMManager.setMessageListener {
            AppUtil.sendNotification(activity, Constant.Notification_title, Constant.Notification_content)
        }
    }

    private fun initProgressBar() {
        //动态调整进度条的大小
        val width = AppUtil.getScreenWidth(activity)
        val height = AppUtil.getScreenHeight(activity)

        val layoutParam = waveProgressView.layoutParams
        layoutParam.width = height * 7 / 32
        layoutParam.height = height * 7 / 32
        waveProgressView.layoutParams = layoutParam

        if (!prepared) return

        val total = DeviceUtil.getTotalExternalMemorySize()
        val free = DeviceUtil.getFreeSpace()
        val percent = 100 * (total - free) / total
        val totalGB = String.format("%.2f", total.toFloat() / 1024 / 1024 / 1024)
        val usedGB = String.format("%.2f", (total - free).toFloat() / 1024 / 1024 / 1024)
        val p = "已使用：${usedGB}GB   总空间：${totalGB}GB"
        phone.text = p

        if (Build.BRAND == "HUAWEI" || Build.BRAND == "HONOR") {
            val name = Dict.getHUAWEIName(Build.MODEL)
            if (name.isNullOrEmpty()) {
                val b = "欢迎您: ${Build.BRAND} ${Build.MODEL} 用户"
                brand.text = b
            } else {
                val b = "欢迎您: $name 用户"
                brand.text = b
            }
        } else {
            val b = "欢迎您: ${Build.BRAND} ${Build.MODEL} 用户"
            brand.text = b
        }

        progressTimer = object : CountDownTimer(4000, 50) {
            override fun onFinish() {
                waveProgressView.progress = percent.toInt()
                prepared = true
            }

            override fun onTick(millisUntilFinished: Long) {
                prepared = false
                waveProgressView.progress = ((4000 - millisUntilFinished) * percent / 4000).toInt()
            }
        }

        progressTimer.start()
    }

    private fun initCurrentStatus() {
        Constant.CURRENT_BACKUP_TIME = mmkv?.decodeLong("backup_time")
        Constant.CURRENT_BACKUP_PATH = mmkv?.decodeString("backup_path")
    }

    private fun initMainRecycleView() {
        rv.layoutManager = GridLayoutManager(activity, 2)

        rVAdapter = DataAdapter.Builder<Resource>()
            .setData(mainPics)
            .setLayoutId(R.layout.item_heart)
            .addBindView { itemView, itemData ->
                Glide.with(this).load(itemData.icon).into(itemView.iv_icon)
                itemView.tv_name.text = itemData.name
                itemView.setOnClickListener {
                    if (lastClickTime == 0L) {
                        lastClickTime = System.currentTimeMillis()
                    } else {
                        if (System.currentTimeMillis() - lastClickTime < 1000) return@setOnClickListener
                    }

                    lastClickTime = System.currentTimeMillis()

                    checkPermissions {
                        when (itemData.type) {
                            "wechat" -> checkPay(1, itemData.type)
                            "friends" -> checkPay(2, itemData.type)
                            "doc" -> goDocRecovery()
                            "video" -> goVideoRecovery()
                            "pic" -> goPicRecovery()
                            "audio" -> goAudioRecovery()
                            "bill" -> checkSinglePay(Constant.BILL)
                            "delete" -> checkSinglePay(Constant.DELETE)
                        }
                    }

                }
            }
            .create()

        rv.adapter = rVAdapter
        rVAdapter.notifyDataSetChanged()
    }

    private fun checkPermission() {
        if (Constant.CLIENT_TOKEN == "") {
            checkPermissions {
                if (!RomUtil.isOppo()) {
                    BaiduAction.setPrivacyStatus(PrivacyStatus.AGREE)
                }
                getAccessToken()
            }
        } else {
            checkPay(activity!!)
        }
    }

    private fun checkPay(context: Context) {
        if (Constant.CLIENT_TOKEN == "") return
        PayManager.getInstance().getPayStatus(context, Constant.COM) {
            when (it.serverExpire) {
                0 -> {
                    val pack = it.packDetail
                    if (pack.isEmpty()) {
                        mmkv?.encode("recovery", 110)
                        return@getPayStatus
                    }

                    if (pack.size == 1) {
                        mmkv?.encode("recovery", 111)
                        return@getPayStatus
                    }
                }

                else -> mmkv?.encode("recovery", 0)
            }
        }
    }

    private fun getAccessToken() {
        val userInfo = mmkv?.decodeParcelable("userInfo", UserInfo::class.java)
        if (userInfo != null) {
            Constant.CLIENT_TOKEN = userInfo.client_token
            Constant.USER_NAME = userInfo.nickname
            Constant.USER_ID = userInfo.id.toString()
            return
        }

        launch(Dispatchers.IO) {
            TokenLoader.getToken(activity!!)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    getUserInfo(it.questToken)
                }, {
                    ToastUtil.show(activity!!, "请求失败，请检查网络")
                })
        }
    }

    private fun getUserInfo(token: String) {
        launch(Dispatchers.IO) {
            UserInfoLoader.getUser(token)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    if (it.isNotEmpty()) {
                        Constant.CLIENT_TOKEN = it[0].client_token
                        Constant.USER_NAME = it[0].nickname
                        Constant.USER_ID = it[0].id.toString()

                        val userInfo = UserInfo(
                            it[0].id,
                            it[0].nickname,
                            it[0].user_type,
                            it[0].addtime,
                            it[0].last_logintime,
                            it[0].login_ip,
                            it[0].popularize_id,
                            it[0].pop_name,
                            it[0].client_token,
                            it[0].city
                        )

                        mmkv?.encode("userInfo", userInfo)

                        JLog.i("baidu active")

                        //active upload
                        if (!RomUtil.isOppo()) {
                            BaiduAction.logAction(ActionType.REGISTER)

//                            val actionParam = JSONObject()
//                            actionParam.put(ActionParam.Key.PURCHASE_MONEY, 100)
//                            BaiduAction.logAction(ActionType.PURCHASE, actionParam)
                        }

                        checkPay(activity!!)

                        //IM register
                        IMManager.register(it[0].nickname, {}, {})
                    }

                }, {
                    JLog.i("error = ${it.message}")
                })
        }
    }

    private fun checkPay(serviceId: Int, type: String) {
        PayManager.getInstance().checkRecoveryPay(activity!!, serviceId) {
            if (it) {
                JLog.i("it = $it")
                when (type) {
                    "wechat" -> goWechatRecovery()
                    "friends" -> goContactRecovery()
                }
            } else {
                toPayPage(serviceId)
            }
        }
    }

    private fun checkSinglePay(serviceCode: String) {
        when (serviceCode) {
            Constant.BILL -> {
                PayManager.getInstance().checkBillPay(activity!!) {
                    if (it) {
                        goBillRecovery()
                    } else {
                        toSinglePayPage(serviceCode)
                    }
                }
            }

            Constant.DELETE -> {
                PayManager.getInstance().checkDeletePay(activity!!) {
                    if (it) {
                        goDeleteRecovery()
                    } else {
                        toSinglePayPage(serviceCode)
                    }
                }
            }
        }
    }

    private fun toPayPage(serviceId: Int) {
        val intent = Intent()
        intent.setClass(activity!!, PayActivity::class.java)
        intent.putExtra("serviceId", serviceId)
        startActivity(intent)
    }

    private fun toSinglePayPage(serviceCode: String) {
        val intent = Intent()
        intent.setClass(activity!!, SinglePayActivity::class.java)
        intent.putExtra("serviceCode", serviceCode)
        startActivity(intent)
    }

    private fun toCustomerServicePage() {
        val intent = Intent()
        intent.setClass(activity!!, CustomerServiceActivity::class.java)
        startActivity(intent)
    }

    private fun toMinePage() {
        val intent = Intent()
        intent.setClass(activity!!, MineActivity::class.java)
        startActivity(intent)
    }

    private fun goWechatRecovery() {
        val intent = Intent()
        intent.setClass(context!!, RecoveryTipsActivity::class.java)
        intent.putExtra("serviceId", 1)
        activity?.startActivity(intent)
    }

    private fun goContactRecovery() {
        val intent = Intent()
        intent.setClass(context!!, RecoveryTipsActivity::class.java)
        intent.putExtra("serviceId", 2)
        activity?.startActivity(intent)
    }

    private fun goDocRecovery() {
        val intent = Intent()
        intent.setClass(context!!, WeChatDocRecoveryActivity::class.java)
        intent.putExtra("serviceId", 3)
        activity?.startActivity(intent)
    }

    private fun goVideoRecovery() {
        val intent = Intent()
        intent.setClass(context!!, WeChatVideoRecoveryActivity::class.java)
        intent.putExtra("serviceId", 4)
        activity?.startActivity(intent)
    }

    private fun goPicRecovery() {
        val intent = Intent()
        intent.setClass(context!!, WeChatPicsRecoveryActivity::class.java)
        intent.putExtra("serviceId", 5)
        activity?.startActivity(intent)
    }

    private fun goAudioRecovery() {
        val intent = Intent()
        intent.setClass(context!!, WeChatVoiceRecoveryActivity::class.java)
        intent.putExtra("serviceId", 6)
        activity?.startActivity(intent)
    }

    private fun goBillRecovery() {
        val intent = Intent()
        intent.setClass(context!!, WechatBillRecoveryActivity::class.java)
        intent.putExtra("serviceId", 13)
        activity?.startActivity(intent)
    }

    private fun goDeleteRecovery() {
        val intent = Intent()
        intent.setClass(context!!, WechatDeleteActivity::class.java)
        intent.putExtra("serviceId", 14)
        activity?.startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        progressTimer.cancel()
        cancel()
        IMManager.removeMessageListener()
        IMManager.logout()
    }
}