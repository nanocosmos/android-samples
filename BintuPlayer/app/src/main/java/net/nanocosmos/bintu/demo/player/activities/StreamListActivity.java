package net.nanocosmos.bintu.demo.player.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import net.nanocosmos.bintu.bintusdk.BintuSDK;
import net.nanocosmos.bintu.bintusdk.handler.StreamInfoListResponseHandler;
import net.nanocosmos.bintu.bintusdk.stream.State;
import net.nanocosmos.bintu.bintusdk.stream.StreamInfo;
import net.nanocosmos.bintu.bintusdk.util.StreamFilter;
import net.nanocosmos.bintu.demo.player.Util.BintuConnector;
import net.nanocosmos.bintu.demo.player.Util.StreamListAdapter;
import net.nanocosmos.nanoStream.demo.player.R;

import java.util.List;

/**
 * Created by nanocosmos GmbH (c) 2016
 */
public class StreamListActivity extends AppCompatActivity {
    private static final boolean LIVE = true;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView streamList;
    private FloatingActionButton search;
    private EditText searchField;
    private BintuSDK bintu;
    private Context ctx;
    private CustomStreamInfoResponseHandler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream_list);
        ctx = this;

        streamList = (ListView) findViewById(R.id.stream_list_view);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.stream_list_container);
        swipeRefreshLayout.setOnRefreshListener(new CustomRefreshListener());

        search = (FloatingActionButton)findViewById(R.id.bintu_serach_btn);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestBintu();
            }
        });
        searchField = (EditText) findViewById(R.id.input_bintu_search);
        searchField.setOnEditorActionListener(new CustomOnEditorActionListener());

        BintuConnector bintuConnector = BintuConnector.getInstance();

        bintu = bintuConnector.getBintu();

        handler = new CustomStreamInfoResponseHandler();
        requestBintu();
    }

    private class CustomRefreshListener implements SwipeRefreshLayout.OnRefreshListener {

        @Override
        public void onRefresh() {
            swipeRefreshLayout.setRefreshing(true);
            requestBintu();
        }
    }

    private class CustomStreamInfoResponseHandler implements StreamInfoListResponseHandler {
        @Override
        public void handle(List<StreamInfo> list) {
            StreamListAdapter adapter = new StreamListAdapter(ctx, list);
            streamList.setAdapter(adapter);
            swipeRefreshLayout.setRefreshing(false);
        }

        @Override
        public void onError(Throwable throwable) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private class CustomOnEditorActionListener implements TextView.OnEditorActionListener {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                search.callOnClick();
                return true;
            }
            return false;
        }
    }

    private void requestBintu(){
        StreamFilter filter = new StreamFilter();
        if(!searchField.getText().toString().isEmpty()) {
            String[] tags = searchField.getText().toString().split(",");
            if (tags.length > 0) {
                filter.addTags(tags);
            }
        }
        if(LIVE) {
            filter.setState(State.LIVE);
        }

        bintu.getStreams(filter, handler);

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
    }
}
