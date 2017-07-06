package com.privatez.androiddemos.install;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import java.io.File;

/**
 * Created by private on 2017/6/6.
 */

public class InstallUtil {

    private static final String INTENT_TYPE_INSTALL_APK = "application/vnd.android.package-archive";

    public static void installApk(Context context) {
        String apk = "/mnt/sdcard/download/1hash.apk";
        String fileProviderName = "com.privatez.androiddemos.download";
        installApk(context, apk, fileProviderName);
    }

    public static void installApk(Context context, String apkPath, String fileProviderName) {
        File file = new File(apkPath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            uri = FileProvider.getUriForFile(context, fileProviderName, file);
        } else {
            uri = Uri.fromFile(file);
        }

        intent.setDataAndType(uri, INTENT_TYPE_INSTALL_APK);
        context.startActivity(intent);
    }

}
