package com.wx.tools.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;

import com.wx.tools.bean.FileStatus;
import com.wx.tools.callback.DBCallback;
import com.wx.tools.callback.FCallback;
import com.wx.tools.callback.FileCallback;
import com.wx.tools.controller.Constant;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class FileUtil {

    private static final int RET_SUCCESS = 0;
    private static final int RET_WARNING = 1;
    private static final int RET_FAULT = 2;
    private static final int RET_COMMAND = 7;
    private static final int RET_MEMORY = 8;
    private static final int RET_USER_STOP = 255;

    //检查SDCard存在并且可以读写
    public static boolean isSDCardState() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取sd卡的绝对路径
     *
     * @return
     */
    public static String getSDPath(Context context) {
        File sdDir = null;
        //判断sd卡是否存在
        boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            if (Build.VERSION.SDK_INT > 29) {
                File externalFileRootDir = context.getExternalFilesDir(null);
                do {
                    externalFileRootDir = Objects.requireNonNull(externalFileRootDir).getParentFile();
                } while (Objects.requireNonNull(externalFileRootDir).getAbsolutePath().contains("/Android"));
                sdDir = Objects.requireNonNull(externalFileRootDir);
            } else {
                sdDir = Environment.getExternalStorageDirectory();
            }

        }
        if (sdDir != null) {
            return sdDir.toString();
        } else return null;
    }

    /**
     * 判断文件是否已经存在
     *
     * @param fileName 要检查的文件名
     * @return boolean, true表示存在，false表示不存在
     */
    public static boolean isFileExist(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

    /**
     * 新建目录
     *
     * @param path 目录的绝对路径
     * @return 创建成功则返回true
     */
    public static boolean createFolder(String path) {
        if (path.equals("")) return false;
        File file = new File(path);
        if (file.exists()) {
            return true;
        } else {
            JLog.i("create path = " + path);
            File parent = file.getParentFile();
            if (parent != null) {
                if (parent.exists()) {
                    return file.mkdir();
                } else {
                    boolean b = parent.mkdir();
                    if (b) {
                        return file.mkdir();
                    }
                }
            }
            return false;
        }

    }

    /**
     * 创建文件
     *
     * @param path     文件所在目录的目录名
     * @param fileName 文件名
     * @return 文件新建成功则返回true
     */
    public static boolean createFile(String path, String fileName) {
        File file = new File(path + File.separator + fileName);
        if (file.exists()) {
            return false;
        } else {
            try {
                return file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 获取缓存大小
     *
     * @param context
     * @return
     * @throws Exception
     */
    public static String getTotalCacheSize(Context context) throws Exception {
        long cacheSize = getFolderSize(context.getCacheDir());
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            cacheSize += getFolderSize(context.getExternalCacheDir());
        }
        return getFormatSize(cacheSize);
    }

    // 获取文件
    //Context.getExternalFilesDir() --> SDCard/Android/data/你的应用的包名/files/ 目录，一般放一些长时间保存的数据
    //Context.getExternalCacheDir() --> SDCard/Android/data/你的应用包名/cache/目录，一般存放临时缓存数据
    public static long getFolderSize(File file) throws Exception {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                // 如果下面还有文件
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i]);
                } else {
                    size = size + fileList[i].length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * 格式化单位
     *
     * @param size
     * @return
     */
    public static String getFormatSize(double size) {
        double kiloByte = size / 1024;
        if (kiloByte < 1) {
//            return size + "Byte";
            return "0K";
        }

        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "KB";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "MB";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
                + "TB";
    }

    /***
     * 清理所有缓存
     * @param context
     */
    public static void clearAllCache(Context context) {
        deleteDir(context.getCacheDir());
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            deleteDir(context.getExternalCacheDir());
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    /**
     * 删除单个文件
     *
     * @param filePath 文件路径
     * @return 删除成功则返回true
     */
    public synchronized static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.delete();
//        if (!file.exists()) return false;
//        Runtime runtime = Runtime.getRuntime();
//        String[] args = new String[]{"cmd.exe", "/c", String.format("rd %s /q /s", filePath)};
//        runtime.exec(args);
//        return true;
    }

    /**
     * 删除一个目录（可以是非空目录）
     *
     * @param dir 目录绝对路径
     */
    public static boolean deleteDirection(File dir) {
        if (dir == null || !dir.exists() || dir.isFile()) {
            return false;
        }

        File[] files = dir.listFiles();
        if (files == null) return false;

        for (File file : files) {
            if (file.isFile()) {
                boolean bl = file.delete();
//                if (bl) {
//                    JLog.i("delete dir success");
//                } else {
//                    JLog.i("delete dir failed");
//                }
            } else if (file.isDirectory()) {
                deleteDirection(file);//递归
            }
        }
        dir.delete();
        return true;
    }

    /**
     * 将字符串写入文件
     *
     * @param text     写入的字符串
     * @param fileStr  文件的绝对路径
     * @param isAppend true从尾部写入，false从头覆盖写入
     */
    public static void writeFile(String text, String fileStr, boolean isAppend) {
        try {
            File file = new File(fileStr);
            File parentFile = file.getParentFile();
            if (parentFile != null && !parentFile.exists()) {
                parentFile.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream f = new FileOutputStream(fileStr, isAppend);
            f.write(text.getBytes());
            f.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
        }
    }

    /**
     * 拷贝文件
     *
     * @param srcPath 绝对路径
     * @param destDir 目标文件所在目录
     * @return boolean true拷贝成功
     */
    public static boolean copyFile(String srcPath, String destDir, FileCallback callback) {
        boolean flag = false;
        File srcFile = new File(srcPath); // 源文件
        if (!srcFile.exists()) {
            JLog.i("源文件不存在");
            return false;
        }

        long length = srcFile.length();
        // 获取待复制文件的文件名
        String fileName = srcPath.substring(srcPath.lastIndexOf(File.separator));
        String destPath = destDir + fileName;
        if (destPath.equals(srcPath)) {
            JLog.i("源文件路径和目标文件路径重复");
            return false;
        }

        JLog.i("export src path = " + srcPath);
        JLog.i("export des path = " + destPath);

        File destFile = new File(destPath); // 目标文件
        if (destFile.exists() && destFile.isFile()) {
            JLog.i("该路径下已经有一个同名文件");
        }

        JLog.i("copy start");
        File destFileDir = new File(destDir);
        if (!destFileDir.exists()) {
            boolean bl = destFileDir.mkdirs();
            if (!bl) {
                callback.onFailed(FileStatus.COPY, "失败");
                return false;
            }
        }

        try {
            FileInputStream fis = new FileInputStream(srcPath);
            FileOutputStream fos = new FileOutputStream(destPath);
            BufferedInputStream bis = new BufferedInputStream(fis);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            byte[] buf = new byte[1024];
            int c;
            while ((c = bis.read(buf)) != -1) {
                bos.write(buf, 0, c);
            }

            JLog.i("copy finished");
            bos.flush();
            bos.close();
            bis.close();
            fos.close();
            fis.close();

            flag = true;
        } catch (IOException e) {
            if (callback != null) {
                callback.onFailed(FileStatus.COPY, "导入发生错误");
            }
        }
        return flag;
    }

    /**
     * 重命名文件
     *
     * @param oldPath 旧文件的绝对路径
     * @param newPath 新文件的绝对路径
     * @return 文件重命名成功则返回true
     */
    public static boolean renameTo(String oldPath, String newPath) {
        if (oldPath.equals(newPath)) {
            JLog.i("FileUtils is renameTo：", "文件重命名失败：新旧文件名绝对路径相同");
            return false;
        }
        File oldFile = new File(oldPath);
        File newFile = new File(newPath);

        return oldFile.renameTo(newFile);
    }

    /**
     * 计算某个文件的大小
     *
     * @param path 文件的绝对路径
     * @return 文件大小
     */
    public static long getFileSize(String path) {
        File file = new File(path);
        return file.length();
    }

    /**
     * 计算某个文件夹的大小
     *
     * @param file 目录所在绝对路径
     * @return 文件夹的大小
     */
    public static double getDirSize(File file) {
        if (file.exists()) {
            //如果是目录则递归计算其内容的总大小
            if (file.isDirectory()) {
                File[] children = file.listFiles();
                double size = 0;
                for (File f : children)
                    size += getDirSize(f);
                return size;
            } else {//如果是文件则直接返回其大小,以“兆”为单位
                return (double) file.length() / 1024 / 1024;
            }
        } else {
            return 0.0;
        }
    }

    /**
     * 获取某个路径下的文件列表
     *
     * @param path 文件路径
     * @return 文件列表File[] files
     */
    public static File[] getFileList(String path) {
        File file = new File(path);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                return files;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static int getFileCounts(String path) {
        return getFiles(path).size();
    }

    /**
     * 获取某个路径下的文件
     *
     * @param path 文件路径
     * @return 文件列表File[] files
     */
    public static List<File> getFiles(String path) {
        List<File> files = new ArrayList<>();
        File folder = new File(path);
        File[] subFolders = folder.listFiles(file -> {

            if (file.isDirectory()) {
                return true;
            }

            if (file.isFile()) {
                files.add(file);
                return false;
            }

            return false;
        });

        if (subFolders != null) {
            for (File file : subFolders) {
                if (file.isDirectory()) {
                    // 如果是文件夹，则递归调用本方法
                    files.addAll(getFiles(file.getPath()));
                }
            }
        }

        return files;
    }

    public static List<File> getChildFolders(String path) {
        List<File> files = new ArrayList<>();
        File folder = new File(path);
        File[] subFolders = folder.listFiles(file -> {

            if (file.isDirectory()) {
                return true;
            }

            return false;
        });

        if (subFolders != null) {
            Collections.addAll(files, subFolders);
        }

        return files;
    }


    /**
     * 获取某个路径下的图片数量
     *
     * @param path 文件路径
     * @return 文件列表File[] files
     */
    public static void getPicFiles(String path, FCallback callback) {
        File folder = new File(path);
        File[] subFolders = folder.listFiles(file -> {

            if (file.isDirectory()) {
                return true;
            }

            if (file.length() == 0) return false;

            if (file.isFile()) {
                callback.onProgress(FileStatus.VIDEO, file);
                return false;
            }

            return false;
        });

        if (subFolders != null) {
            for (File file : subFolders) {
                if (file.isDirectory()) {
                    if (Constant.stopScanPic) break;
                    // 如果是文件夹，则递归调用本方法
                    getPicFiles(file.getPath(), callback);
                }
            }
        }

    }


    /**
     * 获取某个路径下的视频数量
     *
     * @param path 文件路径
     * @return 文件列表File[] files
     */
    public static void getVideoFiles(String path, FCallback callback) {
        File folder = new File(path);
        File[] subFolders = folder.listFiles(file -> {

            if (file.isDirectory()) {
                return true;
            }

            if (file.length() == 0) return false;

            if (file.isFile()) {
                callback.onProgress(FileStatus.VIDEO, file);
                return false;
            }

            return false;
        });

        if (subFolders != null) {
            for (File file : subFolders) {
                if (Constant.stopScanVideo) break;
                if (file.isDirectory()) {
                    // 如果是文件夹，则递归调用本方法
                    getVideoFiles(file.getPath(), callback);
                }
            }
        }
    }

    /**
     * 获取某个路径下的文件数量
     *
     * @param path 文件路径
     * @return 文件列表File[] files
     */
    public static void getVoiceFiles(String path, FCallback callback) {
        File folder = new File(path);
        File[] subFolders = folder.listFiles(file -> {

            if (file.isDirectory()) {
                return true;
            }

            if (file.length() == 0) return false;

            if (file.isFile()) {
                callback.onProgress(FileStatus.VIDEO, file);
                return false;
            }

            return false;
        });

        if (subFolders != null) {
            for (File file : subFolders) {
                if (Constant.stopScanVoice) break;
                if (file.isDirectory()) {
                    // 如果是文件夹，则递归调用本方法
                    getVoiceFiles(file.getPath(), callback);
                }
            }
        }
    }

    /**
     * 获取某个路径下的文档数量
     *
     * @param path 文件路径
     * @return 文件列表File[] files
     */
    public static void getDocsFiles(String path, FCallback callback) {
        File folder = new File(path);
        File[] subFolders = folder.listFiles(file -> {

            if (file.isDirectory()) {
                return true;
            }

            if (file.length() == 0) return false;

            if (file.isFile()) {
                callback.onProgress(FileStatus.VIDEO, file);
                return false;
            }

            return false;
        });

        if (subFolders != null) {
            for (File file : subFolders) {
                if (Constant.stopScanDoc) break;
                if (file.isDirectory()) {
                    // 如果是文件夹，则递归调用本方法
                    getDocsFiles(file.getPath(), callback);
                }
            }
        }
    }

    /**
     * 获取某个路径下的视频文件夹
     *
     * @param path 文件路径
     * @return 文件列表File[] files
     */
    public static ArrayList<File> getWxVideoFolders(String path) {
        ArrayList<File> list = new ArrayList<>();
        File folder = new File(path);
        File[] subFolders = folder.listFiles(file -> {


            if (file.isDirectory()) {
                String p = file.getPath();
                if (p.contains("/sns/")) {
                    list.add(file);
                    return true;
                }

                if (p.contains("/video")) {
                    list.add(file);
                    return true;
                }

                if (p.contains("/Download")) {
                    list.add(file);
                    return true;
                }
            }

            return true;
        });

        if (subFolders != null) {
            for (File file : subFolders) {
                if (file.isDirectory()) {
                    // 如果是文件夹，则递归调用本方法
                    list.addAll(getWxVideoFolders(file.getAbsolutePath()));
                }
            }
        }

        return list;
    }

    /**
     * 获取某个路径下的音频文件夹
     *
     * @param path 文件路径
     * @return 文件列表File[] files
     */
    public static ArrayList<File> getWxVoiceFolders(String path) {
        ArrayList<File> list = new ArrayList<>();
        File folder = new File(path);
        File[] subFolders = folder.listFiles(file -> {


            if (file.isDirectory()) {
                String p = file.getPath();

                if (p.contains("/voice2/")) {
                    list.add(file);
                    return true;
                }

            }

            return true;
        });

        if (subFolders != null) {
            for (File file : subFolders) {
                if (file.isDirectory()) {
                    // 如果是文件夹，则递归调用本方法
                    list.addAll(getWxVoiceFolders(file.getAbsolutePath()));
                }
            }
        }

        return list;
    }

    /**
     * 获取某个路径下的文件
     *
     * @param path
     * @return
     */
    public static File getFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            return file;
        } else return null;
    }

    /**
     * 计算某个目录包含的文件数量
     *
     * @param path 目录的绝对路径
     * @return 文件数量
     */
    public static int getFileCount(String path) {
        File directory = new File(path);
        File[] files = directory.listFiles();
        return files.length;
    }

    /**
     * 获取SDCard 总容量大小(MB)
     *
     * @param path 目录的绝对路径
     * @return 总容量大小
     */
    public long getSDCardTotal(String path) {

        if (null != path && path.equals("")) {

            StatFs statfs = new StatFs(path);
            //获取SDCard的Block总数
            long totalBlocks = statfs.getBlockCount();
            //获取每个block的大小
            long blockSize = statfs.getBlockSize();
            //计算SDCard 总容量大小MB
            return totalBlocks * blockSize / 1024 / 1024;

        } else {
            return 0;
        }
    }

    /**
     * 获取SDCard 可用容量大小(MB)
     *
     * @param path 目录的绝对路径
     * @return 可用容量大小
     */
    public long getSDCardFree(String path) {

        if (null != path && path.equals("")) {

            StatFs statfs = new StatFs(path);
            //获取SDCard的Block可用数
            long availaBlocks = statfs.getAvailableBlocks();
            //获取每个block的大小
            long blockSize = statfs.getBlockSize();
            //计算SDCard 可用容量大小MB
            return availaBlocks * blockSize / 1024 / 1024;

        } else {
            return 0;
        }
    }

    /**
     * 关键字查找文件
     *
     * @param path
     * @param keyword
     * @return
     */
    public static List<File> searchFiles(String path, final String keyword) {
        final List<File> result = new ArrayList<File>();
        File folder = new File(path);
        File[] subFolders = folder.listFiles(file -> {
            if (file.isDirectory()) {
                return true;
            }
            if (file.getName().toLowerCase().contains(keyword.toLowerCase())) {
                result.add(file);
                return true;
            }
            return false;
        });

        if (subFolders != null) {
            for (File file : subFolders) {
                if (file.isDirectory()) {
                    // 如果是文件夹，则递归调用本方法
                    result.addAll(searchFiles(file.getPath(), keyword));
                }
            }
        }

        return result;
    }

    /**
     * 关键字查找文件
     *
     * @param path
     * @param keyword
     * @return
     */
    public static List<File> searchDetailFiles(String path, final String keyword) {
        final List<File> result = new ArrayList<>();
        File folder = new File(path);
        File[] subFolders = folder.listFiles(file -> {
            if (file.isDirectory()) {
                return true;
            }

            if (file.getName().toLowerCase().equals(keyword.toLowerCase())) {
                result.add(file);
                return true;
            }

            return false;
        });

        if (subFolders != null) {
            for (File file : subFolders) {
                if (file.isDirectory()) {
                    // 如果是文件夹，则递归调用本方法
                    result.addAll(searchDetailFiles(file.getPath(), keyword));
                }
            }
        }

        return result;
    }

    /**
     * 关键字查找文件
     *
     * @param path
     * @param keyword
     * @return
     */
    public static List<File> searchFiles(String path, final String keyword, String end) {
        final List<File> result = new ArrayList<File>();
        File folder = new File(path);
        File[] subFolders = folder.listFiles(file -> {
            if (file.isDirectory()) {
                return true;
            }

            String name = file.getName().toLowerCase();
            if (name.contains(keyword.toLowerCase()) && name.endsWith(end)) {
                result.add(file);
                return true;
            }
            return false;
        });

        if (subFolders != null) {
            for (File file : subFolders) {
                if (file.isDirectory()) {
                    // 如果是文件夹，则递归调用本方法
                    result.addAll(searchFiles(file.getPath(), keyword));
                }
            }
        }

        return result;
    }

    /**
     * 获取文件夹的大小
     *
     * @param
     * @param
     * @return
     */
    public static long getTotalSize(File file) {
        if (file.isFile())
            return file.length();
        final File[] children = file.listFiles();
        long total = 0;
        if (children != null)
            for (final File child : children)
                total += getTotalSize(child);
        return total;
    }

    /**
     * 关键字查找文件夹
     *
     * @param path
     * @param keyword
     * @return
     */
    public static List<File> searchFolder(String path, final String keyword) {
        final List<File> result = new ArrayList<File>();
        File folder = new File(path);
        File[] subFolders = folder.listFiles(file -> {

            if (file.getName().toLowerCase().contains(keyword.toLowerCase())) {
                result.add(file);
                return true;
            }
            return false;
        });

        if (subFolders != null) {
            for (File file : subFolders) {
                if (file.isDirectory()) {
                    // 如果是文件夹，则递归调用本方法
                    result.addAll(searchFiles(file.getPath(), keyword));
                }
            }
        }

        return result;
    }

    /**
     * 关键字查找文件
     *
     * @param path
     * @param keyword
     * @return
     */
    public static List<File> searchDbFiles(String path, final String keyword) {
        final List<File> result = new ArrayList<File>();
        File folder = new File(path);
        File[] subFolders = folder.listFiles(file -> {
            if (file.isDirectory()) {
                return true;
            }
            if (file.getName().toLowerCase().equals(keyword.toLowerCase())) {
                result.add(file);
                return true;
            }
            return false;
        });

        if (subFolders != null) {
            for (File file : subFolders) {
                if (file.isDirectory()) {
                    // 如果是文件夹，则递归调用本方法
                    result.addAll(searchDbFiles(file.getPath(), keyword));
                }
            }
        }

        return result;
    }

    /**
     * 查找目录下所有文件
     *
     * @param path
     * @return
     */
    public static List<File> searchFiles(String path) {
        final List<File> result = new ArrayList<File>();
        File folder = new File(path);
        File[] subFolders = folder.listFiles(file -> {
            if (file.isDirectory()) {
                return true;
            }

            if (file.isFile()) {
                result.add(file);
                return true;
            }
            return false;
        });

        if (subFolders != null) {
            for (File file : subFolders) {
                if (file.isDirectory()) {
                    // 如果是文件夹，则递归调用本方法
                    result.addAll(searchFiles(file.getPath()));
                }
            }
        }

        return result;
    }


    /**
     * 查找目录下所有文件
     *
     * @param path
     * @return
     */
    public static List<File> searchFiles(String path, Boolean stop) {
        final List<File> result = new ArrayList<File>();
        File folder = new File(path);
        File[] subFolders = folder.listFiles(file -> {
            if (file.isDirectory()) {
                return true;
            }

            if (file.isFile()) {
                result.add(file);
                return true;
            }
            return false;
        });

        if (subFolders != null) {
            for (File file : subFolders) {
                if (file.isDirectory() && !stop) {
                    // 如果是文件夹，则递归调用本方法
                    result.addAll(searchFiles(file.getPath(), stop));
                }
            }
        }

        return result;
    }


    /**
     * 查找图片文件
     *
     * @param
     * @param
     * @return
     */
    public static void searchQQPics(String path, FCallback callback) {
        File folder = new File(path);
        File[] subFolders = folder.listFiles(file -> {
            if (file.isDirectory()) {
                return true;
            }

            //小于5K的图片没有恢复价值
            if (file.length() < 5 * 1024) {
                return true;
            }

            if (file.isFile()) {
                callback.onProgress(FileStatus.PIC, file);
                return true;
            }

            return false;
        });

        if (subFolders != null) {
            for (File file : subFolders) {
                if (Constant.stopScanPic) break;
                if (file.isDirectory()) {
                    // 如果是文件夹，则递归调用本方法
                    searchQQPics(file.getPath(), callback);
                }
            }
        }
    }


    /**
     * 查找图片文件
     *
     * @param path
     * @param
     * @return
     */
    public static List<File> searchWxPics(String path, FCallback callback) {
        final List<File> result = new ArrayList<>();
        File folder = new File(path);
        File[] subFolders = folder.listFiles(file -> {
            if (file.isDirectory()) {
                return true;
            }

            //小于1K的图片没有恢复价值
            if (file.length() < 1024) {
                return true;
            }

            if (file.isFile()) {
                callback.onProgress(FileStatus.PIC, file);
                return true;
            }

            return false;
        });

        if (subFolders != null) {
            for (File file : subFolders) {
                if (Constant.stopScanPic) break;
                if (file.isDirectory()) {
                    // 如果是文件夹，则递归调用本方法
                    result.addAll(searchWxPics(file.getPath(), callback));
                }
            }
        }

        return result;
    }

    /**
     * 查找图片文件
     *
     * @param
     * @param
     * @return
     */
    public static void searchWxPics2(File file, FCallback callback) {
        //小于5K的图片没有恢复价值
        if (file.length() < 5 * 1024) {
            return;
        }

        String name = file.getName().toLowerCase();
        String path = file.getPath();
        if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".gif")) {
            if (!name.contains("_wm")) {
                callback.onProgress(FileStatus.PIC, file);
            }
        } else {
            //表情包
            if (path.contains("/emoji/") && name.endsWith("_cover")) {
                callback.onProgress(FileStatus.PIC, file);
                return;
            }

            //聊天图片
            if (path.contains("/image/") || name.contains("/image2/") || name.contains("/record/")) {
                callback.onProgress(FileStatus.PIC, file);
                return;
            }

            //高清头像
            if (path.startsWith("th_") && path.endsWith("hd")) {
                callback.onProgress(FileStatus.PIC, file);
                return;
            }

            //缓存聊天图片(包括公众号和好友)
            if (path.contains("cache") && path.contains("/image/")) {
                callback.onProgress(FileStatus.PIC, file);
                return;
            }

            //朋友圈图片
            if (path.contains("/sns/") && name.startsWith("snst") && !name.startsWith("snstblur")) {
                callback.onProgress(FileStatus.PIC, file);
            }
        }
    }

    /**
     * 查找微信视频
     *
     * @param
     * @param
     * @return
     */
    public static void searchVideos(String path, FCallback callback) {

        File folder = new File(path);
        File[] subFolders = folder.listFiles(file -> {
            if (file.isDirectory()) {
                return true;
            }

            if (file.isFile()) {
                callback.onProgress(FileStatus.VOICE, file);
                return true;
            }

            return false;
        });

        if (subFolders != null) {
            for (File file : subFolders) {
                if (file.isDirectory()) {
                    if (Constant.stopScanVideo) break;
                    // 如果是文件夹，则递归调用本方法
                    searchVideos(file.getPath(), callback);
                }
            }
        }
    }


    /**
     * 查找微信语音文件
     *
     * @param
     * @param
     * @return
     */
    public static void searchVoices(String path, boolean stop, FCallback callback) {
        File folder = new File(path);
        File[] subFolders = folder.listFiles(file -> {
            if (file.isDirectory()) {
                return true;
            }

            if (file.isFile()) {
                callback.onProgress(FileStatus.VOICE, file);
                return true;
            }

            return false;
        });

        if (subFolders != null) {
            for (File file : subFolders) {
                if (stop) break;
                if (file.isDirectory()) {
                    if (Constant.stopScanVoice) break;
                    // 如果是文件夹，则递归调用本方法
                    searchVoices(file.getPath(), stop, callback);
                }
            }
        }
    }

    /**
     * 查找微信文档文件
     *
     * @param
     * @param
     * @return
     */
    public static void searchDocs(String path, FCallback callback) {
        File folder = new File(path);
        File[] subFolders = folder.listFiles(file -> {
            if (file.isDirectory()) {
                return true;
            }

            //空文档没有恢复价值
            if (file.length() == 0) {
                return true;
            }

            if (file.isFile()) {
                callback.onProgress(FileStatus.DOC, file);
                return true;
            }

            return false;
        });

        if (subFolders != null) {
            for (File file : subFolders) {
                if (Constant.stopScanDoc) break;
                if (file.isDirectory()) {
                    // 如果是文件夹，则递归调用本方法
                    searchDocs(file.getPath(), callback);
                }
            }
        }
    }


    /**
     * @param archive       解压文件得路径
     * @param decompressDir 解压文件目标路径
     * @param fileStatus    文件解压状态
     * @throws IOException
     */
    public static boolean unZipFile(String archive, String decompressDir, FileStatus fileStatus) {
        return unZipFile(archive, decompressDir, fileStatus, null);
    }

    public static boolean unZipFile(String archive, String decompressDir, FileStatus fileStatus, DBCallback callback) {

        try {
            JLog.i("解压开始");
            JLog.i("from = " + archive);
            JLog.i("dec = " + decompressDir);
            File from = new File(archive);
            File des = new File(decompressDir);
            if (!des.exists()) {
                JLog.i("1");
                boolean bl = des.mkdirs();
                if (!bl) {
                    callback.onFailed(FileStatus.UNZIP_WX, "创建文件失败");
                    return false;
                }
            }

            JLog.i("2");
            long currentTimes = 0;
            ZipFile zf = new ZipFile(from);
            long allTimes = zf.size();
            Enumeration<?> entries = zf.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = ((ZipEntry) entries.nextElement());

                String name = entry.getName();
//                JLog.i("before : " + entry.getName());

                //不解压的文件跳过
                if (name.endsWith(".so") || name.endsWith(".jar") || name.endsWith(".apk")
                        || name.endsWith(".dex") || name.endsWith(".js") || name.endsWith(".pak")
                        || name.endsWith(".zip") || name.endsWith(".dat") || name.endsWith(".bin")) {
                    continue;
                }

                InputStream in = zf.getInputStream(entry);
                String str = decompressDir + File.separator + entry.getName();
                str = new String(str.getBytes("8859_1"), "GB2312");
                File desFile = new File(str);
                if (!desFile.exists()) {
                    File parent = desFile.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    desFile.createNewFile();
                } else {
                    if (!desFile.getName().equals(Constant.DB_NAME))
                        continue;
                }

                OutputStream out = new FileOutputStream(desFile);
                byte[] buf = new byte[1024 * 1024];
                int realLength;
                while ((realLength = in.read(buf)) > 0) {
                    out.write(buf, 0, realLength);
                }

                if (callback != null) {
                    currentTimes++;
                    long d = currentTimes * 100 / allTimes;
                    callback.onProgress(fileStatus, (int) d);
                }

                in.close();
                out.close();
            }

            zf.close();
            JLog.i("解压完成");
            if (callback != null) {
                callback.onProgress(FileStatus.UNZIP_WX, 100);
                callback.onSuccess(fileStatus);
            }
            return true;


        } catch (IOException e) {
            if (callback != null) {
                callback.onFailed(fileStatus, e.toString());
            }
            return false;
        }

    }

    public static void unZipSteamFile(String srcFile, String desFile, FileStatus fileStatus, FileCallback callback) {

        try {
            JLog.i("unzip begin");

            ZipArchiveInputStream i = new ZipArchiveInputStream(new
                    FileInputStream(srcFile), "UTF-8", false, true);
            ArchiveEntry entry = null;

            while ((entry = i.getNextZipEntry()) != null) {
                if (!i.canReadEntryData(entry)) {
                    System.out.println("Can't read entry: " + entry);
                    continue;
                }

                String name = desFile + File.separator + entry.getName();
                JLog.i("file name = " + name);
                File f = new File(name);
                if (entry.isDirectory()) {
                    if (!f.isDirectory() && !f.mkdirs()) {

                    }
                } else {
                    File parent = f.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {

                    }

                    byte[] readContent = new byte[1024];

//                    String substring = entryName.substring(entryName.lastIndexOf("/") + 1, entryName.length());
//                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(decompressDir + "/" + substring));
//                    bi = new BufferedInputStream(zf.getInputStream(ze2));
//                    byte[] readContent = new byte[1024];
//                    int readCount = bi.read(readContent);
//                    while (readCount != -1) {
//                        bos.write(readContent, 0, readCount);
//                        readCount = bi.read(readContent);
//                    }
//                    bos.close();
//
//                    try (OutputStream o = Files.newOutputStream(f.toPath())) {
//                        IOUtils.copy(i, o);
//                    }
                }
            }

            i.close();
        } catch (Exception e) {
            if (callback != null) {
                callback.onFailed(fileStatus, e.toString());
            }
            e.printStackTrace();
        }

    }

    public static void UnZipFolder(String zipFileString, String outPathString) throws Exception {
        ZipInputStream inZip = new ZipInputStream(new FileInputStream(zipFileString));
        ZipEntry zipEntry;
        String szName;
        while ((zipEntry = inZip.getNextEntry()) != null) {
            szName = zipEntry.getName();
            if (zipEntry.isDirectory()) {
                //获取部件的文件夹名
                szName = szName.substring(0, szName.length() - 1);
                File folder = new File(outPathString + File.separator + szName);
                folder.mkdirs();
            } else {
                JLog.i(outPathString + File.separator + szName);
                File file = new File(outPathString + File.separator + szName);
                if (!file.exists()) {
                    JLog.i("Create the file:" + outPathString + File.separator + szName);
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
                // 获取文件的输出流
                FileOutputStream out = new FileOutputStream(file);
                int len;
                byte[] buffer = new byte[1024];
                // 读取（字节）字节到缓冲区
                while ((len = inZip.read(buffer)) != -1) {
                    // 从缓冲区（0）位置写入（字节）字节
                    out.write(buffer, 0, len);
                    out.flush();
                }
                out.close();
            }
        }
        inZip.close();
    }

    public static ZipUtils.ResultType unZipFileWith7Zip(String unZipPath, String outputPath) {
        return ZipUtils.unZip(unZipPath, outputPath);
    }

    public static ZipUtils.ResultType unZipEncryptedFileWith7Zip(String unZipPath, String outputPath, String pwd) {
        return ZipUtils.unZipEncrypted(unZipPath, outputPath, pwd);

    }


    /**
     * 保存图片
     *
     * @param context
     * @param file
     */
    public static void saveImage(Context context, File file) {

        String filePath;
        String fileName = System.currentTimeMillis() + "";

        if (RomUtil.isMiui() || RomUtil.isEmui()) {
            filePath = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/" + fileName;
        } else {
            // Meizu 、Oppo
            filePath = Environment.getExternalStorageDirectory().getPath() + "/DCIM/" + fileName;
        }

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        if (bitmap == null) {
            JLog.i("image is null");
            return;
        }

        if (Build.VERSION.SDK_INT >= 29) {
            saveSignImage(context, file, fileName, bitmap);
        } else {
            File mFile = new File(filePath);

            try {
                FileOutputStream out = new FileOutputStream(mFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();

                MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);

                //刷新相册
                Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                localIntent.setData(Uri.parse("file://" + filePath));
                context.sendBroadcast(localIntent);

                JLog.i("save success");
            } catch (Exception e) {
                JLog.i("save error = " + e);
            }
        }

    }


    /**
     * 将文件保存到公共的媒体文件夹，这里的filepath不是绝对路径，而是某个媒体文件夹下的子路径，和沙盒子文件夹类似，这里的filename单纯的指文件名，不包含路径
     */
    private static void saveSignImage(Context context, File srcFile, String fileName, Bitmap bitmap) {
        try {
            //设置保存参数到ContentValues中
            ContentValues contentValues = new ContentValues();
            //设置文件名
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            //设置文件时间
            contentValues.put(MediaStore.Images.Media.DATE_MODIFIED, System.currentTimeMillis());
            contentValues.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
            //设置文件类型
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                //android Q中不再使用DATA字段，而用RELATIVE_PATH代替，RELATIVE_PATH是相对路径不是绝对路径
                if (RomUtil.isMiui() || RomUtil.isEmui()) {
                    contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Camera/");
                } else {
                    contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/");
                }
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, false);
            } else {
                contentValues.put(MediaStore.Images.Media.DATA, srcFile.getAbsolutePath());
            }

            //执行insert操作，向系统文件夹中添加文件
            //EXTERNAL_CONTENT_URI代表外部存储器，该值不变
            Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            if (uri != null) {
                //若生成了uri，则表示该文件添加成功
                //使用流将内容写入该uri中即可
                OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.flush();
                    outputStream.close();
                }

                //刷新相册
                Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                localIntent.setData(uri);
                context.sendBroadcast(localIntent);
            }
        } catch (Exception e) {
        }
    }


    /**
     * 保存视频
     *
     * @param context
     * @param file
     */
    public static void saveVideo(Context context, File file) {
        String filePath;

        switch (Constant.ROM) {
            case Constant.ROM_MIUI:
            case Constant.ROM_EMUI:
                filePath = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/";
                break;
            default:
                filePath = Environment.getExternalStorageDirectory().getPath() + "/DCIM/";
                break;
        }

        try {
            boolean bl = FileUtil.copyFile(file.getAbsolutePath(), filePath, null);
            if (bl) {
                JLog.i("save success");
            }

//            ContentValues contentValues = getVideoContentValues(fileName);

            //执行insert操作，向系统文件夹中添加文件,EXTERNAL_CONTENT_URI代表外部存储器，该值不变
//            Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
//            if (uri != null) {
//                //若生成了uri，则表示该文件添加成功
//                //使用流将内容写入该uri中即可
//                OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
//                if (outputStream != null) {
//
//                    outputStream.flush();
//                    outputStream.close();
//                }
//            }


        } catch (Exception e) {
            JLog.i("save error = " + e);
        }


        //刷新相册
        Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        localIntent.setData(Uri.parse("file://" + filePath + file.getName()));
        context.sendBroadcast(localIntent);
    }

    public static ContentValues getVideoContentValues(String fileName) {
        //设置保存参数到ContentValues中
        ContentValues contentValues = new ContentValues();
        //设置文件名
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //android Q中不再使用DATA字段，而用RELATIVE_PATH代替,RELATIVE_PATH是相对路径不是绝对路径
            //DCIM是系统文件夹，关于系统文件夹可以到系统自带的文件管理器中查看，不可以写没存在的名字
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/");
        } else {
            contentValues.put(MediaStore.Images.Media.DATA,
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath());
        }

        //设置文件类型
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "video/mp4");
        return contentValues;
    }


}
