package com.wx.tools.utils;

import android.util.Base64;

import com.wx.tools.bean.FileStatus;
import com.wx.tools.callback.FileCallback;

import org.jetbrains.annotations.NotNull;

import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {


    /**
     * 采用AES加密算法
     */
    private static final String KEY_ALGORITHM = "AES";

    /**
     * 加解密算法/工作模式/填充方式
     */
    private static final String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";
    private static final String CIPHER_CBC_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String CIPHER_CTR_ALGORITHM = "AES/CTR/NoPadding";
    private static final int blockSize = 16;

    /**
     * AES 加密
     *
     * @param secretKey 加密密码，长度：16 或 32 个字符
     * @param data      待加密内容
     * @return 返回Base64转码后的加密数据
     */
    public static String encrypt(String secretKey, String data) {
        try {
            // 创建AES秘钥
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), KEY_ALGORITHM);
            // 创建密码器
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            // 初始化加密器
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encryptByte = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            // 将加密以后的数据进行 Base64 编码
            return base64Encode(encryptByte);
        } catch (Exception e) {
            handleException("encrypt", e);
        }
        return null;
    }

    /**
     * AES 解密
     *
     * @param secretKey  解密的密钥，长度：16 或 32 个字符
     * @param base64Data 加密的密文 Base64 字符串
     */
    public static String decrypt(String secretKey, String base64Data) {
        try {
            byte[] data = base64Decode(base64Data);
            // 创建AES秘钥
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), KEY_ALGORITHM);
            // 创建密码器
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            // 初始化解密器
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            // 执行解密操作
            byte[] result = cipher.doFinal(data);
            return new String(result, StandardCharsets.UTF_8);
        } catch (Exception e) {
            handleException("decrypt", e);
        }
        return null;
    }


    /**
     * AES CBC解密
     *
     * @param secretKey  解密的密钥，长度：16 或 32 个字符
     * @param initVector 偏移量
     * @param base64Data 加密的密文 Base64 字符串
     */
    public static String decrypt(String secretKey, String initVector, String base64Data) {
        try {
            byte[] data = base64Decode(base64Data);
            // 创建AES秘钥
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), KEY_ALGORITHM);
            //创建偏移量
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
            // 创建密码器
            Cipher cipher = Cipher.getInstance(CIPHER_CBC_ALGORITHM);
            // 初始化解密器
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv);
            // 执行解密操作
            byte[] result = cipher.doFinal(data);
            return new String(result, StandardCharsets.UTF_8);
        } catch (Exception e) {
            handleException("decrypt", e);
        }
        return null;
    }

    /**
     * AES CTR解密
     *
     * @param secretKey   解密的密钥，长度：16 或 32 个字符
     * @param initVector  初始偏移量
     * @param unZipPath   加密的文件路径
     * @param outFilePath 解密的文件路径
     */
    public static void decrypt(ThreadPoolExecutor executor, byte[] secretKey, byte[] initVector,
                               String unZipPath, String outFilePath, FileCallback callback) {
        try {
            if (initVector.length != blockSize) {
                JLog.i("error iv length");
                callback.onFailed(FileStatus.UNZIP_BACKUP, "错误的参数");
                return;
            }

            // 创建AES秘钥
            final SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, KEY_ALGORITHM);
            final RandomAccessFile src = new RandomAccessFile(unZipPath, "rw");

            //计算整个文件的长度
            long size = src.length();
            src.close();

            //计算整个文件分块处理的次数
            int eachBlockSize = 8 * 1024 * 1024;
            int times;
            int remainder = (int) (size % eachBlockSize);
            if (remainder > 0) {
                times = (int) (size / eachBlockSize + 1);
            } else {
                times = (int) (size / eachBlockSize);
            }

            List<Integer> count = new ArrayList<>();
            for (int i = 0; i < times; i++) {
                writeDecryptData(executor, initVector, i, times, remainder, secretKeySpec, unZipPath, outFilePath, new FileCallback() {
                    @Override
                    public void onSuccess(@NotNull Enum<FileStatus> step) {

                    }

                    @Override
                    public void onProgress(@NotNull Enum<FileStatus> step, int index) {
                        count.add(index);
                        if (count.size() == times) {
                            callback.onSuccess(FileStatus.UNZIP_BACKUP);
                        }
                    }

                    @Override
                    public void onFailed(@NotNull Enum<FileStatus> step, @NotNull String message) {

                    }
                });
            }

        } catch (Exception e) {
            handleException("decrypt", e);
            callback.onFailed(FileStatus.UNZIP_BACKUP, "解压出现错误");
        }

    }

    private static void writeDecryptData(ThreadPoolExecutor executor, byte[] initVector, int j, int times, int remainder,
                                         SecretKeySpec secretKeySpec, String unZipPath, String outFilePath, FileCallback callback) {
        executor.execute(() -> {
            byte[] counter = Arrays.copyOf(initVector, blockSize);
            int eachBlockSize = 8 * 1024 * 1024;
            try {
                RandomAccessFile src = new RandomAccessFile(unZipPath, "rw");
                RandomAccessFile des = new RandomAccessFile(outFilePath, "rw");

                long l = (long) eachBlockSize * (long) j;
                long diff = l / blockSize;
                src.seek(l);
                des.seek(l);

                if (j == 0) {
                    byte[] readBytes = new byte[eachBlockSize];

                    //一次性读取分块的大小
                    src.read(readBytes);

                    // 创建密码器
                    Cipher cipher = Cipher.getInstance(CIPHER_CTR_ALGORITHM);

                    byte[] nonce = Arrays.copyOf(counter, blockSize);
                    for (int i2 = counter.length - 1; i2 >= 0; i2--) {
                        //对count加1
                        if (++counter[i2] != 0) {
                            break;
                        }
                    }

                    //创建初始化向量
                    IvParameterSpec iv = new IvParameterSpec(nonce);

                    // 初始化解密器
                    cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv);

                    //解密
                    byte[] writeBytes = cipher.doFinal(readBytes);


                    //一次性写入到文件
                    des.write(writeBytes);

                    callback.onProgress(FileStatus.UNZIP_BACKUP, j);
                    src.close();
                    des.close();

                } else if (j != times - 1) {
                    byte[] readBytes = new byte[eachBlockSize];

                    // 创建密码器
                    Cipher cipher = Cipher.getInstance(CIPHER_CTR_ALGORITHM);

                    //一次性读取分块的大小
                    src.read(readBytes);

                    //初始化向量值
                    byte[] nonce = Arrays.copyOf(initVector, blockSize);
                    for (int index = 0; index < diff; index++) {
                        for (int i2 = nonce.length - 1; i2 >= 0; i2--) {
                            //对count加1
                            if (++nonce[i2] != 0) {
                                break;
                            }
                        }
                    }

                    byte[] start = Arrays.copyOf(nonce, blockSize);
                    for (int i2 = nonce.length - 1; i2 >= 0; i2--) {
                        //对count加1
                        if (++nonce[i2] != 0) {
                            break;
                        }
                    }

                    //创建初始化向量
                    IvParameterSpec iv = new IvParameterSpec(start);

                    // 初始化解密器
                    cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv);

                    //解密
                    byte[] writeBytes = cipher.doFinal(readBytes);


                    //写入到文件
                    des.write(writeBytes);

                    callback.onProgress(FileStatus.UNZIP_BACKUP, j);

                    src.close();
                    des.close();

                } else {
                    byte[] readBytes = new byte[remainder];

                    src.read(readBytes);

                    // 创建密码器
                    Cipher cipher = Cipher.getInstance(CIPHER_CTR_ALGORITHM);

                    byte[] nonce = Arrays.copyOf(initVector, blockSize);
                    for (int index = 0; index < diff; index++) {
                        for (int i2 = nonce.length - 1; i2 >= 0; i2--) {
                            //对count加1
                            if (++nonce[i2] != 0) {
                                break;
                            }
                        }
                    }

                    byte[] start = Arrays.copyOf(nonce, blockSize);
                    for (int i2 = nonce.length - 1; i2 >= 0; i2--) {
                        //对count加1
                        if (++nonce[i2] != 0) {
                            break;
                        }
                    }

                    //创建初始化向量
                    IvParameterSpec iv = new IvParameterSpec(start);


                    // 初始化解密器
                    cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv);

                    byte[] writeBytes = cipher.doFinal(readBytes);


                    //写入到文件
                    des.write(writeBytes);

                    callback.onProgress(FileStatus.UNZIP_BACKUP, j);

                    src.close();
                    des.close();

                }

            } catch (Exception e) {
                e.printStackTrace();
                callback.onFailed(FileStatus.UNZIP_BACKUP, "解压出现错误");
            }
        });


    }

    /**
     * int转byte（低字节在前）
     *
     * @param n
     * @return
     */
    public static byte[] toLH(int n) {
        byte[] b = new byte[4];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8 & 0xff);
        b[2] = (byte) (n >> 16 & 0xff);
        b[3] = (byte) (n >> 24 & 0xff);
        return b;
    }

    /**
     * int转byte（高字节在前）
     *
     * @param n
     * @return
     */
    public static byte[] toHH(int n) {
        byte[] b = new byte[4];
        b[3] = (byte) (n & 0xff);
        b[2] = (byte) (n >> 8 & 0xff);
        b[1] = (byte) (n >> 16 & 0xff);
        b[0] = (byte) (n >> 24 & 0xff);
        return b;
    }

    /**
     * 将 字节数组 转换成 Base64 编码
     * 用Base64.DEFAULT模式会导致加密的text下面多一行（在应用中显示是这样）
     */
    public static String base64Encode(byte[] data) {
        return Base64.encodeToString(data, Base64.NO_WRAP);
    }

    /**
     * 将 Base64 字符串 解码成 字节数组
     */
    public static byte[] base64Decode(String data) {
        return Base64.decode(data, Base64.NO_WRAP);
    }

    /**
     * 处理异常
     */
    private static void handleException(String methodName, Exception e) {
        e.printStackTrace();
        JLog.e(methodName + "---->" + e);
    }


}
