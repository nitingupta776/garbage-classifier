package com.bhavuk.majorproject.garbageclassifier;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class InfoWndowAdapter extends Activity implements GoogleMap.InfoWindowAdapter {

    private Context context;

    public InfoWndowAdapter(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v =  inflater.inflate(R.layout.custom_google_marker, null);

        TextView itemType = (TextView) v.findViewById(R.id.ItemType);
        TextView otherInfo = (TextView) v.findViewById(R.id.organicProbability);

        itemType.setText("Type: " + marker.getTitle());

        otherInfo.setText(marker.getSnippet());


        return v;
    }

}
