package be.lode.jukebox.front.android;

/**
 * Created by Lode on 28/04/2015.
 */
public class Constants {
    private static final String LOGTAG = "JukeboxLog";
    private static final String URL = "http://192.168.123.103:8080/";
    private static final String PAYPALSANDBOXAPPID = "APP-80W284485P519543T";

    public static String getPayPalSandboxAppId() {
        return PAYPALSANDBOXAPPID;
    }

    public static String getUrl() {
        return URL;
    }

    public static String getLogtag() {
        return LOGTAG;
    }
}
