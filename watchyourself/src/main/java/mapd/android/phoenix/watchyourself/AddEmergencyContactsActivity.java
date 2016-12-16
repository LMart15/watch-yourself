package mapd.android.phoenix.watchyourself;

/**
 * Team Phoenix
 */


import android.Manifest;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;


public class AddEmergencyContactsActivity extends AppCompatActivity {

    ArrayList<String> contactList;
    Cursor cursor;
    int counter;
    ListView mListView;
    final public static int SEND_SMS = 101;
    String phoneno,msg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_contacts);
        mListView = (ListView) findViewById(R.id.list);

        checkReadContactsPermission();

        // Set onclicklistener to the list item.
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //TODO Do whatever you want with the list data
                //Toast.makeText(getApplicationContext(), "item clicked : \n"+contactList.get(position), Toast.LENGTH_SHORT).show();
                String[] val = contactList.get(position).split(":");
                Log.e("####",val.length+"");
                if(val.length>0)
                    addContactSharedPreferences("contact1",contactList.get(position));

            }
        });
    }

    /* Send SMS Method Ends*/
    public void getContacts() {
        contactList = new ArrayList<String>();
        String phoneNumber = null;
        String email = null;
        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;
        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

        StringBuffer output;
        ContentResolver contentResolver = getContentResolver();
        cursor = contentResolver.query(CONTENT_URI, null,null, null, null);

        Log.e("numbers count",""+cursor.getCount());
        // Iterate every contact in the phone
        if (cursor.getCount() > 0) {
            counter = 0;
            while (cursor.moveToNext()) {
                output = new StringBuffer();

                String contact_id = cursor.getString(cursor.getColumnIndex( _ID ));
                String name = cursor.getString(cursor.getColumnIndex( DISPLAY_NAME ));
                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex( HAS_PHONE_NUMBER )));
                if (hasPhoneNumber > 0) {
                    output.append(name);
                    //This is to read multiple phone numbers associated with the same contact
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[] { contact_id }, null);
                    while (phoneCursor.moveToNext()) {
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                        output.append(":" + phoneNumber);
                    }
                    phoneCursor.close();

                }
                // Add the contact to the ArrayList
                contactList.add(output.toString());
            }
            // ListView has to be updated using a ui thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item, R.id.text1, contactList);
                    mListView.setAdapter(adapter);
                }
            });

        }
    }
    private void checkReadContactsPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_CONTACTS}, 30);
        }
        else
        {
            // Since reading contacts takes more time, let's run it on a separate thread.
            new Thread(new Runnable() {
                @Override
                public void run() {
                    getContacts();
                }
            }).start();
        }
    }
    public void addContactSharedPreferences(String key, String value)
    {
        SharedPreferences sharedPreferences = getSharedPreferences("Emer_contact",1);
        SharedPreferences.Editor edit= sharedPreferences.edit();
        edit.putString(key, value);
        edit.commit();
        Toast.makeText(this,getResources().getString(R.string.add_emergencyContact),Toast.LENGTH_SHORT).show();
        finish();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {

                //Call
                case 30:

                    // Since reading contacts takes more time, let's run it on a separate thread.
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            getContacts();
                        }
                    }).start();
//

                    break;

//                case SEND_SMS:
//                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                        sendSMS(phoneno, msg);
//                    } else {
//
//                        Toast.makeText(AddEmergencyContactsActivity.this, "SEND_SMS Denied", Toast.LENGTH_SHORT)
//                                .show();
//                    }
//                    break;

            }

            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();

        }
        else
        {
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();

        }
    }

}
