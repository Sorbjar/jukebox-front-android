package be.lode.jukebox.front.android.song;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.paypal.android.MEP.CheckoutButton;
import com.paypal.android.MEP.PayPal;
import com.paypal.android.MEP.PayPalActivity;
import com.paypal.android.MEP.PayPalInvoiceData;
import com.paypal.android.MEP.PayPalPayment;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;

import be.lode.jukebox.front.android.Constants;
import be.lode.jukebox.front.android.R;

/**
 * Created by Lode on 28/04/2015.
 */
public class SongActivity extends ListActivity implements View.OnClickListener {

    private static final int PAYPAL_BUTTON_ID = 10001;
    private static final String LOGTAG = Constants.getLogtag();
    private static final String SONG_URL = Constants.getUrl() + "alltitles";
    private static final String ORDERSONG_URL = Constants.getUrl() + "ordersong";
    private ListAdapter songListAdapter;
    private ArrayList<SongItem> listData = new ArrayList<SongItem>();
    private String artistName;
    private String songName;
    private String jukeboxId;
    private CheckoutButton launchPayPalButton;
    private boolean paypalLibraryInit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOGTAG, this.getClass().getSimpleName() + " onCreate");
        setContentView(R.layout.activity_song);

        Intent i = getIntent();
        artistName = i.getStringExtra("artistName");
        jukeboxId = i.getStringExtra("jukeboxId");

        //Start async task
        new GetSong().execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LOGTAG, this.getClass().getSimpleName() + " onPause");
        // Logs 'app deactivate' App Event.
        //AppEventsLogger.deactivateApp(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(LOGTAG, this.getClass().getSimpleName() + " onCreateOptionsMenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_song, menu);
        return true;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Log.i(LOGTAG, this.getClass().getSimpleName() + " onListItemClick");
        super.onListItemClick(l, v, position, id);

        Object o = songListAdapter.getItem(position);
        SongItem songData = (SongItem) o;

        songName = songData.getName();
        //serialize the data of the food and put as extra in an Intent.
        //TODO paypal
        //Start async task
        if (!paypalLibraryInit)
            initPaypalLibrary();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(LOGTAG, this.getClass().getSimpleName() + " onOptionsItemSelected");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Paypal part
    public void initPaypalLibrary() {
        PayPal pp = PayPal.getInstance();

        if (pp == null) {  // Test to see if the library is already initialized

            // This main initialization call takes your Context, AppID, and target server
            pp = PayPal.initWithAppID(this, Constants.getPayPalSandboxAppId(), PayPal.ENV_NONE);

            // Required settings:

            // Set the language for the library
            pp.setLanguage("en_US");

            // Some Optional settings:

            // Sets who pays any transaction fees. Possible values are:
            // FEEPAYER_SENDER, FEEPAYER_PRIMARYRECEIVER, FEEPAYER_EACHRECEIVER, and FEEPAYER_SECONDARYONLY
            pp.setFeesPayer(PayPal.FEEPAYER_EACHRECEIVER);

            // true = transaction requires shipping
            pp.setShippingEnabled(true);

            paypalLibraryInit = true;
        }
    }

    @Override
    public void onClick(View v) {
        if (v == (CheckoutButton) findViewById(PAYPAL_BUTTON_ID)) {
            PayPalButtonClick(v);
        }
    }

    public void PayPalButtonClick(View arg0) {
// Create a basic PayPal payment
        PayPalPayment payment = new PayPalPayment();

// Set the currency type
        payment.setCurrencyType("EUR");

// Set the recipient for the payment (can be a phone number)
        payment.setRecipient("lode.deckers-receiver@gmail.com");

// Set the payment amount, excluding tax and shipping costs
        payment.setSubtotal(new BigDecimal(1.23));

// Set the payment type--his can be PAYMENT_TYPE_GOODS,
// PAYMENT_TYPE_SERVICE, PAYMENT_TYPE_PERSONAL, or PAYMENT_TYPE_NONE
        payment.setPaymentType(PayPal.PAYMENT_TYPE_GOODS);

// PayPalInvoiceData can contain tax and shipping amounts, and an
// ArrayList of PayPalInvoiceItem that you can fill out.
// These are not required for any transaction.
        PayPalInvoiceData invoice = new PayPalInvoiceData();

// Set the tax amount
        //invoice.setTax(new BigDecimal(_taxAmount));
    }

    private void showPayPalButton() {

// Generate the PayPal checkout button and save it for later use
        PayPal pp = PayPal.getInstance();
        launchPayPalButton = pp.getCheckoutButton(this, PayPal.BUTTON_278x43, CheckoutButton.TEXT_PAY);

// The OnClick listener for the checkout button
        launchPayPalButton.setOnClickListener(this);

// Add the listener to the layout
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.bottomMargin = 10;
        launchPayPalButton.setLayoutParams(params);
        /*
        launchPayPalButton.setId(PAYPAL_BUTTON_ID);
        */
        ((RelativeLayout) findViewById(R.id.activity_song)).addView(launchPayPalButton);
        ((RelativeLayout) findViewById(R.id.activity_song)).setGravity(Gravity.CENTER_HORIZONTAL);

    }

    public void PayPalActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (resultCode) {
// The payment succeeded
            case Activity.RESULT_OK:
                String payKey = intent.getStringExtra(PayPalActivity.EXTRA_PAY_KEY);
                this.paymentSucceeded(payKey);
                break;

// The payment was canceled
            case Activity.RESULT_CANCELED:
                this.paymentCanceled();
                break;

// The payment failed, get the error from the EXTRA_ERROR_ID and EXTRA_ERROR_MESSAGE
            case PayPalActivity.RESULT_FAILURE:
                String errorID = intent.getStringExtra(PayPalActivity.EXTRA_ERROR_ID);
                String errorMessage = intent.getStringExtra(PayPalActivity.EXTRA_ERROR_MESSAGE);
                this.paymentFailed(errorID, errorMessage);
        }
    }

    private void paymentFailed(String errorID, String errorMessage) {
        //TODO payment failed
    }

    private void paymentCanceled() {
        //TODO payment cancelled
    }

    private void paymentSucceeded(String payKey) {
        new OrderSong().execute();
    }

    private class GetSong extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Log.i(LOGTAG, this.getClass().getSimpleName() + " doInBackground");
            // Loads JSON file and process data
            try {
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet();

                String uri = SONG_URL;
                if (artistName != null && artistName.length() > 0) {
                    String artistEncoded = URLEncoder.encode(artistName, "UTF-8");
                    uri = SONG_URL + "?artist=" + artistEncoded;
                }
                request.setURI(new URI(uri));

                HttpResponse response = client.execute(request);
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    JSONObject respObject = new JSONObject(EntityUtils.toString(entity));
                    JSONArray songArray = respObject.getJSONArray("allTitles");
                    for (int i = 0; i < songArray.length(); i++) {
                        String song = songArray.getString(i);
                        //JSONObject song = songArray.getJSONObject(i);
                        SongItem item = new SongItem();
                        item.setName(song);
                        listData.add(item);
                    }

                } else {
                    Log.i(LOGTAG, "Failed: No entity");
                }
            } catch (Exception e) {
                Log.i(LOGTAG, "Exception occurred: " + e.toString());
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(LOGTAG, this.getClass().getSimpleName() + " onPreExecute");
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Log.i(LOGTAG, this.getClass().getSimpleName() + " onPostExecute");
            // set adapter after async task has loaded json file.
            songListAdapter = new SongListAdapter(getApplicationContext(), listData);
            setListAdapter(songListAdapter);
        }
    }

    private class OrderSong extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Log.i(LOGTAG, this.getClass().getSimpleName() + " doInBackground");
            // Loads JSON file and process data
            try {
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet();

                String uri = ORDERSONG_URL;

                if (artistName != null && artistName.length() > 0 && songName != null && songName.length() > 0) {
                    String artistNameEncoded = URLEncoder.encode(artistName, "UTF-8");
                    String songNameEncoded = URLEncoder.encode(songName, "UTF-8");
                    String jukeboxIdEncoded = URLEncoder.encode(jukeboxId, "UTF-8");
                    uri = ORDERSONG_URL + "?jukeboxid=" + jukeboxIdEncoded + "&artist=" + artistNameEncoded + "&title=" + songNameEncoded;
                }


                request.setURI(new URI(uri));

                HttpResponse response = client.execute(request);
                HttpEntity entity = response.getEntity();
                if (entity != null) {

                } else {
                    Log.i(LOGTAG, "Failed: No entity");
                }
            } catch (Exception e) {
                Log.i(LOGTAG, "Exception occurred: " + e.toString());
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(LOGTAG, this.getClass().getSimpleName() + " onPreExecute");
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Log.i(LOGTAG, this.getClass().getSimpleName() + " onPostExecute");
            // set adapter after async task has loaded json file.
        }
    }
}
