package be.lode.jukebox.front.android;

/**
 * Created by Lode on 28/04/2015.
 */
public class Constants {
    private static final String LOGTAG = "JukeboxLog";
    private static final String URL = "http://192.168.56.1:8080/";

    public static String getUrl() {
        return URL;
    }

    public static String getLogtag() {
        return LOGTAG;
    }
}
