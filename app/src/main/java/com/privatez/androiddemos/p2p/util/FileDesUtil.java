package com.privatez.androiddemos.p2p.util;

import android.support.annotation.NonNull;

import com.privatez.androiddemos.util.LogHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by private on 2017/5/17.
 */

public class FileDesUtil {
    //秘钥算法
    private static final String KEY_ALGORITHM = "AES";
    //加密算法：algorithm/mode/padding 算法/工作模式/填充模式
    private static final String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";

    public static final byte[] KEY = {56, 57, 58, 59, 60, 61, 62, 63, 56, 57, 58, 59, 60, 61, 62, 63};//DES 秘钥长度必须是8 位或以上

    public static void encrypt(String fromFilePath, String toFilePath) {
        encrypt(fromFilePath, toFilePath, KEY);
    }

    /**
     * 文件进行加密并保存加密后的文件到指定目录
     *
     * @param fromFilePath 要加密的文件 如c:/test/待加密文件.txt
     * @param toFilePath   加密后存放的文件 如c:/加密后文件.txt
     */
    public static void encrypt(String fromFilePath, String toFilePath, byte[] key) {
        LogHelper.log("encrypting...");

        File fromFile = new File(fromFilePath);
        if (!fromFile.exists()) {
            LogHelper.log("to be encrypt file no exist!");
            return;
        }

        File toFile = getFile(toFilePath);

        SecretKey secretKey = new SecretKeySpec(key, KEY_ALGORITHM);
        InputStream is = null;
        OutputStream out = null;
        CipherInputStream cis = null;
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            is = new FileInputStream(fromFile);
            out = new FileOutputStream(toFile);
            cis = new CipherInputStream(is, cipher);
            byte[] buffer = new byte[1024];
            int r;
            while ((r = cis.read(buffer)) > 0) {
                out.write(buffer, 0, r);
            }
        } catch (Exception e) {
            LogHelper.log(e.toString());
        } finally {
            try {
                if (cis != null) {
                    cis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        LogHelper.log("encrypt completed");
    }

    @NonNull
    private static File getFile(String filePath) {
        File fromFile = new File(filePath);
        if (!fromFile.getParentFile().exists()) {
            fromFile.getParentFile().mkdirs();
        }
        return fromFile;
    }

    public static void decrypt(String fromFilePath, String toFilePath) {
        decrypt(fromFilePath, toFilePath, KEY);
    }


    /**
     * 文件进行解密并保存解密后的文件到指定目录
     *
     * @param fromFilePath 已加密的文件 如c:/加密后文件.txt
     * @param toFilePath   解密后存放的文件 如c:/ test/解密后文件.txt
     */
    public static void decrypt(String fromFilePath, String toFilePath, byte[] key) {
        LogHelper.log("decrypting...");

        File fromFile = new File(fromFilePath);
        if (!fromFile.exists()) {
            LogHelper.log("to be decrypt file no exist!");
            return;
        }
        File toFile = getFile(toFilePath);

        SecretKey secretKey = new SecretKeySpec(key, KEY_ALGORITHM);

        InputStream is = null;
        OutputStream out = null;
        CipherOutputStream cos = null;
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            is = new FileInputStream(fromFile);
            out = new FileOutputStream(toFile);
            cos = new CipherOutputStream(out, cipher);
            byte[] buffer = new byte[1024];
            int r;
            while ((r = is.read(buffer)) >= 0) {
                cos.write(buffer, 0, r);
            }
        } catch (Exception e) {
            LogHelper.log(e.toString());
        } finally {
            try {
                if (cos != null) {
                    cos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        LogHelper.log("decrypt completed");
    }
}
