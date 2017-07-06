package com.privatez.androiddemos.p2p.batch;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by private on 2017/5/16.
 */

public class FileReceiverHandle implements Runnable {
    private static List<Socket> socketsPool = new LinkedList<Socket>();
    private Socket connection = null;

    /**
     * @param incomingSocket 降接收到的连接放到一个池中
     */
    public static void processConnection(Socket incomingSocket) {
        synchronized (socketsPool) {
            socketsPool.add(socketsPool.size(), incomingSocket);
            socketsPool.notifyAll();
        }
    }

    /**
     * 按zip流解码获取接收到的流
     */
    public void handlerConnection() {
        InputStream inputFromSocket = null;
        BufferedOutputStream fileWriter = null;
        BufferedInputStream socketReader = null;
        byte[] buff = new byte[8192];
        int c = 0;
        try {
            inputFromSocket = connection.getInputStream();
            ZipInputStream zis = new ZipInputStream(inputFromSocket);
            socketReader = new BufferedInputStream(zis);
            ZipEntry e = null;
            while ((e = zis.getNextEntry()) != null) {
                System.out.println(e.getName());
                fileWriter = new BufferedOutputStream(new FileOutputStream(
                        new File("D:\\receive\\" + e.getName())));
                while ((c = socketReader.read(buff)) != -1) {
                    fileWriter.write(buff, 0, c);
                }
                try {
                    fileWriter.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socketReader != null) {
                    socketReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    /*
     * 处理池中的连接
     */
    public void run() {
        while (true) {
            synchronized (socketsPool) {
                while (socketsPool.isEmpty()) {
                    try {
                        socketsPool.wait();
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                connection = socketsPool.remove(socketsPool.size() - 1);
            }
            handlerConnection();
        }
    }
}
