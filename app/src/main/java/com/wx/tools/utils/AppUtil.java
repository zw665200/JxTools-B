package com.wx.tools.utils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import com.wx.tools.BuildConfig;
import com.wx.tools.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppUtil {

    private final static String[] hexDigits = {
            "0", "1", "2", "3", "4", "5", "6", "7",
            "8", "9", "a", "b", "c", "d", "e", "f"};


    public static boolean isDebugger(Context context) {
        boolean debuggable = false;
        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo appinfo = pm.getApplicationInfo(context.getPackageName(), 0);
            debuggable = (0 != (appinfo.flags & ApplicationInfo.FLAG_DEBUGGABLE));
        } catch (PackageManager.NameNotFoundException e) {
            /*debuggable variable will remain false*/
        }
        return debuggable;
    }

    public static String MD5Encode(String origin) {
        String resultString = null;
        try {
            resultString = new String(origin);
            MessageDigest md = MessageDigest.getInstance("MD5");
            resultString = byteArrayToHexString(md.digest(resultString.getBytes()));
        } catch (Exception ex) {

        }
        return resultString;
    }

    public static String MD5Encode(byte[] origin) {
        String resultString = null;
        try {
            resultString = new String(origin);
            MessageDigest md = MessageDigest.getInstance("MD5");
            resultString = byteArrayToHexString(md.digest(resultString.getBytes()));
        } catch (Exception ex) {

        }
        return resultString;
    }

    public static String byteArrayToHexString(byte[] b) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }


    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n = 256 + n;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    /**
     * 时间戳转日期
     *
     * @param timeStamp
     * @return
     */
    public static String timeStamp2Date(String timeStamp, String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            pattern = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.CHINA);
        return sdf.format(new Date(Long.parseLong(timeStamp)));
    }

    /**
     * 时间戳转日期
     *
     * @param date
     * @return
     */
    public static long date2TimeStamp(String date) {
        Date d;
        long timeStamp = 0;
        try {
            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            d = sf.parse(date);// 日期转换为时间戳
            timeStamp = d.getTime();
        } catch (ParseException e) {
            return 0;
        }

        return timeStamp;
    }

    /**
     * 获得渠道值
     *
     * @param ctx
     * @param key
     * @return
     */
    public static String getAppMetaData(Context ctx, String key) {
        if (ctx == null || TextUtils.isEmpty(key)) {
            return null;
        }

        String resultData = null;
        try {
            PackageManager packageManager = ctx.getPackageManager();
            if (packageManager != null) {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        resultData = applicationInfo.metaData.getString(key);
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {

        }
        return resultData;
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics outMetrics = context.getResources().getDisplayMetrics();
        return outMetrics.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics outMetrics = context.getResources().getDisplayMetrics();
        return outMetrics.heightPixels;
    }

    /**
     * 跳转QQ聊天界面
     */
    public static void joinQQ(Activity activity, String qqNum) {
        try {
            //第二种方式：可以跳转到添加好友，如果qq号是好友了，直接聊天
            String url = "mqqwpa://im/chat?chat_type=wpa&uin=" + qqNum;//uin是发送过去的qq号码
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.showShort(activity, "请检查是否安装QQ");
        }
    }

    /**
     * 检查包是否存在
     *
     * @param packname
     * @return
     */
    public static boolean checkPackageInfo(Context context, String packname) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packname, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            JLog.i("pack null");
            return false;
        }

        return packageInfo != null;
    }


    public static void getAllInstalledAppPakageName(Context context) {
        //当前应用pid
        final PackageManager packageManager = context.getPackageManager();
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        // get all apps
        final List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
        for (int i = 0; i < apps.size(); i++) {
            String name = apps.get(i).activityInfo.packageName;
            int version = getPackageVersionCode(context, name);
            JLog.i("getAppProcessName: " + name);
            JLog.i("getAppProcessVersion: " + version);
        }
    }


    /**
     * 获取应用版本号
     *
     * @return 当前应用的版本号
     */
    public static String getPackageVersionName(Context context, String packageName) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(packageName, 0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取应用版本号
     *
     * @return 当前应用的版本号
     */
    public static int getPackageVersionCode(Context context, String packageName) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(packageName, 0);
            return info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 保存到本地
     *
     * @param context
     * @param fileName assets下的文件名
     * @param path     存储路径
     */
    public static void copyApkFromAssets(Context context, String fileName, String path) {
        try {
            InputStream is = context.getAssets().open(fileName);
            File file = new File(path + fileName);
            JLog.i(path + fileName);
            if (!file.exists()) {
                boolean create = file.createNewFile();
                if (!create) return;
            }

            JLog.i("filename = " + file.getPath());
            FileOutputStream fos = new FileOutputStream(file);
            byte[] temp = new byte[1024];
            int i = 0;
            while ((i = is.read(temp)) > 0) {
                fos.write(temp, 0, i);
            }
            fos.close();
            is.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 安装apk （注意在安装apk时的Android7.0配置path的问题）
     *
     * @param file
     * @param c
     */
    public static void installApk(File file, Activity c) {
        if (!file.exists()) return;

        c.runOnUiThread(() -> {
            if (Build.VERSION.SDK_INT >= 26) {
                if (!c.getPackageManager().canRequestPackageInstalls()) {
                    //注意这个是8.0新API
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                    c.startActivityForResult(intent, 0x10086);
                    return;
                }
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            //判断是否是AndroidN以及更高的版本
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = FileProvider.getUriForFile(c, BuildConfig.APPLICATION_ID + ".fileprovider", file);
                intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            } else {
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            c.startActivityForResult(intent, 0x88);
        });
    }

    public static void sendNotification(Activity context, String title, String content) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setClass(context, context.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent pi = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationManager notifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification;

        //兼容Android8.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String id = "my_channel_01";
            int importance = NotificationManager.IMPORTANCE_LOW;
            CharSequence name = "notice";
            NotificationChannel mChannel = new NotificationChannel(id, name, importance);
            mChannel.enableLights(true);
            mChannel.setDescription("just show notice");
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.GREEN);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notifyMgr.createNotificationChannel(mChannel);

            Notification.Builder builder = new Notification.Builder(context, id);
            builder.setAutoCancel(true)
                    .setContentIntent(pi)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setOngoing(false)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setWhen(System.currentTimeMillis());
//            if (obj.subText != null && obj.subText.trim().length() > 0) {
//                builder.setSubText(obj.subText);
//            }
            notification = builder.build();
        } else if (Build.VERSION.SDK_INT >= 23) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setContentTitle(title)
                    .setContentText(content)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .setOngoing(false)
                    .setWhen(System.currentTimeMillis());
//            if (obj.subText != null && obj.subText.trim().length() > 0) {
//                builder.setSubText(obj.subText);
//            }
            notification = builder.build();
        } else {

            Notification.Builder builder = new Notification.Builder(context);
            builder.setAutoCancel(true)
                    .setContentIntent(pi)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setOngoing(false)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setWhen(System.currentTimeMillis());
//            if (obj.subText != null && obj.subText.trim().length() > 0) {
//                builder.setSubText(obj.subText);
//            }
            notification = builder.build();
        }

        if (notification != null) {
            notifyMgr.notify(1, notification);
        }
    }

}
