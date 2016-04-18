package net.nanocosmos.bintu.demo.player.Util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import net.nanocosmos.bintu.bintusdk.stream.RtmpPlayout;
import net.nanocosmos.bintu.bintusdk.stream.StreamInfo;
import net.nanocosmos.bintu.demo.player.activities.PlayerActivity;
import net.nanocosmos.nanoStream.demo.player.R;

import java.util.List;

/**
 * Created by nanocosmos GmbH (c) 2016
 */
public class StreamListAdapter extends ArrayAdapter<StreamInfo> {
    private List<StreamInfo> streamInfos;
    private Context ctx;
    private int layoutResourceId;

    public StreamListAdapter(Context context, List<StreamInfo> objects) {
        super(context, R.layout.stream_list_element, objects);
        this.layoutResourceId = R.layout.stream_list_element;
        this.ctx = context;
        streamInfos = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
        {
            LayoutInflater inflater = ((Activity) ctx).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);
        }

        StreamInfo streamInfo = streamInfos.get(position);

        TextView tags = (TextView)convertView.findViewById(R.id.streamTags);
        TextView title = (TextView)convertView.findViewById(R.id.streamTitle);

        String strTags = "";
        String strTitle = "";
        for (String t : streamInfo.getTags()) {
            if(t.startsWith("title:") || t.startsWith("Title:")) {
                strTitle = t.replaceFirst("[Tt][Ii][Tt][Ll][Ee]:","");
            }else if(!t.contains(":")) {
                strTags += t + ", ";
            }
        }

        if(!strTags.isEmpty()) {
            strTags = strTags.trim();
            strTags = strTags.substring(0, strTags.length() - 1);
        }
        tags.setText("Tags: " + strTags);

        if(!strTitle.isEmpty()) {
            strTitle = strTitle.trim();
        }
        title.setText("Title: " + strTitle);

        convertView.setOnClickListener(new StreamClickListener(streamInfo));

        return convertView;
    }

    private class StreamClickListener implements View.OnClickListener {
        private StreamInfo info;

        public StreamClickListener(StreamInfo info) {
            this.info = info;
        }

        @Override
        public void onClick(View v) {

            if (info != null && info.getRtmpPlayouts() != null && info.getRtmpPlayouts().size() > 0) {
                RtmpPlayout rtmpPlayout = info.getRtmpPlayouts().get(0);
                String rtmpURL = rtmpPlayout.getUrl();
                String streamName = rtmpPlayout.getStreamName();

                Intent intent = new Intent(ctx, PlayerActivity.class);
                intent.putExtra("url", rtmpPlayout.getUrl());
                intent.putExtra("streamname", rtmpPlayout.getStreamName());
                ctx.startActivity(intent);
            }
       }
    }
}
