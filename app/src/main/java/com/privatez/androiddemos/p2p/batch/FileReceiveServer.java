package com.privatez.androiddemos.p2p.batch;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by private on 2017/5/16.
 */

public class FileReceiveServer {
    protected int listenPort;
    private int maxConnection;

    public FileReceiveServer(int listenPort, int maxConnection) {
        this.listenPort = listenPort;
        this.maxConnection = maxConnection;
    }

    /**
     * 建立监听端口
     */
    public void setUpConnection() {
        for (int i = 0; i < maxConnection; i++) {
            new Thread(new FileReceiverHandle(), "handle" + i).start();
        }


    }

    /**
     * 接收文件客户端的连接
     */
    public void acceptConnection() {
        try {
            ServerSocket server = new ServerSocket(listenPort);
            Socket incomingSocket = null;
            while (true) {
                incomingSocket = server.accept();
                handlerConnection(incomingSocket);
            }
        } catch (BindException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * @param incomingSocket 处理接收到的连接（socket）
     */
    private void handlerConnection(Socket incomingSocket) {
        FileReceiverHandle.processConnection(incomingSocket);

    }

    public static void main(String[] args) {
        FileReceiveServer fileReceiveServer = new FileReceiveServer(3000, 5);
        fileReceiveServer.setUpConnection();
        fileReceiveServer.acceptConnection();
    }
}
