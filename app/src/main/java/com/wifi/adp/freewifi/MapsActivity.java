package com.wifi.adp.freewifi;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.maps.android.MarkerManager;
import com.google.maps.android.SphericalUtil;
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
    public ViewFlipper vf;
    public Marker currentMarker;
    public double distance;
    public boolean openedInfoBar = false;
    public boolean useMetric = true;
    public static String posinfo = "";
    public static String info_A = "";
    public static String info_B = "";
    public boolean firstClick = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        //On marker click listener which routes and opens bottom info bar
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (firstClick) {
                    currentMarker.setIcon(BitmapDescriptorFactory.fromAsset("open_wifi_icon.png"));
                }
                firstClick = true;
                currentMarker = marker;
                marker.setIcon(BitmapDescriptorFactory.fromAsset("icon_selected.png"));
                routeSearch(marker);
                //TODO Check for internet
                return false;
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latlong) {
                if (firstClick) {
                    currentMarker.setIcon(BitmapDescriptorFactory.fromAsset("open_wifi_icon.png"));
                }
                if (line != null) {
                    line.remove();
                }
                hideInfoBar();
            }
        });
        vf = (ViewFlipper) findViewById(R.id.viewFlipper);
    }

    private void hideInfoBar() {
        RelativeLayout infobar = (RelativeLayout) findViewById(R.id.infobar);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) infobar.getLayoutParams();
        openedInfoBar = false;
        //TODO use SLOW animation
        params.height = 0;
    }

    private void openInfoBar(List<LatLng> path) {
        //TODO Add infobar shadow
        //TODO use SLOW animation
        RelativeLayout infobar = (RelativeLayout) findViewById(R.id.infobar);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) infobar.getLayoutParams();
        openedInfoBar = true;
        //Use wrap_content
        params.height = -2;
//        Log.i("INFOBARHEIGHT", Integer.toString(infobar.getHeight()));

        TextView wifiNameText = (TextView) findViewById(R.id.wifiname);
        wifiNameText.setText(currentMarker.getTitle().toUpperCase());

        double distance = SphericalUtil.computeLength(path);

        writeDistance(distance);
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
                mMap.addMarker(new MarkerOptions().position(new LatLng(ja.getDouble("latitude"), ja.getDouble("longitude"))).title(ja.getString("name_en")).infoWindowAnchor(99999999, 999999).icon(BitmapDescriptorFactory.fromAsset("open_wifi_icon.png")));
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

    private void writeDistance(double distanceToDisplay) {
        TextView distanceText = (TextView) findViewById(R.id.wifidistance);
        distance = distanceToDisplay;
        if (useMetric) {
            if (distance >= 1000) {
                distance = distance / 100;
                distance = Math.round(distance * 100) / 100;
                distance = distance / 10;
                distanceText.setText(distance + " km");
            } else {
                distanceText.setText(distance + " m");
            }
        } else {
            //meters to feet
            distance = distance * 3.28084;

            //more than 1000 feet? use miles
            if (distance >= 1000) {
                distance = distance / 528;
//                Log.i("distance", Double.toString(distance));
                distance = Math.round(distance * 100) / 100;
                distance = distance / 10;
//                Log.i("distance", Double.toString(distance));
                distanceText.setText(distance + " miles");
            } else {
                distanceText.setText(distance + " feet");
            }
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
        if (vf.getDisplayedChild() != 1) {
            vf.setDisplayedChild(1);
        }
    }

    public void switchToMap(View view) {
        setUpMapIfNeeded();
        if (vf.getDisplayedChild() != 0) {
            vf.setDisplayedChild(0);
        }
    }

    public void switchToSettings(View view) {
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
            useMetric = false;
        } else {
            unitText.setText("Metric");
            useMetric = true;
        }
        if (openedInfoBar) {
            writeDistance(distance);
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

    public void startNavigation(View view) {
        //Starts navigation in walking mode
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + currentMarker.getPosition().latitude + "," + currentMarker.getPosition().longitude + "&mode=w");
        Intent intent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        startActivity(intent);
    }

    public void openMoreInfo(View view) {
    }

    public void openPrivacyPolicy(View view) {
//        AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this).create(); //Read Update
//        alertDialog.setTitle("Hi");
//        alertDialog.setMessage("I hope it works");
//        alertDialog.setButton("Continue..", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) {
//                // here you can add functions
//            }
//        });
//        alertDialog.show();
        LayoutInflater inflater = getLayoutInflater();
        View dialogue = inflater.inflate(R.layout.alertlayout, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("TESTINGTITLE");
        builder.setView(dialogue);
        builder.show();

    }

    public void openAboutUs(View view) {
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

                        //TODO Check if user has internet, if not, estimate distance
                        openInfoBar(points);
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
