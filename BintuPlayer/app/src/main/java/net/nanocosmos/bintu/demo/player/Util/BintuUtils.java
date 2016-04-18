package net.nanocosmos.bintu.demo.player.Util;

import android.net.Uri;
import android.text.TextUtils;

import org.w3c.dom.Text;

/**
 * Created by nanocosmos GmbH (c) 2015 - 2016
 */
public class BintuUtils {

    public static String getStreamIDFromPlayoutURL(String URL){
        Uri path = null;
        if (TextUtils.isEmpty(URL)){
            throw new RuntimeException(new IllegalArgumentException("You have to provide a valid playout url."));
        }
        path = Uri.parse(URL);
        if (path == null){
            throw new RuntimeException(new IllegalArgumentException("You have to provide a valid playout url."));
        }
        return getStreamIDFromPlayoutURI(path);
    }
    public static String getStreamIDFromPlayoutURI(Uri uri){
        return uri.getLastPathSegment();
    }
}
