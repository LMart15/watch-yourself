package mapd.android.phoenix.watchyourself;

/**
 * Team Phoenix
 */


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    /* Location Variables */
    private static final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    private GoogleApiClient googleApiClient;
    private static final String APP_ID = "AIzaSyDn9osFVDgjKdsqnlP8btgkn13s4eqiui0";
    String locationLink;

    /*     Video Recording Variables    */
    MediaRecorder recorder;
    SurfaceHolder holder;
    boolean recording = false;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(MainActivity.this, ProviderService.class);
        startService(intent);

        askForLocationPermission();  // Called Location Permission

        /*Home Screen Buttons Declarations */

        ImageButton msg_button = (ImageButton) findViewById(R.id.icon_msg);
        ImageButton mic_button = (ImageButton) findViewById(R.id.icon_mic);
        ImageButton camera_button = (ImageButton) findViewById(R.id.icon_camera);
        ImageButton call_button = (ImageButton) findViewById(R.id.icon_call);

        /* Send SMS Call */
        msg_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendMessage();
            }
        });

        /* Record Audio Call */
        mic_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,RecordingList.class);
                startActivity(intent);
            }
        });

        /* Record Video Call */
        camera_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendMessage();
            }
        });

        /* Emergency Calling Call */
        call_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Calling Emergency Contact.", Toast.LENGTH_LONG).show();
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

            case R.id.action_help:
                Intent help = new Intent();
                help.setAction(Intent.ACTION_VIEW);
                help.addCategory(Intent.CATEGORY_BROWSABLE);
                help.setData(Uri.parse("http://www.wiredsafety.com/"));
                startActivity(help);

                return true;

            case R.id.action_watchyourself:
                Intent watchyourself = new Intent();
                watchyourself.setAction(Intent.ACTION_VIEW);
                watchyourself.addCategory(Intent.CATEGORY_BROWSABLE);
                watchyourself.setData(Uri.parse("https://m.watchyourself.ca"));
                startActivity(watchyourself);
                return true;
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

    public void askForLocationPermission()
    {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION }, PERMISSION_ACCESS_FINE_LOCATION);
            googleApiClient = new GoogleApiClient.Builder(this, this, this).addApi(LocationServices.API).build();
        }
        else
        {
            Log.e("$$$","###");
            googleApiClient = new GoogleApiClient.Builder(this, this, this).addApi(LocationServices.API).build();
            getLocation();
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // All good!
                  getLocation();
                } else {
                    Toast.makeText(this, R.string.need_location, Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(MainActivity.class.getSimpleName(), "Connected to Google Play Services!");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

//            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
//            double lat = lastLocation.getLatitude(), lon = lastLocation.getLongitude();

            getLocation();

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(MainActivity.class.getSimpleName(), "Can't connect to Google Play Services!");
    }

//public String mapLocation() {
//    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
//            == PackageManager.PERMISSION_GRANTED) {
//        Log.e("inside map location","###");
//        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
//
//        double lat = lastLocation.getLatitude(), lon = lastLocation.getLongitude();
//        locationLink= "http://maps.google.com/?q="+lat+","+lon;
//    }
//    else
//    {
//        ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
//                PERMISSION_ACCESS_FINE_LOCATION);
//    }
//    return "";
//}
    protected void getLocation() {
         String  bestProvider="";
        if (isLocationEnabled(MainActivity.this)) {
            LocationManager locationManager = (LocationManager)  this.getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();

            //You can still do this if you like, you might get lucky:
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(bestProvider);
           if (location != null) {
                Log.e("TAG", "GPS is on");
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                Toast.makeText(MainActivity.this, "latitude:" + latitude + " longitude:" + longitude, Toast.LENGTH_SHORT).show();
                locationLink= "http://maps.google.com/?q="+latitude+","+longitude;

               }
                else{
                Toast.makeText(MainActivity.this, "Unable to find your location", Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Toast.makeText(MainActivity.this, "Location Access Permission is denied", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            //prompt user to enable location....
            //.................
        }
    }
    public static boolean isLocationEnabled(Context context)
    {
        //...............
        return true;
    }
    /*
    Location code ends
     */
    /*
    Video Recording
     */
//    private void initRecorder() {
//        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
//        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
//
//        CamcorderProfile cpHigh = CamcorderProfile
//                .get(CamcorderProfile.QUALITY_HIGH);
//        recorder.setProfile(cpHigh);
//        recorder.setOutputFile("/sdcard/videocapture_example.mp4");
//        recorder.setMaxDuration(50000); // 50 seconds
//        recorder.setMaxFileSize(5000000); // Approximately 5 megabytes
//    }
//
//    private void prepareRecorder() {
//        recorder.setPreviewDisplay(holder.getSurface());
//
//        try {
//            recorder.prepare();
//        } catch (IllegalStateException e) {
//            e.printStackTrace();
//            finish();
//        } catch (IOException e) {
//            e.printStackTrace();
//            finish();
//        }
//    }
//
//    public void onClick(View v) {
//        if (recording) {
//            recorder.stop();
//            recording = false;
//
//            // Let's initRecorder so we can record again
//            initRecorder();
//            prepareRecorder();
//        } else {
//            recording = true;
//            recorder.start();
//        }
//    }
//
//    public void surfaceCreated(SurfaceHolder holder) {
//        prepareRecorder();
//    }
//
//    public void surfaceChanged(SurfaceHolder holder, int format, int width,
//                               int height) {
//    }
//
//    public void surfaceDestroyed(SurfaceHolder holder) {
//        if (recording) {
//            recorder.stop();
//            recording = false;
//        }
//        recorder.release();
//        finish();
//    }
}
