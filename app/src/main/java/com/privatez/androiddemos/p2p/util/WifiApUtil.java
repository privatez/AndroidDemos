package com.privatez.androiddemos.p2p.util;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import com.privatez.androiddemos.util.LogHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by private on 2017/4/28.
 */

public class WifiApUtil {

    public static final int TYPE_NO_PWD = 0x11;
    public static final int TYPE_WEP = 0x12;
    public static final int TYPE_WPA = 0x13;


    private WifiApUtil() {
    }

    public static boolean createWifiAp(WifiManager wifiManager, String SSID) {
        Method method1;
        boolean ret = false;
        try {
            method1 = wifiManager.getClass().getMethod("setWifiApEnabled",
                    WifiConfiguration.class, boolean.class);
            WifiConfiguration apConfig = createWifiInfo(wifiManager, SSID);
            ret = (Boolean) method1.invoke(wifiManager, apConfig, true);
        } catch (InvocationTargetException e) {
            LogHelper.log("此处接收被调用方法内部未被捕获的异常");
            Throwable t = e.getTargetException();// 获取目标异常
            t.printStackTrace();
            LogHelper.log(t.toString());
            LogHelper.log(t.getLocalizedMessage());
        } catch (Exception e) {
            LogHelper.log("createWifiAp" + e);
        }
        return ret;
    }

    public static void closeWifiAp(WifiManager wifiManager) {
        try {
            Method method = wifiManager.getClass().getMethod("getWifiApConfiguration");
            method.setAccessible(true);

            WifiConfiguration config = (WifiConfiguration) method.invoke(wifiManager);

            Method method2 = wifiManager.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method2.invoke(wifiManager, config, false);
        } catch (Exception e) {
            LogHelper.log("closeWifiAp" + e);
        }
    }

    public static boolean isWifiApEnabled(WifiManager wifiManager) {
        try {
            Method method = wifiManager.getClass().getMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifiManager);
        } catch (Exception e) {
            LogHelper.log("isWifiApEnabled" + e);
        }

        return false;
    }

    public static WifiConfiguration createWifiInfo(WifiManager wifiManager, String SSID) {
        return createWifiInfo(wifiManager, SSID, null, TYPE_NO_PWD);
    }


    public static WifiConfiguration createWifiInfo(WifiManager wifiManager, String SSID, String password, int type) {

        LogHelper.log("SSID = " + SSID + "## Password = " + password + "## Type = " + type);

        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = SSID;

        WifiConfiguration tempConfig = isExsits(wifiManager, SSID);

        if (tempConfig != null) {
            wifiManager.removeNetwork(tempConfig.networkId);
        }

        // 分为三种情况：1没有密码2用wep加密3用wpa加密
        if (type == TYPE_NO_PWD) {// WIFICIPHER_NOPASS
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == TYPE_WEP) {  //  WIFICIPHER_WEP
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == TYPE_WPA) {   // WIFICIPHER_WPA
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }

        return config;
    }

    private static WifiConfiguration isExsits(WifiManager wifiManager, String SSID) {
        List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();

        if (existingConfigs == null || existingConfigs.size() == 0) {
            return null;
        }

        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"") /*&& existingConfig.preSharedKey.equals("\"" + password + "\"")*/) {
                return existingConfig;
            }
        }
        return null;
    }


}

