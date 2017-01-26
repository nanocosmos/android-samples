
package net.nanocosmos.bintu.demo.player.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.Toast;

import net.nanocosmos.bintu.demo.player.Util.Configuration;
import net.nanocosmos.bintu.demo.views.MetadataListview;
import net.nanocosmos.nanoStream.streamer.Logging;
import net.nanocosmos.nanoStream.streamer.NanostreamEvent;
import net.nanocosmos.nanoStream.streamer.NanostreamPlayer;
import net.nanocosmos.nanoStream.streamer.NanostreamPlayer.PlayerEventListener;
import net.nanocosmos.nanoStream.streamer.NanostreamPlayer.PlayerSettings;
import net.nanocosmos.nanoStream.streamer.NanostreamPlayer.PlayerState;
import net.nanocosmos.nanoStream.streamer.nanoStream;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nanocosmos GmbH (c) 2015 - 2016
 */
public class PlayerActivity extends Activity implements PlayerEventListener, TextureView.SurfaceTextureListener, NanostreamPlayer.MetadataListener {
    private static final boolean ENABLE_META_DATA_VIEW = false;
    private RetainedFragment dataFragment;

    // private RelativeLayout root;
    private RelativeLayout root;

    private MediaController controller;

    private static final boolean doAutoStart = false;


    private String strStreamUrl;
    private String strStreamname;

    private static final String authUser = "";
    private static final String authPass = "";

    private static final String LOG_TAG = "PlayerActivity";

    private Activity mContext = null;

    private NanostreamPlayer mPlayer = null;

    private SurfacePlayerView surfaceView = null;

    private Surface surface = null;
    private MetadataListview metadataView;
    private int currentRotation = 0;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        RelativeLayout.LayoutParams containerParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


        Intent intent = getIntent();
        strStreamUrl = intent.getStringExtra("url");
        strStreamname = intent.getStringExtra("streamname");

        root = new RelativeLayout(this);
//        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(containerParams);
        root.setBackgroundColor(Color.BLACK);

        // find the retained fragment on activity restarts
        FragmentManager fm = getFragmentManager();
        dataFragment = (RetainedFragment) fm.findFragmentByTag("data");

        // create the fragment and data the first time
        if (dataFragment == null) {
            // add the fragment
            dataFragment = new RetainedFragment();
            fm.beginTransaction().add(dataFragment, "data").commit();

            mPlayer = nanoStream.createNanostreamPlayer();

            PlayerSettings settings = mPlayer.new PlayerSettings();

            settings.setLicense(Configuration.NANOSTREAM_LICENSE);
            settings.setUrl(strStreamUrl);
            settings.setStreamname(strStreamname);
            settings.setAuthUsername(authUser);
            settings.setAuthPassword(authPass);
            settings.setBufferTimeMs(2000);

            mPlayer.setSettings(settings);
            mPlayer.setPlayerEventListener(this);
            Logging.LogSettings logSettings = new Logging.LogSettings();
            mPlayer.setLogSettings(logSettings);

            dataFragment.setData(mPlayer);
        } else {
            mPlayer = dataFragment.getData();
            // Update event listener
            mPlayer.setPlayerEventListener(this);
        }

        RelativeLayout.LayoutParams surfaceParams1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        surfaceParams1.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        surfaceView = new SurfacePlayerView(this);
        surfaceView.setLayoutParams(surfaceParams1);
        surfaceView.setSurfaceTextureListener(this);

        root.addView(surfaceView);

        if(ENABLE_META_DATA_VIEW) {
            metadataView = new MetadataListview(this);
            root.addView(metadataView);
        }

        controller = new MediaController(this, false);
        controller.setAnchorView(root);
        controller.setMediaPlayer(mPlayer);

        mPlayer.start();


        setContentView(root);

        mPlayer.addMetaDataListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {

        super.onPostCreate(savedInstanceState, persistentState);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(controller != null) {
            controller.show(5000);
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onAttachedToWindow() {
        if(controller != null) {
            controller.show(5000);
        }
        super.onAttachedToWindow();
    }

    protected void onDestroy() {
        releasePlayerInstance();
        super.onDestroy();
    }

    @Override
    public void onMetadata(final JSONObject jsonObject) {
        if(null != jsonObject) {
            if (jsonObject.has("onMetaData") || jsonObject.has("onCuePoint") || jsonObject.has("onTextData") || jsonObject.has("onFI")) {
                try {
                    if(jsonObject.getJSONObject("onMetaData").has("nanoStreamStatus"))
                    {
                        JSONObject nanoStreamStatus = jsonObject.getJSONObject("onMetaData").getJSONObject("nanoStreamStatus");
                        if(nanoStreamStatus.has("VideoRotation"))
                        {
                            int rotation = nanoStreamStatus.getInt("VideoRotation");
                            if(currentRotation != rotation){
                                currentRotation = rotation;
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        surfaceView.setRotation(currentRotation);
                                    }
                                });
                            }
                        }
                    }else {
                        PlayerActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (metadataView != null) {
                                    metadataView.setNewMetadata(jsonObject);
                                }

                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class SurfacePlayerView extends TextureView {
        int videoWidth = 1;
        int videoHeight = 1;

        int viewWidth = 1;
        int viewHeight = 1;

        SurfacePlayerView(Context context) {
            super(context);
        }

        public void setDimension(int width, int height) {
            videoWidth = width;
            videoHeight = height;

            Log.d(LOG_TAG, "Video Dimension " + videoWidth + "x" + videoHeight);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            viewWidth = MeasureSpec.getSize(widthMeasureSpec);
            viewHeight = MeasureSpec.getSize(heightMeasureSpec);

            // TODO: Get aspect ratio from stream
            // int videoWidth = 16;
            // int videoHeight = 9;
            double aspectScreen = (double) viewWidth / (double) viewHeight;
            double aspectVideo = (double) videoWidth / (double) videoHeight;

            if (aspectScreen > aspectVideo) {
                viewWidth = (viewHeight * videoWidth) / videoHeight;
            } else {
                viewHeight = (viewWidth * videoHeight) / videoWidth;
            }

            Log.d(LOG_TAG, "View Dimension " + viewWidth + "x" + viewHeight);

            setMeasuredDimension(viewWidth, viewHeight);
        }
    }

    public static class RetainedFragment extends Fragment {

        // data object we want to retain
        private NanostreamPlayer mPlayer = null;

        // this method is only called once for this fragment
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // retain this fragment
            setRetainInstance(true);
        }

        public void setData(NanostreamPlayer player) {
            this.mPlayer = player;
        }

        public NanostreamPlayer getData() {
            return mPlayer;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onPlayerEvent(NanostreamEvent event, NanostreamPlayer instance) {
        if (event.GetCode() == NanostreamEvent.CODE_STREAM_VIDEO_FORMAT_AVAILABLE) {
            if (mPlayer != null && surfaceView != null) {
                MediaFormat videoFormat = mPlayer.getVideoFormat();

                int dur = mPlayer.getDuration();

                int aspectRatioWidth = videoFormat.getInteger(NanostreamPlayer.KEY_ASPECT_RATIO_WIDTH);
                int aspectRatioHeight = videoFormat.getInteger(NanostreamPlayer.KEY_ASPECT_RATIO_HEIGHT);

                if (aspectRatioWidth > 0 && aspectRatioHeight > 0) {
                    surfaceView.setDimension(aspectRatioWidth, aspectRatioHeight);

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Log.d(LOG_TAG, "Requesting Layout");
                            surfaceView.requestLayout();
                        }
                    });
                }
            }
        }
        if (event.GetCode() == NanostreamEvent.CODE_STREAM_AUDIO_FORMAT_AVAILABLE) {
            MediaFormat audioFormat = mPlayer.getAudioFormat();
        }

        final String msg = event.GetDescription();
        Log.d(this.getClass().getName(), event.GetDescription());

        mContext.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (null != this.surface) {
            mPlayer.surfaceCreated(new Surface(surface));


            if (doAutoStart) {
                try {
                    if (!mPlayer.getState().equals(PlayerState.STARTED) && !mPlayer.getState().equals(PlayerState.PLAYBACKCOMPLETED)) {
                        mPlayer.start();
                    }
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        }
        this.surface = new Surface(surface);
        mPlayer.surfaceChanged(this.surface, width, height);

        if (mPlayer != null && surfaceView != null) {
            MediaFormat videoFormat = mPlayer.getVideoFormat();

            if (videoFormat != null) {
                int aspectRatioWidth = videoFormat.getInteger(NanostreamPlayer.KEY_ASPECT_RATIO_WIDTH);
                int aspectRatioHeight = videoFormat.getInteger(NanostreamPlayer.KEY_ASPECT_RATIO_HEIGHT);

                if (aspectRatioWidth > 0 && aspectRatioHeight > 0) {
                    surfaceView.setDimension(aspectRatioWidth, aspectRatioHeight);
                }

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        surfaceView.requestLayout();
                    }
                });
            }
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mPlayer.surfaceDestroyed(this.surface);
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        releasePlayerInstance();
    }

    public void releasePlayerInstance() {
        if (mPlayer != null){
            mPlayer.stop();
            mPlayer.close();
            mPlayer.release();
        }
    }
}
