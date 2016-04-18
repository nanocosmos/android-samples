package net.nanocosmos.bintu.demo.encoder.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.nanocosmos.bintu.bintusdk.BintuSDK;
import net.nanocosmos.bintu.demo.encoder.BuildConfig;
import net.nanocosmos.bintu.demo.encoder.R;
import net.nanocosmos.nanoStream.streamer.nanoStream;

/**
 * Created by nanocosmos GmbH (c) 2015 - 2016
 */
public class About extends Fragment {
    private static final String TAG = "About";

    private TextView appVersion = null;
    private TextView nanoStreamVersion = null;
    private TextView bintuVersion = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);

        appVersion = (TextView) rootView.findViewById(R.id.textView_appVersion);
        nanoStreamVersion = (TextView) rootView.findViewById(R.id.textView_nanoStreamVersion);
        bintuVersion = (TextView) rootView.findViewById(R.id.textView_bintuVersion);

        appVersion.setText(BuildConfig.VERSION_NAME);
        nanoStreamVersion.setText(nanoStream.getVersion().fullVersion);
        bintuVersion.setText(BintuSDK.getVersion());

        return rootView;
    }
}
