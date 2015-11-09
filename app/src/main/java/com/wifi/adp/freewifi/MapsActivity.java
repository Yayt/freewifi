package com.wifi.adp.freewifi;

import android.app.ListActivity;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private static Location mMyLocation = null;
    private static boolean mMyLocationCentering = false;
    private Polyline line = null;

    public static String posinfo = "";
    public static String info_A = "";
    public static String info_B = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        //On marker click listener which routes and open bottom info bar
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                openInfoBar(marker);
                routeSearch(marker);
                return false;
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latlong){
                hideInfoBar();
            }
        });
    }

    private void hideInfoBar() {
        LinearLayout infobar = (LinearLayout) findViewById(R.id.infobar);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) infobar.getLayoutParams();

        //TODO change to weight
        params.height = 0;
    }

    private void openInfoBar(Marker marker) {
        //TODO overlay infobar over mapfragment
        //Changing to weight will fix the jumping around of the map since it wont have to resize.
        //TODO Add infobar shadow

        LinearLayout infobar = (LinearLayout) findViewById(R.id.infobar);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) infobar.getLayoutParams();

        //TODO change to weight
        params.height = 500;

        TextView wifiNameText = (TextView) findViewById(R.id.wifiname);
        wifiNameText.setText(marker.getTitle());
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        //json read
        String json = null;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(this.getAssets().open("inout_wlan.json")));
            String line;
            String str = "";
            while ((line = in.readLine()) != null) {
                str += line;
            }
            JSONArray jo = new JSONArray(str);
            for (int i = 0; i < jo.length(); i++) {
                JSONObject ja = jo.getJSONObject(i);
//                Log.i("LocationName", ja.getString("name_en"));
                //TODO Add to item to some list array here?
                mMap.addMarker(new MarkerOptions().position(new LatLng(ja.getDouble("latitude"), ja.getDouble("longitude"))).title(ja.getString("name_en")).icon(BitmapDescriptorFactory.fromAsset("open_wifi_icon.png")));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Add own location
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                mMyLocation = location;
                if (mMyLocation != null && mMyLocationCentering == false) { // Getting device GPS and focus
                    mMyLocationCentering = true;
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(mMyLocation.getLatitude(), mMyLocation.getLongitude()), 14.0f);
                    mMap.animateCamera(cameraUpdate);
                }
            }
        });
    }

    private void routeSearch(Marker marker) {
        //Check if the user's location has been determined
        if (mMyLocation != null) {
            LatLng origin = new LatLng(mMyLocation.getLatitude(), mMyLocation.getLongitude());
            LatLng dest = marker.getPosition();

            String url = getDirectionsUrl(origin, dest);
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute(url);
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String sensor = "sensor=false";
        //パラメータ
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&language=ja" + "&mode=" + "walking";

        //JSON指定
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {
            Log.d("Exception while downloading url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    public void switchToList(View view) {
        ViewFlipper vf = (ViewFlipper) findViewById(R.id.viewFlipper);
        if (vf.getDisplayedChild() != 1) {
            vf.setDisplayedChild(1);
        }
    }

    public void switchToMap(View view) {
        setUpMapIfNeeded();
        ViewFlipper vf = (ViewFlipper) findViewById(R.id.viewFlipper);
        if (vf.getDisplayedChild() != 0) {
            vf.setDisplayedChild(0);
        }
    }

    public void switchToSettings(View view) {
        ViewFlipper vf = (ViewFlipper) findViewById(R.id.viewFlipper);
        if (vf.getDisplayedChild() != 2) {
            vf.setDisplayedChild(2);
        }
    }

    public void openFilters(View view) {
    }

    public void sendFeedback(View view) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "kevinmarczyk@gmail.com", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback Wifi Helsinki");
        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }

    public void switchUnits(View view) {
        //TODO Actually switch units instead of only showing it
        TextView unitText = (TextView) findViewById(R.id.unitOfLengthUsed);
        if (unitText.getText().equals("Metric")) {
            unitText.setText("Imperial");
        } else {
            unitText.setText("Metric");
        }

    }

    public void openPlayStore(View view) {
        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {
        //get in AsyncTask

        @Override
        protected String doInBackground(String... url) {


            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }


        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }

    /*parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                parseJsonpOfDirectionAPI parser = new parseJsonpOfDirectionAPI();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        //ルート検索で得た座標を使って経路表示
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {


            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            if (result != null) {
                if (result.size() != 0) {

                    for (int i = 0; i < result.size(); i++) {
                        points = new ArrayList<LatLng>();
                        lineOptions = new PolylineOptions();

                        List<HashMap<String, String>> path = result.get(i);

                        for (int j = 0; j < path.size(); j++) {
                            HashMap<String, String> point = path.get(j);

                            double lat = Double.parseDouble(point.get("lat"));
                            double lng = Double.parseDouble(point.get("lng"));
                            LatLng position = new LatLng(lat, lng);

                            points.add(position);
                        }

                        //polyline
                        lineOptions.addAll(points);
                        lineOptions.width(10);
                        lineOptions.color(0x550000ff);

                    }

                    //draw and remove previous polyline
                    if (line != null) {
                        line.remove();
                    }
                    line = mMap.addPolyline(lineOptions);
                } else {
                    mMap.clear();
                    Toast.makeText(MapsActivity.this, "ルート情報を取得できませんでした", Toast.LENGTH_LONG).show();
                }
            }


        }
    }


}
