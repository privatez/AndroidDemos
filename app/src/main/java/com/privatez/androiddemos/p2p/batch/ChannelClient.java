package com.privatez.androiddemos.p2p.batch;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by private on 2017/5/16.
 */

public class ChannelClient {

    private SocketChannel sc;
    private String hostIp;
    private int hostPort;

    public ChannelClient(String hostIp, int port) {
        this.hostIp = hostIp;
        this.hostPort = port;
    }

    private void setUpConnection() {
        try {
            SocketAddress remote = new InetSocketAddress(hostIp, hostPort);
            sc = SocketChannel.open();
            sc.connect(remote);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendFiles(File[] files) {
        FileChannel fileChannel = null;
        try {
            for (int i = 0; i < files.length; i++) {
                byte[] namebyte = files[i].getName().getBytes("UTF-8");
                long size = files[i].length();
                int nameLength = namebyte.length;
                fileChannel = new FileInputStream(files[i]).getChannel();
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                buffer.clear();
                buffer.putInt(4 + 8 + nameLength);
                buffer.putInt(nameLength);
                buffer.put(namebyte);
                buffer.putLong(size);
                buffer.flip();
                while (buffer.hasRemaining()) {
                    sc.write(buffer);
                }
                long count = 1024 * 1024;
                long read = 0L;
                while (read < size) {
                    if (size - read < count)
                        count = size - read;
                    read += fileChannel.transferTo(0 + read, count, sc);
                    System.out.println("read:" + read);
                }
                fileChannel.close();
                if (i < files.length - 1) {
                    sc.write(ByteBuffer.wrap(new byte[]{1}));
                    System.out.println(1);
                } else
                    sc.write(ByteBuffer.wrap(new byte[]{0}));
            }

            sc.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ChannelClient client = new ChannelClient("127.0.0.1", 3000);
        client.setUpConnection();
        File[] files = new File("D:\\send").listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        });
        client.sendFiles(files);
    }
}
