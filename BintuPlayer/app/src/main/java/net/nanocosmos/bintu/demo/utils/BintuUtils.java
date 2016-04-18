package net.nanocosmos.bintu.demo.utils;

import android.net.Uri;
import android.text.TextUtils;

import java.text.ParseException;

/**
 * Created by Jelger on 03.02.2016.
 */
public class BintuUtils {
    public static String getStreamIDFromPlayoutURL(String URL){
        if (URL == null){
            throw new IllegalArgumentException("Please provide an valid Playout-URL");
        }
        Uri uri = Uri.parse(URL);
        String id = getStreamIDFromPlayoutURL(uri);
        if (TextUtils.isEmpty(id)){
            throw new RuntimeException(new ParseException("Wasn't able to parse the URL.",URL.lastIndexOf('/')));
        }
        return id;
    }

    public static String getStreamIDFromPlayoutURL(Uri uri) {
        if (uri == null){
            throw new IllegalArgumentException("Please provide an valid Playout-URL");
        }
        return uri.getLastPathSegment();
    }
}
