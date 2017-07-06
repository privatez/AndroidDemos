package com.privatez.androiddemos.p2p.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.privatez.androiddemos.base.BaseActivity;
import com.privatez.androiddemos.p2p.helper.FileReceiveHelper;
import com.privatez.androiddemos.p2p.helper.NsdHelper;
import com.privatez.androiddemos.p2p.helper.WifiApHelper;
import com.privatez.androiddemos.p2p.receive.ReceiveHandler;

/**
 * Created by private on 2017/4/24.
 */

public class ReceiveActivity extends BaseActivity {

    private WifiApHelper mWifiApHelper;

    private FileReceiveHelper mFileReceiveHelper;

    private NsdHelper mNsdHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNsdHelper = new NsdHelper(mContext);
        mWifiApHelper = new WifiApHelper(mContext);
        mWifiApHelper.createWifiAp();
        mFileReceiveHelper = new FileReceiveHelper(new ReceiveHandler(mContext),
                new FileReceiveHelper.OnServerSocketListener() {

            @Override
            public void onSocketOpen(int port) {
                mNsdHelper.registerService(generateName(), port);
            }
        });

    }

    private String generateName() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("private");
        int position = (int) (Math.random() * 10 + 1);
        stringBuffer.append(String.valueOf(position));
        return stringBuffer.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFileReceiveHelper.tearDown();
        mWifiApHelper.closeWifiAp();
        mNsdHelper.unregisterService();
    }

}
