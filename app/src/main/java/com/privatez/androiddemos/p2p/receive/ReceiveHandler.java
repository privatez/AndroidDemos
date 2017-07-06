package com.privatez.androiddemos.p2p.receive;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

/**
 * Created by private on 2017/5/11.
 */

public class ReceiveHandler extends Handler {

    public static final int USER_NAME = 0x001;

    private Context mContext;

    public ReceiveHandler(Context context) {
        mContext = context;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        int what = msg.what;
        if (USER_NAME == what) {
            Toast.makeText(mContext, String.valueOf(msg.obj), Toast.LENGTH_SHORT).show();
        }
    }

}
