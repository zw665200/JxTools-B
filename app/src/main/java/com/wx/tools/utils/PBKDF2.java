package com.wx.tools.utils;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PBKDF2 {
    public static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";


    public static final int SALT_BYTE_SIZE = 32;         //盐的长度
    public static final int PBKDF2_ITERATIONS = 5000;        //迭代次数


    /**
     * @auther: Ragty
     * @describe: 对输入的密码进行验证
     * @param: [attemptedPassword(待验证密码), encryptedPassword(密文), salt(盐值)]
     * @return: boolean
     * @date: 2018/11/2
     */
//    public boolean authenticate(String attemptedPassword, String encryptedPassword, String salt)
//            throws NoSuchAlgorithmException, InvalidKeySpecException {
//        // 用相同的盐值对用户输入的密码进行加密
//        String encryptedAttemptedPassword = getEncryptedPassword(attemptedPassword, salt);
//        // 把加密后的密文和原密文进行比较，相同则验证成功，否则失败
//        return encryptedAttemptedPassword.equals(encryptedPassword);
//    }


    /**
     *
     * @param password 密码
     * @param salt 盐
     * @return key
     * @throws NoSuchAlgorithmException 加密数据异常
     * @throws InvalidKeySpecException 无效的key异常
     */
    public byte[] getEncryptedPassword(String password, byte[] salt) throws NoSuchAlgorithmException,
            InvalidKeySpecException {

        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, 256);
        SecretKeyFactory f = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
        return f.generateSecret(spec).getEncoded();
    }


    /**
     * @auther: Ragty
     * @describe: 通过加密的强随机数生成盐(最后转换为16进制)
     * @param: []
     * @return: java.lang.String
     * @date: 2018/11/2
     */
    public String generateSalt() throws NoSuchAlgorithmException {
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[SALT_BYTE_SIZE];
        random.nextBytes(salt);

        return toHex(salt);
    }


    /**
     * @auther: Ragty
     * @describe: 十六进制字符串转二进制字符串
     * @param: [hex]
     * @return: byte[]
     * @date: 2018/11/2
     */
    private static byte[] fromHex(String hex) {
        byte[] binary = new byte[hex.length() / 2];
        for (int i = 0; i < binary.length; i++) {
            binary[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return binary;
    }


    /**
     * @auther: Ragty
     * @describe: 二进制字符串转十六进制字符串
     * @param: [array]
     * @return: java.lang.String
     * @date: 2018/11/2
     */
    private static String toHex(byte[] array) {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if (paddingLength > 0)
            return String.format("%0" + paddingLength + "d", 0) + hex;
        else
            return hex;
    }

}