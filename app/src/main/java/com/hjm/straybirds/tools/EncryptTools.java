package com.hjm.straybirds.tools;

import android.support.annotation.NonNull;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by hejunming on 2018/3/24.
 */

public class EncryptTools {

    //MD5
    public static String generateMD5(@NonNull String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] result = digest.digest(content.getBytes());
            return Hex.encode(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    //秘钥算法
    private static final String KEY_ALGORITHM = "AES";
    //加密算法：algorithm/mode/padding 算法/工作模式/填充模式
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    //秘钥 这只是测试密匙,实际使用不能硬编码在代码中
    //private static final String KEY = "12345678";//DES 秘钥长度必须是8 位或以上
    private static final String KEY = "1234567890123456";//AES 秘钥长度必须是16 位

    public static String encodeAES(String password) {
        //初始化秘钥
        SecretKey secretKey = new SecretKeySpec(KEY.getBytes(), KEY_ALGORITHM);
        Cipher cipher = null;
        //AES、DES 在CBC 操作模式下需要iv 参数
        IvParameterSpec iv = new IvParameterSpec(KEY.getBytes());
        try {
            cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            //加密
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            byte[] result = cipher.doFinal(password.getBytes());
            return Hex.encode(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decodeAES(String encodeStr) {
        //初始化秘钥
        SecretKey secretKey = new SecretKeySpec(KEY.getBytes(), KEY_ALGORITHM);
        Cipher cipher = null;
        //AES、DES 在CBC 操作模式下需要iv 参数
        IvParameterSpec iv = new IvParameterSpec(KEY.getBytes());
        try {
            cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            //加密
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            byte[] result = cipher.doFinal(Hex.decode(encodeStr));
            return new String(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
