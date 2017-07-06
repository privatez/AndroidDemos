package com.privatez.androiddemos.p2p.batch;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by private on 2017/5/16.
 */

public class FilesChannelServer {
    private String fileName = null;
    private long fileSize = 0L;
    private int hostPort;
    private ServerSocketChannel ssl = null;
    private SocketChannel clientChannel = null;
    private ByteBuffer buffer = null;

    public FilesChannelServer(int port) {
        this.hostPort = port;
    }

    private void setUpConnection() {
        try {
            ssl = ServerSocketChannel.open();
            SocketAddress address = new InetSocketAddress("127.0.0.1", hostPort);
            ssl.socket().bind(address);
            System.out.println("bind.....");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseHead(int headlength) {
        buffer = ByteBuffer.allocate(headlength);
        try {
            while (buffer.position() < buffer.capacity())
                clientChannel.read(buffer);
            buffer.flip();
            byte[] filenamebyte = new byte[buffer.getInt()];
            buffer.get(filenamebyte);
            fileName = new String(filenamebyte, "UTF-8");
            fileSize = buffer.getLong();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseBody(long size) {
        long read = 0L;
        long count = 8192;
        FileChannel fileChannel = null;
        try {
            fileChannel = new FileOutputStream("D:\\receive\\" + fileName)
                    .getChannel();
            System.out.println(fileName);
            while (read < size) {
                if (size - read < count)
                    count = size - read;
                read += fileChannel
                        .transferFrom(clientChannel, 0 + read, count);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fileChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int parse() {
        int i = 0;
        try {
            buffer = ByteBuffer.allocate(4);
            while (buffer.position() < buffer.capacity())
                clientChannel.read(buffer);
            buffer.flip();
            parseHead(buffer.getInt());
            parseBody(fileSize);
            buffer = ByteBuffer.allocate(1);
            while (buffer.position() < buffer.capacity())
                clientChannel.read(buffer);
            buffer.flip();
            i = buffer.get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return i;
    }

    private void acceptConnection() {
        while (true) {
            try {
                clientChannel = ssl.accept();
                int j = 1;
                while (j != 0) {
                    j = parse();
                    System.out.println(j);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

                try {
                    if (clientChannel != null)
                        clientChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }
    }

    public static void main(String[] args) {
        FilesChannelServer cs = new FilesChannelServer(3000);
        cs.setUpConnection();
        cs.acceptConnection();
    }
}
