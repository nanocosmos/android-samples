package net.nanocosmos.bintu.demo.encoder;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import net.nanocosmos.nanoStream.streamer.DeviceProperties;
import net.nanocosmos.nanoStream.streamer.Logging;
import net.nanocosmos.nanoStream.streamer.nanoStream;

/**
 * Created by nanocosmos GmbH (c) 2015 - 2016
 */
public class BintuApplication extends Application {
    private static final String TAG = "BintuApplication";

    private static final String ANDROID_API     = "Android_API";
    private static final String APP_VERSION     = "App_Version";
    private static final String CHECK_VERSION   = "Check_Version";
    private static final String CHECK_RESULT    = "Check_Result";

    private static Context context;
    private static DeviceProperties deviceProp = null;

    @Override
    public void onCreate() {
        super.onCreate();

        Thread chkThread = new Thread(new Runnable()
        {

            @Override
            public void run()
            {
            try
            {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                int curApiVer = Build.VERSION.SDK_INT;
                int curAppVer = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
                int curChkVer = DeviceProperties.VERSION;

                int oldApiVer = prefs.getInt(ANDROID_API, 0);
                int oldAppVer = prefs.getInt(APP_VERSION, 0);
                int oldChkVer = prefs.getInt(CHECK_VERSION, 0);
                int oldChkResult = prefs.getInt(CHECK_RESULT, -1);

                // run the check thread only one of following variables change.
                // We don't need to run the check every time the app starts, because
                // the returned value wil be the same every time.
                if (((oldApiVer * oldAppVer * oldApiVer) == 0)
                    || (oldApiVer < curApiVer)
                    || (oldAppVer < curAppVer)
                    || (oldChkVer < curChkVer)
                    || oldChkResult < 0)
                {

                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putInt(ANDROID_API, curApiVer);
                    edit.putInt(APP_VERSION, curAppVer);

                    /* new Encoder Test Run */
                    try
                    {

                        deviceProp = nanoStream.getDeviceProperties();

                        edit.putInt(CHECK_RESULT, deviceProp.getFlags());
                        edit.putInt(CHECK_VERSION, deviceProp.getVersion());
                        edit.apply();
                    } catch (RuntimeException e)
                    {
                        Logging.log(Logging.LogLevel.INFO, TAG, "Device check failed: " + e.toString());
                        edit.putInt(CHECK_RESULT, -1);
                        edit.putInt(CHECK_VERSION, 0);
                        edit.apply();
                    }
                } else
                {
                    // use the old device check values.
                    deviceProp = new DeviceProperties(oldChkResult);
                }
                Logging.log(Logging.LogLevel.INFO, TAG, "Device Properties: " + deviceProp.toString());
            } catch (Exception e)
            {
                Logging.log(Logging.LogLevel.INFO, TAG, "Device Check Runnable");
                e.printStackTrace();
            }
            }
        });
        // we need only the device properties if the Android Version on the device is < 4.3
        if (android.os.Build.VERSION.SDK_INT < 18)
        {
            chkThread.start();
        }

        BintuApplication.context = getApplicationContext();
    }

    public static Context getAppContext()
    {
        return BintuApplication.context;
    }

    public static DeviceProperties getDeviceProperties()
    {
        return deviceProp;
    }
}
