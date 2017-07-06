package com.privatez.androiddemos.p2p.security;

import com.privatez.androiddemos.util.LogHelper;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;

/**
 * Created by private on 2017/5/11.
 */

public class RSACipher extends BaseCipher {

    //一次性加密数据的长度不能大于117 字节
    private static final int ENCRYPT_BLOCK_MAX = 117;
    //一次性解密的数据长度不能大于128 字节
    private static final int DECRYPT_BLOCK_MAX = 128;

    //秘钥算法
    private static final String KEY_ALGORITHM = "RSA";
    //加密算法：algorithm/mode/padding 算法/工作模式/填充模式
    private static final String CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding";

    private KeyPair mKeyPair;

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

        mKeyPair = KeyPairGenerator.getInstance(algorithm).generateKeyPair();
    }

    @Override
    public byte[] encrypt(byte[] content) {

        try {
            return encryptByPublicKeyForSpilt(content, mKeyPair.getPublic().getEncoded());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public byte[] decrypt(byte[] content) {

        try {
            return decryptByPrivateKeyForSpilt(content, mKeyPair.getPrivate().getEncoded());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

   /* @Override
    protected byte[] doFinal(byte[] content, int opmode, Key key) {
        try {
            mCipher.init(opmode, key);
            return doFinalWithBatch(content, ENCRYPT_BLOCK_MAX);
        } catch (Exception e) {
            LogHelper.log("doFinal :" + e);
        }
        return null;
    }*/

    public void setPublicKeyEncoded(byte[] publicKeyEncoded) {

        PublicKey publicK = mKeyPair.getPublic();

        try {
            KeyFactory keyFactory = KeyFactory.getInstance(getKeyAlgorithm());
            publicK = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyEncoded));
        } catch (GeneralSecurityException e) {
            isGeneralKeyError = true;
            LogHelper.log("setPublicKeyEncoded GeneralSecurityException:" + e);
        }

        mKeyPair = new KeyPair(publicK, null);
    }

    public byte[] getPublicKeyEncoded() {

        return mKeyPair.getPublic().getEncoded();
    }

    public static final byte[] DEFAULT_SPLIT = "#PART#".getBytes();

    /**
     * 用公钥对字符串进行分段加密
     */
    public static byte[] encryptByPublicKeyForSpilt(byte[] data, byte[] publicKey) throws Exception {
        int dataLen = data.length;
        if (dataLen <= ENCRYPT_BLOCK_MAX) {
            return encryptByPublicKey(data, publicKey);
        }
        List<Byte> allBytes = new ArrayList<>(2048);
        int bufIndex = 0;
        int subDataLoop = 0;
        byte[] buf = new byte[ENCRYPT_BLOCK_MAX];
        for (int i = 0; i < dataLen; i++) {
            buf[bufIndex] = data[i];
            if (++bufIndex == ENCRYPT_BLOCK_MAX || i == dataLen - 1) {
                subDataLoop++;
                if (subDataLoop != 1) {
                    for (byte b : DEFAULT_SPLIT) {
                        allBytes.add(b);
                    }
                }
                byte[] encryptBytes = encryptByPublicKey(buf, publicKey);
                for (byte b : encryptBytes) {
                    allBytes.add(b);
                }
                bufIndex = 0;
                if (i == dataLen - 1) {
                    buf = null;
                } else {
                    buf = new byte[Math.min(ENCRYPT_BLOCK_MAX, dataLen - i - 1)];
                }
            }
        }
        byte[] bytes = new byte[allBytes.size()];
        int i = 0;
        for (Byte b : allBytes) {
            bytes[i++] = b.byteValue();
        }
        return bytes;
    }

    /**
     * 用公钥对字符串进行加密
     *
     * @param data 原文
     */
    public static byte[] encryptByPublicKey(byte[] data, byte[] publicKey) throws Exception {
        // 得到公钥
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey keyPublic = kf.generatePublic(keySpec);
        // 加密数据
        Cipher cp = Cipher.getInstance("RSA");
        cp.init(Cipher.ENCRYPT_MODE, keyPublic);
        return cp.doFinal(data);
    }

    /**
     * 使用私钥分段解密
     */
    public static byte[] decryptByPrivateKeyForSpilt(byte[] encrypted, byte[] privateKey) throws Exception {
        int splitLen = DEFAULT_SPLIT.length;
        if (splitLen <= 0) {
            return decryptByPrivateKey(encrypted, privateKey);
        }
        int dataLen = encrypted.length;
        List<Byte> allBytes = new ArrayList<>(1024);
        int latestStartIndex = 0;
        for (int i = 0; i < dataLen; i++) {
            byte bt = encrypted[i];
            boolean isMatchSplit = false;
            if (i == dataLen - 1) {
                // 到data的最后了
                byte[] part = new byte[dataLen - latestStartIndex];
                System.arraycopy(encrypted, latestStartIndex, part, 0, part.length);
                byte[] decryptPart = decryptByPrivateKey(part, privateKey);
                for (byte b : decryptPart) {
                    allBytes.add(b);
                }
                latestStartIndex = i + splitLen;
                i = latestStartIndex - 1;
            } else if (bt == DEFAULT_SPLIT[0]) {
                // 这个是以split[0]开头
                if (splitLen > 1) {
                    if (i + splitLen < dataLen) {
                        // 没有超出data的范围
                        for (int j = 1; j < splitLen; j++) {
                            if (DEFAULT_SPLIT[j] != encrypted[i + j]) {
                                break;
                            }
                            if (j == splitLen - 1) {
                                // 验证到split的最后一位，都没有break，则表明已经确认是split段
                                isMatchSplit = true;
                            }
                        }
                    }
                } else {
                    // split只有一位，则已经匹配了
                    isMatchSplit = true;
                }
            }
            if (isMatchSplit) {
                byte[] part = new byte[i - latestStartIndex];
                System.arraycopy(encrypted, latestStartIndex, part, 0, part.length);
                byte[] decryptPart = decryptByPrivateKey(part, privateKey);
                for (byte b : decryptPart) {
                    allBytes.add(b);
                }
                latestStartIndex = i + splitLen;
                i = latestStartIndex - 1;
            }
        }
        byte[] bytes = new byte[allBytes.size()];
        {
            int i = 0;
            for (Byte b : allBytes) {
                bytes[i++] = b.byteValue();
            }
        }
        return bytes;
    }

    /**
     * 使用私钥进行解密
     */
    public static byte[] decryptByPrivateKey(byte[] encrypted, byte[] privateKey) throws Exception {
        // 得到私钥
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey keyPrivate = kf.generatePrivate(keySpec);

        // 解密数据
        Cipher cp = Cipher.getInstance("RSA");
        cp.init(Cipher.DECRYPT_MODE, keyPrivate);
        byte[] arr = cp.doFinal(encrypted);
        return arr;
    }

}
