package be.lode.jukebox.front.android.choosejukebox;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.facebook.Profile;

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

public class ChooseJukeboxActivity extends ListActivity {

    private static final String LOGTAG = Constants.getLogtag();
    private static final String JUKEBOX_URL = Constants.getUrl() + "alljukeboxes";
    private ListAdapter jukeboxListAdapter;
    private ArrayList<JukeboxItem> listData = new ArrayList<JukeboxItem>();
    private static final int SHOW_ARTISTS_ITEM = 1;
    private String serviceName;
    private String serviceId;
    private Profile profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOGTAG, this.getClass().getSimpleName() + " onCreate");
        setContentView(R.layout.activity_song);

        Intent i = getIntent();


        serviceName = "facebook";
        profile = Profile.getCurrentProfile();
        serviceId  = profile.getId();

        //Start async task
        new GetJukebox().execute();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Log.i(LOGTAG, this.getClass().getSimpleName() + " onListItemClick");
        super.onListItemClick(l, v, position, id);

        Object o = jukeboxListAdapter.getItem(position);
        JukeboxItem jukeboxData = (JukeboxItem) o;

        //serialize the data of the food and put as extra in an Intent.
        Intent i = new Intent(ChooseJukeboxActivity.this, ArtistActivity.class);
        i.putExtra("jukeboxId", jukeboxData.getId());
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
            return true;
        }

        return super.onOptionsItemSelected(item);
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

                if(serviceName != null && serviceName.length() > 0 && serviceId != null && serviceId.length() > 0) {
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
            }catch(Exception e){
                Log.i(LOGTAG,"Exception occurred: " + e.toString());
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
