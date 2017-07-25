package com.privatez.androiddemos.zeromq;

import android.os.AsyncTask;
import android.os.Handler;

import com.privatez.androiddemos.util.LogHelper;

import org.zeromq.ZMQ;

import java.nio.ByteBuffer;
import java.util.Arrays;
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
        LogHelper.log("recv" + data);
        LogHelper.log("recv" + result);
        data = worker.recv();
        result = new String(data);
        LogHelper.log("recv" + data);
        LogHelper.log("recv" + result);
        data = worker.recv();
        result = new String(data);

        Serializer serializer = new Serializer(data);
        int height = serializer.readUnsignedInt();

        LogHelper.log("recv" + data);
        LogHelper.log("recv" + result);
        LogHelper.log("recv" + serializer.readUnsignedInt());


        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        uiThreadHandler.sendMessage(Util.bundledMessage(uiThreadHandler, result));
    }

    private static class Serializer {
        private byte[] data_;
        private int iterator_;
        private int length;

        public Serializer(byte[] bytes) {
            data_ = Arrays.copyOf(bytes, bytes.length);
            iterator_ = 0;
            length = data_.length;
        }

        public int iterator() {
            return iterator_;
        }

        public int readUnsignedInt() throws IndexOutOfBoundsException {
            if (iterator_ >= length)
                throw new IndexOutOfBoundsException();
            int result = (int) data_[iterator_] & 0xFF;
            for (int i = 1; i < 4; i++)
                result += ((int) data_[++iterator_] & 0xFF) << (i * 8);
            iterator_++;
            return result;
        }

        public String readString() throws IndexOutOfBoundsException {
            if (iterator_ >= length)
                throw new IndexOutOfBoundsException();
            int idx = iterator_;
            while (idx < length && 0 <= (data_[idx] & 0xFF) && (data_[idx] & 0xFF) <= 127) {
                idx++;
            }
            String str = idx > iterator_ ? new String(Arrays.copyOfRange(data_, iterator_, idx)) : null;
            iterator_ = idx > iterator_ ? idx : iterator_;
            return str;
        }

        public byte readByte() throws IndexOutOfBoundsException {
            if (iterator_ >= length)
                throw new IndexOutOfBoundsException();
            byte data = data_[iterator_++];
            return data;
        }

        private String readHash(int size) throws IndexOutOfBoundsException {
            if (iterator_ >= length)
                throw new IndexOutOfBoundsException();
            int hashSize = size;
            String hash = new String(Arrays.copyOfRange(data_, iterator_, iterator_ + hashSize));
            iterator_ += hashSize;
            return hash;
        }

        public String readHash() {
            return readHash(32);
        }

        public String readShortHash() {
            return readHash(20);
        }
    }
}
