package be.lode.jukebox.front.android.song;

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
import be.lode.jukebox.front.android.pay.PayActivity;

/**
 * Created by Lode on 28/04/2015.
 */
public class SongActivity extends ListActivity {


    private static final String LOGTAG = Constants.getLogtag();
    private static final String SONG_URL = Constants.getUrl() + "alltitles";
    private ListAdapter songListAdapter;
    private ArrayList<SongItem> listData = new ArrayList<SongItem>();
    private String artistName;
    private String songName;
    private String jukeboxId;
    private static final int SHOW_PAYMENT_ITEM = 1;

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
        Intent i = new Intent(SongActivity.this, PayActivity.class);
        i.putExtra("artistName", artistName);
        i.putExtra("songName", songName);
        i.putExtra("jukeboxId", jukeboxId);
        startActivityForResult(i,SHOW_PAYMENT_ITEM);
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
    //TODO move paypal to new activity, summarise purchase and add paypal button



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
}
