package net.nanocosmos.nanoStream.demo.simpleEncoder.activities;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import net.nanocosmos.bintu.bintusdk.BintuSDK;
import net.nanocosmos.bintu.bintusdk.handler.StreamInfoResponseHandler;
import net.nanocosmos.bintu.bintusdk.stream.StreamInfo;
import net.nanocosmos.nanoStream.demo.simpleEncoder.R;
import net.nanocosmos.nanoStream.demo.simpleEncoder.StreamerApplication;
import net.nanocosmos.nanoStream.demo.simpleEncoder.ui.StreamPreview;
import net.nanocosmos.nanoStream.demo.simpleEncoder.util.CheckAppPermissions;
import net.nanocosmos.nanoStream.demo.simpleEncoder.util.Configuration;
import net.nanocosmos.nanoStream.streamer.AdaptiveBitrateControlSettings;
import net.nanocosmos.nanoStream.streamer.AspectRatio;
import net.nanocosmos.nanoStream.streamer.AudioSettings;
import net.nanocosmos.nanoStream.streamer.FocusCallback;
import net.nanocosmos.nanoStream.streamer.Logging;
import net.nanocosmos.nanoStream.streamer.NanostreamEvent;
import net.nanocosmos.nanoStream.streamer.NanostreamEventListener;
import net.nanocosmos.nanoStream.streamer.NanostreamException;
import net.nanocosmos.nanoStream.streamer.Resolution;
import net.nanocosmos.nanoStream.streamer.Rotation;
import net.nanocosmos.nanoStream.streamer.RotationHelper;
import net.nanocosmos.nanoStream.streamer.VideoSettings;
import net.nanocosmos.nanoStream.streamer.nanoResults;
import net.nanocosmos.nanoStream.streamer.nanoStream;
import net.nanocosmos.nanoStream.streamer.nanoStreamSettings;

import java.io.File;
import java.util.List;

/**
 * Created by nanocosmos GmbH (c) 2015 - 2016
 */
public class MainActivity extends Activity implements NanostreamEventListener, FocusCallback{
    private static  final String TAG = "MainActivity";

    private CheckAppPermissions appPermissions = null;

    // TODO: REPLACE THE RTMP URL AND STREAM NAME
    private String serverUrl = "";
    private String streamName = "";

    private String  authUser     = "";
    private String  authPassword = "";
    private boolean sendRtmp     = true;
    private boolean recordMp4    = true;
    private String  mp4FilePath      = "";
    private boolean streamVideo  = true;
    private boolean streamAudio  = true;

    private AdaptiveBitrateControlSettings.AdaptiveBitrateControlMode abcMode = AdaptiveBitrateControlSettings.AdaptiveBitrateControlMode.DISABLED;
    AdaptiveBitrateControlSettings abcSettings = null;
    private Logging.LogSettings logSettings = null;

    // Video config
    public static Resolution            videoResolution         = new Resolution(640, 480);
    private int                         videoBitrate            = 500000;
    private int                         videoFramerate          = 15;
    private int                         videoKeyFrameInterval   = 5;
    private nanoStream.VideoSourceType  videoSourceType         = nanoStream.VideoSourceType.INTERNAL_BACK;
    private boolean                     useAutoFocus            = true;  // false
    private boolean                     useTorch                = false; // true

    // for a documentation of the behavior of aspect ratio in combination with the orientation and resolution
    // see http://www.nanocosmos.de/v4/documentation/android_resolution_aspect_ratio_and_orientation
    private AspectRatio                 videoAspectRatio        = AspectRatio.RATIO_KEEP_INPUT; // AspectRatio.RATIO_4_3
                                                                                                // AspectRatio.RATIO_9_16
                                                                                                // AspectRatio.RATIO_3_4
                                                                                                // AspectRatio.RATIO_1_1
                                                                                                // AspectRatio.RATIO_16_9
    // audio config
    private int audioChannels   = 2; // default/max value = 2; min value = 1
    private int audioBitrate    = 64000; // supported bit rates = 32k, 64k, 128k (experimental)
                                         // the internal audio encoder force the bit rate to one of the supported.
    private int audioSamplerate = 44100; // supported sample rates = 44.1k, 48k
                                         // the internal audio encoder force the sample rate to one of the supported.

    // nanoStream SDK
    private nanoStream streamLib = null;

    // Surface to render the Preview
    private StreamPreview surface = null;

    // Stream and Preview Orientation
    private Rotation prevRotation = Rotation.ROTATION_0;
    private Rotation streamRotation = Rotation.ROTATION_0;
    private CustomOrientationEventListener orientation = null;

    // Zoom
    private ScaleGestureDetector scaleGestureDetector;
    private List<Integer> mZoomRatios = null;

    // Focus
    // you can find a documentation here (http://www.nanocosmos.de/v4/documentation/android_camera_focus)
    private GestureDetector gestureDetector;

    //
    private AspectRatio arPortrait = AspectRatio.RATIO_3_4;
    private AspectRatio arLandscape = AspectRatio.RATIO_4_3;

    private boolean isDefaultOrientationLandscape = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        surface = (StreamPreview) findViewById(R.id.surface);

        serverUrl = Configuration.SERVER_URL;
        streamName = Configuration.STREAM_NAME;

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            initBintu();
        }else {
            appPermissions = new CheckAppPermissions(this);
            boolean needPermission = false;
            if(streamVideo) {
                needPermission |= !appPermissions.checkCameraPermissions();
            }
            if(streamAudio) {
                needPermission |= !appPermissions.checkRecordAudioPermission();
            }
            if(recordMp4) {
                needPermission |= !appPermissions.checkWriteExternalStoragePermission();
            }

            if(needPermission) {
                appPermissions.requestMissingPermissions();
            }else {
                initBintu();
            }
        }

        isDefaultOrientationLandscape = (RotationHelper.getDeviceDefaultOrientation(this) == android.content.res.Configuration.ORIENTATION_LANDSCAPE);

        orientation = new CustomOrientationEventListener(this, SensorManager.SENSOR_DELAY_UI);
        orientation.enable();
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        surface.setVisibility(View.VISIBLE);

        initStreamLib();
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        if (streamLib != null) {
            streamLib.release();
            streamLib = null;
        }
    }

    @Override
    public void onNanostreamEvent(NanostreamEvent event)
    {
        this.runOnUiThread(new NotificationRunable(event));
    }

    @Override
    public void onSuccess() {
        Logging.log(Logging.LogLevel.DEBUG, TAG, "Focus: success");
    }

    @Override
    public void onSuccess(Rect rect, Boolean aBoolean) {
        Logging.log(Logging.LogLevel.DEBUG, TAG, "Focus: success");
    }

    @Override
    public void onFailure() {
        Logging.log(Logging.LogLevel.DEBUG, TAG, "Focus: failed");
    }

    private class NotificationRunable implements Runnable
    {
        private NanostreamEvent m_event;

        public NotificationRunable(NanostreamEvent event)
        {
            m_event = event;
        }

        @Override
        public void run()
        {
            if (m_event.GetType() != NanostreamEvent.TYPE_RTMP_QUALITY)
            {
                Toast.makeText(getApplicationContext(), m_event.GetDescription(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (gestureDetector != null)
        {
            gestureDetector.onTouchEvent(event);
        }
        if (scaleGestureDetector != null)
        {
            scaleGestureDetector.onTouchEvent(event);
        }
        return true;
    }

    private void initBintu() {
        if(!Configuration.BINTU_API_KEY.isEmpty()) {
            BintuSDK bintu = new BintuSDK(Configuration.BINTU_API_KEY);

            bintu.createStream(new StreamInfoResponseHandler() {
                @Override
                public void handle(StreamInfo streamInfo) {
                    serverUrl = streamInfo.getIngest().getUrl();
                    streamName =streamInfo.getIngest().getStreamName();
                    initStreamLib();
                }

                @Override
                public void onError(Throwable throwable) {
                    Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                    initStreamLib();
                }
            });
        }else {
            initStreamLib();
        }
    }

    private nanoStreamSettings configureNanostreamSettings()
    {
        if(recordMp4) {
            try {
                // get the external DCIM Folder for mp4 recording.
                // for a docu about mp4 recording see http://www.nanocosmos.de/v4/documentation/android_mp4_recording
                File _path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                File subDir = new File(_path, "FullStreamer");
                boolean result = true;

                if(!subDir.exists()) {
                    result = subDir.mkdirs();
                }

                if (result) {
                    File filePath = new File(subDir, "Test.mp4");
                    int i = 1;
                    while (filePath.exists()) {
                        filePath = new File(subDir, "Test_" + i + ".mp4");
                        i++;
                    }
                    mp4FilePath = filePath.getAbsolutePath();
                }else {
                    recordMp4 = false;
                }

            } catch (Exception e) {
                recordMp4 = false;
                Logging.log(Logging.LogLevel.ERROR, TAG, "Failed to get video path. ", e);
            }
        }

        // new AdaptiveBitrateControlSettings(mode) creates a default AdaptiveBitrateControlSettings object
        // with the following values:
        // adaptive bitrate control mode = mode (DISABLE, QUALITY_DEGRADE, FRAME_DROP, QUALITY_DEGRADE_AND_FRAME_DROP)
        // min bit rate                  = 50k
        // max bit rate                  = 2M
        // flush Buffer Threshold        = 50%
        // min frame rate                = 5
        abcSettings = new AdaptiveBitrateControlSettings(abcMode);

        // new LogSettings() creates a default LogSettings object with the following values:
        // log path     = own directory (e.g. /sdcard/Android/com.example.appname/files/)
        // log name     = RTMPStream.log
        // Log level    = LogLevel.ERROR
        // log enabled  = 1
        logSettings = new Logging.LogSettings(Logging.LogLevel.VERBOSE, 1);

        // new VideoSettings() creates a default VideoSettings object with the following values:
        // video resolution     = 640x480
        // video bit rate       = 500k
        // video frame rate     = 15
        // key frame interval   = 5
        // video source type    = VideoSourceType.INTERNAL_BACK
        // use auto focus       = true
        // use torch            = false
        // aspect ratio         = AspectRatio.RATIO_KEEP_INPUT
        VideoSettings vs = new VideoSettings();
        vs.setBitrate(videoBitrate);
        vs.setFrameRate(videoFramerate);
        vs.setResolution(videoResolution);
        vs.setVideoSourceType(videoSourceType);
        vs.setAspectRatio(videoAspectRatio);
        vs.setKeyFrameInterval(videoKeyFrameInterval);
        vs.setUseAutoFocus(useAutoFocus);
        vs.setUseTorch(useTorch);

        // new AudioSettings() creates a default AudioSettings object with the following values:
        // audio channels       = 2
        // audio bit rate       = 64k
        // audio sample rate    = 44.1k
        AudioSettings as = new AudioSettings();
        as.setBitrate(audioBitrate);
        as.setChannels(audioChannels);
        as.setSamplerate(audioSamplerate);

        // new nanoStreamSettings() creates a default nanoStreamSettings object with the following values:
        // has video                    = true
        // video settings               = default video settings
        // has audio                    = true
        // audio settings               = default audio settings
        // preview holder               = null
        // license                      = ""
        // stream url                   = ""
        // stream name                  = ""
        // event listener               = null
        // Adaptive Bit rate settings   = disabled
        // log settings                 = default log settings
        // send rtmp                    = true
        // record MP4                   = false
        // mp4 path                     = ""
        // you need to set at least the license, stream url and the stream name to be able to start a stream.
        nanoStreamSettings nss = new nanoStreamSettings();
        nss.setVideoSettings(vs);
        nss.setHaveVideo(streamVideo);
        nss.setPreviewHolder(surface.getHolder());
        nss.setAudioSettings(as);
        nss.setHaveAudio(streamAudio);
        nss.setAbcSettings(abcSettings);
        nss.setLogSettings(logSettings);
        nss.setLicense(Configuration.NANOSTREAM_LICENSE);
        nss.setLogSettings(logSettings);
        nss.setStreamUrl(serverUrl);
        nss.setStreamName(streamName);
        nss.setAuthUser(authUser);
        nss.setAuthPassword(authPassword);
        nss.setSendRtmp(sendRtmp);
        nss.setEventListener(this);
        nss.setMp4Path(mp4FilePath);
        nss.setRecordMp4(recordMp4);

        return nss;
    }

    private void initStreamLib()
    {
        if (null == streamLib)
        {
            try
            {
                nanoStreamSettings nss = configureNanostreamSettings();
                streamLib = new nanoStream(nss);
            } catch (NanostreamException en)
            {
                Toast.makeText(getApplicationContext(), en.toString(), Toast.LENGTH_LONG).show();
            }

            if(null != streamLib) {

                try{
                    streamLib.init();
                }catch (NanostreamException e) {
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                }catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }

                // set the Device properties collected at the first time app was started in StreamerApplication.java
                // this is necessary for pre Android 4.3 Devices, because this Devices may show some color format issues.
                // this will correct these color issues.
                // for mor information http://www.nanocosmos.de/v4/documentation/android_device_properties
                streamLib.setDeviceProperties(StreamerApplication.getDeviceProperties());

                // initial check if the device is in portrait mode (default is landscape Rotation.ROTATION_0)
                if (getResources().getConfiguration().orientation == getResources().getConfiguration().ORIENTATION_PORTRAIT) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    prevRotation = Rotation.ROTATION_90;
                    streamRotation = Rotation.ROTATION_90;
                    streamLib.setPreviewRotation(prevRotation);
                    streamLib.setStreamRotation(streamRotation);
                }else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    prevRotation = Rotation.ROTATION_0;
                    streamRotation = Rotation.ROTATION_0;
                    streamLib.setPreviewRotation(prevRotation);
                    streamLib.setStreamRotation(streamRotation);
                }

                if (streamVideo) {
                    mZoomRatios = streamLib.getZoomRatios();
                    streamLib.addFocusCalback(this);
                }
            }
            // the scaleGestureDetector is needed for pinch to zoom.
            if(null == scaleGestureDetector) {
                scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureListener());
            }

            // the gestureDetector is needed for tap to focus and long press to focus lock
            if (null == gestureDetector) {
                gestureDetector = new GestureDetector(this, new GestureListener());
            }
        }
    }

    public void toggleStreaming(View clicked)
    {
        if (null == streamLib)
        {
            Toast.makeText(getApplicationContext(), "nanoStream failed to initialize", Toast.LENGTH_LONG).show();
            return;
        }

        if (!streamLib.hasState(nanoStream.EncoderState.RUNNING))
        {
            Toast.makeText(getApplicationContext(), "Starting...", Toast.LENGTH_SHORT).show();

            if (streamLib.hasState(nanoStream.EncoderState.STOPPED) || streamLib.hasState(nanoStream.EncoderState.CREATED))
            {
                try
                {
                    Logging.log(Logging.LogLevel.DEBUG, TAG, "toggleStreaming init nanoStream");
                    streamLib.init();
                } catch (NanostreamException en)
                {
                    Toast.makeText(getApplicationContext(), en.toString(), Toast.LENGTH_LONG).show();
                    return;
                }
            }

            try
            {
                streamLib.start();
            } catch (NanostreamException en)
            {
                Toast.makeText(getApplicationContext(), en.toString(), Toast.LENGTH_LONG).show();
                return;
            }

            ((ImageButton) clicked).setImageDrawable(getResources().getDrawable(R.drawable.but_stop));
        } else
        {
            Toast.makeText(getApplicationContext(), "Stopping...", Toast.LENGTH_SHORT).show();

            streamLib.stop();
            ((ImageButton) clicked).setImageDrawable(getResources().getDrawable(R.drawable.but_start));
        }
    }

    public void flipCamera(View clicked) {
        if (streamLib == null)
        {
            Toast.makeText(getApplicationContext(), "nanoStream failed to initialize", Toast.LENGTH_LONG).show();
            return;
        }

        try
        {
            streamLib.rotateCamera();
        } catch (NanostreamException e)
        {
            if (e.getCode() == nanoResults.N_CAMERA_NOSECOND)
            {
                Toast.makeText(getApplicationContext(), nanoResults.GetDescription(nanoResults.N_CAMERA_NOSECOND), Toast.LENGTH_LONG).show();
            } else
            {
                e.printStackTrace();
            }
        }
    }

    public void toggleTorchlight(View clicked) {
        if (streamLib != null)
        {
            streamLib.toggleTorch(!streamLib.isTorchEnabled());
            if (streamLib.isTorchEnabled())
            {
                ((ImageButton) clicked).setImageDrawable(getResources().getDrawable(R.drawable.but_flash_on));
            } else
            {
                ((ImageButton) clicked).setImageDrawable(getResources().getDrawable(R.drawable.but_flash_off));
            }
        }
    }

    public void pinch2Zoom(float scaleFactor) {
        if (streamLib.hasZoom() && null != mZoomRatios) {
            int zoomFactor = streamLib.getZoom();
            float zoomRatio = mZoomRatios.get(zoomFactor) / 100f;
            zoomRatio *= scaleFactor;
            if (zoomRatio > 1.0f) {
                if (scaleFactor > 1.0f) {
                    for (int i = zoomFactor; i < mZoomRatios.size(); i++) {
                        Double zoom = mZoomRatios.get(i) / 100.0;
                        if (zoom >= zoomRatio) {
                            streamLib.setZoom(i);
                            break;
                        }
                    }
                } else {
                    for (int i = zoomFactor; i > 0; i--) {
                        Double zoom = mZoomRatios.get(i) / 100.0;
                        if (zoom <= zoomRatio) {
                            streamLib.setZoom(i);
                            break;
                        }
                    }
                }
            }
        }
    }

    private class CustomOrientationEventListener extends OrientationEventListener {
        private int lastScreenOrientation = 0;
        public CustomOrientationEventListener(Context context, int rate) {
            super(context, rate);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (null != streamLib) {
                if(!streamLib.hasState(nanoStream.EncoderState.RUNNING)) {
                    if (isDefaultOrientationLandscape) {
                        orientation -= 90;

                        if (orientation < 0) {
                            orientation += 360;
                        }
                    }
                    int screenOrientation = -1;

                    if (orientation > 70 && orientation < 110) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                        screenOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    } else if (orientation > 160 && orientation < 200) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                        screenOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    } else if (orientation > 250 && orientation < 290) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    } else if ((orientation > 340 && orientation <= 360) || (orientation >= 0 && orientation < 20)) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    }

                    if (screenOrientation != lastScreenOrientation) {
                        Rotation rotation = RotationHelper.getRotation(screenOrientation, isDefaultOrientationLandscape);
                        if (null != rotation) {

                            Log.d(TAG, "orientation: " + orientation + " rotation: " + rotation.getDegrees());

                            try {
                                Log.d(TAG, "Rotation: " + rotation.toString());
                                streamLib.setPreviewRotation(rotation);
                                streamLib.setStreamRotation(rotation);
                                streamLib.setAspectRatio(videoAspectRatio);
                            } catch (IllegalStateException e) {
                                Logging.log(Logging.LogLevel.ERROR, TAG, "Camera rotate failed", e);
                            }
                        }
                        lastScreenOrientation = screenOrientation;
                    }
                }
            }
        }
    }

    private class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if(null != streamLib) {
                if (streamLib.hasZoom()) {
                    pinch2Zoom(detector.getScaleFactor());
                }
            }
            return true;
        }
    }

    private class GestureListener implements OnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent e)
        {
            if (streamLib != null)
            {
                streamLib.setFocusArea(300, 300, 1f, (int) e.getX(), (int) e.getY(), surface.getWidth(), surface.getHeight(), 1000);
            }
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e)
        {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e)
        {
            if (streamLib != null)
            {
                streamLib.setFocusLockArea(300, 300, 1f, (int) e.getX(), (int) e.getY(), surface.getWidth(), surface.getHeight(), 1000);
            }
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e)
        {
            // TODO Auto-generated method stub
            return false;
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if (requestCode == CheckAppPermissions.requestPermissionCode)
        {
            if (streamVideo) {
                streamVideo = appPermissions.checkCameraPermissions();
            }

            if (streamAudio) {
                streamAudio = appPermissions.checkRecordAudioPermission();
            }

            if (recordMp4) {
                recordMp4 = appPermissions.checkWriteExternalStoragePermission();
            }

            initBintu();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
