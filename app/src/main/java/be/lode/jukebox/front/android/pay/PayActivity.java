package be.lode.jukebox.front.android.pay;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

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

import be.lode.jukebox.front.android.Constants;
import be.lode.jukebox.front.android.R;
import be.lode.jukebox.front.android.artist.ArtistActivity;
import be.lode.jukebox.front.android.choosejukebox.JukeboxItem;
import be.lode.jukebox.front.android.login.LoginActivity;
import be.lode.jukebox.front.android.song.SongItem;
import be.lode.jukebox.front.android.song.SongListAdapter;
import be.lode.jukebox.front.android.splash.Splash;

public class PayActivity extends Activity implements View.OnClickListener {

    private static final String LOGTAG = Constants.getLogtag();
    private static final String ORDERSONG_URL = Constants.getUrl() + "ordersong";
    private static final int PAYPAL_BUTTON_ID = 10001;
    private static final int REQUEST_PAYPAL_CHECKOUT = 2;
    private CheckoutButton launchPayPalButton;
    private boolean paypalLibraryInit;
    private String artistName;
    private String songName;
    private String jukeboxId;
    private BigDecimal price;
    private String currency;
    private String paymentReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        Intent i = getIntent();
        artistName = i.getStringExtra("artistName");
        songName = i.getStringExtra("songName");
        jukeboxId = i.getStringExtra("jukeboxId");

        //Start async task
        new GetPaypal().execute();

        if (!paypalLibraryInit)
            initPaypalLibrary();
        showPayPalButton();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!paypalLibraryInit)
            initPaypalLibrary();
        showPayPalButton();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pay, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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

    public void initPaypalLibrary() {
        PayPal pp = PayPal.getInstance();

        if (pp == null) {  // Test to see if the library is already initialized

            // This main initialization call takes your Context, AppID, and target server
            //TODO set live
            pp = PayPal.initWithAppID(this, Constants.getPayPalSandboxAppId(), PayPal.ENV_SANDBOX);

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
        //TODO can't perform payment
        //TODO if price  =  0 => order freely
// Create a basic PayPal payment
        PayPalPayment payment = new PayPalPayment();

// Set the currency type
        if(currency ==  null || currency.length() == 0)
            payment.setCurrencyType("EUR");
        else
            payment.setCurrencyType(currency);

// Set the recipient for the payment (can be a phone number)
        payment.setRecipient(paymentReceiver);

// Set the payment amount, excluding tax and shipping costs
        if(price ==  null || price == new BigDecimal(0))
            payment.setSubtotal(new BigDecimal(0.0));
        else
            payment.setSubtotal(price);

// Set the payment type--his can be PAYMENT_TYPE_GOODS,
// PAYMENT_TYPE_SERVICE, PAYMENT_TYPE_PERSONAL, or PAYMENT_TYPE_NONE
        payment.setPaymentType(PayPal.PAYMENT_TYPE_SERVICE);

// PayPalInvoiceData can contain tax and shipping amounts, and an
// ArrayList of PayPalInvoiceItem that you can fill out.
// These are not required for any transaction.
        //PayPalInvoiceData invoice = new PayPalInvoiceData();

// Set the tax amount
        //invoice.setTax(new BigDecimal(_taxAmount));


        Intent checkoutIntent = PayPal.getInstance().checkout(payment, this /*, new ResultDelegate()*/);
        this.startActivityForResult(checkoutIntent, REQUEST_PAYPAL_CHECKOUT);
    }

    private void showPayPalButton() {
        removePayPalButton();
// Generate the PayPal checkout button and save it for later use
        PayPal pp = PayPal.getInstance();
        launchPayPalButton = pp.getCheckoutButton(this, PayPal.BUTTON_278x43, CheckoutButton.TEXT_PAY);

// The OnClick listener for the checkout button
        launchPayPalButton.setOnClickListener(this);

// Add the listener to the layout
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.bottomMargin = 10;
        launchPayPalButton.setLayoutParams(params);

        launchPayPalButton.setId(PAYPAL_BUTTON_ID);
        ((RelativeLayout) findViewById(R.id.activity_pay)).addView(launchPayPalButton);
        ((RelativeLayout) findViewById(R.id.activity_pay)).setGravity(Gravity.CENTER_HORIZONTAL);

    }

    private void removePayPalButton() {
        // Avoid an exception for setting a parent more than once
        if (launchPayPalButton != null) {
            ((RelativeLayout) findViewById(R.id.activity_pay))
                    .removeView(launchPayPalButton);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        PayPalActivityResult(requestCode, resultCode, data);
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
        Intent intent = new Intent(PayActivity.this,ArtistActivity.class);
        PayActivity.this.startActivity(intent);
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

    private class GetPaypal extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Log.i(LOGTAG, this.getClass().getSimpleName() + " doInBackground");
            // Loads JSON file and process data
            try {
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet();

                String uri = PAYPAL_URL;
                if (jukeboxId != null && artistName.length() > 0) {
                    String jukeboxEncoded = URLEncoder.encode(jukeboxId, "UTF-8");
                    uri = PAYPAL_URL + "?jukebox=" + jukeboxEncoded;
                }
                request.setURI(new URI(uri));

                HttpResponse response = client.execute(request);
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    JSONObject respObject = new JSONObject(EntityUtils.toString(entity));
                    JSONArray songArray = respObject.getJSONArray("allTitles");
                    for (int i = 0; i < songArray.length(); i++) {
                        JSONObject jukebox = jukeboxArray.getJSONObject(i);
                        item.setName(jukebox.getString("name"));
                        item.setId(jukebox.getString("id"));
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
            payListAdapter = new PayListAdapter(getApplicationContext(), listData);
            setListAdapter(payListAdapter);
        }
    }
}
