package mapd.android.phoenix.watchyourself;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddEmergencyContactsActivity extends AppCompatActivity {

    EditText edName,edPhone;
    Button addContactBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_emergency_contacts);

        edName= (EditText)findViewById(R.id.editTextName);
        edPhone= (EditText)findViewById(R.id.editTextPhone);
        addContactBtn =(Button)findViewById(R.id.button_add);

        addContactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edName.getText().toString().trim();
                String phone = edPhone.getText().toString().trim();
                if(name.length()>0 && phone.length()>0)
                {
                    addContactSharedPreferences("contact1",name+":"+phone);
                }

            }
        });
        setLastSharedPrefData();
    }
    public void addContactSharedPreferences(String key, String value)
    {
        SharedPreferences sharedPreferences = getSharedPreferences("Emer_contact",1);
        SharedPreferences.Editor edit= sharedPreferences.edit();
        edit.putString(key, value);
        edit.commit();
        Toast.makeText(AddEmergencyContactsActivity.this,"Contact Added Successfully",Toast.LENGTH_LONG).show();
        finish();
    }

    public void setLastSharedPrefData()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("Emer_contact",1);
        String value = sharedPreferences.getString("contact1","null");
        if(value.equals("null"))
        {
            Toast.makeText(getApplicationContext(),"Please add emergency contact details first.",Toast.LENGTH_LONG).show();
        }
        else {
            String[] arr = value.split(":");
            edPhone.setText(arr[1]);
            edName.setText(arr[0]);
        }
    }
}
