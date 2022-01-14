//package com.jxtools.wx.view.views
//
//import android.app.Activity
//import android.app.Dialog
//import android.content.Context
//import android.text.Html
//import android.view.Gravity
//import android.view.LayoutInflater
//import android.view.View
//import android.view.WindowManager
//import android.widget.*
//import com.aidou.video.component.Framework
//import com.aidou.video.manager.PreferenceManager
//import com.jxtools.wx.R
//import com.wang.avi.AVLoadingIndicatorView
//
///**
// * Created by fish on 17-7-6.
// */
//
////Dialogs 后续需要优化
//class Dialogs {
//    companion object {
//        fun create(ctx: Context, layoutID: Int, isCancel: Boolean): Dialog {
//            return Dialog(ctx, R.style.app_dialog).apply {
//                setCancelable(false)
//                setCanceledOnTouchOutside(isCancel)
//                setContentView(layoutID)
//            }
//        }
//
//        fun create(activity: Activity, layoutID: Int, isCancel: Boolean): Dialog {
//            return Dialog(activity, R.style.app_dialog).apply {
//                setCancelable(false)
//                setCanceledOnTouchOutside(isCancel)
//                setContentView(layoutID)
//
//            }
//        }
//
//        fun createWait(ctx: Context): Dialog {
//            return Dialog(ctx, R.style.staticDialog).apply {
//                val view = LayoutInflater.from(ctx).inflate(R.layout.d_waiting, null)
//                setContentView(view)
//                view.findViewById<AVLoadingIndicatorView>(R.id.prograssbar).apply {
//                    this.show()
//                }
//                setCanceledOnTouchOutside(false)
//            }
//        }
//
//        fun limitL(a: Long, b: Long) = if (a > b) a - b else 0
//        fun Dialog.fullShow() {
//            window!!.decorView.setPadding(0, 0, 0, 0)
//            window!!.attributes = window?.attributes.apply {
//                gravity = Gravity.BOTTOM
//                width = WindowManager.LayoutParams.MATCH_PARENT
//                height = WindowManager.LayoutParams.MATCH_PARENT
//            }
//            show()
//        }
//
//    }
//
//
//
//    class Common {
//        private val dialog: Dialog
//        private var hideKey: String? = null
//
//
//        constructor(ctx: Context) {
//            dialog = create(ctx, R.layout.d_common, false)
//        }
//
//        fun getInstance(): Dialog {
//            return dialog
//        }
//
//        fun title(title: String) = dialog.findTV(R.id.tv_d_common_title).let { it?.text = title }
//        fun content(content: CharSequence) = dialog.findTV(R.id.tv_d_common_content).let { it?.text = Html.fromHtml(content.toString()) }
//        fun displayClose() = dialog.findViewById<View>(R.id.img_d_common_close).let { it.visibility = View.VISIBLE }
//        fun positive(keep: Boolean, po: () -> Unit) = dialog.findTV(R.id.tv_d_common_apply).let {
//            it?.visibility = View.VISIBLE
//            it?.setOnClickListener {
//                if (hideKey != null)
//                    PreferenceManager.ConfSPMgr.putValue(hideKey
//                            ?: "ex", dialog.findViewById<CheckBox>(R.id.cb_d_common_hide).isChecked)
//                po.invoke()
//                if (!keep)
//                    dialog.cancel()
//            }
//        }
//
//        fun positive(title: String, keep: Boolean, po: () -> Unit) = dialog.findTV(R.id.tv_d_common_apply).let {
//            it?.text = title
//            it?.visibility = View.VISIBLE
//            it?.setOnClickListener {
//                if (hideKey != null)
//                    PreferenceManager.ConfSPMgr.putValue(hideKey
//                            ?: "ex", dialog.findViewById<CheckBox>(R.id.cb_d_common_hide).isChecked)
//                po.invoke()
//                if (!keep)
//                    dialog.cancel()
//            }
//        }
//
//        fun hideAble(spKey: String) = dialog.findViewById<CheckBox>(R.id.cb_d_common_hide).let {
//            it.visibility = View.VISIBLE
//            hideKey = spKey
//        }
//
//        fun positive(po: () -> Unit) = positive(false, po)
//
//        fun negative(po: () -> Unit) = dialog.findTV(R.id.tv_d_common_cancel).let {
//            it?.visibility = View.VISIBLE
//            it?.setOnClickListener {
//                po.invoke()
//                dialog.cancel()
//            }
//        }
//
//        fun negative(title: String, po: () -> Unit) = dialog.findTV(R.id.tv_d_common_cancel).let {
//            it?.text = title
//            it?.visibility = View.VISIBLE
//            it?.setOnClickListener {
//                po.invoke()
//                dialog.cancel()
//            }
//        }
//
//        fun show() {
//            if (hideKey == null || !PreferenceManager.ConfSPMgr.getBooleanValue(hideKey ?: ""))
//                dialog.show()
//        }
//
//        fun disMiss() {
//            dialog.dismiss()
//        }
//
//        private fun Dialog.findTV(tvID: Int) = findViewById<TextView>(tvID).apply { visibility = View.VISIBLE }
//    }
//
//
//}
//package com.jxtools.wx.view.views
//
//import android.app.Activity
//import android.app.Dialog
//import android.content.Context
//import android.text.Html
//import android.view.Gravity
//import android.view.LayoutInflater
//import android.view.View
//import android.view.WindowManager
//import android.widget.*
//import com.aidou.video.component.Framework
//import com.aidou.video.manager.PreferenceManager
//import com.jxtools.wx.R
//import com.wang.avi.AVLoadingIndicatorView
//
///**
// * Created by fish on 17-7-6.
// */
//
////Dialogs 后续需要优化
//class Dialogs {
//    companion object {
//        fun create(ctx: Context, layoutID: Int, isCancel: Boolean): Dialog {
//            return Dialog(ctx, R.style.app_dialog).apply {
//                setCancelable(false)
//                setCanceledOnTouchOutside(isCancel)
//                setContentView(layoutID)
//            }
//        }
//
//        fun create(activity: Activity, layoutID: Int, isCancel: Boolean): Dialog {
//            return Dialog(activity, R.style.app_dialog).apply {
//                setCancelable(false)
//                setCanceledOnTouchOutside(isCancel)
//                setContentView(layoutID)
//
//            }
//        }
//
//        fun createWait(ctx: Context): Dialog {
//            return Dialog(ctx, R.style.staticDialog).apply {
//                val view = LayoutInflater.from(ctx).inflate(R.layout.d_waiting, null)
//                setContentView(view)
//                view.findViewById<AVLoadingIndicatorView>(R.id.prograssbar).apply {
//                    this.show()
//                }
//                setCanceledOnTouchOutside(false)
//            }
//        }
//
//        fun limitL(a: Long, b: Long) = if (a > b) a - b else 0
//        fun Dialog.fullShow() {
//            window!!.decorView.setPadding(0, 0, 0, 0)
//            window!!.attributes = window?.attributes.apply {
//                gravity = Gravity.BOTTOM
//                width = WindowManager.LayoutParams.MATCH_PARENT
//                height = WindowManager.LayoutParams.MATCH_PARENT
//            }
//            show()
//        }
//
//    }
//
//
//
//    class Common {
//        private val dialog: Dialog
//        private var hideKey: String? = null
//
//
//        constructor(ctx: Context) {
//            dialog = create(ctx, R.layout.d_common, false)
//        }
//
//        fun getInstance(): Dialog {
//            return dialog
//        }
//
//        fun title(title: String) = dialog.findTV(R.id.tv_d_common_title).let { it?.text = title }
//        fun content(content: CharSequence) = dialog.findTV(R.id.tv_d_common_content).let { it?.text = Html.fromHtml(content.toString()) }
//        fun displayClose() = dialog.findViewById<View>(R.id.img_d_common_close).let { it.visibility = View.VISIBLE }
//        fun positive(keep: Boolean, po: () -> Unit) = dialog.findTV(R.id.tv_d_common_apply).let {
//            it?.visibility = View.VISIBLE
//            it?.setOnClickListener {
//                if (hideKey != null)
//                    PreferenceManager.ConfSPMgr.putValue(hideKey
//                            ?: "ex", dialog.findViewById<CheckBox>(R.id.cb_d_common_hide).isChecked)
//                po.invoke()
//                if (!keep)
//                    dialog.cancel()
//            }
//        }
//
//        fun positive(title: String, keep: Boolean, po: () -> Unit) = dialog.findTV(R.id.tv_d_common_apply).let {
//            it?.text = title
//            it?.visibility = View.VISIBLE
//            it?.setOnClickListener {
//                if (hideKey != null)
//                    PreferenceManager.ConfSPMgr.putValue(hideKey
//                            ?: "ex", dialog.findViewById<CheckBox>(R.id.cb_d_common_hide).isChecked)
//                po.invoke()
//                if (!keep)
//                    dialog.cancel()
//            }
//        }
//
//        fun hideAble(spKey: String) = dialog.findViewById<CheckBox>(R.id.cb_d_common_hide).let {
//            it.visibility = View.VISIBLE
//            hideKey = spKey
//        }
//
//        fun positive(po: () -> Unit) = positive(false, po)
//
//        fun negative(po: () -> Unit) = dialog.findTV(R.id.tv_d_common_cancel).let {
//            it?.visibility = View.VISIBLE
//            it?.setOnClickListener {
//                po.invoke()
//                dialog.cancel()
//            }
//        }
//
//        fun negative(title: String, po: () -> Unit) = dialog.findTV(R.id.tv_d_common_cancel).let {
//            it?.text = title
//            it?.visibility = View.VISIBLE
//            it?.setOnClickListener {
//                po.invoke()
//                dialog.cancel()
//            }
//        }
//
//        fun show() {
//            if (hideKey == null || !PreferenceManager.ConfSPMgr.getBooleanValue(hideKey ?: ""))
//                dialog.show()
//        }
//
//        fun disMiss() {
//            dialog.dismiss()
//        }
//
//        private fun Dialog.findTV(tvID: Int) = findViewById<TextView>(tvID).apply { visibility = View.VISIBLE }
//    }
//
//
//}
