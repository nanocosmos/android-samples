package net.nanocosmos.bintu.demo.player.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.EditText;

import net.nanocosmos.bintu.demo.player.activities.PlayerActivity;
import net.nanocosmos.nanoStream.demo.player.R;

import java.net.URI;

/**
 * Created by nanocosmos GmbH (c) 2015 - 2016
 */
public class RTMPFragment extends Fragment {
    private static final String PREFERENCE_NAME = "RTMP_DATA";
    public static final String RTMP_URL_PREF_NAME = "RTMP_URL";
    public static final String RTMP_STREAMNAME_PREF_NAME = "RTMP_STREAMNAME";
    private static final int MINIMUM_URL_LENGTH = 7;
    private View view;
    private EditText rtmpUrlText;
    private EditText rtmpStreamnameText;
    private FloatingActionButton startStreamButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_rtmp, null);
            rtmpUrlText = (EditText) view.findViewById(R.id.input_rtmp_url_text);
            rtmpUrlText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

            rtmpStreamnameText = (EditText) view.findViewById(R.id.input_rtmp_streamname_text);
            rtmpStreamnameText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            startStreamButton = (FloatingActionButton) view.findViewById(R.id.rtmp_stream_start_btn);
            startStreamButton.setOnClickListener(new PlayRTMPStreamClickListener());
            Context ctx = getContext();
            toggleButtonState(false);
            if (ctx != null) {
                SharedPreferences prefs = ctx.getSharedPreferences(PREFERENCE_NAME, 0);
                String url = prefs.getString(RTMP_URL_PREF_NAME, null);
                String streamname = prefs.getString(RTMP_STREAMNAME_PREF_NAME, null);
                if (url != null) {
                    rtmpUrlText.setText(url);
                    toggleButtonState(true);
                }
                if (streamname != null) {
                    rtmpStreamnameText.setText(streamname);
                    toggleButtonState(true);
                }

            }
            rtmpStreamnameText.addTextChangedListener(new CheckStreamdataTextWatcher());
            rtmpStreamnameText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    rtmpStreamnameText.setError(null);
                    return false;
                }
            });
            rtmpStreamnameText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    rtmpStreamnameText.setError(null);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            rtmpUrlText.addTextChangedListener(new CheckStreamdataTextWatcher());
            rtmpUrlText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    rtmpUrlText.setError(null);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            rtmpUrlText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    rtmpUrlText.setError(null);
                    return false;
                }
            });
        }

        return view;
    }
    public void networkAvailable(boolean isNetworkAvailable) {
        if(null != startStreamButton) {
            toggleButtonState(isNetworkAvailable && (rtmpUrlText.getText().toString().length() >= MINIMUM_URL_LENGTH && rtmpStreamnameText.getText().toString().length() > 0));
        }
    }

    private void toggleButtonState(Boolean isEnabled) {
        if (isEnabled) {
            startStreamButton.setEnabled(true);
            startStreamButton.setAlpha(1.0f);
        } else {
            startStreamButton.setEnabled(false);
            startStreamButton.setAlpha(0.3f);
        }
    }

    private class CheckStreamdataTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            String rtmpStreamname = rtmpStreamnameText.getText().toString();
            String rtmpUrl = rtmpUrlText.getText().toString();


            toggleButtonState((rtmpUrl.length() >= MINIMUM_URL_LENGTH && rtmpStreamname.length() > 0));
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }


    private class PlayRTMPStreamClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(rtmpStreamnameText.getWindowToken(), 0);

            String rtmpURL = rtmpUrlText.getText().toString();
            String streamName = rtmpStreamnameText.getText().toString();
            SharedPreferences prefs = getActivity().getSharedPreferences(PREFERENCE_NAME, 0);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(RTMP_URL_PREF_NAME, rtmpURL);
            editor.putString(RTMP_STREAMNAME_PREF_NAME, streamName);
            editor.commit();
            Intent intent = new Intent(getActivity(), PlayerActivity.class);
            intent.putExtra("url", rtmpURL);
            intent.putExtra("streamname", streamName);

            startActivity(intent);

        }
    }
}
