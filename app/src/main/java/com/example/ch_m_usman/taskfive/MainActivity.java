package com.example.ch_m_usman.taskfive;

import android.app.Dialog;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    EditText etsrc, etdestination;
    Button btnsubmitt, btnatm, btnhotel, btnhospital, btnfuel;
    TextView tvtime, tvdistance;
    String source, destination;

    private GoogleMap mMap;
    final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Double latitude, longitude;
    Location location;
    int GPS_ERROR_DIALOG = 0;
    Context mContext;
    SupportMapFragment mapFragment;


    private String location_route = "";

    PolylineOptions polylineOptions;

    double srclat, srclng, deslat, deslng;

    ArrayList<String> latList = new ArrayList<>();
    ArrayList<String> lngList = new ArrayList<>();
    ArrayList<String> vacinity = new ArrayList<>();
    ArrayList<String> name = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();

        polylineOptions = new PolylineOptions();
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MainActivity.this);
        mContext = this;

        if (serviceOK()) {
            mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                    // The next two lines tell the new client that “this” current class will handle connection stuff
                    .addConnectionCallbacks(MainActivity.this)
                    .addOnConnectionFailedListener(MainActivity.this)
                    //fourth line adds the LocationServices API endpoint from GooglePlayServices
                    .addApi(LocationServices.API)
                    .build();
            // Create the LocationRequest object
            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                    .setFastestInterval(1 * 1000);


        }
    }

    public void setMarker(String locality, double lat, double lng) {
        MarkerOptions options = new MarkerOptions()
                .title(locality)
                .position(new LatLng(lat, lng))
                .icon(BitmapDescriptorFactory.defaultMarker());
        mMap.addMarker(options);
    }

    public void showJson(String response) {
        try {
            Log.e("Response:", response + "");
            JSONObject object = new JSONObject(response);
            JSONArray routes_array = object.getJSONArray("routes");
            JSONObject zero = routes_array.getJSONObject(0);
            JSONArray legs = zero.getJSONArray("legs");
            JSONObject zero_legs = legs.getJSONObject(0);

            JSONObject distance = zero_legs.getJSONObject("distance");
            JSONObject duration = zero_legs.getJSONObject("duration");
            String endAddress = zero_legs.getString("end_address");
            etdestination.setText(endAddress);
            tvtime.setText(distance.getString("text"));
            tvdistance.setText(duration.getString("text"));


            JSONObject over_view = zero.getJSONObject("overview_polyline");
            String poly = over_view.getString("points");

            List<LatLng> waypoints = decodePoly(poly);

            polylineOptions.addAll(waypoints);
            mMap.addPolyline(polylineOptions);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


//    private void drawLine(){
//        PolylineOptions options = new PolylineOptions()
//                .add(marker1.getPosition())
//                .add(marker2.getPosition());
//        mMap.addPolyline(options);
//
//    }

    private void gotoLocation(double lat, double lng, float zoom) {
        LatLng latLng = new LatLng(lat, lng);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        if (mMap == null) {
            Toast.makeText(this, "Google map is null", Toast.LENGTH_SHORT).show();
        } else {
            mMap.moveCamera(cameraUpdate);
        }

    }

    public boolean serviceOK() {
        int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            Toast.makeText(this, "Ready to maps...", Toast.LENGTH_SHORT).show();
            return true;
        } else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvailable, this, GPS_ERROR_DIALOG);
            dialog.show();
        } else {
            Toast.makeText(this, "Can't connect to google play services", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public void bindViews() {
        etsrc = (EditText) findViewById(R.id.etsrc);
        etdestination = (EditText) findViewById(R.id.etdest);
        btnsubmitt = (Button) findViewById(R.id.btnsubmitt);
        btnhospital = (Button) findViewById(R.id.btnhospital);
        btnatm = (Button) findViewById(R.id.btnatm);
        btnfuel = (Button) findViewById(R.id.btnfuel);
        btnhotel = (Button) findViewById(R.id.btnhotel);

        tvdistance = (TextView) findViewById(R.id.tvdistance);
        tvtime = (TextView) findViewById(R.id.tvtime);

        btnhospital.setOnClickListener(this);
        btnsubmitt.setOnClickListener(this);
        btnatm.setOnClickListener(this);
        btnfuel.setOnClickListener(this);
        btnhotel.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (mGoogleApiClient.isConnected()) {
//            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (com.google.android.gms.location.LocationListener) this);
//            mGoogleApiClient.disconnect();
//        }
//    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnsubmitt:
                findPath();
                break;
            case R.id.btnhospital:
                findHospital();
                break;
            case R.id.btnhotel:
                findHotel();
                break;

            case R.id.btnatm:
                findAtm();
                break;
            case R.id.btnfuel:
                findFuel();
                break;
        }
    }

    public void findFuel(){

        latList.clear();
        lngList.clear();
        name.clear();
        mMap.clear();
        String hotelUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + srclat + "," + srclng + "&radius=500&type=fuel&key=AIzaSyAY84jh4mitlx5sXU_Z2p4ck4ToOJjeRhU";
        StringRequest request = new StringRequest(Request.Method.GET, hotelUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    JSONObject object = new JSONObject(s);
                    JSONArray resultArray = object.getJSONArray("results");
                    for (int position = 0; position < resultArray.length(); position++) {
                        JSONObject node = resultArray.getJSONObject(position);
                        name.add(node.getString("name"));
                        JSONObject geometry = node.getJSONObject("geometry");
                        JSONObject location = geometry.getJSONObject("location");
                        latList.add(location.getString("lat"));
                        lngList.add(location.getString("lng"));
                    }

                    for (int l = 0; l < latList.size(); l++) {
                        LatLng latLng = new LatLng(Double.parseDouble(latList.get(l)), Double.parseDouble(lngList.get(l)));
                        mMap.addMarker(new MarkerOptions().position(latLng).title(name.get(l)));
                    }

                    mMap.animateCamera(CameraUpdateFactory.zoomTo(14), 2000, null);


                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        queue.add(request);


    }

    public void findHotel() {
        latList.clear();
        lngList.clear();
        name.clear();
        mMap.clear();
        String hotelUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + srclat + "," + srclng + "&radius=500&type=restaurant&key=AIzaSyAY84jh4mitlx5sXU_Z2p4ck4ToOJjeRhU";
        StringRequest request = new StringRequest(Request.Method.GET, hotelUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    JSONObject object = new JSONObject(s);
                    JSONArray resultArray = object.getJSONArray("results");
                    for (int position = 0; position < resultArray.length(); position++) {
                        JSONObject node = resultArray.getJSONObject(position);
                        name.add(node.getString("name"));
                        JSONObject geometry = node.getJSONObject("geometry");
                        JSONObject location = geometry.getJSONObject("location");
                        latList.add(location.getString("lat"));
                        lngList.add(location.getString("lng"));
                    }

                    for (int l = 0; l < latList.size(); l++) {
                        LatLng latLng = new LatLng(Double.parseDouble(latList.get(l)), Double.parseDouble(lngList.get(l)));
                        mMap.addMarker(new MarkerOptions().position(latLng).title(name.get(l)));
                    }

                    mMap.animateCamera(CameraUpdateFactory.zoomTo(14), 2000, null);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        queue.add(request);


    }

    public void findAtm() {
        latList.clear();
        lngList.clear();
        name.clear();
        mMap.clear();
        String url_bank = "https://maps.googleapis.com/maps/api/place/search/json?location=" + srclat + "," + srclng + "&radius=1000&name=bank&sensor=false&key=AIzaSyAY84jh4mitlx5sXU_Z2p4ck4ToOJjeRhU";
        Log.e("Request:", url_bank + "");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url_bank, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    JSONObject object = new JSONObject(s);
                    JSONArray root = object.getJSONArray("results");
                    for (int i = 0; i < root.length(); i++) {
                        JSONObject node = root.getJSONObject(i);
                        name.add(node.getString("name"));
                        JSONObject geometry = node.getJSONObject("geometry");
                        JSONObject location = geometry.getJSONObject("location");
                        latList.add(location.getString("lat"));
                        lngList.add(location.getString("lng"));


                    }

                    for (int l = 0; l < latList.size(); l++) {
                        LatLng latLng = new LatLng(Double.parseDouble(latList.get(l)), Double.parseDouble(lngList.get(l)));
                        mMap.addMarker(new MarkerOptions().position(latLng).title(name.get(l)));
                    }

                    mMap.animateCamera(CameraUpdateFactory.zoomTo(14), 2000, null);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(stringRequest);
    }

    public void findHospital() {
        latList.clear();
        lngList.clear();
        name.clear();
        mMap.clear();
        String url_hospital = "https://maps.googleapis.com/maps/api/place/search/json?location=" + srclat + "," + srclng + "&radius=1000&name=hospital&sensor=false&key=AIzaSyAY84jh4mitlx5sXU_Z2p4ck4ToOJjeRhU";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url_hospital,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("Response::", response + "");
                        try {
                            hospitalJson(response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(stringRequest);
    }

    public void hospitalJson(String response) throws IOException {
        try {
            JSONObject responeObject = new JSONObject(response);
            JSONArray result_array = responeObject.getJSONArray("results");
            int length = result_array.length();
            for (int i = 0; i < length; i++) {
                JSONObject root = result_array.getJSONObject(i);
                JSONObject geometry = root.getJSONObject("geometry");
                JSONObject location = geometry.getJSONObject("location");
                latList.add(location.getString("lat"));
                lngList.add(location.getString("lng"));
                vacinity.add(root.getString("vicinity"));
                name.add(root.getString("name"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        int size = latList.size();
        for (int s = 0; s < size; s++) {
            Double lat = Double.parseDouble(latList.get(s));
            Double lng = Double.parseDouble(lngList.get(s));
            LatLng latLng = new LatLng(lat, lng);
            mMap.addMarker(new MarkerOptions().position(latLng).title(name.get(s)));
        }
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14), 2000, null);
    }

    public void findPath() {
        destination = etdestination.getText().toString();
        Geocoder geocoder = new Geocoder(MainActivity.this);
        List<Address> list;

        try {
            list = geocoder.getFromLocationName(destination, 1);
            if (list.size() > 0) {
                Address address = list.get(0);
                String locality = address.getLocality();
                deslat = address.getLatitude();
                deslng = address.getLongitude();
                double lat = address.getLatitude();
                double lng = address.getLongitude();

                gotoLocation(lat, lng, 6);

                setMarker(locality, lat, lng);

                location_route = "https://maps.googleapis.com/maps/api/directions/json?origin=" + srclat + "," + srclng + "&destination=" + deslat + "," + deslng + "&key=AIzaSyDaChznYcNbZwMC850NIobZVbLy6ypb6Ns";

                StringRequest stringRequest = new StringRequest(Request.Method.GET, location_route, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        showJson(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
                RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
                requestQueue.add(stringRequest);


            } else {
                (Toast.makeText(MainActivity.this, "Destination not found", Toast.LENGTH_LONG)).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        latitude = location.getLatitude();
        longitude = location.getLongitude();


        try {
            Geocoder geocoder = new Geocoder(this);
            List<android.location.Address> list = geocoder.getFromLocation(latitude, longitude, 1);
            Address address = list.get(0);
            String locality = address.getLocality();
            etsrc.setText(locality);
            srclat = address.getLatitude();
            srclng = address.getLongitude();
            setMarker(locality, address.getLatitude(), address.getLongitude());
            gotoLocation(address.getLatitude(), address.getLongitude(), 6);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, latitude + " WORKS " + longitude + "", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);
        } else {
            //If everything went fine lets get latitude and longitude
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            Geocoder gcd = new Geocoder(MainActivity.this, Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = gcd.getFromLocation(latitude, longitude, 1);
                Address address = addresses.get(0);
                srclat = address.getLatitude();
                srclng = address.getLongitude();
                setMarker(address.getLocality(), latitude, longitude);
                if (addresses.size() > 0) {
                    String mUserLocation = "";
                    for (int i = 0; i < 3; i++) { //Since it return only four value we declare this as static.
                        mUserLocation = mUserLocation + addresses.get(0).getAddressLine(i).replace(",", "") + " ";
                    }

                    Log.e("Current Location:", mUserLocation + "");
                    etsrc.setText(mUserLocation);

                    gotoLocation(latitude, longitude, 6);
                    Toast.makeText(this, mUserLocation + " ", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Can not find adress", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                Toast.makeText(MainActivity.this, "Error:" + e, Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        } else {
            Toast.makeText(MainActivity.this, "Error: Location services connection failed with code=" + connectionResult.getErrorCode(), Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mMap.setMyLocationEnabled(true);


    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}
