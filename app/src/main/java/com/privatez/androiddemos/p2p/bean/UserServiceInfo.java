package com.privatez.androiddemos.p2p.bean;

import android.net.nsd.NsdServiceInfo;

/**
 * Created by private on 2017/5/11.
 */

public class UserServiceInfo {

    public static final String NAME_SEPARATOR = "-/-";

    public NsdServiceInfo mNsdServiceInfo;

    public UserServiceInfo(NsdServiceInfo nsdServiceInfo) {
        mNsdServiceInfo = nsdServiceInfo;
    }

    public String getDisplayName() {

        return mNsdServiceInfo.getServiceName().split(NAME_SEPARATOR)[1];
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UserServiceInfo) {
            NsdServiceInfo serviceInfo = ((UserServiceInfo) obj).mNsdServiceInfo;
            if (mNsdServiceInfo.getServiceName().equals(serviceInfo.getServiceName())) {
                return true;
            }
        }
        return super.equals(obj);
    }

}
