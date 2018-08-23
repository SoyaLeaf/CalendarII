package top.soyask.calendarii.utils;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

public class PermissionUtils {

    public static boolean checkSelfPermission(Activity activity, String permission, int requestCode) {
        boolean had = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(new String[]{ permission }, requestCode);
                had = false;
            }
        }
        return had;
    }

    public static boolean checkSelfPermission(Fragment fragment, String permission, int requestCode) {
        boolean had = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (fragment.getActivity().checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                fragment.requestPermissions(new String[]{permission}, requestCode);
                had = false;
            }
        }
        return had;
    }

    public static boolean handleResults(String[] permissions, int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static void manual(Activity activity) {
        new AlertDialog.Builder(activity)
                .setMessage("你拒绝授予权限，该功能将无法正常使用...")
                .setPositiveButton("手动授权", (dialog, which) -> {
                    Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri);
                    activity.startActivity(intent);
                })
                .setNegativeButton("不使用该功能", null)
                .show();
    }
}
