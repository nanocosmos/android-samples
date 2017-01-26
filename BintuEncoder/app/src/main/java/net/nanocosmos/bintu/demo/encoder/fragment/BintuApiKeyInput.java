package net.nanocosmos.bintu.demo.encoder.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.nanocosmos.bintu.bintusdk.BintuSDK;
import net.nanocosmos.bintu.bintusdk.exceptions.BintuError;
import net.nanocosmos.bintu.bintusdk.handler.StreamInfoResponseHandler;
import net.nanocosmos.bintu.bintusdk.stream.Playout;
import net.nanocosmos.bintu.bintusdk.stream.StreamInfo;
import net.nanocosmos.bintu.bintusdk.stream.Type;
import net.nanocosmos.bintu.bintusdk.util.StreamBuilder;
import net.nanocosmos.bintu.demo.encoder.R;
import net.nanocosmos.bintu.demo.encoder.activities.BandwidthCheckActivity;
import net.nanocosmos.bintu.demo.encoder.util.Configuration;
import net.nanocosmos.bintu.demo.encoder.util.Constants;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;

/**
 * Created by nanocosmos GmbH (c) 2015 - 2016
 */
public class BintuApiKeyInput extends Fragment {
    private static final String TAG = "BintuApiKeyInput";

    private BintuSDK bintu = null;
    private EditText etApiKey = null;
    private EditText etTags = null;
    private EditText etTitle = null;
    private Switch videoSwitch = null;
    private Switch audioSwitch = null;
    private Switch logSwitch = null;
    private boolean videoEnabled = true;
    private boolean audioEnabled = true;
    private boolean logEnabled = true;
    private FloatingActionButton fabBintuDone = null;
    private RelativeLayout progressLayout = null;
    private SharedPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragmen_bintu, container, false);

        progressLayout = (RelativeLayout) rootView.findViewById(R.id.bintu_load_stream_progress_layout);
        etApiKey = (EditText) rootView.findViewById(R.id.editTextBintuApiKey);
        etApiKey.setOnEditorActionListener(new CustomOnEditorActionListener());
        etApiKey.addTextChangedListener(new CustomTextWatcher());
        etApiKey.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        etTags = (EditText) rootView.findViewById(R.id.editTextBintuTags);
        etTitle = (EditText) rootView.findViewById(R.id.editTextBintuTitle);

        videoSwitch = (Switch) rootView.findViewById(R.id.switch_video);
        videoSwitch.setActivated(true);
        videoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                videoEnabled = isChecked;
            }
        });
        audioSwitch = (Switch) rootView.findViewById(R.id.switch_audio);
        audioSwitch.setActivated(true);
        audioSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                audioEnabled = isChecked;
            }
        });
        logSwitch = (Switch) rootView.findViewById(R.id.switch_log);
        logSwitch.setActivated(true);
        logSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                logEnabled = isChecked;
            }
        });

        fabBintuDone = (FloatingActionButton) rootView.findViewById(R.id.fab_bintuDone);
        fabBintuDone.setOnClickListener(new customOnClickListener());
        fabBintuDone.setEnabled(false);


        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        
        String apiKey = prefs.getString("BINTU_API_KEY", Configuration.BINTU_API_KEY);

        if (!apiKey.isEmpty()) {
            etApiKey.setText(apiKey);
        }

        enableProgress(false);

        return rootView;
    }

    public void networkAvailable(boolean isNetworkAvailable) {
        if (null != fabBintuDone) {
            if (isNetworkAvailable && etApiKey.getText().toString().length() > 3) {
                fabBintuDone.setAlpha(1.0f);
                fabBintuDone.setEnabled(true);
            } else {
                fabBintuDone.setAlpha(0.3f);
                fabBintuDone.setEnabled(false);
            }
        }
    }

    public void enableProgress(boolean enable) {
        if(enable) {
            progressLayout.setVisibility(View.VISIBLE);
            fabBintuDone.setVisibility(View.INVISIBLE);
        }else{
            progressLayout.setVisibility(View.GONE);
            fabBintuDone.setVisibility(View.VISIBLE);
        }
    }

    private class customOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(final View v) {
            enableProgress(true);
            final String apiKey = etApiKey.getText().toString();
            if (apiKey.isEmpty()) {
                Toast.makeText(getActivity(), "Pleas enter a Valid Bintu Api Key", Toast.LENGTH_LONG).show();
            } else {
                bintu = new BintuSDK(apiKey);

                String _tags = etTags.getText().toString();
                String _title = "";

                if(!etTitle.getText().toString().isEmpty()) {
                   _title = etTitle.getText().toString();
                }

                String tags[] = _tags.split(",");
                for(int i=0; i<tags.length; i++){
                    tags[i] = tags[i].trim();
                }
                StreamBuilder streamBuilder = new StreamBuilder().addTags(tags).setTitleTag(_title);

                bintu.createStream(streamBuilder, new StreamInfoResponseHandler() {
                    @Override
                    public void handle(StreamInfo streamInfo) {

                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

                        SharedPreferences.Editor edit = prefs.edit();

                        edit.putString("BINTU_API_KEY", apiKey);
                        edit.apply();

                        String serverUrl = streamInfo.getIngest().getUrl();
                        String streamName = streamInfo.getIngest().getStreamName();
                        String webPlayout = "";

                        for (Playout p : streamInfo.getWebPlayouts()) {
                            if(p.getType() == Type.LIVE){
                                webPlayout = p.toString();
                                break;
                            }
                        }


                        enableProgress(false);

                        Intent bwCheckIntent = new Intent(getActivity(), BandwidthCheckActivity.class);

                        bwCheckIntent.putExtra(Constants.KEY_SERVER_URL, serverUrl);
                        bwCheckIntent.putExtra(Constants.KEY_STREAM_NAME, streamName);
                        bwCheckIntent.putExtra(Constants.KEY_WEB_PLAYOUT, webPlayout);
                        bwCheckIntent.putExtra(Constants.KEY_VIDEO_ENABLED, videoEnabled);
                        bwCheckIntent.putExtra(Constants.KEY_AUDIO_ENABLED, audioEnabled);
                        bwCheckIntent.putExtra(Constants.KEY_LOG_ENABLED, logEnabled);

                        startActivity(bwCheckIntent);

                    }

                    @Override
                    public void onError(Throwable error) {
                        if (error != null) {
                            if (error instanceof BintuError) {
                                String errorMessage = ((BintuError) error).getMessage();
                                if (!TextUtils.isEmpty(errorMessage)) {
                                    etApiKey.setError(errorMessage);
                                } else {
                                    etApiKey.setError("Sorry, an unexpected error occurred!");
                                }
                            }else if (error instanceof UnknownHostException) {
                                etApiKey.setError("Please check your network connection.");
                            }else if(error instanceof IOException) {
                                etApiKey.setError("Can not resolve host.");
                            }else if(error instanceof ConnectException) {
                                etApiKey.setError("Wasn't able to connect to bintu.");
                            }else {
                                etApiKey.setError("Sorry, an unexpected error occurred!");
                            }
                        }else {
                            etApiKey.setError("Sorry, an unexpected error occurred!");
                        }
                        enableProgress(false);
                    }

                });
            }
        }
    }

    private class CustomOnEditorActionListener implements TextView.OnEditorActionListener {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                fabBintuDone.callOnClick();
                return true;
            }
            return false;
        }
    }

    private class CustomTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (etApiKey.getText().toString().length() > 3) {
                fabBintuDone.setAlpha(1.0f);
                fabBintuDone.setEnabled(true);
            } else {
                fabBintuDone.setAlpha(0.3f);
                fabBintuDone.setEnabled(false);
            }
        }
    }
}
