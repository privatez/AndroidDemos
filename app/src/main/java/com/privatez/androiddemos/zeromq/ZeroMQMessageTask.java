package com.privatez.androiddemos.zeromq;

import android.os.AsyncTask;
import android.os.Handler;

import com.privatez.androiddemos.util.LogHelper;

import org.zeromq.ZMQ;

import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Created by private on 2017/7/19.
 */

public class ZeroMQMessageTask extends AsyncTask<String, Void, String> {
    private final Handler uiThreadHandler;

    public ZeroMQMessageTask(Handler uiThreadHandler) {
        this.uiThreadHandler = uiThreadHandler;
    }

    private String addr = "tcp://10.10.0.135:9091";

    private String command = "blockchain.fetch_last_height";

    @Override
    protected String doInBackground(String... params) {
        LogHelper.log("start");
        String result = "";
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket worker = context.socket(ZMQ.DEALER);
        worker.connect(addr);

        worker.send(command.getBytes(), ZMQ.SNDMORE);
        Random rn = new Random();
        int rand = rn.nextInt();
        byte[] bytes = ByteBuffer.allocate(4).putInt(rand).array();
        worker.send(bytes, ZMQ.SNDMORE);
        worker.send("".getBytes(), 0);

        LogHelper.log("sended");
        byte[] data = worker.recv();
        result = new String(data);

        LogHelper.log("recv" + result);

        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        uiThreadHandler.sendMessage(Util.bundledMessage(uiThreadHandler, result));
    }
}
