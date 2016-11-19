package com.example.save.sharelocation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.InputType;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.LocationResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    Toolbar toolbar;
    NavigationView navigationView;

    EditText etUserName, etUserNumber, etLocationName, etPhoneNumberForGetLocation1,
            etPhoneNumberForGetLocation2, etPhoneNumberForGetLocation3, etSecurityCode;
    LinearLayout userNameNumberLo, locationNotifierLo, securityCodeEtLo,securityCodeTvLo;

    ScrollView locationSmsLo;
    TextView tvSecurityCode;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;


    int flagForRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        floatingActionButtonAndDrawer();
        initialize();


    }

    private void initialize() {

        etUserNumber = (EditText) findViewById(R.id.etUserNumber);
        etUserName = (EditText) findViewById(R.id.etUserName);
        etLocationName = (EditText) findViewById(R.id.etLocationName);
        etPhoneNumberForGetLocation1 = (EditText) findViewById(R.id.etPhoneNumberForGetLocation1);
        etPhoneNumberForGetLocation2 = (EditText) findViewById(R.id.etPhoneNumberForGetLocation2);
        etPhoneNumberForGetLocation3 = (EditText) findViewById(R.id.etPhoneNumberForGetLocation3);
        tvSecurityCode = (TextView)findViewById(R.id.tvSecurityCode);
        userNameNumberLo = (LinearLayout) findViewById(R.id.userNameNumberLo);
        locationNotifierLo = (LinearLayout) findViewById(R.id.locationNotifierLo);
        locationSmsLo = (ScrollView) findViewById(R.id.locationSmsLo);
        securityCodeEtLo = (LinearLayout) findViewById(R.id.securityCodeEtLo);
        securityCodeTvLo = (LinearLayout) findViewById(R.id.securityCodeTvLo);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        sharedPreferences = getSharedPreferences("SaveData", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        flagForRegister = sharedPreferences.getInt("flagForRegister", 1);

        //  flagForRegister=1;

        if (flagForRegister == 1) {
            toolbar.setVisibility(View.GONE);
            userNameNumberLo.setVisibility(View.VISIBLE);
            locationNotifierLo.setVisibility(View.GONE);
            locationSmsLo.setVisibility(View.GONE);
        } else {
            userNameNumberLo.setVisibility(View.GONE);
            locationNotifierLo.setVisibility(View.VISIBLE);
            toolbar.setVisibility(View.VISIBLE);
            setTitle("Location Notifier");
        }

        // saveIntInSharedPreferences("flagForRegister", 1);



    }

    String userName;
    String userNumber;
    public void btnRegister(View view) {

        /*Send to server user information*/
        userName = etUserName.getText().toString();
        userNumber = etUserNumber.getText().toString();


        if (userName.length() > 0 && userNumber.length() > 0) {
            sendSmsForVerify(userNumber);
            alertDialog();

        } else {
            Toast.makeText(this, "Input is Blank", Toast.LENGTH_SHORT).show();
        }

    }

    private void alertDialog( ) {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Code Verification");
            alertDialog.setCancelable(false);
            alertDialog.setMessage("Please Enter Your Verification Code");
            alertDialog.setIcon(R.drawable.alert_icon);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);

            final EditText etVerificationCode = new EditText(this);
            etVerificationCode.setLayoutParams(lp);
            alertDialog.setView(etVerificationCode);
            etVerificationCode.setInputType(InputType.TYPE_CLASS_NUMBER);


            alertDialog.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            String verificationCode  = etVerificationCode.getText().toString();

                            if (verificationCode.length()>0) {


                                if (verificationCode.equals(random)) {
                                    saveStringInSharedPreferences("userName", userName);
                                    saveStringInSharedPreferences("userNumber", userNumber);
                                    saveIntInSharedPreferences("flagForRegister", 0);
                                    userNameNumberLo.setVisibility(View.GONE);
                                    locationNotifierLo.setVisibility(View.VISIBLE);
                                    toolbar.setVisibility(View.VISIBLE);
                                    Toast.makeText(getApplicationContext(), "Registered", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(getApplicationContext(), "Phone Number is verified", Toast.LENGTH_LONG).show();
                                    //sendUserInformationToServer(userName, userNumber);
                                } else {

                                    dialog.cancel();
                                    Toast.makeText(MainActivity.this, "Wrong Code", Toast.LENGTH_SHORT).show();

                                    alertDialog();
                                }
                            }else {
                                dialog.cancel();
                                Toast.makeText(MainActivity.this, "Input is blank", Toast.LENGTH_SHORT).show();
                                alertDialog();
                            }


                        }
                    });

            alertDialog.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            alertDialog.setNeutralButton("Try Auto",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            tryAutoVerification(userName,userNumber);

                        }
                    });

            alertDialog.show();

    }


    private void alertDialogForEditSecurityCode( ) {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Edit");
        alertDialog.setCancelable(false);
        alertDialog.setMessage("Do you want edit Security code");
        alertDialog.setIcon(R.drawable.alert_icon);


        alertDialog.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {


                    }
                });

        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });


        alertDialog.show();

    }


    private void alertDialogForExit( ) {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Exit");
        alertDialog.setCancelable(false);
        alertDialog.setMessage("Do you want to exit");
        alertDialog.setIcon(R.drawable.alert_icon);


        alertDialog.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {


                    }
                });

        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });


        alertDialog.show();

    }


    private void tryAutoVerification(String userName,String userNumber){

//        Intent intent = getIntent();
//        String msg = intent.getStringExtra("get_msg");
//
//        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
//      //  msg = msg.replace("\n", "");
//       String message = msg.substring(msg.lastIndexOf(":") + 1, msg.length());
 //      String senderNumber = msg.substring(0, msg.lastIndexOf(":"));


        Uri mSmsQueryUri = Uri.parse("content://sms/inbox");
        String messageBody="";
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(mSmsQueryUri, null, null, null, null);

            while (cursor.moveToNext()){
                cursor.moveToFirst();
                messageBody = cursor.getString(cursor.getColumnIndexOrThrow("body"));
               // Toast.makeText(this, body, Toast.LENGTH_SHORT).show();
                break;
            }
            cursor.close();


        } catch (Exception e) {
        } finally {

        }



            /*    Toast.makeText(context, "From: " + senderNumber + " Message: " + message, Toast.LENGTH_LONG).show();*/
        String random = sharedPreferences.getString("codeForNumberVerification","fdffaudvfdvfvvfd+asvvvsavc");
        String userPhone = userNumber;


       // if (PhoneNumberUtils.compare(getApplicationContext(), senderNumber, userPhone) && random.equals(message)) {
        if (random.equals(messageBody)) {

            saveStringInSharedPreferences("userName", userName);
            saveStringInSharedPreferences("userNumber", userNumber);
            saveIntInSharedPreferences("flagForRegister", 0);
            userNameNumberLo.setVisibility(View.GONE);
            locationNotifierLo.setVisibility(View.VISIBLE);
            toolbar.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Registered", Toast.LENGTH_SHORT).show();

           // sendUserInformationToServer(userName, userNumber);
            Toast.makeText(getApplicationContext(), "Phone Number is verified", Toast.LENGTH_SHORT).show();


        } else {
            Toast.makeText(getApplicationContext(), "Wrong Code", Toast.LENGTH_SHORT).show();
            alertDialog();
        }

    }
    String random;
    private void sendSmsForVerify(final String phoneNumber) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {


                random = String.valueOf((int) (Math.random() * 1000 + 100));
                saveStringInSharedPreferences( "codeForNumberVerification", random);

                if (phoneNumber.length() > 0 && random.length() > 0) {

                    SmsManager smsManager = SmsManager.getDefault();    // *************************
                    smsManager.sendTextMessage(phoneNumber, null, String.valueOf(random), null, null);  // *****************


                } else {

                    Toast.makeText(MainActivity.this, "Code not sent", Toast.LENGTH_SHORT).show();
                }
            }
        }, 300);

    }


    public void sendUserInformationToServer(String userName, final String userNumber) {

        ConnectivityManager connectivity = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        RequestQueue requestQueue = Volley.newRequestQueue(this);


        String insertUrl = "";
        StringRequest request = new StringRequest(Request.Method.POST, insertUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NoConnectionError) {
                    finish();
                    Toast.makeText(getApplicationContext(), "Slow net connection", Toast.LENGTH_SHORT).show();
                }
            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("userNumber", userNumber);
                return parameters;
            }
        };
        NetworkInfo info = connectivity.getActiveNetworkInfo();

        if (info != null && info.isConnected()) {
            requestQueue.add(request);
        }
        //  int i = 0;
        //Toast.makeText(ctx, "'" + strRes + "'", Toast.LENGTH_LONG).show();
        // if (Operations.strRes != null) i = Integer.parseInt(Operations.strRes);

    }


    private void saveStringInSharedPreferences(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    private void saveIntInSharedPreferences(String key, int value) {
        editor.putInt(key, value);
        editor.commit();
    }


    private void floatingActionButtonAndDrawer() {


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.getMenu().getItem(0).setChecked(true);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_location_notifier) {
            setTitle("Location Notifier");
            locationNotifierLo.setVisibility(View.VISIBLE);
            locationSmsLo.setVisibility(View.GONE);

        } else if (id == R.id.nav_location_SMS) {
            setTitle("Location SMS");
            locationNotifierLo.setVisibility(View.GONE);
            locationSmsLo.setVisibility(View.VISIBLE);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    double latitude;
    double longitude;
    Location mLocation;

    @Override
    public void onLocationChanged(Location location) {

        latitude = location.getLatitude();
        mLocation = location;
        longitude = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        setMarker();
        new Handler().postDelayed(new Runnable() {
            public void run() {

                if (mLocation != null) {
                    LatLng latlng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());//Dhaka, Bangladesh

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 11));
                    mMap.addMarker(new MarkerOptions()
                            .title("akbar")
                            .position(latlng)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    );
                } else {
                    // Toast.makeText(MainActivity.this, "Please Turn on GPS and Internet", Toast.LENGTH_SHORT).show();
                }
            }
        }, 1500);


    }


    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void setMarker() {
        double lat = 0, lon = 0;
//        if (SApplication.LOCATION != null) {
//            lat = SApplication.LOCATION.getLatitude();
//            lon = SApplication.LOCATION.getLongitude();
//        }
//        LatLng curentLocation = new LatLng(lat, lon);
//        selectedLocation = curentLocation;
//        tvLatLng.setText(lat + "," + lon);
//        // tvLatLng.setText(Operations.GetFullAddress(getApplicationContext()));
//
//        // Toast.makeText(this, getAddress(lat,lon), Toast.LENGTH_SHORT).show();
//
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curentLocation, 17f));
//        mMap.addMarker(new MarkerOptions()
//                .position(curentLocation)
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
//        );
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onMapClick(LatLng latLng) {
                //tvLatLng.setText(latLng.latitude + "," + latLng.longitude);
                //  tvLatLng.setText(Operations.GetFullAddress(getApplicationContext(),latLng));
                // Toast.makeText(LocationNotifierActivity.this, getAddress(latLng.latitude,latLng.longitude), Toast.LENGTH_SHORT).show();

                etLocationName.setText(latLng.latitude + ", " + latLng.longitude);

                mMap.clear();
                Marker m = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                );
                // selectedLocation = latLng;
            }
        });
    }




    public void btnSaveCode(View view) {

        securityCodeEtLo.setVisibility(View.GONE);
        securityCodeTvLo.setVisibility(View.VISIBLE);

    }

    public void btnEditCode(View view) {

        securityCodeEtLo.setVisibility(View.VISIBLE);
        securityCodeTvLo.setVisibility(View.GONE);
    }

    public void btnSendCode(View view) {

    }

    public void btnSavePhoneNumber1(View view) {
    }

    public void btnSavePhoneNumber2(View view) {
    }

    public void btnSavePhoneNumber3(View view) {
    }
}
