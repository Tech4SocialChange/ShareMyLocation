package com.example.save.sharelocation;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Created by Akbar on 18-Nov-16.
 */

public class LocationService extends Service implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {


    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    public void onCreate() {
        super.onCreate();

        buildGoogleApiClient();

        sharedPreferences = getSharedPreferences("SaveData", MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        checkLocation();

        return super.onStartCommand(intent, flags, startId);
    }

    private void checkLocation() {

        String saveLatLan = sharedPreferences.getString("latLng","23.536244523,78.526455263");
        //String currLatlan = getCurrentLocation().getLatitude()+getCurrentLocation().getLatitude();


        double saveLat= Double.parseDouble(saveLatLan.split(",")[0]) ;
        double saveLan = Double.parseDouble(saveLatLan.split(",")[1]);

        double a= getCurrentLocation().getLatitude();
        double b= getCurrentLocation().getLongitude();

        //   double distance = distance(ll.latitude, ll.longitude, temp.Latitude, temp.Longitue);
        double distance = distance(saveLat, saveLan, a, b);

        if (distance > 1  ) {
            editor.putInt("bit", 1);
            editor.commit();
        }




        if (distance < 0.1 ) {

            String ePhoneNumber = sharedPreferences.getString("phoneNumberLn","");

                android.telephony.SmsManager smsManager = android.telephony.SmsManager.getDefault();
                smsManager.sendTextMessage(ePhoneNumber, null, "Your friends and family are in/at " + getFullAddress(a,b), null, null);// + "\n\"http://maps.google.com/maps?q=" + location.getLatitude() + "," + location.getLongitude(), null, null);

//                String msg = "Your friends and family are in/at " + temp.Name + "\n\"http://maps.google.com/maps?q=" + location.getLatitude() + "," + location.getLongitude();
//                Notify(ctx,"Current Location", msg);
//                currentPlace = temp.Name;
//                rnd = new Random().nextInt();
            }
         else {

        }


    }




private static double distance(double lat1, double lng1, double lat2, double lng2) {

        double earthRadius = 6371; // in miles, change to 6371 for kilometer output

        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);

        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
        * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c; // output distance, in KM
        }




@Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public Location getCurrentLocation() {
        if (mGoogleApiClient == null) {
            buildGoogleApiClient();
        }
        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        return mLastLocation;
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks( this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    GoogleApiClient mGoogleApiClient;

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public String getFullAddress(double lat, double lon) {
        Context context = this;
        String address = "";

        Geocoder geocoder = new Geocoder(context, new Locale("en"));
        try {
            // get address from location
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);

            if (addresses != null && addresses.size() != 0) {
                StringBuilder builder = new StringBuilder();
                Address returnAddress = addresses.get(0);
                for (int i = 0; i < returnAddress.getMaxAddressLineIndex(); i++) {
                    builder.append(returnAddress.getAddressLine(i));
                    builder.append(", ");
                }
                builder.append(returnAddress.getLocality() + ", ");
                builder.append(returnAddress.getPostalCode() + ", ");
                builder.append(returnAddress.getSubAdminArea() + ", ");
                builder.append(returnAddress.getSubLocality() + ", ");
                builder.append(returnAddress.getPremises() + ", ");
                builder.append(returnAddress.getCountryName() + ", ");
                address = builder.toString();


                // Toast.makeText(getApplicationContext(), messageLocation, Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
        }

        return address.replaceAll("null,","");
    }
}
