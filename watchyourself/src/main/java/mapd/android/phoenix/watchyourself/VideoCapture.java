package mapd.android.phoenix.watchyourself;

/**
 * Team Phoenix
 */


import android.Manifest;
import android.app.Activity;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;


public class VideoCapture extends Activity implements View.OnClickListener, SurfaceHolder.Callback {
    MediaRecorder recorder;
    SurfaceHolder holder;
    boolean recording = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//setContentView(R.layout.video);
        recorder = new MediaRecorder();
        askPermissions();
    }
        public void askPermissions()
        {
//
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA ,READ_EXTERNAL_STORAGE,WRITE_EXTERNAL_STORAGE,RECORD_AUDIO},
                        1);
            }
            else
            {
                Log.e("$$$","###");
                initRecorder();
            }

        }
    private void initRecorder() {
        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

        CamcorderProfile cpHigh = CamcorderProfile
                .get(CamcorderProfile.QUALITY_HIGH);
        recorder.setProfile(cpHigh);
        //String filePATH = getFilesDir()+"/videocapture_example.mp4";
        //recorder.setOutputFile(filePATH);
        String filePath = getOutputMediaFile(MEDIA_TYPE_VIDEO).getPath();
        Uri fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
        recorder.setOutputFile(filePath);
        recorder.setMaxDuration(50000); // 50 seconds
        recorder.setMaxFileSize(5000000);
//
        setContent();

    }

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

        public void setContent()
        {

            setContentView(R.layout.video);

            SurfaceView cameraView = (SurfaceView) findViewById(R.id.CameraView);
            holder = cameraView.getHolder();
            holder.addCallback(this);
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

            cameraView.setClickable(true);
            cameraView.setOnClickListener(VideoCapture.this);


            recorder.setMaxDuration(5000); // 5 seconds
            recorder.setMaxFileSize(5000000); // Approximately 5 megabytes
          //  recorder.start();
            Toast.makeText(VideoCapture.this,R.string.recording_started,Toast.LENGTH_LONG).show();
        }
    private void prepareRecorder() {
        recorder.setPreviewDisplay(holder.getSurface());

        try {
            recorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            finish();
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }
    }



    public void surfaceCreated(SurfaceHolder holder) {
        prepareRecorder();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (recording) {
            recorder.stop();
            recording = false;
        }
        recorder.release();

        finish();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    boolean StoragePermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean ReadPermission = grantResults[2] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission && ReadPermission) {
                        Toast.makeText(VideoCapture.this, R.string.permission_granted,
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(VideoCapture.this, R.string.permission_denied, Toast.LENGTH_LONG).show();
                    }
                }
                break;

            case 2:
                //for audio
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, R.string.camera_granted, Toast.LENGTH_SHORT).show();
                    // All good!
                    //startRecording();
                } else {
                    Toast.makeText(this, R.string.need_camera_access, Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(VideoCapture.this, new String[]{Manifest.permission.RECORD_AUDIO}, 2);
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {

            Log.e("########","!!!!!!!");
            if (recording) {
                recorder.stop();
                recording = false;
                Toast.makeText(VideoCapture.this, R.string.recording_stopped,Toast.LENGTH_LONG).show();
                ///data/user/0/mapd.android.phoenix.watchyourself/files/videocapture_example.mp4
                //File file = new File(getFilesDir()+"/videocapture_example.mp4");
                String filePath = getOutputMediaFile(MEDIA_TYPE_VIDEO).getPath();
                Uri fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
                recorder.setOutputFile(filePath);
                //int file_size = Integer.parseInt(String.valueOf(file.length()/1024));
                //Log.e("filesize",file_size+" >>> "+file.getPath());
                //File file = new File("fileUri");
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(fileUri, "video/*");
                startActivity(intent);
                // Let's initRecorder so we can record again
//                initRecorder();
//                prepareRecorder();
            } else {
                recording = true;
                recorder.start();
                Toast.makeText(VideoCapture.this,R.string.recording_started,Toast.LENGTH_LONG).show();
            }

    }
}
