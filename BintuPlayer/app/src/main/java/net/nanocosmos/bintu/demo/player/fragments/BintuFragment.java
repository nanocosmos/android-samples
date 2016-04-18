package net.nanocosmos.bintu.demo.player.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import net.nanocosmos.bintu.bintusdk.BintuSDK;
import net.nanocosmos.bintu.bintusdk.handler.OrganisationResponseHandler;
import net.nanocosmos.bintu.demo.player.Util.BintuConnector;
import net.nanocosmos.bintu.demo.player.Util.Configuration;
import net.nanocosmos.bintu.demo.player.activities.StreamListActivity;
import net.nanocosmos.nanoStream.demo.player.R;

/**
 * Created by nanocosmos GmbH (c) 2015 - 2016
 */
public class BintuFragment extends Fragment {
    public static final int MINIMUM_URL_SIZE = 7;
    private View view;
    private EditText bintuApiKey;
    private FloatingActionButton startStreamButton;
    private RelativeLayout progressLayout;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_bintu, null);
            bintuApiKey = (EditText) view.findViewById(R.id.input_bintu_api_key);
            bintuApiKey.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            startStreamButton = (FloatingActionButton) view.findViewById(R.id.bintu_start_stream_btn);
            progressLayout = (RelativeLayout) view.findViewById(R.id.bintu_load_stream_progress_layout);

            prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            String apiKey = prefs.getString("BINTU_API_KEY", Configuration.BINTU_API_KEY);

            if (!apiKey.isEmpty()) {
                bintuApiKey.setText(apiKey);
            }

            if(bintuApiKey.getText().toString().length() >= MINIMUM_URL_SIZE) {
                toggleButtonState(true);
            }
            bintuApiKey.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    bintuApiKey.setError(null);
                    return false;
                }
            });
            bintuApiKey.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    bintuApiKey.setError(null);
                    toggleButtonState(s.length() >= MINIMUM_URL_SIZE);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            startStreamButton.setOnClickListener(new StartStreamClickListener());
        }
        return view;
    }

    public void networkAvailable(boolean isNetworkAvailable) {
        if (null != startStreamButton) {
            toggleButtonState(isNetworkAvailable && bintuApiKey.getText().toString().length() > MINIMUM_URL_SIZE);
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

    @Override
    public void onResume() {

        enableProgress(false);
        super.onResume();
    }

    public void enableProgress(boolean enable) {
        if(enable) {
            progressLayout.setVisibility(View.VISIBLE);
            startStreamButton.setVisibility(View.INVISIBLE);
        }else{
            progressLayout.setVisibility(View.GONE);
            startStreamButton.setVisibility(View.VISIBLE);
        }
    }
    private class StartStreamClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            enableProgress(true);

            BintuConnector bintuConnector = BintuConnector.getInstance();
            bintuConnector.setBintuApiKey(bintuApiKey.getText().toString());

            Intent streamList = new Intent(getActivity(), StreamListActivity.class);
            startActivity(streamList);
            
        }
    }
}
