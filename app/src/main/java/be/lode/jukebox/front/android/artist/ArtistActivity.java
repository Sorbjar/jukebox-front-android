package be.lode.jukebox.front.android.artist;

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
import java.util.ArrayList;

import be.lode.jukebox.front.android.Constants;
import be.lode.jukebox.front.android.R;
import be.lode.jukebox.front.android.song.SongActivity;


public class ArtistActivity extends ListActivity {

    private static final String LOGTAG = Constants.getLogtag();
    private static final String ARTIST_URL = Constants.getUrl() + "allartists";
    private ListAdapter artistListAdapter;
    private ArrayList<ArtistItem> listData = new ArrayList<ArtistItem>();
    private static final int SHOW_SONGS_ITEM = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOGTAG, this.getClass().getSimpleName() + " onCreate");
        setContentView(R.layout.activity_artist);

        //Start async task
        new GetArtist().execute();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Log.i(LOGTAG, this.getClass().getSimpleName() + " onListItemClick");
        super.onListItemClick(l, v, position, id);

        Object o = artistListAdapter.getItem(position);
        ArtistItem artistData = (ArtistItem) o;

        //serialize the data of the food and put as extra in an Intent.
        Intent i = new Intent(ArtistActivity.this, SongActivity.class);
        i.putExtra("artistName", artistData.getName());
        startActivityForResult(i,SHOW_SONGS_ITEM);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(LOGTAG, this.getClass().getSimpleName() + " onCreateOptionsMenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class GetArtist extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Log.i(LOGTAG, this.getClass().getSimpleName() + " doInBackground");
            // Loads JSON file and process data
            try {
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet();

                request.setURI(new URI(ARTIST_URL));

                HttpResponse response = client.execute(request);
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    JSONObject respObject = new JSONObject(EntityUtils.toString(entity));
                    JSONArray artistArray = respObject.getJSONArray("allArtists");
                    for (int i = 0; i < artistArray.length(); i++) {
                        String artist = artistArray.getString(i);
                        //JSONObject artist = artistArray.getJSONObject(i);
                        ArtistItem item = new ArtistItem();
                        item.setName(artist);
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
            artistListAdapter = new ArtistListAdapter(getApplicationContext(), listData);
            setListAdapter(artistListAdapter);
        }
    }
}
