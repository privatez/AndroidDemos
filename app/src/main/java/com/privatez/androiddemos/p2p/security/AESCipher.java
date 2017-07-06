package com.privatez.androiddemos.p2p.security;

import com.privatez.androiddemos.p2p.util.FileDesUtil;
import com.privatez.androiddemos.util.LogHelper;

import java.security.GeneralSecurityException;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by private on 2017/5/12.
 */

public class AESCipher extends BaseCipher {

    private SecretKey mSecretKey;

    //秘钥算法
    private static final String KEY_ALGORITHM = "AES";
    //加密算法：algorithm/mode/padding 算法/工作模式/填充模式
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

    @Override
    protected String getKeyAlgorithm() {
        return KEY_ALGORITHM;
    }

    @Override
    protected String getCipherAlgorithm() {
        return CIPHER_ALGORITHM;
    }

    @Override
    protected void generateSecreteKey(String algorithm) throws GeneralSecurityException {

        mSecretKey = KeyGenerator.getInstance(algorithm).generateKey();
    }

    @Override
    public byte[] encrypt(byte[] content) {

        return doFinal(content, Cipher.ENCRYPT_MODE, mSecretKey);
    }

    @Override
    public byte[] decrypt(byte[] content) {

        return doFinal(content, Cipher.DECRYPT_MODE, mSecretKey);
    }

    @Override
    protected byte[] doFinal(byte[] content, int opmode, Key key) {
        if (isGeneralKeyError || content == null || content.length == 0) {
            return null;
        }

        byte[] result = null;

        try {
            IvParameterSpec ivParameterSpec = new IvParameterSpec(FileDesUtil.KEY);
            mCipher.init(opmode, key, ivParameterSpec);
            result = mCipher.doFinal(content);
        } catch (GeneralSecurityException e) {
            LogHelper.log("doFinal GeneralSecurityException:  " + e);
        }

        LogHelper.log("opmode:" + opmode + "....doFinal result string:" + new String(result));
        return result;
    }

    public boolean setKey(String key) {
        return setKey(key.getBytes());
    }

    /**
     * key 的长度必须是 16/24/32 中的一个
     *
     * @param key
     */
    public boolean setKey(byte[] key) {
        if (key.length == 16 || key.length == 24 || key.length == 32) {
            mSecretKey = new SecretKeySpec(key, getKeyAlgorithm());
            return true;
        }

        LogHelper.log("setKey error :key 的长度必须是 16/24/32 中的一个");
        return false;
    }

    public byte[] getKeyEncoded() {

        return mSecretKey.getEncoded();
    }

}
