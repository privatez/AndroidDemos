package com.privatez.androiddemos.p2p.batch;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by private on 2017/5/16.
 */

public class FileSendClient {
    protected String hostIP;
    protected int hostPort;
    protected OutputStream socketInS;
    private Socket client;

    public FileSendClient(String ip, int portNumber) {
        this.hostIP = ip;
        this.hostPort = portNumber;
    }

    /**
     * 建立连接
     */
    public void setUpConnection() {
        try {
            client = new Socket(hostIP, hostPort);
            socketInS = client.getOutputStream();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e.toString());
        } catch (IOException e) {
            throw new RuntimeException(e.toString());
        }

    }

    /**
     * @param files 用zip流发送多个文件
     */
    public void sendFile(File[] files) {
        BufferedInputStream fileReader = null;
        ZipOutputStream zos = new ZipOutputStream(socketInS);
        BufferedOutputStream socketWriter = new BufferedOutputStream(zos);
        byte[] buff = new byte[8192];
        int c = 0;
        try {
            for (File file : files) {
                fileReader = new BufferedInputStream(new FileInputStream(file));
                zos.putNextEntry(new ZipEntry(file.getName()));
                while ((c = fileReader.read(buff)) != -1) {
                    socketWriter.write(buff, 0, c);
                }
                socketWriter.flush();
                try {
                    if (fileReader != null)
                        fileReader.close();
                } catch (IOException e) {
                    System.out.println("Error closing fileReader" + e);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socketWriter != null)
                    socketWriter.close();
            } catch (IOException e) {
                System.out.println("Error closing socketWriter" + e);
            } finally {
                if (client != null && client.isConnected()) {
                    try {
                        client.close();
                    } catch (IOException e) {
                        System.out.println("Error closing socket" + e);
                    }
                }
            }

        }

    }

    public static void main(String[] args) {
        FileSendClient fileSendClient = new FileSendClient("127.0.0.1", 3000);
        fileSendClient.setUpConnection();
        File[] files = new File("D:\\send").listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        });
        fileSendClient.sendFile(files);

    }
}
