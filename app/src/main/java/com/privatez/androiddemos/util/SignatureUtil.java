package com.privatez.androiddemos.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.util.DisplayMetrics;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

/**
 * Created by private on 2017/7/7.
 */

public class SignatureUtil {

    /**
     * 获取当前 apk 的签名
     *
     * @param context
     * @param packageName
     * @return
     */
    public static Signature getPackageSignature(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> apps = pm.getInstalledPackages(PackageManager.GET_SIGNATURES);
        Iterator<PackageInfo> it = apps.iterator();
        while (it.hasNext()) {
            PackageInfo info = it.next();
            if (info.packageName.equals(packageName)) {
                return info.signatures[0];
            }
        }

        return null;
    }

    /**
     * 获取 apk 文件的签名
     *
     * @param context
     * @param apkPath apk 文件路径
     * @return
     */
    public static Signature getUninstallApkSignature(Context context, String apkPath) {

        try {
            Class clazz = Class.forName("android.content.pm.PackageParser");
            Object packageParser = getParserObject(clazz);

            Object packag = getPackage(context, clazz, packageParser, apkPath);

            Method collectCertificatesMethod = clazz.getMethod("collectCertificates",
                    Class.forName("android.content.pm.PackageParser$Package"), int.class);
            collectCertificatesMethod.invoke(packageParser, packag, PackageManager.GET_SIGNATURES);
            Signature signatures[] = (Signature[]) packag.getClass().getField("mSignatures").get(packag);

            return signatures.length > 0 ? signatures[0] : null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Object getParserObject(Class clazz) throws Exception {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
                clazz.getConstructor().newInstance() :
                clazz.getConstructor(String.class).newInstance("");
    }

    private static Object getPackage(Context c, Class clazz, Object instance, String apkPath) throws Exception {
        Object pkg = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Method method = clazz.getMethod("parsePackage", File.class, int.class);
            pkg = method.invoke(instance, new File(apkPath), 0x0004);
        } else {
            Method method = clazz.getMethod("parsePackage", File.class, String.class, DisplayMetrics.class, int.class);
            pkg = method.invoke(instance, new File(apkPath), null, c.getResources().getDisplayMetrics(), 0x0004);
        }

        return pkg;
    }

}
