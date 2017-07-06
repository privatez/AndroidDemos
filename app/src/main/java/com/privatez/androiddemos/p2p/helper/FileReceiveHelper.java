package com.privatez.androiddemos.p2p.helper;

import com.privatez.androiddemos.p2p.bean.ContentInfo;
import com.privatez.androiddemos.p2p.receive.ReceiveHandler;
import com.privatez.androiddemos.p2p.security.AESCipher;
import com.privatez.androiddemos.p2p.security.RSACipher;
import com.privatez.androiddemos.p2p.util.Constant;
import com.privatez.androiddemos.p2p.util.FileDesUtil;
import com.privatez.androiddemos.util.LogHelper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static com.privatez.androiddemos.util.LogHelper.log;

/**
 * Created by private on 2017/5/11.
 */

public class FileReceiveHelper {

    private ReceiveHandler mReceiveHandler;

    private ServerSocket mServerSocket;

    private ExecutorService mExecutorService;

    private OnServerSocketListener mOnServerSocketListener;

    public FileReceiveHelper(ReceiveHandler receiveHandler, OnServerSocketListener listener) {
        mReceiveHandler = receiveHandler;
        mOnServerSocketListener = listener;
        mExecutorService = Executors.newFixedThreadPool(6);
        openServerSocket();
    }

    private void openServerSocket() {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mServerSocket = new ServerSocket(0);
                    if (mOnServerSocketListener != null) {
                        mOnServerSocketListener.onSocketOpen(mServerSocket.getLocalPort());
                    }

                    while (true) {
                        Socket socket = mServerSocket.accept();
                        mExecutorService.execute(new ServerHandleRunnable(socket, mReceiveHandler));
                    }
                } catch (IOException e) {
                    log("onSocketOpen exception " + e);
                    e.printStackTrace();
                }
            }
        });
    }

    public void tearDown() {
        if (mServerSocket != null) {
            try {
                mServerSocket.close();
            } catch (IOException e) {
                log("tearDown exception " + e);
                e.printStackTrace();
            }
        }
        mExecutorService.shutdownNow();
    }

    private static class ServerHandleRunnable implements Runnable {

        private Socket mSocket;

        private ReceiveHandler mReceiveHandler;

        private RSACipher mRSACipher;
        private AESCipher mAESCipher;

        private InputStream mInputStream = null;
        private OutputStream mOutputStream = null;

        public ServerHandleRunnable(Socket socket, ReceiveHandler receiveHandler) {
            mSocket = socket;
            mReceiveHandler = receiveHandler;
            mRSACipher = new RSACipher();
            mAESCipher = new AESCipher();
        }

        @Override
        public void run() {
            try {
                mInputStream = new BufferedInputStream(mSocket.getInputStream());
                mOutputStream = new BufferedOutputStream(mSocket.getOutputStream());

                byte[] header = new byte[ContentHandle.HEADER_SIZE];

                while (mInputStream.read(header) != -1) {
                    final ContentInfo contentInfo = ContentHandle.parse(header);
                    final String schem = contentInfo.schem;
                    final long size = contentInfo.size;
                    LogHelper.log("receive header: " + new String(header));
                    LogHelper.log("receive schem: " + schem);
                    LogHelper.log("receive size: " + size);

                    if (Constant.Schem.SEND_RSA_KEY.equals(schem)) {
                        sendAESKey((int) size);
                    } else if (Constant.Schem.SEND_NORMAL.equals(schem)) {
                        newReceiveFile();
                    }
                }
            } catch (IOException e) {
                log("Error creating ServerSocket: " + e);
            } finally {
                closeStream(mInputStream, mOutputStream);
            }
        }

        private void newReceiveFile() throws IOException {
            File file = new File("/mnt/sdcard/download/1hash.txt");
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();

            FileOutputStream fileOutputStream = new FileOutputStream(file);

            byte[] buff = new byte[1024];
            int len;
            /*while ((len = ) != -1) {
                LogHelper.log("receive len:" + len);
                LogHelper.log("receive:" + new String(buff));
            }*/
            LogHelper.log(Thread.currentThread().getState() + "");
            len = mInputStream.read(buff);
            LogHelper.log(Thread.currentThread().getState() + "");
            fileOutputStream.write(buff, 0, len);
            LogHelper.log(Thread.currentThread().getState() + "");

            LogHelper.log("111");
            fileOutputStream.flush();
            LogHelper.log("222");
            FileDesUtil.decrypt("/mnt/sdcard/download/1hash.txt", "/mnt/sdcard/download/2hash.txt", mAESCipher.getKeyEncoded());
        }

        private void receiveFile() throws IOException {
            File file = new File("/mnt/sdcard/download/1hash.txt");
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();

            FileOutputStream fileOutputStream = new FileOutputStream(file);

            Cipher cipher = null;

            try {
                cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                SecretKey secretKey = new SecretKeySpec(FileDesUtil.KEY, "AES");
                IvParameterSpec iv = new IvParameterSpec(FileDesUtil.KEY);
                cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            } catch (GeneralSecurityException e) {
                LogHelper.log("receiveFile e:" + e);
            }

            CipherInputStream cipherInputStream = new CipherInputStream(mInputStream, cipher);
            byte[] buff = new byte[1024];
            int len;
            while ((len = cipherInputStream.read(buff)) != -1) {
                LogHelper.log("receive len:" + len);
                LogHelper.log("receive:" + new String(buff));
                fileOutputStream.write(buff, 0, len);
            }
            fileOutputStream.flush();
        }

        private void sendAESKey(int size) throws IOException {
            byte[] key = new byte[size];
            mInputStream.read(key);
            mRSACipher.setPublicKeyEncoded(key);
            output(Constant.Schem.SEND_AES_KEY, mAESCipher.getKeyEncoded());
        }

        private void output(String schem, byte[] content) throws IOException {
            byte[] header = ContentHandle.generate(schem, content.length);

            mOutputStream.write(header);
            mOutputStream.write(content);
            mOutputStream.flush();
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

    }

    public interface OnServerSocketListener {
        void onSocketOpen(int port);
    }

}
