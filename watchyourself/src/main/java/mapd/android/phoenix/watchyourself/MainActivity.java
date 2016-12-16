package mapd.android.phoenix.watchyourself;

/**
 * Team Phoenix
 */


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationListener;

import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    /* Location Variables */
    private static final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    private GoogleApiClient googleApiClient;
    private static final String APP_ID = "AIzaSyDn9osFVDgjKdsqnlP8btgkn13s4eqiui0";
    String locationLink;

    double latitude;
    double longitude;

    GoogleMap googleMap;
    MarkerOptions markerOptions;
    LatLng latLng;
    TextView tAddress;
    String country;

    /*     Video Recording Variables    */
    MediaRecorder recorder;
    SurfaceHolder holder;
    boolean recording = false;

    //location related variables
    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);



        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);

        Intent intent = new Intent(MainActivity.this, ProviderService.class);
        startService(intent);

        //askForLocationPermission();  // Called Location Permission

        /*Home Screen Buttons Declarations */

        ImageButton msg_button = (ImageButton) findViewById(R.id.icon_msg);
        ImageButton mic_button = (ImageButton) findViewById(R.id.icon_mic);
        ImageButton camera_button = (ImageButton) findViewById(R.id.icon_camera);
        ImageButton call_button = (ImageButton) findViewById(R.id.icon_call);

        /* Send SMS Call */
        msg_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "Messaging Emergency Contact.", Toast.LENGTH_LONG).show();
                sendMessage();
            }
        });
//
//        /* Record Audio Call */
        mic_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,RecordingList.class);
                startActivity(intent);
            }
        });

//        /* Record Video Call */
        camera_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendMessage();
            }
        });
//
//        /* Emergency Calling Call */
        call_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "Calling Emergency Contact.", Toast.LENGTH_LONG).show();
                makeCall();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater= getMenuInflater();
        inflater.inflate(R.menu.menu_watchyourself,menu);
        return true;
    }

    /* Tool Bar Method */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.add_emergencyContact:
                Intent intentC = new Intent(MainActivity.this, AddEmergencyContactsActivity.class);
                startActivity(intentC);
                return true;
//            case R.id.configure_message:
//                Intent intentM = new Intent(MainActivity.this, CreateEmergencyMessageActivity.class);
//                startActivity(intentM);

            default:
                return super.onOptionsItemSelected(item);

        }
    }


    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mGoogleMap=googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        }
        else {
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}

    @Override
    public void onLocationChanged(Location location)
    {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

        //move map camera
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(14));

        //optionally, stop location updates if only current location is needed
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mGoogleMap.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    /* Send SMS Method Starts*/
    public void sendMessage() {
        try {
                String message = getString(R.string.emergency_msg)+locationLink;
                SmsManager smsManager = SmsManager.getDefault();
                SharedPreferences sharedPreferences = getSharedPreferences("Emer_contact",1);
                String value = sharedPreferences.getString("contact1","null");

                if(value.equals("null"))
                {
                    Toast.makeText(getApplicationContext(), R.string.add_contact_req,Toast.LENGTH_LONG).show();
                }
                else {
                    String[] arr=   value.split(":");
                    String phoneno= arr[1];
                    Log.e("phone no :: ",phoneno);

                    //messaging stopping here???
                    smsManager.sendTextMessage(phoneno, null, message, null, null);


                    Toast.makeText(getApplicationContext(), R.string.sms_sent, Toast.LENGTH_LONG).show();
                }
            }
            catch (SecurityException e)
            {

            }
        } /* Send SMS Method Ends*/

    public void makeCall() {

        SharedPreferences sharedPreferences = getSharedPreferences("Emer_contact", 1);
        String value = sharedPreferences.getString("contact1", "null");
        if (value.equals("null")) {
            Toast.makeText(getApplicationContext(), R.string.add_contact_req,Toast.LENGTH_LONG).show();
        } else {
            String[] arr = value.split(":");
            String phoneno = arr[1];
            Log.e("phone no :: ", phoneno);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 3);

            } else {
                try {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneno));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                } catch (SecurityException e) {

                }
            }
        }
    }

    /*
    LOcation data
     */
//
//    public void askForLocationPermission()
//    {
//
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION }, PERMISSION_ACCESS_FINE_LOCATION);
//            googleApiClient = new GoogleApiClient.Builder(this, this, this).addApi(LocationServices.API).build();
//        }
//        else
//        {
//            Log.e("$$$","###");
//            googleApiClient = new GoogleApiClient.Builder(this, this, this).addApi(LocationServices.API).build();
//            getLocation();
//        }
//
//
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//
//        switch (requestCode) {
//            case PERMISSION_ACCESS_FINE_LOCATION:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // All good!
//                  getLocation();
//                } else {
//                    Toast.makeText(this, R.string.need_location, Toast.LENGTH_SHORT).show();
//                }
//
//                break;
//        }
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        if (googleApiClient != null) {
//            googleApiClient.connect();
//        }
//    }
//
//    @Override
//    protected void onStop() {
//        googleApiClient.disconnect();
//        super.onStop();
//    }
//
//    @Override
//    public void onConnected(Bundle bundle) {
//        Log.i(MainActivity.class.getSimpleName(), "Connected to Google Play Services!");
//
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//
//            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
//            if (lastLocation != null) {
//                double lat = lastLocation.getLatitude(), lon = lastLocation.getLongitude();
//                getLocation();
//            }else{
//                Toast.makeText(MainActivity.this, "Unable to find your location", Toast.LENGTH_SHORT).show();
//            }
//
//        }
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
//
//    }
//
//    @Override
//    public void onConnectionFailed(ConnectionResult connectionResult) {
//        Log.i(MainActivity.class.getSimpleName(), "Can't connect to Google Play Services!");
//    }
//
//
//    protected void getLocation() {
//         String  bestProvider="";
//        if (isLocationEnabled(MainActivity.this)) {
//            LocationManager locationManager = (LocationManager)  this.getSystemService(Context.LOCATION_SERVICE);
//            Criteria criteria = new Criteria();
//            bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();
//
//            //You can still do this if you like, you might get lucky:
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            Location location = locationManager.getLastKnownLocation(bestProvider);
//           if (location != null) {
//                Log.e("TAG", "GPS is on");
//                double latitude = location.getLatitude();
//                double longitude = location.getLongitude();
//                Toast.makeText(MainActivity.this, "latitude:" + latitude + " longitude:" + longitude, Toast.LENGTH_SHORT).show();
//                locationLink= "http://maps.google.com/?q="+latitude+","+longitude;
//
//               }
//                else{
//                Toast.makeText(MainActivity.this, "Unable to find your location", Toast.LENGTH_SHORT).show();
//                }
//            }
//            else{
//                Toast.makeText(MainActivity.this, "Location Access Permission is denied", Toast.LENGTH_SHORT).show();
//            }
//        }
//        else
//        {
//            //prompt user to enable location....
//            //.................
//        }
//    }
//    public static boolean isLocationEnabled(Context context)
//    {
//        //...............
//        return true;
//    }
    /*
    Location code ends
     */

}
