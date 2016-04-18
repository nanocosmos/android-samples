package net.nanocosmos.bintu.demo.encoder.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import net.nanocosmos.bintu.demo.encoder.R;
import net.nanocosmos.bintu.demo.encoder.activities.BandwidthCheckActivity;
import net.nanocosmos.bintu.demo.encoder.util.Constants;

/**
 * Created by nanocosmos GmbH (c) 2015 - 2016
 */
public class RtmpInput extends Fragment {
    private static final String TAG = "RtmpInput";

    private EditText etServerUrl = null;
    private EditText etStreamName = null;
    private FloatingActionButton fabRtmpInputDone = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rtmp, container, false);

        etServerUrl = (EditText) rootView.findViewById(R.id.editText_rtmp_url);
        etServerUrl.addTextChangedListener(new CustomTextWatcher());
        etServerUrl.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        etStreamName = (EditText) rootView.findViewById(R.id.editText_stream_name);
        etStreamName.setOnEditorActionListener(new CustomOnEditorActionListener());
        etStreamName.addTextChangedListener(new CustomTextWatcher());
        etStreamName.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        fabRtmpInputDone = (FloatingActionButton) rootView.findViewById(R.id.fab_rtmpInputDone);
        fabRtmpInputDone.setOnClickListener(new customOnClickListener());
        fabRtmpInputDone.setEnabled(false);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        String url = prefs.getString("RTMP_URL", "");
        String name = prefs.getString("STREAM_NAME", "");

        if (!url.isEmpty()) {
            etServerUrl.setText(url);
        }

        if (!name.isEmpty()) {
            etStreamName.setText(name);
        }

        return rootView;
    }

    public void networkAvailable(boolean isNetworkAvailable) {
        if(null != fabRtmpInputDone) {
            if (isNetworkAvailable && etServerUrl.getText().toString().length() > 7 && etStreamName.getText().toString().length() > 0) {
                fabRtmpInputDone.setAlpha(1.0f);
                fabRtmpInputDone.setEnabled(true);
            } else {
                fabRtmpInputDone.setAlpha(0.3f);
                fabRtmpInputDone.setEnabled(false);
            }
        }
    }

    private class customOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            String serverUrl = etServerUrl.getText().toString();
            String streamName = etStreamName.getText().toString();

            if (serverUrl.isEmpty() || streamName.isEmpty()) {
                Toast.makeText(getActivity(), "Pleas enter a RTMP Url and Stream Name", Toast.LENGTH_LONG).show();
            } else {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

                SharedPreferences.Editor edit = prefs.edit();

                edit.putString("RTMP_URL", serverUrl);
                edit.putString("STREAM_NAME", streamName);
                edit.apply();

                Intent bwCheckIntent = new Intent(getActivity(), BandwidthCheckActivity.class);

                bwCheckIntent.putExtra(Constants.KEY_SERVER_URL, serverUrl);
                bwCheckIntent.putExtra(Constants.KEY_STREAM_NAME, streamName);

                startActivity(bwCheckIntent);


            }
        }
    }

    private class CustomOnEditorActionListener implements TextView.OnEditorActionListener {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                fabRtmpInputDone.callOnClick();
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
            if (etServerUrl.getText().toString().length() > 7 && etStreamName.getText().toString().length() > 0) {
                fabRtmpInputDone.setAlpha(1.0f);
                fabRtmpInputDone.setEnabled(true);
            } else {
                fabRtmpInputDone.setAlpha(0.3f);
                fabRtmpInputDone.setEnabled(false);
            }
        }
    }
}
