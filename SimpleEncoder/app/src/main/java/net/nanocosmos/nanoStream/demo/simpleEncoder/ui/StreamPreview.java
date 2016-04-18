package net.nanocosmos.nanoStream.demo.simpleEncoder.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.SurfaceView;

import net.nanocosmos.nanoStream.demo.simpleEncoder.activities.MainActivity;

/**
 * Created by nanocosmos GmbH (c) 2015 - 2016
 */
public class StreamPreview extends SurfaceView
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
        int camWidth = MainActivity.videoResolution.getWidth();
        int camHeight = MainActivity.videoResolution.getHeight();

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            camWidth = MainActivity.videoResolution.getHeight();
            camHeight = MainActivity.videoResolution.getWidth();
            width = (height * camWidth) / camHeight;
        }else {
            height = (width * camHeight) / camWidth;
        }

        setMeasuredDimension(width, height);
    }
}
