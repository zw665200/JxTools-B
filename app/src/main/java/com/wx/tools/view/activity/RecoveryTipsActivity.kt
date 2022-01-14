package com.wx.tools.view.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.wx.tools.R
import com.wx.tools.adapter.DataAdapter
import com.wx.tools.bean.FileBean
import com.wx.tools.callback.DialogCallback
import com.wx.tools.callback.FileDialogCallback
import com.wx.tools.controller.Constant
import com.wx.tools.controller.DBManager
import com.wx.tools.controller.WxManager
import com.wx.tools.utils.*
import com.wx.tools.view.base.BaseActivity
import com.wx.tools.view.views.*
import com.tencent.mmkv.MMKV
import kotlinx.android.synthetic.main.banner.view.*
import kotlinx.android.synthetic.main.pics_big_item.view.*
import kotlinx.coroutines.*

class RecoveryTipsActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var beginBackup: Button
    private lateinit var dialog: BackupFilesDialog
    private lateinit var beginAna: Button
    private var mainPics = mutableListOf<Int>()
    private lateinit var history: TextView
    private var serviceId: Int = 0
    private lateinit var mAdapter: DataAdapter<Int>
    private lateinit var vp: ViewPager2
    private lateinit var title: TextView
    private lateinit var pwdDialog: HuaWeiPwdDialog
    private lateinit var authDialog: AuthDialog
    private lateinit var description: TextView


    override fun setLayout(): Int {
        return R.layout.a_wechat_recovery_tips
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        back.setOnClickListener { finish() }

        beginBackup = findViewById(R.id.begin_backup)
        beginAna = findViewById(R.id.go_ana)
        beginBackup.setOnClickListener { goBackup() }
        beginAna.setOnClickListener { getBackups() }
        vp = findViewById(R.id.vp_banner)

        history = findViewById(R.id.recovery_history)
        history.setOnClickListener { getBackups() }

        title = findViewById(R.id.tv_tip_title)
        description = findViewById(R.id.recovery_tips_description)

    }

    override fun initData() {
        serviceId = intent.getIntExtra("serviceId", 0)
        when (serviceId) {
            1 -> title.text = getString(R.string.recovery_tips_title)
            2 -> title.text = getString(R.string.recovery_tips_title_1)
        }

        mainPics.clear()
        when (Constant.ROM) {
            Constant.ROM_EMUI -> {
                mainPics.add(R.mipmap.hwh_0)
                mainPics.add(R.mipmap.hwh_1)
                mainPics.add(R.mipmap.hwh_2)
                mainPics.add(R.mipmap.hwh_3)
                mainPics.add(R.mipmap.hwh_4)
                mainPics.add(R.mipmap.hwh_5)
                mainPics.add(R.mipmap.hwh_6)
                mainPics.add(R.mipmap.hwh_7)
                mainPics.add(R.mipmap.hwh_8)
                mainPics.add(R.mipmap.hwh_9)
                mainPics.add(R.mipmap.hwh_10)
                mainPics.add(R.mipmap.hwh_11)
                mainPics.add(R.mipmap.hwh_12)
                description.text = getString(R.string.recovery_tips_huawei_description)
            }

            Constant.ROM_MIUI -> {
                mainPics.add(R.mipmap.xm_1)
                mainPics.add(R.mipmap.xm_2)
                mainPics.add(R.mipmap.xm_3)
                mainPics.add(R.mipmap.xm_4)
                mainPics.add(R.mipmap.xm_5)
                mainPics.add(R.mipmap.xm_6)
                mainPics.add(R.mipmap.xm_7)
                mainPics.add(R.mipmap.xm_8)
                description.text = getString(R.string.recovery_tips_mi_description)
            }

            Constant.ROM_OPPO -> {
                mainPics.add(R.mipmap.o_1)
                mainPics.add(R.mipmap.o_2)
                mainPics.add(R.mipmap.o_3)
                mainPics.add(R.mipmap.o_4)
                mainPics.add(R.mipmap.o_5)
                description.text = getString(R.string.recovery_tips_oppo_description)
            }

            Constant.ROM_VIVO -> {
                mainPics.add(R.mipmap.vv_1)
                mainPics.add(R.mipmap.vv_2)
                mainPics.add(R.mipmap.vv_3)
                mainPics.add(R.mipmap.vv_4)
                mainPics.add(R.mipmap.vv_5)
                mainPics.add(R.mipmap.vv_6)
                mainPics.add(R.mipmap.vv_7)
                mainPics.add(R.mipmap.vv_8)
                mainPics.add(R.mipmap.vv_9)
                mainPics.add(R.mipmap.vv_10)
                mainPics.add(R.mipmap.vv_11)
                mainPics.add(R.mipmap.vv_12)
                description.text = getString(R.string.recovery_tips_vivo_description)
            }

            Constant.ROM_FLYME -> {
                mainPics.add(R.mipmap.mz_1)
                mainPics.add(R.mipmap.mz_2)
                mainPics.add(R.mipmap.mz_3)
                mainPics.add(R.mipmap.mz_4)
                mainPics.add(R.mipmap.mz_5)
                mainPics.add(R.mipmap.mz_6)
                description.text = getString(R.string.recovery_tips_flyme_description)
            }
        }

        initBannerViewPager()
        showDialog()

        WxManager.getInstance(this).checkBackupPath()
    }


    private fun initBannerViewPager() {
        vp.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        mAdapter = DataAdapter.Builder<Int>()
            .setData(mainPics)
            .setLayoutId(R.layout.banner)
            .addBindView { itemView, itemData ->
                Glide.with(this).load(itemData).into(itemView.iv_banner)
            }
            .create()

        vp.apply {
            offscreenPageLimit = 2
            (getChildAt(0) as RecyclerView).apply {
                val padding = resources.getDimensionPixelOffset(R.dimen.dp_15)
                // setting padding on inner RecyclerView puts overscroll effect in the right place
                setPadding(padding, 0, padding, 0)
                clipToPadding = false
            }
        }

        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(ScaleInTransformer())
        compositePageTransformer.addTransformer(MarginPageTransformer(resources.getDimension(R.dimen.dp_5).toInt()))
        vp.setPageTransformer(compositePageTransformer)
        vp.adapter = mAdapter
        mAdapter.notifyDataSetChanged()
        vp.setCurrentItem(0, false)

    }


    private fun getBackups() {
        launch(Dispatchers.IO) {
            val backupFiles = WxManager.getInstance(this@RecoveryTipsActivity).getBackupFiles()
            launch(Dispatchers.Main) {
                if (backupFiles.isNotEmpty()) {
                    dialog.cancel()
                    dialog.setData(backupFiles)
                    dialog.show()
                } else {
                    NotFindFileDialog(this@RecoveryTipsActivity).show()
                }
            }
        }
    }

    private fun goBackup() {
        when (Constant.ROM) {
            Constant.ROM_FLYME -> checkMeiZuBackup()
            Constant.ROM_MIUI -> startActivity(Intent(Settings.ACTION_SETTINGS))
            Constant.ROM_OPPO -> checkOppoBackup()
            Constant.ROM_EMUI -> checkHuaweiBackup()
            else -> ToastUtil.showShort(this, "请参照教程操作")
        }
    }


    private fun showPwdDialog(c: Context, file: FileBean) {

        pwdDialog = HuaWeiPwdDialog(c, object : FileDialogCallback {
            override fun onSuccess(str: String) {
                toRecoveryPage(file, str)
                pwdDialog.cancel()
            }

            override fun onCancel() {
            }
        })
        pwdDialog.show()
    }


    private fun showDialog() {
        dialog = BackupFilesDialog(this, object : DialogCallback {
            override fun onSuccess(file: FileBean) {
                launch(Dispatchers.IO) {
                    Constant.CURRENT_BACKUP_TIME = file.date
                    val accountList = DBManager.getAccountsBySrcTime(this@RecoveryTipsActivity, file.date)
                    launch(Dispatchers.Main) {
                        if (accountList.isNullOrEmpty()) {
                            JLog.i("can not find account record")
                            dialog.dismiss()
                            checkAuth(file)
                        } else {
                            toChooseAccountPage(file)
                        }
                    }
                }
            }

            override fun onCancel() {
                dialog.cancel()
            }
        })
    }

    private fun checkAuth(child: FileBean) {
        val kv = MMKV.defaultMMKV()
        val code = kv?.decodeBool("auth")
        if (code == null || !code) {
            authDialog = AuthDialog(this, object : DialogCallback {
                override fun onSuccess(file: FileBean) {
                    kv?.encode("auth", true)
                    authDialog.cancel()
                    if (Constant.ROM == Constant.ROM_EMUI) {
                        showPwdDialog(this@RecoveryTipsActivity, child)
                    } else {
                        toRecoveryPage(child)
                    }
                }

                override fun onCancel() {
                    authDialog.cancel()
                }
            })

            authDialog.show()

        } else {
            if (Constant.ROM == Constant.ROM_EMUI) {
                showPwdDialog(this, child)
            } else {
                toRecoveryPage(child)
            }
        }
    }


    private fun toRecoveryPage(file: FileBean) {
        val intent = Intent()
        intent.setClass(this@RecoveryTipsActivity, WeChatRecoveryActivity::class.java)
        intent.putExtra("filePath", file.path)
        intent.putExtra("fileDate", file.date)
        intent.putExtra("serviceId", serviceId)
        startActivity(intent)
    }


    private fun toRecoveryPage(file: FileBean, pwd: String) {
        val intent = Intent()
        intent.setClass(this@RecoveryTipsActivity, HuaWeiRecoveryActivity::class.java)
        intent.putExtra("filePath", file.path)
        intent.putExtra("fileDate", file.date)
        intent.putExtra("serviceId", serviceId)
        intent.putExtra("pwd", pwd)
        startActivity(intent)
    }

    private fun toChooseAccountPage(file: FileBean) {
        val intent = Intent()
        intent.setClass(this@RecoveryTipsActivity, ChooseAccountActivity::class.java)
        intent.putExtra("filePath", file.path)
        intent.putExtra("serviceId", serviceId)
        startActivity(intent)
    }

    private fun checkHuaweiBackup() {
        var packName = "com.huawei.hisuite"
        if (AppUtil.checkPackageInfo(this, packName)) {
            ToastUtil.showShort(this, "请连接你的电脑端华为手机助手并参照教程操作")
            val version = AppUtil.getPackageVersionCode(this, packName)
            JLog.i("version = $version")

            val intent = packageManager.getLaunchIntentForPackage(packName)
            if (intent != null) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            } else {
                ToastUtil.showShort(this, "请连接你的电脑端华为手机助手并参照教程操作")
            }

        } else {
            packName = "com.huawei.KoBackup"
            if (AppUtil.checkPackageInfo(this, packName)) {
                ToastUtil.showShort(this, "请升级你的华为手机助手并参照教程操作")
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_LAUNCHER)
                intent.component = ComponentName(packName, "com.huawei.KoBackup.InitializeActivity")
                startActivity(intent)
            } else {
                ToastUtil.showShort(this, "请参照教程操作")
            }
        }
    }

    private fun checkOppoBackup() {
        val packName = "com.coloros.backuprestore"
        if (AppUtil.checkPackageInfo(this, packName)) {
            val version = AppUtil.getPackageVersionCode(this, packName)
            JLog.i("version = $version")
            if (version > 165) {
                val intent = Intent(Intent.ACTION_DELETE)
                intent.data = Uri.parse("package:$packName")
                startActivityForResult(intent, 0x2)
            } else {
                val intent = packageManager.getLaunchIntentForPackage(packName)
                if (intent != null) {
                    startActivity(intent)
                }
            }
        } else {
            ToastUtil.showShort(this, "请参照教程操作")
        }
    }

    private fun checkMeiZuBackup() {
        val packName = "com.meizu.backup"
        if (AppUtil.checkPackageInfo(this, packName)) {
            ToastUtil.showShort(this, "请参照教程操作")
            val version = AppUtil.getPackageVersionCode(this, packName)
            JLog.i("version = $version")

//            val intent = packageManager.getLaunchIntentForPackage(packName)
//            if (intent != null) {
//                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                startActivity(intent)
//            } else {
//                ToastUtil.showShort(this, "请参照教程操作")
//            }
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.component = ComponentName(packName, "com.meizu.backup.MainActivity")
        } else {
            ToastUtil.showShort(this, "请参照教程操作")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x1) {
            JLog.i("卸载回调")
            val packName = "com.huawei.KoBackup"
            if (AppUtil.checkPackageInfo(this, packName)) {
                val version = AppUtil.getPackageVersionCode(this, packName)
                JLog.i("version = $version")
                if (version == 80002301) {
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.addCategory(Intent.CATEGORY_LAUNCHER)
                    intent.component = ComponentName(packName, "com.huawei.KoBackup.InitializeActivity")
                    startActivity(intent)
                }
            } else {
                WxManager.getInstance(this).installKoBackupApk(this)
            }
        }

        if (requestCode == 0x2) {
            JLog.i("卸载回调")
            val packName = "com.coloros.backuprestore"
            if (AppUtil.checkPackageInfo(this, packName)) {
                val version = AppUtil.getPackageVersionCode(this, packName)
                JLog.i("version = $version")
                if (version == 165) {
                    val intent = packageManager.getLaunchIntentForPackage(packName)
                    if (intent != null) {
                        startActivity(intent)
                    }
                }
            }
        }

        if (requestCode == 0x10086) {
            JLog.i("安装未知应用权限回调")
            if (Build.VERSION.SDK_INT >= 26 && packageManager.canRequestPackageInstalls()) {
                WxManager.getInstance(this).installKoBackupApk(this)
            }
        }
    }

}