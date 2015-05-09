package be.lode.jukebox.front.android.qrscanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import be.lode.jukebox.front.android.R;


public class QRScannerActivity extends Activity {
    Button b1;
    static String contents;

    public static final int REQUEST_CODE = 1;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);
        b1 = (Button) findViewById(R.id.button1);
        b1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                IntentIntegrator integrator = new IntentIntegrator(QRScannerActivity.this);
                integrator.initiateScan();


            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            String re = scanResult.getContents();
            //TODO use scanning data
            Log.d("code", re);
        }

        //TODO else
    }

}