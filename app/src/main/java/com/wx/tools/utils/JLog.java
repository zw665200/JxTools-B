package com.wx.tools.utils;

import android.util.Log;

import com.wx.tools.controller.Constant;

public class JLog {

    /**
     * 调试开关
     */

    private static final String TAG = "QL";

    /**
     * 错误级别日志
     */
    public static void e(String tag, String log) {
        if (Constant.isDebug) {
            Log.e(tag, log);
        }
    }

    /**
     * 警告级别日志
     */
    public static void w(String tag, String log) {
        if (Constant.isDebug) {
            Log.w(tag, log);
        }
    }

    /**
     * 信息级别日志
     */
    public static void i(String tag, String log) {
        if (Constant.isDebug) {
            Log.i(tag, log);
        }
    }

    /**
     * 调试级别日志
     */
    public static void d(String tag, String log) {
        if (Constant.isDebug) {
            Log.d(tag, log);
        }
    }

    /**  */
    /**
     * 错误级别日志
     */
    public static void e(String log) {
        if (Constant.isDebug) {
            Log.e(TAG, log);
        }
    }

    /**
     * 警告级别日志
     */
    public static void w(String log) {
        if (Constant.isDebug) {
            Log.w(TAG, log);
        }
    }

    /**
     * 信息级别日志
     */
    public static void i(String log) {
        if (Constant.isDebug) {
            Log.i(TAG, log);
        }
    }

    /**
     * 调试级别日志
     */
    public static void d(String log) {
        if (Constant.isDebug) {
            Log.d(TAG, log);
        }
    }
}
