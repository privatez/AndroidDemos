/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.privatez.androiddemos.p2p.helper;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Message;

import com.privatez.androiddemos.p2p.bean.UserServiceInfo;
import com.privatez.androiddemos.p2p.send.SendHandler;
import com.privatez.androiddemos.util.LogHelper;

public class NsdHelper {

    private static final String SERVICE_TYPE = "_http._tcp.";

    private Context mContext;

    private NsdManager mNsdManager;
    private NsdManager.ResolveListener mResolveListener;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdManager.RegistrationListener mRegistrationListener;

    private SendHandler mSendHandler;

    private String mPackageName;

    private boolean mIsDiscovering;

    public NsdHelper(Context context) {
        this(context, null);
    }

    public NsdHelper(Context context, SendHandler sendHandler) {
        mContext = context;
        mSendHandler = sendHandler;
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        mPackageName = mContext.getPackageName();
        initializeNsd();
    }

    public void initializeNsd() {
        initializeResolveListener();
        initializeDiscoveryListener();
        initializeRegistrationListener();
    }

    private void initializeDiscoveryListener() {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
                LogHelper.log("Service discovery started");
                mIsDiscovering = true;
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                LogHelper.log("onServiceFound" + service.toString());
                if (service.getServiceType().equals(SERVICE_TYPE) && service.getServiceName().startsWith(mPackageName)) {
                    mNsdManager.resolveService(service, mResolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                LogHelper.log("service lost" + service);
                sendMessage(SendHandler.LOST_SERVICE, new UserServiceInfo(service));
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                LogHelper.log("Discovery stopped: " + serviceType);
                mIsDiscovering = false;
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                LogHelper.log("onStartDiscoveryFailed: Error code:" + errorCode);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                LogHelper.log("onStopDiscoveryFailed: Error code:" + errorCode);
            }
        };
    }

    private void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                LogHelper.log("Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                LogHelper.log("Resolve Succeeded. " + serviceInfo);
                sendMessage(SendHandler.RESOLVE_SERVICE, new UserServiceInfo(serviceInfo));
            }
        };
    }

    private void sendMessage(int what, UserServiceInfo serviceInfo) {
        Message message = new Message();
        message.what = what;
        message.obj = serviceInfo;
        mSendHandler.sendMessage(message);
    }

    private void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {

            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            }

        };
    }

    public void registerService(String displayName, int port) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setPort(port);
        serviceInfo.setServiceName(generateServiceName(mPackageName, displayName));
        serviceInfo.setServiceType(SERVICE_TYPE);
        mNsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
    }

    private String generateServiceName(String prefix, String displayName) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(prefix);
        stringBuffer.append(UserServiceInfo.NAME_SEPARATOR);
        stringBuffer.append(displayName);
        return stringBuffer.toString();
    }

    public void unregisterService() {
        mNsdManager.unregisterService(mRegistrationListener);
    }

    public void discoverServices() {
        if (mIsDiscovering) {
            return;
        }
        mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    public void stopDiscovery() {
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
    }

}
