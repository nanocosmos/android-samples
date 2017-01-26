package net.nanocosmos.bintu.demo.encoder.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.TextureView;

import net.nanocosmos.bintu.demo.encoder.activities.StreamActivity;

/**
 * Created by nanocosmos GmbH (c) 2015
 */
public class StreamPreview extends TextureView
{
    public StreamPreview(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public StreamPreview(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public StreamPreview(Context context)
    {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int camWidth = StreamActivity.videoResolution.getHeight();
        int camHeight = StreamActivity.videoResolution.getHeight();

        if(StreamActivity.usedVideoResolution != null) {
            camWidth = StreamActivity.usedVideoResolution.getWidth();
            camHeight = StreamActivity.usedVideoResolution.getHeight();
        }
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            if(StreamActivity.usedVideoResolution != null) {
                camWidth = StreamActivity.usedVideoResolution.getHeight();
                camHeight = StreamActivity.usedVideoResolution.getWidth();
            }else{
                camWidth = StreamActivity.videoResolution.getHeight();
                camHeight = StreamActivity.videoResolution.getHeight();
            }
            width = (height * camWidth) / camHeight;
        }else {
            height = (width * camHeight) / camWidth;
        }

        setMeasuredDimension(width, height);
    }
}
