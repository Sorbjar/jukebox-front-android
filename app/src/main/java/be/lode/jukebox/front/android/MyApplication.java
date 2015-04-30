package be.lode.jukebox.front.android;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Lode on 29/04/2015.
 */
public class MyApplication extends Application {
    private static final String LOGTAG = Constants.getLogtag();
    @Override
    public void onCreate() {
        super.onCreate();
        printHashKey();
    }
     public void printHashKey()
     {
         try {
             PackageInfo info = getPackageManager().getPackageInfo(
                     "be.lode.jukebox.front.android",
                     PackageManager.GET_SIGNATURES);
             for (Signature signature : info.signatures) {
                 MessageDigest md = MessageDigest.getInstance("SHA");
                 md.update(signature.toByteArray());
                 Log.d(LOGTAG + "KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
             }
         } catch (PackageManager.NameNotFoundException e) {

         } catch (NoSuchAlgorithmException e) {

         }
     }
}
