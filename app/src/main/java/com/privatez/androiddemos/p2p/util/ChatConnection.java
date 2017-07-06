/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.privatez.androiddemos.p2p.util;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.privatez.androiddemos.p2p.security.RSACipher;
import com.privatez.androiddemos.util.LogHelper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ChatConnection {

    private Handler mUpdateHandler;
    private ChatServer mChatServer;
    private ChatClient mChatClient;

    private static final String TAG = "ChatConnection";

    private Socket mSocket;
    private int mPort = -1;

    private Context mContext;

    public ChatConnection(Handler handler) {
        mUpdateHandler = handler;
        mChatServer = new ChatServer(handler);
    }

    public ChatConnection(Handler handler, Context context) {
        this(handler);
        mContext = context;
    }

    public void tearDown() {
        mChatServer.tearDown();
        if (mChatClient != null) {
            mChatClient.tearDown();
        }
    }

    public void connectToServer(InetAddress address, int port) {
        mChatClient = new ChatClient(address, port);
    }

    public void sendMessage(String msg) {
        if (mChatClient != null) {
            mChatClient.sendMessage(msg);
        }
    }

    public int getLocalPort() {
        return mPort;
    }

    public void setLocalPort(int port) {
        mPort = port;
        LogHelper.log("setLocalPort" + port);
    }


    public synchronized void updateMessages(String msg, boolean local) {
        LogHelper.log("Updating message: " + msg);

        if (local) {
            msg = "me: " + msg;
        } else {
            msg = "them: " + msg;
        }

        Bundle messageBundle = new Bundle();
        messageBundle.putString("msg", msg);

        Message message = new Message();
        message.setData(messageBundle);
        mUpdateHandler.sendMessage(message);
    }

    private synchronized void setSocket(Socket socket) {
        LogHelper.log("setSocket being called.");
        if (socket == null) {
            LogHelper.log("Setting a null socket.");
        }
        if (mSocket != null) {
            if (mSocket.isConnected()) {
                try {
                    mSocket.close();
                } catch (IOException e) {
                    // TODO(alexlucas): Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        mSocket = socket;
    }

    private Socket getSocket() {
        return mSocket;
    }

    private class ChatServer {
        ServerSocket mServerSocket = null;
        Thread mThread = null;

        public ChatServer(Handler handler) {
            mThread = new Thread(new ServerThread());
            mThread.start();
        }

        public void tearDown() {
            mThread.interrupt();
            if (mServerSocket != null) {
                try {
                    mServerSocket.close();
                } catch (IOException ioe) {
                    LogHelper.log("Error when closing server socket.");
                }
            }
        }

        class ServerThread implements Runnable {

            @Override
            public void run() {

                BufferedReader input = null;

                try {
                    // Since discovery will happen via Nsd, we don't need to care which port is
                    // used.  Just grab an available one  and advertise it via Nsd.
                    mServerSocket = new ServerSocket(0);
                    setLocalPort(mServerSocket.getLocalPort());
                    mUpdateHandler.sendEmptyMessage(110);

                    Socket socket = mServerSocket.accept();
                    LogHelper.log("accept" + socket.toString());
                    input = new BufferedReader(new InputStreamReader(
                            socket.getInputStream()));
                    while (true) {
                        String messageStr = null;
                        messageStr = input.readLine();
                        if (messageStr != null) {
                            LogHelper.log("Read from the stream: " + messageStr);
                            if (messageStr.equals("1hash-1")) {
                                PrintWriter out = new PrintWriter(
                                        new BufferedWriter(
                                                new OutputStreamWriter(socket.getOutputStream())), true);
                                out.println("fuck u");
                                out.flush();
                            }
                            //updateMessages(messageStr, false);
                        } else {
                            LogHelper.log("The nulls! The nulls!");
                            break;
                        }
                    }
                } catch (IOException e) {
                    LogHelper.log("Error creating ServerSocket: " + e);
                    e.printStackTrace();
                } finally {
                    if (input != null) {
                        try {
                            LogHelper.log("inputStream close");
                            input.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private class ChatClient {

        private InetAddress mAddress;
        private int PORT;

        private final String CLIENT_TAG = "ChatClient";

        private Thread mSendThread;
        private Thread mRecThread;

        public ChatClient(InetAddress address, int port) {

            LogHelper.log("Creating chatClient");
            this.mAddress = address;
            this.PORT = port;

            mSendThread = new Thread(new SendingThread());
            mSendThread.start();
        }

        class SendingThread implements Runnable {

            BlockingQueue<String> mMessageQueue;
            private int QUEUE_CAPACITY = 10;

            private RSACipher mRSACipher;

            public SendingThread() {
                mRSACipher = new RSACipher();
                mMessageQueue = new ArrayBlockingQueue<String>(QUEUE_CAPACITY);
            }

            @Override
            public void run() {
                Socket socket = null;
                BufferedInputStream inputStream = null;
                BufferedOutputStream outputStream = null;

                BufferedWriter bufferedWriter = null;
                try {
                    socket = new Socket(mAddress, PORT);
                    inputStream = new BufferedInputStream(socket.getInputStream());
                    outputStream = new BufferedOutputStream(socket.getOutputStream());
                    if (getSocket() == null) {
                        setSocket(new Socket(mAddress, PORT));
                        LogHelper.log("Client-side socket initialized.");

                    } else {
                        LogHelper.log("Socket already initialized. skipping!");
                    }

                    mRecThread = new Thread(new ReceivingThread());
                    mRecThread.start();

                  /*  PrintWriter out = new PrintWriter(
                            new BufferedWriter(
                                    new OutputStreamWriter(socket.getOutputStream())), true);
                    out.println(new String(mRSACipher.getPublicKeyEncoded()));
                    out.flush();*/
                    //LogHelper.log("Client sent message: " + "1hash-1");

                    outputStream = new BufferedOutputStream(socket.getOutputStream());
                    outputStream.write(mRSACipher.getPublicKeyEncoded());
                    outputStream.flush();

                    LogHelper.log("Client sent key: ");

                    byte[] key = new byte[256];
                    while (inputStream.read(key) != -1) {
                        String messageStr = null;
                        LogHelper.log("Read from the stream: " + new String(mRSACipher.decrypt(key)));
                        if (messageStr != null) {
                            //updateMessages(messageStr, false);
                        } else {
                            LogHelper.log("The nulls! The nulls!");
                        }
                    }

                } catch (UnknownHostException e) {
                    LogHelper.log("Initializing socket failed, UHE");
                } catch (IOException e) {
                    LogHelper.log("Initializing socket failed, IOE.");
                }
            }
        }

        class ReceivingThread implements Runnable {

            @Override
            public void run() {

                BufferedReader input;
                try {
                    input = new BufferedReader(new InputStreamReader(
                            mSocket.getInputStream()));
                    while (!Thread.currentThread().isInterrupted()) {

                        String messageStr = null;
                        messageStr = input.readLine();
                        if (messageStr != null) {
                            LogHelper.log("Read from the stream: " + messageStr);
                            updateMessages(messageStr, false);
                        } else {
                            LogHelper.log("The nulls! The nulls!");
                            break;
                        }
                    }
                    input.close();

                } catch (IOException e) {
                    Log.e(CLIENT_TAG, "Server loop error: ");
                }
            }
        }

        public void tearDown() {
            if (getSocket() != null) {
                try {
                    getSocket().close();
                } catch (IOException ioe) {
                    Log.e(CLIENT_TAG, "Error when closing server socket.");
                }
            }
        }

        public void sendMessage(String msg) {
            try {
                Socket socket = getSocket();
                if (socket == null) {
                    LogHelper.log("Socket is null, wtf?");
                } else if (socket.getOutputStream() == null) {
                    LogHelper.log("Socket output stream is null, wtf?");
                }

                PrintWriter out = new PrintWriter(
                        new BufferedWriter(
                                new OutputStreamWriter(getSocket().getOutputStream())), true);
                out.println(msg);
                out.flush();
                updateMessages(msg, true);
            } catch (UnknownHostException e) {
                LogHelper.log("Unknown Host");
            } catch (IOException e) {
                LogHelper.log("I/O Exception");
            } catch (Exception e) {
                LogHelper.log("Error3" + e.getMessage());
            }
            LogHelper.log("Client sent message: " + msg);
        }
    }
}
