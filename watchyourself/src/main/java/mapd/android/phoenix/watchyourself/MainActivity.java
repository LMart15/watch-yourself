package mapd.android.phoenix.watchyourself;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private List<WImages> wyimages;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                    Toast.makeText(getApplicationContext(), "Audio Recoding Initiated.", Toast.LENGTH_LONG).show();
                }
                if(imageName.contains("Msg Emergency Contacts")) {
                    String phoneNo = "6475881235";
                    String message = "Emergency! Please locate and help me! ";

                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNo, null, message, null, null);
                    Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();
                }
                if(imageName.contains("Call Emergency Contacts")) {
                    Intent in=new Intent(Intent.ACTION_CALL,Uri.parse("6477105918"));
                    startActivity(in);
                    Toast.makeText(getApplicationContext(), "Calling Emergency Contact.", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    /*@Override
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
    }*/

}
