package com.privatez.androiddemos.delete;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;

import com.privatez.androiddemos.util.LogHelper;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by private on 2017/4/24.
 */

public class WifiP2pServer {

    private WifiP2pManager mWifiP2pManager;

    private ServerSocket mServerSocket;
    private WifiP2pManager.Channel mChannel;
    private int mLocalPort;

    private Context mContext;

    public WifiP2pServer(Context context) {
        mContext = context.getApplicationContext();
        init(context);
    }

    private void init(Context context) {
        mWifiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mWifiP2pManager.initialize(context, context.getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                LogHelper.log("onChannelDisconnected");
            }
        });
        initializeServerSocket();

        try {
            Method method1 = mWifiP2pManager.getClass().getMethod("enableP2p", WifiP2pManager.Channel.class);
            method1.invoke(mWifiP2pManager, mChannel);
            //Toast.makeText(getActivity(), "method found",
            //       Toast.LENGTH_SHORT).show();
            LogHelper.log("method found");
        } catch (Exception e) {
            //Toast.makeText(getActivity(), "method did not found",
            //   Toast.LENGTH_SHORT).show();
            LogHelper.log("method not found");
        }
    }

    public void initializeServerSocket() {
        // Initialize a server socket on the next available port.
        try {
            mServerSocket = new ServerSocket(0);
        } catch (IOException e) {
            LogHelper.log("initializeServerSocket " + e.getMessage());
            e.printStackTrace();
        }

        // Store the chosen port.
        mLocalPort = mServerSocket.getLocalPort();
    }

    public void startRegistration() {
        //  Create a string map containing information about your service.
        Map record = new HashMap();
        LogHelper.log("port:" + mLocalPort);
        record.put("listenport", String.valueOf(mLocalPort));
        record.put("buddyname", "John Doe" + (int) (Math.random() * 1000));
        record.put("available", "visible");

        // Service information.  Pass it an instance name, service type
        // _protocol._transportlayer , and the map containing
        // information other devices will want once they connect to this one.
        WifiP2pDnsSdServiceInfo serviceInfo =
                WifiP2pDnsSdServiceInfo.newInstance("_test_wifip2p", "_presence._tcp", record);

        // Add the local service, sending the service info, network channel,
        // and listener that will be used to indicate success or failure of
        // the request.
        mWifiP2pManager.addLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // Command successful! Code isn't necessarily needed here,
                // Unless you want to update the UI or add logging statements.
                LogHelper.log("startRegistration onSuccess");
            }

            @Override
            public void onFailure(int arg0) {
                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                LogHelper.log("startRegistration onFailure:" + arg0);
            }
        });
    }

}
