package com.wifi.adp.freewifi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class WifiListAdapter extends ArrayAdapter<WifiObject> {

    private ArrayList<WifiObject> wifiObjects;

    public WifiListAdapter(Context context, int resource,
                           ArrayList<WifiObject> wifiObjects) {
        super(context, resource, wifiObjects);
        this.wifiObjects = wifiObjects;
    }

    @Override

    public View getView(int position, View convertView, ViewGroup parent) {
        View targetView = convertView;
        if (targetView == null) {
            LayoutInflater li = (LayoutInflater) getContext().getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            targetView = li.inflate(R.layout.listitem, null);
        }
        WifiObject thisWifiObject = wifiObjects.get(position);

        if (thisWifiObject != null) {
            TextView tv = (TextView) targetView.findViewById(R.id.nameText);
            tv.setText(thisWifiObject.getName_en());
            tv = (TextView) targetView.findViewById(R.id.distanceText);
            double distanceMetric = thisWifiObject.getDistance();
            if (thisWifiObject.getDistance() >= 1000) {
                distanceMetric = distanceMetric / 100;
                distanceMetric = Math.round(distanceMetric * 100) / 100;
                distanceMetric = distanceMetric / 10;
                tv.setText(distanceMetric + " km");
            } else {
                tv.setText(Integer.toString((int) thisWifiObject.getDistance()) + " m");
            }
        }
        return targetView;
    }
}