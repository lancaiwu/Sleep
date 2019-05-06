package com.lancaiwu.sleep.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

public class PerUtils {

    public static boolean checkNetPer(Context context,String packageName){
        PackageManager pm = context.getPackageManager();
        boolean permission = (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission("android.permission.INTERNET", packageName));
        if (permission) {
            Log.e("lanc",packageName+"有网络权限");
            return true;
        }else {
            Log.e("lanc",packageName+"没有网络权限");
            return false;
        }

//            try {
//                PackageInfo pack = pm.getPackageInfo("packageName", PackageManager.GET_PERMISSIONS);
//                        String[] permissionStrings = pack.requestedPermissions;
//                Log.e("lanc", "权限清单--->" + permissionStrings.toString());
//            } catch (PackageManager.NameNotFoundException e) {
//                e.printStackTrace();
//            }
    }
}
