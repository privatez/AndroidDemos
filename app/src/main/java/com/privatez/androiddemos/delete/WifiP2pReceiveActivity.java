package com.privatez.androiddemos.delete;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by private on 2017/4/24.
 */

public class WifiP2pReceiveActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Wifi wifi = new Wifi(this);
        wifi.startRegistration();
    }
}
