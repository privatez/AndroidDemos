package com.privatez.androiddemos.p2p.helper;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.privatez.androiddemos.p2p.util.WifiApUtil;

import java.util.UUID;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by private on 2017/5/4.
 */

public class WifiApHelper {

    private static final int NO_CONNECTION_NETWORK = -1;

    private String mSSID;

    private int mConnectionNetworkId = NO_CONNECTION_NETWORK;

    private final Context mContext;

    private final WifiManager mWifiManager;

    public WifiApHelper(Context context) {
        mContext = context;
        mSSID = generateSSID(mContext);
        mWifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
    }

    public void createWifiAp() {
        createWifiAp(mSSID);
    }

    public void createWifiAp(String SSID) {
        if (mWifiManager.isWifiEnabled()) {
            mConnectionNetworkId = mWifiManager.getConnectionInfo().getNetworkId();
            mWifiManager.setWifiEnabled(false);
        }
        WifiApUtil.createWifiAp(mWifiManager, SSID);
    }

    private String generateSSID(Context context) {
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        final String deviceId = tm.getDeviceId();
        final String androidId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        final UUID deviceUuid = new UUID(androidId.hashCode(),
                ((long) deviceId.hashCode() << 32) | Build.DEVICE.hashCode());

        return deviceUuid.toString().substring(19);
    }

    public void closeWifiAp() {
        WifiApUtil.closeWifiAp(mWifiManager);
        if (mConnectionNetworkId != NO_CONNECTION_NETWORK) {
            mWifiManager.enableNetwork(mConnectionNetworkId, false);
        }
    }

}
