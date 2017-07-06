package com.privatez.androiddemos.p2p.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.privatez.androiddemos.R;
import com.privatez.androiddemos.base.BaseActivity;
import com.privatez.androiddemos.p2p.security.AESCipher;
import com.privatez.androiddemos.p2p.security.RSACipher;
import com.privatez.androiddemos.p2p.util.FileDesUtil;
import com.privatez.androiddemos.util.LogHelper;

/**
 * Created by private on 2017/4/28.
 */

public class P2pActivity extends BaseActivity implements View.OnClickListener {

    private TextView tvPush;
    private TextView tvReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p2p);

        tvPush = (TextView) findViewById(R.id.tv_push);
        tvReceiver = (TextView) findViewById(R.id.tv_receiver);

        tvPush.setOnClickListener(this);
        tvReceiver.setOnClickListener(this);

        //file();
        //rsa();
        //aes();
    }

    private void file() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileDesUtil.encrypt("/mnt/sdcard/download/1hash.txt", "/mnt/sdcard/download/2hash.txt");
                FileDesUtil.decrypt("/mnt/sdcard/download/2hash.txt", "/mnt/sdcard/download/3hash.txt");
            }
        }).start();

    }

    private void rsa() {
        RSACipher sendRsa = new RSACipher();

        RSACipher receiveRsa = new RSACipher();

        receiveRsa.setPublicKeyEncoded(sendRsa.getPublicKeyEncoded());

        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < 20; i++) {
            buffer.append(i + "hello world!!!");
        }
        LogHelper.log("content length" + buffer.toString().getBytes().length);
        byte[] enByte = sendRsa.encrypt(buffer.toString());

        byte[] deByte = sendRsa.decrypt(enByte);

        LogHelper.log("de:" + new String(deByte));
    }

    private void aes() {
        AESCipher receive = new AESCipher();

        AESCipher send = new AESCipher();
        send.setKey(receive.getKeyEncoded());
        byte[] enByte = send.encrypt("hello world!!!");

        byte[] deByte = receive.decrypt(enByte);

        LogHelper.log("de:" + new String(deByte));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_push:
                startActivity(new Intent(this, SendActivity.class));
                break;
            case R.id.tv_receiver:
                startActivity(new Intent(this, ReceiveActivity.class));
               /* LogHelper.log("isGPRSOpen" + NetworkUtil.isGPRSOpen(this));
                LogHelper.log("getDataEnabled" + NetworkUtil.getDataEnabled(this));
                if (NetworkUtil.getDataEnabled(this)) {
                    Intent intent = new Intent(Settings.ACTION_SETTINGS);
                    startActivity(intent);
                } else {
                }*/
                break;
            default:
                break;
        }
    }

}
