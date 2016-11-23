package mapd.android.phoenix.watchyourself;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import static android.provider.UserDictionary.Words.APP_ID;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    private List<WImages> wyimages;
    private static final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    private GoogleApiClient googleApiClient;
    private static final String APP_ID = "AIzaSyDn9osFVDgjKdsqnlP8btgkn13s4eqiui0";
    String locationLink;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(MainActivity.this, ProviderService.class);
        startService(intent);

        askForLocationPermission();

        wyimages = ImagesList.getCatalog(getResources());

        // Create the list
        GridView listViewCatalog = (GridView) findViewById(R.id.ListViewCatalog);
        listViewCatalog.setAdapter(new ImageAdapter(wyimages, getLayoutInflater()));

        listViewCatalog.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                WImages selectedImage;
                String imageName;
                List<WImages> catalog = ImagesList.getCatalog(getResources());
                int productIndex = position;

                selectedImage = catalog.get(productIndex);
                imageName = selectedImage.title;

                if(imageName.contains("Record Video")) {

                    Toast.makeText(getApplicationContext(), "Video Recoding Initiated.", Toast.LENGTH_LONG).show();
                }
                if(imageName.contains("Record Voice")) {
                   Intent intent = new Intent(MainActivity.this,RecordingList.class);
                    startActivity(intent);
                }
                if(imageName.contains("Msg Emergency Contacts")) {
                    sendMessage();
                }
                if(imageName.contains("Call Emergency Contacts")) {
                    /*Intent in=new Intent(Intent.ACTION_CALL,Uri.parse("6477105918"));
                    startActivity(in);*/
                    Toast.makeText(getApplicationContext(), "Calling Emergency Contact.", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater= getMenuInflater();
        inflater.inflate(R.menu.menu_watchyourself,menu);
        return true;
    }

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
            case R.id.configure_message:
                Intent intentM = new Intent(MainActivity.this, CreateEmergencyMessageActivity.class);
                startActivity(intentM);

            default:
                return super.onOptionsItemSelected(item);

        }
    }
        public void sendMessage()
        {
            try {

                String message = "Emergency! Please locate and help me! "+locationLink;

                SmsManager smsManager = SmsManager.getDefault();

                SharedPreferences sharedPreferences = getSharedPreferences("Emer_contact",1);
                String value = sharedPreferences.getString("contact1","null");
                if(value.equals("null"))
                {
                    Toast.makeText(getApplicationContext(),"Please add emergency contact details first.",Toast.LENGTH_LONG).show();
                }
                else {
                    String[] arr=   value.split(":");
                    String phoneno= arr[1];
                    Log.e("phone no :: ",phoneno);
                    smsManager.sendTextMessage(phoneno, null, message, null, null);


                    Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();
                }
            }
            catch (SecurityException e)
            {

            }
        }

    /*
    LOcation data
     */

    public void askForLocationPermission()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION },
                    PERMISSION_ACCESS_FINE_LOCATION);
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
                    Toast.makeText(this, "Need your location!", Toast.LENGTH_SHORT).show();
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            double lat = lastLocation.getLatitude(), lon = lastLocation.getLongitude();

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(MainActivity.class.getSimpleName(), "Can't connect to Google Play Services!");
    }

public String mapLocation() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
        Log.e("inside map location","###");
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        double lat = lastLocation.getLatitude(), lon = lastLocation.getLongitude();
        locationLink= "http://maps.google.com/?q="+lat+","+lon;
    }
    else
    {
        ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                PERMISSION_ACCESS_FINE_LOCATION);
    }
    return "";
}
    protected void getLocation() {
         String  bestProvider="";
        if (isLocationEnabled(MainActivity.this)) {
            LocationManager locationManager = (LocationManager)  this.getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();

            //You can still do this if you like, you might get lucky:
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(bestProvider);
            if (location != null) {
                Log.e("TAG", "GPS is on");
               double latitude = location.getLatitude();
               double longitude = location.getLongitude();
                Toast.makeText(MainActivity.this, "latitude:" + latitude + " longitude:" + longitude, Toast.LENGTH_SHORT).show();
                 locationLink= "http://maps.google.com/?q="+latitude+","+longitude;

            }
            }
            else{
                //This is what you need:
               // locationManager.requestLocationUpdates(bestProvider, 1000, 0, this);
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
}
