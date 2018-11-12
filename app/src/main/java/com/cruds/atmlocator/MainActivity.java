package com.cruds.atmlocator;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSION_ACCESS_FINE_LOCATION = 4567;

    private LocationManager locationManager;
    private LocationListener locationListenerGPS;
    private Location currentLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
        }

        locationListenerGPS = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Toast.makeText(MainActivity.this, "Long/Lat" + location.getLongitude()
                        + ":" + location.getLatitude(), Toast.LENGTH_LONG).show();

                currentLocation = location;

                //locationManager.removeUpdates(locationListenerGPS);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };

    }

    public void handleATMBtnClick(View v)
    {
       if(currentLocation == null)
       {
           startLocationUpdates();
       }
       else
       {
           //getGeoAddresss();
           new GeoCoderTask().execute();
       }
    }

    private void startLocationUpdates()
    {
        Log.d(LOG_TAG,"Starting Location Updates");
        int permissionCheck = ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER,
                        10000, 0, locationListenerGPS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListenerGPS);
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationManager.removeUpdates(locationListenerGPS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        int permissionCheck = ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER,
                    10000, 0, locationListenerGPS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Log.d(LOG_TAG,"GPS Permission Granted");
                    startLocationUpdates();
                }
                else
                {
                    Toast.makeText(this,"Unable to Request Location!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private class GeoCoderTask extends AsyncTask<Void,Void,String>
    {
        @Override
        protected String doInBackground(Void... voids) {
                Log.d(LOG_TAG,"Getting GEO Address ==>" + currentLocation.getLatitude() + "/" + currentLocation.getLongitude());
                Geocoder gecoder = new Geocoder(MainActivity.this, Locale.getDefault());
                String straddress = "";
                Log.d(LOG_TAG,"GEOCODER PRESENT?" + Geocoder.isPresent());
                try {
                    List<Address> addressList = gecoder.getFromLocation(currentLocation.getLatitude(),currentLocation.getLongitude(),1);

                    if(addressList != null && (addressList.size() > 0))
                    {
                        Address address = addressList.get(0);

                        for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                            straddress = straddress + address.getAddressLine(i) + ",";
                        }

                        Log.d(LOG_TAG,"GEO Address-->" + straddress);
                        //Toast.makeText(this, straddress, Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Log.e(LOG_TAG,"Exception while getting address for location",e);
                    Toast.makeText(MainActivity.this,"Network Error while getting Address for GPS Location!",Toast.LENGTH_LONG).show();
                }
                return straddress;
        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(MainActivity.this, "Address is " + s, Toast.LENGTH_SHORT).show();
        }
    }
}
