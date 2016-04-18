package net.nanocosmos.bintu.demo.player.fragments;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.nanocosmos.bintu.bintusdk.BintuSDK;
import net.nanocosmos.nanoStream.demo.player.R;
import net.nanocosmos.nanoStream.streamer.nanoStream;

/**
 * Created by nanocosmos GmbH (c) 2015 - 2016
 */
public class AboutFragment extends Fragment {
    private View view;
    private TextView bintuVersionText;
    private TextView appVersionText;
    private String tag = "NANO_ABOUT_FRAGMENT";
    private TextView nanoStreamVersionText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_about, null);
            bintuVersionText = (TextView) view.findViewById(R.id.initial_header_bintu_version);
            bintuVersionText.setText(getResources().getString(R.string.about_bintu_version) + " " + BintuSDK.getVersion());
            appVersionText = (TextView) view.findViewById(R.id.initial_header_app_version);
            try {
                appVersionText.setText(getResources().getString(R.string.about_app_version) + " " + getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionName);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(tag, "Failed to get version name", e);
            }
            nanoStreamVersionText = (TextView) view.findViewById(R.id.initial_header_nanoStream_version);
            nanoStreamVersionText.setText(getResources().getString(R.string.about_nanostream_version) + " " + nanoStream.getVersion().fullVersion);

        }
        return view;
    }
}
