package be.lode.jukebox.front.android.choosejukebox;

import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;

import be.lode.jukebox.front.android.Constants;
import be.lode.jukebox.front.android.R;
import be.lode.jukebox.front.android.artist.ArtistActivity;
import be.lode.jukebox.front.android.login.LoginActivity;

public class ChooseJukeboxActivity extends ListActivity {

    private static final String LOGTAG = Constants.getLogtag();
    private static final String JUKEBOX_URL = Constants.getUrl() + "alljukeboxes";
    private ListAdapter jukeboxListAdapter;
    private ArrayList<JukeboxItem> listData = new ArrayList<JukeboxItem>();
    private static final int SHOW_ARTISTS_ITEM = 1;
    private String serviceName;
    private String serviceId;
    private Profile profile;
    private Button scanButton;
    private String registerUrl;
    private String jbId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOGTAG, this.getClass().getSimpleName() + " onCreate");
        setContentView(R.layout.activity_choose_jukebox);

        Intent i = getIntent();


        serviceName = "facebook";
        profile = Profile.getCurrentProfile();
        serviceId  = profile.getId();

        scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                openBarCodeScanner();
            }
        });

        //Start async task
        new GetJukebox().execute();
    }

    private void openBarCodeScanner() {
        IntentIntegrator integrator = new IntentIntegrator(ChooseJukeboxActivity.this);
        integrator.initiateScan();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null && scanResult.getContents() != null) {
            String re = scanResult.getContents();
            Log.i(LOGTAG, this.getClass().getSimpleName() + " scanResult" + re);
            if(re.contains("jukeboxid") && re.contains("registercustomer"))
            {
                Profile profile = Profile.getCurrentProfile();

                this.registerUrl = re;
                this.serviceName = "facebook";
                this.serviceId = profile.getId();
                jbId = re.substring(re.lastIndexOf("=") + 1);
                new RegisterCustomer().execute();
            }
            else
            {
                showPopup("Register failed", "This is not a jukbeox login QR", false);
            }
        }
        else
        {
            // if intent is null, the back key was pressed
            if(intent != null)
                showPopup("Register failed", "Failed to register to the jukebox", false);
        }
    }

    private void showPopup(String title, String message, Boolean succes) {
        AlertDialog.Builder helpBuilder = new AlertDialog.Builder(this, R.style.JukeboxAlertDialogStyle);
        helpBuilder.setTitle(title);
        helpBuilder.setMessage(message);
        if (succes) {
            helpBuilder.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // not used
                        }
                    });
        } else {

            helpBuilder.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //return to choosejukebox view
                        }
                    });

        }

        // Remember, create doesn't show the dialog
        AlertDialog helpDialog = helpBuilder.create();
        helpDialog.show();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Log.i(LOGTAG, this.getClass().getSimpleName() + " onListItemClick");
        super.onListItemClick(l, v, position, id);

        Object o = jukeboxListAdapter.getItem(position);
        JukeboxItem jukeboxData = (JukeboxItem) o;
        openArtists(jukeboxData.getId());
    }

    private void openArtists(String id) {
        Intent i = new Intent(ChooseJukeboxActivity.this, ArtistActivity.class);
        i.putExtra("jukeboxId", id);
        startActivityForResult(i, SHOW_ARTISTS_ITEM);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LOGTAG, this.getClass().getSimpleName() + " onPause");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(LOGTAG, this.getClass().getSimpleName() + " onCreateOptionsMenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_choose_jukebox, menu);
        return true;
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
            LoginManager.getInstance().logOut();
            Intent intent = new Intent(ChooseJukeboxActivity.this,LoginActivity.class);
            ChooseJukeboxActivity.this.startActivity(intent);
            ChooseJukeboxActivity.this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class RegisterCustomer  extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Log.i(LOGTAG, this.getClass().getSimpleName() + " doInBackground");
            try {
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet();

                String uri = registerUrl;

                if (serviceName != null && serviceName.length() > 0 && serviceId != null && serviceId.length() > 0) {
                    String serviceNameEncoded = URLEncoder.encode(serviceName, "UTF-8");
                    String serviceIdEncoded = URLEncoder.encode(serviceId, "UTF-8");
                    uri = registerUrl + "&servicename=" + serviceNameEncoded + "&serviceid=" + serviceId;
                }

                request.setURI(new URI(uri));

                HttpResponse response = client.execute(request);
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    Log.i(LOGTAG, "Register Succes");
                    openArtists(jbId);

                } else {
                    Log.i(LOGTAG, "Failed: No entity");
                }
            } catch (Exception e) {
                Log.i(LOGTAG, "Exception occurred: " + e.toString());
            }
            return null;



        }
    }

    private class GetJukebox extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Log.i(LOGTAG, this.getClass().getSimpleName() + " doInBackground");
            // Loads JSON file and process data
            try {
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet();


                String uri = JUKEBOX_URL;

                if (serviceName != null && serviceName.length() > 0 && serviceId != null && serviceId.length() > 0) {
                    String serviceNameEncoded = URLEncoder.encode(serviceName, "UTF-8");
                    String serviceIdEncoded = URLEncoder.encode(serviceId, "UTF-8");
                    uri = JUKEBOX_URL + "?servicename=" + serviceNameEncoded + "&serviceid=" + serviceId;
                }

                request.setURI(new URI(uri));

                HttpResponse response = client.execute(request);
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    JSONObject respObject = new JSONObject(EntityUtils.toString(entity));
                    JSONArray jukeboxArray = respObject.getJSONArray("allJukeboxes");
                    for (int i = 0; i < jukeboxArray.length(); i++) {
                        JSONObject jukebox = jukeboxArray.getJSONObject(i);
                        JukeboxItem item = new JukeboxItem();
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
            if (listData.size() == 0)
            {
                openBarCodeScanner();
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
            jukeboxListAdapter = new JukeboxListAdapter(getApplicationContext(), listData);
            setListAdapter(jukeboxListAdapter);
        }
    }
}
