package com.privatez.androiddemos.delete;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;

import com.privatez.androiddemos.util.LogHelper;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by private on 2017/4/24.
 */

public class Wifi {

    private WifiP2pManager mWifiP2pManager;

    private ServerSocket mServerSocket;
    private WifiP2pManager.Channel mChannel;
    private int mLocalPort;

    private Context mContext;

    public Wifi(Context context) {
        init(context.getApplicationContext());
        mContext = context.getApplicationContext();
    }

    private void init(Context context) {
        mWifiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mWifiP2pManager.initialize(context, context.getMainLooper(), null);
        initializeServerSocket();
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
        record.put("listenport", String.valueOf(mLocalPort));
        record.put("buddyname", "John Doe" + (int) (Math.random() * 1000));
        record.put("available", "visible");

        // Service information.  Pass it an instance name, service type
        // _protocol._transportlayer , and the map containing
        // information other devices will want once they connect to this one.
        WifiP2pDnsSdServiceInfo serviceInfo =
                WifiP2pDnsSdServiceInfo.newInstance("_test", "_presence._tcp", record);

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
                LogHelper.log("startRegistration onFailure");
            }
        });


        NsdServiceInfo nsdServiceInfo = new NsdServiceInfo();

        // The name is subject to change based on conflicts
        // with other services advertised on the same network.
        nsdServiceInfo.setServiceName("NsdChat");
        nsdServiceInfo.setServiceType("_http._tcp.");
        nsdServiceInfo.setPort(mLocalPort);

        NsdManager.RegistrationListener mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                // Save the service name.  Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                LogHelper.log(NsdServiceInfo.getServiceName());
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Registration failed!  Put debugging code here to determine why.
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                // Service has been unregistered.  This only happens when you call
                // NsdManager.unregisterService() and pass in this listener.
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Unregistration failed.  Put debugging code here to determine why.
            }
        };

        NsdManager mNsdManager = (NsdManager) mContext.getSystemService(Context.NSD_SERVICE);

        mNsdManager.registerService(nsdServiceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
    }

    final HashMap<String, String> buddies = new HashMap<>();

    public void discoverService() {
        DnsSdTxtRecordListener txtListener = new DnsSdTxtRecordListener() {
            @Override
        /* Callback includes:
         * fullDomain: full domain name: e.g "printer._ipp._tcp.local."
         * record: TXT record dta as a map of key/value pairs.
         * device: The device running the advertised service.
         */

            public void onDnsSdTxtRecordAvailable(
                    String fullDomain, Map record, WifiP2pDevice device) {
                buddies.put(device.deviceAddress, (String) record.get("buddyname"));
                LogHelper.log("DnsSdTxtRecord available -" + record.toString());
                LogHelper.log(String.valueOf(record.get("buddyname")));
            }
        };

        DnsSdServiceResponseListener servListener = new DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType,
                                                WifiP2pDevice resourceType) {

                // Update the device name with the human-friendly version from
                // the DnsTxtRecord, assuming one arrived.
                resourceType.deviceName = buddies
                        .containsKey(resourceType.deviceAddress) ? buddies
                        .get(resourceType.deviceAddress) : resourceType.deviceName;


                LogHelper.log("onBonjourServiceAvailable " + instanceName);
            }
        };

        mWifiP2pManager.setDnsSdResponseListeners(mChannel, servListener, txtListener);

        WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        mWifiP2pManager.addServiceRequest(mChannel,
                serviceRequest,
                new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        // Success!
                        LogHelper.log("addServiceRequest onSuccess");
                    }

                    @Override
                    public void onFailure(int code) {
                        LogHelper.log("addServiceRequest onFailure" + code);
                        // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                    }
                });

        mWifiP2pManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        // Success!
                        LogHelper.log("discoverServices onSuccess");
                    }

                    @Override
                    public void onFailure(int code) {
                        // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                        LogHelper.log("discoverServices onFailure" + code);
                        if (code == WifiP2pManager.P2P_UNSUPPORTED) {
                            LogHelper.log("P2P isn't supported on this device.");
                        }
                    }
                }
        );
    }

}
