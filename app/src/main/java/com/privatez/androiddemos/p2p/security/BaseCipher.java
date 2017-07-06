package com.privatez.androiddemos.p2p.security;

import com.privatez.androiddemos.util.LogHelper;

import java.security.GeneralSecurityException;
import java.security.Key;

import javax.crypto.Cipher;

/**
 * Created by private on 2017/5/12.
 */

public abstract class BaseCipher {

    protected Cipher mCipher;

    protected boolean isGeneralKeyError;

    public BaseCipher() {
        init(getCipherAlgorithm(), getKeyAlgorithm());
    }

    protected void init(String cipherAlgorithm, String keyAlgorithm) {
        try {
            mCipher = Cipher.getInstance(cipherAlgorithm);
            generateSecreteKey(keyAlgorithm);
        } catch (GeneralSecurityException e) {
            isGeneralKeyError = true;
            LogHelper.log("init GeneralSecurityException:  " + e);
        }
    }

    public byte[] encrypt(String content) {

        return encrypt(content.getBytes());
    }

    public byte[] decrypt(String content) {

        return decrypt(content.getBytes());
    }

    /**
     * 加解密
     *
     * @param content 内容
     * @param opmode  模式 Cipher.ENCRYPT_MODE / Cipher.DECRYPT_MODE
     * @param key     秘钥
     * @return
     */
    protected byte[] doFinal(byte[] content, int opmode, Key key) {
        if (isGeneralKeyError || content == null || content.length == 0) {
            return null;
        }

        byte[] result = null;

        try {
            mCipher.init(opmode, key);
            result = mCipher.doFinal(content);
        } catch (GeneralSecurityException e) {
            LogHelper.log("doFinal GeneralSecurityException:  " + e);
        }

        LogHelper.log("opmode:" + opmode + "....doFinal result string:" + new String(result));
        return result;
    }

    /**
     * 指定加解密类型
     * eg. RSA/AES/DES
     *
     * @return
     */
    protected abstract String getKeyAlgorithm();

    /**
     * 指定加解密类型
     * eg. RSA/AES/DES
     *
     * @return
     */
    protected abstract String getCipherAlgorithm();

    /**
     * 生成秘钥
     *
     * @param transformation
     */
    protected abstract void generateSecreteKey(String transformation) throws GeneralSecurityException;

    /**
     * 加密
     *
     * @param content
     * @return
     */
    public abstract byte[] encrypt(byte[] content);

    /**
     * 解密
     *
     * @param content
     * @return
     */
    public abstract byte[] decrypt(byte[] content);

}
