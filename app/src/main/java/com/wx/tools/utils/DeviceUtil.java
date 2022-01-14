package com.wx.tools.utils;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.UUID;

import static android.content.Context.TELEPHONY_SERVICE;

/**
 * @author ZW
 * @description:
 * @date : 2020/11/25 15:50
 */
public class DeviceUtil {

    private static final String TAG = DeviceUtil.class.getSimpleName();

    private static final String TEMP_DIR = "system_config";
    private static final String TEMP_FILE_NAME = "system_file";
    private static final String TEMP_FILE_NAME_MIME_TYPE = "application/octet-stream";

    /**
     * 获取设备id
     *
     * @param context
     * @return
     */
    public static String getDeviceId(Context context) {
        String deviceId = "";

        ArrayList<String> list = getIMEIs(context);
        if (list.size() > 0) {
            StringBuilder builder = new StringBuilder();
            for (int index = 0; index < list.size(); index++) {
                builder.append(list.get(index));
                if (index != list.size() - 1) {
                    builder.append("-");
                }
            }
            deviceId = AppUtil.MD5Encode(builder.toString());
        }

        return deviceId;
    }

    private static String createUUID(Context context) {
        String uuid = UUID.randomUUID().toString().replace("-", "");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            Uri externalContentUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
            ContentResolver contentResolver = context.getContentResolver();
            String[] projection = new String[]{
                    MediaStore.Downloads._ID
            };
            String selection = MediaStore.Downloads.TITLE + "=?";
            String[] args = new String[]{
                    TEMP_FILE_NAME
            };
            Cursor query = contentResolver.query(externalContentUri, projection, selection, args, null);
            if (query != null && query.moveToFirst()) {
                Uri uri = ContentUris.withAppendedId(externalContentUri, query.getLong(0));
                query.close();

                InputStream inputStream = null;
                BufferedReader bufferedReader = null;
                try {
                    inputStream = contentResolver.openInputStream(uri);
                    if (inputStream != null) {
                        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                        uuid = bufferedReader.readLine();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.Downloads.TITLE, TEMP_FILE_NAME);
                contentValues.put(MediaStore.Downloads.MIME_TYPE, TEMP_FILE_NAME_MIME_TYPE);
                contentValues.put(MediaStore.Downloads.DISPLAY_NAME, TEMP_FILE_NAME);
                contentValues.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + File.separator + TEMP_DIR);

                Uri insert = contentResolver.insert(externalContentUri, contentValues);
                if (insert != null) {
                    OutputStream outputStream = null;
                    try {
                        outputStream = contentResolver.openOutputStream(insert);
                        if (outputStream == null) {
                            return uuid;
                        }
                        outputStream.write(uuid.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        } else {
            File externalDownloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File applicationFileDir = new File(externalDownloadsDir, TEMP_DIR);
            if (!applicationFileDir.exists()) {
                if (!applicationFileDir.mkdirs()) {
                    JLog.e(TAG, "文件夹创建失败: " + applicationFileDir.getPath());
                }
            }
            File file = new File(applicationFileDir, TEMP_FILE_NAME);
            if (!file.exists()) {
                FileWriter fileWriter = null;
                try {
                    if (file.createNewFile()) {
                        fileWriter = new FileWriter(file, false);
                        fileWriter.write(uuid);
                    } else {
                        JLog.e(TAG, "文件创建失败：" + file.getPath());
                    }
                } catch (IOException e) {
                    JLog.e(TAG, "文件创建失败：" + file.getPath());
                    e.printStackTrace();
                } finally {
                    if (fileWriter != null) {
                        try {
                            fileWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                FileReader fileReader = null;
                BufferedReader bufferedReader = null;
                try {
                    fileReader = new FileReader(file);
                    bufferedReader = new BufferedReader(fileReader);
                    uuid = bufferedReader.readLine();

                    bufferedReader.close();
                    fileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (fileReader != null) {
                        try {
                            fileReader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        return uuid;
    }

    /**
     * 一个正常的全网通手机会有2-3个iMei
     *
     * @param context
     * @return
     */
    public static ArrayList<String> getIMEIs(Context context) {
        try {
            ArrayList<String> iMeiList = new ArrayList<String>();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                //9.0以上的设备iMei获取不到，改由AndroidId
                String iMei = Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                if (iMei.isEmpty()) {
                    return iMeiList;
                }
                iMeiList.add(iMei);
            } else {
                //9.0以下的设备iMei的获取
                ArrayList<String> list = JudgeSIM(context);
                if (list != null && list.size() > 0) {
                    iMeiList.addAll(list);
                }
            }

            if (iMeiList.size() == 0) {
                JLog.i("can't find imei");
            }

            //imei去重
            HashSet<String> set = new HashSet<>(iMeiList);
            iMeiList.clear();
            iMeiList.addAll(set);

            return iMeiList;
        } catch (Exception e) {
            return new ArrayList<>();
        }

    }

    private static ArrayList<String> JudgeSIM(Context context) {
        ArrayList<String> iMeiList = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= 23) {
            //检查权限
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
        }

        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
            Method method = tm.getClass().getMethod("getDeviceId", int.class);

            //CDMA的手机会有meid
            if (Build.VERSION.SDK_INT >= 26 && Build.VERSION.SDK_INT < 29) {
                String mEid = tm.getMeid();
                String iMei1 = tm.getImei(0);
                String iMei2 = tm.getImei(1);
                if (mEid != null) {
                    JLog.i("meid = " + mEid);
                    iMeiList.add(mEid);
                }
                if (iMei1 != null) {
                    JLog.i("iMei1 = " + iMei1);
                    iMeiList.add(iMei1);
                }
                if (iMei2 != null) {
                    JLog.i("iMei2 = " + iMei2);
                    iMeiList.add(iMei2);
                }
            } else {
                String iMei1 = tm.getDeviceId();
                String iMei2 = (String) method.invoke(tm, 1);
                String mEid = (String) method.invoke(tm, 2);

                if (mEid != null) {
                    JLog.i("meid = " + mEid);
                    iMeiList.add(mEid);
                }
                if (iMei1 != null) {
                    JLog.i("iMei1 = " + iMei1);
                    iMeiList.add(iMei1);
                }
                if (iMei2 != null) {
                    JLog.i("iMei2 = " + iMei2);
                    iMeiList.add(iMei2);
                }
            }

        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return iMeiList;
    }


    /**
     * Android 6.0 之前（不包括6.0）获取mac地址
     * 必须的权限 <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"></uses-permission>
     *
     * @param context * @return
     */
    public static String getMacDefault(Context context) {
        String mac = "";
        if (context == null) {
            return mac;
        }
        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = null;
        try {
            info = wifi.getConnectionInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (info == null) {
            return null;
        }
        mac = info.getMacAddress();
        if (!TextUtils.isEmpty(mac)) {
            mac = mac.toUpperCase(Locale.ENGLISH);
        }
        return mac;
    }

    /**
     * Android 6.0-Android 7.0 获取mac地址
     */
    public static String getMacAddress() {
        String macSerial = null;
        String str = "";

        try {
            Process pp = Runtime.getRuntime().exec("cat/sys/class/net/wlan0/address");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            while (null != str) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();//去空格
                    break;
                }
            }
        } catch (IOException ex) {
            // 赋予默认值
            ex.printStackTrace();
        }

        return macSerial;
    }

    /**
     * Android 7.0之后获取Mac地址
     * 遍历循环所有的网络接口，找到接口是 wlan0
     * 必须的权限 <uses-permission android:name="android.permission.INTERNET"></uses-permission>
     *
     * @return
     */
    public static String getMacFromHardware() {
        try {
            ArrayList<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equals("wlan0"))
                    continue;
                byte macBytes[] = nif.getHardwareAddress();
                if (macBytes == null) return "";
                StringBuilder res1 = new StringBuilder();
                for (Byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }
                if (!TextUtils.isEmpty(res1)) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * 获取mac地址（适配所有Android版本）
     *
     * @return
     */
    public static String getMac(Context context) {
        String mac = "";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mac = getMacDefault(context);
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mac = getMacAddress();
        } else {
            mac = getMacFromHardware();
        }
        return mac;
    }

    /**
     * 获取手机外部总空间大小
     *
     * @return 总大小，字节为单位
     */
    public static long getTotalExternalMemorySize() {
        if (isSDCardEnable()) {
            //获取SDCard根目录
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long totalBlocks = stat.getBlockCountLong();
            return totalBlocks * blockSize;
        } else {
            return 0;
        }
    }

    public static long getFreeSpace() {
        if (!isSDCardEnable()) return 0;
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
        long blockSize, availableBlocks;
        availableBlocks = stat.getAvailableBlocksLong();
        blockSize = stat.getBlockSizeLong();
        return availableBlocks * blockSize;
    }

    /**
     * 判断SD卡是否可用
     *
     * @return true : 可用<br>false : 不可用
     */
    public static boolean isSDCardEnable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

}
