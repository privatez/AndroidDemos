package com.privatez.androiddemos.p2p.helper;

import com.privatez.androiddemos.p2p.bean.ContentInfo;
import com.privatez.androiddemos.p2p.security.AESCipher;
import com.privatez.androiddemos.p2p.security.RSACipher;
import com.privatez.androiddemos.p2p.send.SendHandler;
import com.privatez.androiddemos.p2p.util.Constant;
import com.privatez.androiddemos.p2p.util.FileDesUtil;
import com.privatez.androiddemos.util.LogHelper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by private on 2017/5/16.
 */

public class FileSendHelper {

    private Thread mSendThread;

    private SendHandler mSendHandler;

    public FileSendHelper(SendHandler sendHandler) {
        mSendHandler = sendHandler;
    }

    public void connectToServer(int serverPort, InetAddress serverAddress) {
        mSendThread = new Thread(new SendingThread(serverPort, serverAddress));
        mSendThread.start();
    }

    public static class SendingThread implements Runnable {

        private RSACipher mRSACipher;
        private AESCipher mAESCipher;

        private BufferedInputStream mInputStream = null;
        private BufferedOutputStream mOutputStream = null;

        private int mServerPort;
        private InetAddress mServerAddress;

        private Socket mSocket = null;

        public SendingThread(int serverPort, InetAddress serverAddress) {
            mServerPort = serverPort;
            mServerAddress = serverAddress;
            mRSACipher = new RSACipher();
            mAESCipher = new AESCipher();
        }

        @Override
        public void run() {
            try {
                mSocket = new Socket(mServerAddress, mServerPort);
                mInputStream = new BufferedInputStream(mSocket.getInputStream());
                mOutputStream = new BufferedOutputStream(mSocket.getOutputStream());

                output(Constant.Schem.SEND_RSA_KEY, mRSACipher.getPublicKeyEncoded());

                byte[] header = new byte[ContentHandle.HEADER_SIZE];
                while (mInputStream.read(header) != -1) {
                    final ContentInfo contentInfo = ContentHandle.parse(header);
                    final String schem = contentInfo.schem;
                    final long size = contentInfo.size;
                    LogHelper.log("send schem: " + schem);
                    if (Constant.Schem.SEND_AES_KEY.equals(schem)) {
                        byte[] key = readAESKey((int) size);
                        mAESCipher.setKey(key);
                        newSendFile();
                        //sendFile();
                    }
                }
            } catch (IOException e) {
                LogHelper.log("Initializing socket failed, IOE." + e);
            } finally {
                closeStream(mInputStream, mOutputStream);
            }
        }

        private void newSendFile() throws IOException{
            FileDesUtil.encrypt("/mnt/sdcard/download/1hash.txt", "/mnt/sdcard/download/2hash.txt", mAESCipher.getKeyEncoded());

            File file = new File("/mnt/sdcard/download/2hash.txt");

            FileInputStream fileInputStream = new FileInputStream(file);

            byte[] header = ContentHandle.generate(Constant.Schem.SEND_NORMAL, file.length());
            mOutputStream.write(header);

            byte[] buff = new byte[1024];
            int len;
            while ((len = fileInputStream.read(buff)) != -1) {
                mOutputStream.write(buff, 0, len);
            }

            mOutputStream.flush();
        }

        private void sendFile(byte[] key) throws IOException {
            File file = new File("/mnt/sdcard/download/1hash.txt");

            FileInputStream fileInputStream = new FileInputStream(file);

            Cipher cipher = null;

            try {
                cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                SecretKey secretKey = new SecretKeySpec(key, "AES");
                IvParameterSpec iv = new IvParameterSpec(key);
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            } catch (GeneralSecurityException e) {
                LogHelper.log("sendFile e:" + e);
            }

            CipherOutputStream cipherOutputStream = new CipherOutputStream(mOutputStream, cipher);

            byte[] buff = new byte[1024];
            int len;
            while ((len = fileInputStream.read(buff)) != -1) {
                cipherOutputStream.write(buff, 0, len);
            }

            cipherOutputStream.close();
            LogHelper.log("send:....");
        }

        private void output(String schem, byte[] content) throws IOException {
            byte[] header = ContentHandle.generate(schem, content.length);

            mOutputStream.write(header);
            mOutputStream.write(content);
            mOutputStream.flush();
        }

        private byte[] readAESKey(int size) throws IOException {
            byte[] key = new byte[size];
            mInputStream.read(key);

            return key;
        }

        private void closeStream(InputStream inputStream, OutputStream outputStream) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void tearDown() {
            if (mSocket != null) {
                try {
                    mSocket.close();
                } catch (IOException ioe) {
                    LogHelper.log("Error when closing server socket.");
                }
            }
        }
    }

}
