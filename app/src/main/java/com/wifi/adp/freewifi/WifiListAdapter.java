package com.wifi.adp.freewifi;

import android.app.Activity;
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

            TextView unitOfLengthView = (TextView) ((Activity) getContext()).findViewById(R.id.unitOfLengthUsed);
            String unitOfLength = (String) unitOfLengthView.getText();

            tv = (TextView) targetView.findViewById(R.id.distanceText);
            double distance = thisWifiObject.getDistance();
            if (unitOfLength.equals("Metric")) {
                double distanceMetric = distance;
                if (thisWifiObject.getDistance() >= 1000) {
                    distanceMetric = distanceMetric / 100;
                    distanceMetric = Math.round(distanceMetric * 100) / 100;
                    distanceMetric = distanceMetric / 10;
                    tv.setText(distanceMetric + " km");
                } else {
                    tv.setText(Integer.toString((int) thisWifiObject.getDistance()) + " m");
                }
            } else {
                //meters to feet
                double distanceImperial = distance * 3.28084;

                //more than 1000 feet? use miles
                if (distanceImperial >= 1000) {
                    distanceImperial = distanceImperial / 528;
                    distanceImperial = Math.round(distanceImperial * 100) / 100;
                    distanceImperial = distanceImperial / 10;
                    tv.setText(distanceImperial + " mi");
                } else {
                    tv.setText((int) distanceImperial + " ft");
                }
            }
        }
        return targetView;
    }
}
