package mapd.android.phoenix.watchyourself;

/**
 * Created by lawrence.martin on 2016-11-20.
 */

import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;


        import android.content.Intent;
        import android.media.MediaPlayer;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ArrayAdapter;
        import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
        import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
        import java.util.List;
import java.util.Random;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static mapd.android.phoenix.watchyourself.ProviderService.RequestPermissionCode;

public class RecordingList extends AppCompatActivity {
    List<HashMap<String,String>> files = new ArrayList<>();
    ListView listView;
    Button start,stop;
    String AudioSavePathInDevice = null;
    Random random= new Random();
    String RandomAudioFileName = "ABCDEFGHIJKLMNOP";
    MediaRecorder mRecorder;
    MediaPlayer mp = new MediaPlayer();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_list);
        listView = (ListView)findViewById(R.id.list);
        start= (Button)findViewById(R.id.btn_start);
        stop= (Button)findViewById(R.id.btn_stop);
        stop.setEnabled(false);
        getFiles();
        if(files.size()>0)
        {
            listView.setAdapter(new ListAda());
        }

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            startRecording();
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mRecorder.stop();
                stop.setEnabled(false);

                start.setEnabled(true);


                Toast.makeText(RecordingList.this, "Recording Completed",
                        Toast.LENGTH_LONG).show();
                getFiles();
                listView.setAdapter(new ListAda());
            }
        });
    }

    public void getFiles()
    {
        files.clear();
        File[] file = getFilesDir().listFiles();


        for (File f : file)
        {
            Log.e("file name without if ",f.getPath());
            if (f.isFile() && f.getPath().toString().endsWith(".3gp")) {
                HashMap<String,String> map = new HashMap<>();
                map.put("filePath",f.getPath().toString());
                map.put("fileName",f.getName().toString());
                files.add(map);
            }
        }
    }

    class ListAda extends BaseAdapter{

        @Override
        public int getCount() {
            return files.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater= getLayoutInflater();
            View rowview = inflater.inflate(R.layout.list_detail,null);

            TextView tv = (TextView)rowview.findViewById(R.id.text);
            tv.setText(files.get(position).get("fileName"));
            final String path = files.get(position).get("filePath");
            final String name =files.get(position).get("fileName");
            rowview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            ToggleButton btn = (ToggleButton)rowview.findViewById(R.id.toggle);
               btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                   @Override
                   public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                       if(isChecked)
                       {
                            if(mp == null)
                            {
                                mp = new MediaPlayer();
                            }
                           else
                            {
                                if(mp.isPlaying())
                                {
                                    Toast.makeText(RecordingList.this,"An audio file is already playing. Please stop that",Toast.LENGTH_LONG).show();
                                }
                                else
                                {
                                    try {
                                        mp.setDataSource(path);
                                        mp.prepare();
                                        mp.start();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }


                       }
                       else {
                           Log.e("media player playing",mp.isPlaying()+"");
                           if(mp.isPlaying()) {
                               mp.stop();
                               mp = null;
                           }
                       }
                   }
               });

            return rowview;
        }
    }

    public void startRecording (){


        if (checkPermission()) {

            AudioSavePathInDevice =
                    getFilesDir().getAbsolutePath() + "/" +
                            getCurrentTimeStamp() + "AudioRecording.3gp";

            MediaRecorderReady();

            try {
                mRecorder.prepare();
                mRecorder.start();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            start.setEnabled(false);
            stop.setEnabled(true);

            Toast.makeText(RecordingList.this, "Recording started",
                    Toast.LENGTH_LONG).show();
        } else {
            requestPermission();
        }

    }

    public void MediaRecorderReady() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mRecorder.setOutputFile(AudioSavePathInDevice);
    }

    public String CreateRandomAudioFileName(int string) {
        StringBuilder stringBuilder = new StringBuilder(string);
        int i = 0;
        while (i < string) {
            stringBuilder.append(RandomAudioFileName.
                    charAt(random.nextInt(RandomAudioFileName.length())));

            i++;
        }
        return stringBuilder.toString();
    }

    public String getCurrentTimeStamp() {
        return new SimpleDateFormat("yyyyMMdd_HH:mm_").format(new Date());
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(RecordingList.this, new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length > 0) {
                    boolean StoragePermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {
                        Toast.makeText(RecordingList.this, "Permission Granted",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(RecordingList.this, "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }
}

