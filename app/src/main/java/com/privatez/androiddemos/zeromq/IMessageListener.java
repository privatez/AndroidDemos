package com.privatez.androiddemos.zeromq;

/**
 * Created by private on 2017/7/19.
 */

public interface IMessageListener {
    void messageReceived(String messageBody);
}
