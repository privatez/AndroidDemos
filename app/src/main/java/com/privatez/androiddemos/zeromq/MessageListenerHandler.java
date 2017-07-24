package com.privatez.androiddemos.zeromq;

import android.os.Handler;
import android.os.Message;

/**
 * Created by private on 2017/7/19.
 */

public class MessageListenerHandler extends Handler {
    private final IMessageListener messageListener;
    private final String payloadKey;

    public MessageListenerHandler(IMessageListener messageListener, String payloadKey) {
        this.messageListener = messageListener;
        this.payloadKey = payloadKey;
    }

    @Override
    public void handleMessage(Message msg) {
        messageListener.messageReceived(msg.getData().getString(payloadKey));
    }
}
