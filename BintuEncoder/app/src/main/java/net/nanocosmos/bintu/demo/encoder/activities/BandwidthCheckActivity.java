package net.nanocosmos.bintu.demo.encoder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import net.nanocosmos.bintu.demo.encoder.R;
import net.nanocosmos.bintu.demo.encoder.util.Constants;
import net.nanocosmos.nanoStream.streamer.BandwidthCheck;
import net.nanocosmos.nanoStream.streamer.BandwidthCheckResultCallback;
import net.nanocosmos.nanoStream.streamer.BandwidthCheckSettings;
import net.nanocosmos.nanoStream.streamer.nanoResults;
import net.stream.rtmp.jni.BandwidthCheckResult;

import java.util.UUID;

/**
 * Created by nanocosmos GmbH (c) 2015 - 2016
 */
public class BandwidthCheckActivity extends AppCompatActivity implements BandwidthCheckResultCallback {
    private static final String TAG = "BandwidthCheckActivity";

    private Toolbar toolbar;

    private FloatingActionButton fabSkipBwCheck = null;
    private FloatingActionButton fabAcceptBwCheck = null;
    private FloatingActionButton fabCancelBwCheck = null;
    private ProgressBar progressbar = null;
    private TextView tvBandwidthCheckResult = null;
    private TextView tvDefault = null;
    private TextView tvAccept = null;

    private BandwidthCheck bwCheck = null;

    private String serverUrl = "";
    private String streamName = "";
    private String webPlayoutUrl = "";
    private boolean videoEnabled = true;
    private boolean audioEnabled = true;
    private boolean logEnabled = true;

    private int avgBitrate = 0;
    int videoBitrate = 500000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_bandwidthcheck);

        Intent intent = getIntent();
        serverUrl = intent.getStringExtra(Constants.KEY_SERVER_URL);
        streamName = intent.getStringExtra(Constants.KEY_STREAM_NAME);
        webPlayoutUrl = intent.getStringExtra(Constants.KEY_WEB_PLAYOUT);
        videoEnabled = intent.getBooleanExtra(Constants.KEY_VIDEO_ENABLED, true);
        audioEnabled = intent.getBooleanExtra(Constants.KEY_AUDIO_ENABLED, true);
        logEnabled = intent.getBooleanExtra(Constants.KEY_LOG_ENABLED, true);

        fabSkipBwCheck = (FloatingActionButton) findViewById(R.id.fab_skipBwCheck);
        fabSkipBwCheck.setOnClickListener(new SkipClickListener());

        fabAcceptBwCheck = (FloatingActionButton) findViewById(R.id.fab_accept_bw_check);
        fabAcceptBwCheck.setOnClickListener(new AcceptClickListener());

        fabCancelBwCheck = (FloatingActionButton) findViewById(R.id.fab_cancel_bw_check);
        fabCancelBwCheck.setOnClickListener(new CancelClickListener());

        progressbar = (ProgressBar) findViewById(R.id.progressBar);
        tvBandwidthCheckResult = (TextView) findViewById(R.id.tv_bandwidthCheckResult);
        tvDefault = (TextView) findViewById(R.id.tvDefault);
        tvAccept = (TextView) findViewById(R.id.tvAccept);

        setToolbar();

        initBandwidthCheck();
    }

    /**
     * This method to configure the toolbar.
     * Set a Toolbar to replace the ActionBar
     */
    private void setToolbar() {
        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.bandwidthCheck_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initBandwidthCheck() {
        if(null == bwCheck) {
            BandwidthCheckSettings settings = new BandwidthCheckSettings();
            settings.setRtmpUrl(serverUrl);
            settings.setStreamId(streamName);
            settings.setLogEnabled(logEnabled?1:0);
            bwCheck = new BandwidthCheck();
            bwCheck.runBandwidthCheck(settings, this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finished(final BandwidthCheckResult bandwidthCheckResult) {
        Log.d(TAG, "BandwidthCheck results: " +
                "\n\tAverage Bitrate (kBit/s): " + bandwidthCheckResult.getAverageBitrate() / 1000 +
                "\n\tMedian Bitrate  (kBit/s): " + bandwidthCheckResult.getMedianBitrate() / 1000 +
                "\n\tMax Bitrate     (kBit/s): " + bandwidthCheckResult.getMaxBitrate() / 1000 +
                "\n\tMin Bitrate     (kBit/s): " + bandwidthCheckResult.getMinBitrate() / 1000 +
                "\n\tRun Time        (ms)    : " + bandwidthCheckResult.getRunTimeMS());


        runOnUiThread(new Runnable() {
            public void run() {
                if(bandwidthCheckResult.getErrorCode() != nanoResults.N_OK) {
                    Toast.makeText(getApplicationContext(), nanoResults.GetDescription(bandwidthCheckResult.getErrorCode()), Toast.LENGTH_LONG).show();
                }
                progressbar.setVisibility(View.INVISIBLE);
                fabSkipBwCheck.setVisibility(View.INVISIBLE);
                fabAcceptBwCheck.setVisibility(View.VISIBLE);
                fabCancelBwCheck.setVisibility(View.VISIBLE);
                tvBandwidthCheckResult.setVisibility(View.VISIBLE);

                avgBitrate = (avgBitrate + 49) / 50 * 50;

                String lowBandwidth = "";
                if(avgBitrate < 100000) {
                    lowBandwidth = "\nWarning the bandwidth is very low.";
                }else if(avgBitrate < 300000) {
                    lowBandwidth = "\nWarning the bandwidth is low.";
                }

                int tmpBitrate = avgBitrate;
                tmpBitrate =(tmpBitrate + 49000) / 50000 * 50000;
                tmpBitrate = (int)(tmpBitrate * 0.75);

                if(tmpBitrate < 100000) {
                    tmpBitrate = 100000;
                }else if(tmpBitrate > 1000000) {
                    tmpBitrate = 1000000;
                }

                videoBitrate = (tmpBitrate + 49000) / 50000 * 50000;


                tvBandwidthCheckResult.setText("Measured bandwidth: " + avgBitrate / 1000 + " kBit/s" + lowBandwidth);
                tvAccept.setText("Recommended Bitrate: " + videoBitrate / 1000 + " kBit/s");
                tvAccept.setVisibility(View.VISIBLE);
                tvDefault.setText("Default Bitrate: 500 kBit/s");
                tvDefault.setVisibility(View.VISIBLE);

            }
        });

        avgBitrate = bandwidthCheckResult.getAverageBitrate();
    }

    public void startStreamingActivity() {

        Intent streamIntent = new Intent(this, StreamActivity.class);

        streamIntent.putExtra(Constants.KEY_SERVER_URL, serverUrl);
        streamIntent.putExtra(Constants.KEY_STREAM_NAME, streamName);
        streamIntent.putExtra(Constants.KEY_WEB_PLAYOUT, webPlayoutUrl);
        streamIntent.putExtra(Constants.KEY_BITRATE, videoBitrate);
        streamIntent.putExtra(Constants.KEY_VIDEO_ENABLED, videoEnabled);
        streamIntent.putExtra(Constants.KEY_AUDIO_ENABLED, audioEnabled);
        streamIntent.putExtra(Constants.KEY_LOG_ENABLED, logEnabled);

        startActivityForResult(streamIntent, Constants.REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == Constants.REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        bwCheck.abort();
        super.onBackPressed();
    }

    private class SkipClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            bwCheck.forceStop();
        }
    }

    private class AcceptClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            startStreamingActivity();
        }
    }

    private class CancelClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            videoBitrate = 500000;
            startStreamingActivity();
        }
    }
}
