package net.nanocosmos.nanoStream.demo.simpleEncoder.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.Manifest;

import java.util.ArrayList;

/**
 * Created by nanocosmos GmbH (c) 2015 - 2016
 */
public class CheckAppPermissions {
    private Activity activity = null;
    ArrayList<String> missingPermissions;
    public static final int requestPermissionCode = 23;

    public CheckAppPermissions(Activity activity) {
        this.activity = activity;
        missingPermissions = new ArrayList<String>();
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean checkCameraPermissions() {
        boolean chk = activity.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        if(!chk) {
            missingPermissions.add(Manifest.permission.CAMERA);
        }
        return chk;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean checkRecordAudioPermission() {
        boolean chk = activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        if(!chk) {
            missingPermissions.add(Manifest.permission.RECORD_AUDIO);
        }
        return chk;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean checkWriteExternalStoragePermission() {
        boolean chk = activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if(!chk) {
            missingPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        return chk;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestMissingPermissions() {
        if(!missingPermissions.isEmpty()) {
            activity.requestPermissions(missingPermissions.toArray(new String[]{}), requestPermissionCode);
        }
    }
}
