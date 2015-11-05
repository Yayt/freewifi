package com.wifi.adp.freewifi;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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


    private static final int MENU_A = 0;
    private static final int MENU_B = 1;
    private static final int MENU_c = 2;

    public static String posinfo = "";
    public static String info_A = "";
    public static String info_B = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // TODO Auto-generated method stub
                //test static route for demo
                    /*
                    LatLng ll = marker.getPosition();
                    Intent i = new Intent();
                    i.setAction(Intent.ACTION_VIEW);
                    i.setClassName("com.google.android.apps.maps", "com.google.android.maps.driveabout.app.NavigationActivity");

                    Uri uri = Uri.parse("google.navigation:///?ll=60.16736,24.946413&q=WLAN base station at Esplanadi");
                    i.setData(uri);
                    startActivity(i);
                    */
                routeSearch(marker);
                return false;
            }
        });

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
        //test limited date for demo
        mMap.addMarker(new MarkerOptions().position(new LatLng(60.16736, 24.946413)).title("WLAN base station at Esplanadi").icon(BitmapDescriptorFactory.fromAsset("open_wifi_icon.png")));
        mMap.addMarker(new MarkerOptions().position(new LatLng(60.217216, 24.887)).title("Riistavuori comprehensive service centre Service Centre").icon(BitmapDescriptorFactory.fromAsset("open_wifi_icon.png")));
        mMap.addMarker(new MarkerOptions().position(new LatLng(60.25529, 24.99727)).title("Northern activity centre for kin care").icon(BitmapDescriptorFactory.fromAsset("open_wifi_icon.png")));
        mMap.addMarker(new MarkerOptions().position(new LatLng(60.229687, 24.883745)).title("Western social work  Haaga").icon(BitmapDescriptorFactory.fromAsset("open_wifi_icon.png")));
        mMap.addMarker(new MarkerOptions().position(new LatLng(60.2235, 25.075596)).title("Myllypuro neighbourhood station").icon(BitmapDescriptorFactory.fromAsset("open_wifi_icon.png")));
        mMap.addMarker(new MarkerOptions().position(new LatLng(60.16822, 24.92685)).title("Kamppi service centre").icon(BitmapDescriptorFactory.fromAsset("open_wifi_icon.png")));
        mMap.addMarker(new MarkerOptions().position(new LatLng(60.189045, 24.889673)).title("Munkkiniemi service centre Meilahti recreation centre").icon(BitmapDescriptorFactory.fromAsset("open_wifi_icon.png")));
        mMap.addMarker(new MarkerOptions().position(new LatLng(60.201553, 24.876307)).title("Munkkiniemi service centre").icon(BitmapDescriptorFactory.fromAsset("open_wifi_icon.png")));
        mMap.addMarker(new MarkerOptions().position(new LatLng(60.188896, 24.962563)).title("Kinapori comprehensive service centre Service centre").icon(BitmapDescriptorFactory.fromAsset("open_wifi_icon.png")));
        mMap.addMarker(new MarkerOptions().position(new LatLng(60.212097, 25.07988)).title("Itäkeskus Library").icon(BitmapDescriptorFactory.fromAsset("open_wifi_icon.png")));


        //json read

        String json = null;
        try {

            InputStream is = getAssets().open("inout_wlan.json");

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");
            // Log.i("MyActivity", json);


        } catch (IOException ex) {
            ex.printStackTrace();
        }


        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                mMyLocation = location;
                if (mMyLocation != null && mMyLocationCentering == false) { // Getting device GPS and foucus
                    mMyLocationCentering = true;
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(mMyLocation.getLatitude(), mMyLocation.getLongitude()), 14.0f);
                    mMap.animateCamera(cameraUpdate);
                }
            }
        });
    }

    private void routeSearch(Marker marker) {


        LatLng origin = new LatLng(mMyLocation.getLatitude(), mMyLocation.getLongitude());
        LatLng dest = marker.getPosition();


        String url = getDirectionsUrl(origin, dest);

        DownloadTask downloadTask = new DownloadTask();


        downloadTask.execute(url);

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
