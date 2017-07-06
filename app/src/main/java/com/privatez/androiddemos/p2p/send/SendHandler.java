package com.privatez.androiddemos.p2p.send;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.privatez.androiddemos.p2p.bean.UserServiceInfo;

/**
 * Created by private on 2017/5/11.
 */

public abstract class SendHandler extends Handler {

    public static final int LOST_SERVICE = 0x001;
    public static final int RESOLVE_SERVICE = 0x002;

    private Context mContext;

    public SendHandler(Context context) {
        mContext = context;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        final int what = msg.what;
        if (LOST_SERVICE == what) {
            lostService((UserServiceInfo) msg.obj);
        } else if (RESOLVE_SERVICE == what) {
            resolveService((UserServiceInfo) msg.obj);
        }
    }

    public abstract void lostService(UserServiceInfo serviceInfo);

    public abstract void resolveService(UserServiceInfo serviceInfo);

}
