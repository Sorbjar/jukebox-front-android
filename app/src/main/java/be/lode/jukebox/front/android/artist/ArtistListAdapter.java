package be.lode.jukebox.front.android.artist;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import be.lode.jukebox.front.android.Constants;
import be.lode.jukebox.front.android.R;

/**
 * Created by LOD on 27/4/2015.
 */
public class ArtistListAdapter extends BaseAdapter {
    private ArrayList listData;
    private LayoutInflater layoutInflater = null;
    protected Context context;
    private static final String LOGTAG = Constants.getLogtag();

    public ArtistListAdapter(Context context, ArrayList listData) {
        this.listData = listData;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        Log.i(LOGTAG, "ArtistListAdapter constructor executed.");
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        //convertView = The old view to reuse, if possible. Note: You should check that this view is non-null and of an appropriate type before using.
        if (convertView == null) {
            //create new holder & inflate view
            convertView = layoutInflater.inflate(R.layout.row_item, null);
            holder = new ViewHolder();
            holder.nameView = (TextView) convertView.findViewById(R.id.name);
            convertView.setTag(holder);
        } else {
            //reuse holder & view of convertView
            holder = (ViewHolder) convertView.getTag();
        }

        ArtistItem item = (ArtistItem) listData.get(position);
        holder.nameView.setText(item.getName());
        return convertView;
    }

    static class ViewHolder {
        TextView nameView;
    }
}
