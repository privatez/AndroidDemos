package com.privatez.androiddemos;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.privatez.androiddemos.bitcoin.BitCoinActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivity(new Intent(this, BitCoinActivity.class));
        //InstallUtil.installApk(this);
/*        String apkPath = "/mnt/sdcard/Download/app-release.apk";
        Signature signature1 = SignatureUtil.getPackageSignature(this, this.getPackageName());
        Signature signature2 = SignatureUtil.getUninstallApkSignature(this, apkPath);

        LogHelper.log(signature1.equals(signature2) + "");
        Toast.makeText(this, signature1.equals(signature2) + "", Toast.LENGTH_SHORT).show();
        LogHelper.log("1: " + signature1.toCharsString());
        LogHelper.log("1: " + MD5.getMessageDigest(signature1.toByteArray()));
        LogHelper.log("2: " + signature2.toCharsString());
        LogHelper.log("2: " + MD5.getMessageDigest(signature2.toByteArray()));*/
    }

}
