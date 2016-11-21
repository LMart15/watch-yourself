package mapd.android.phoenix.watchyourself;

/**
 * Created by lawrence.martin on 2016-11-03.
 */



import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.widget.Toast;
import android.util.Log;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.accessory.*;

import mapd.android.phoenix.watchyourself.library.PermissionEverywhere;
import mapd.android.phoenix.watchyourself.library.PermissionResponse;
import mapd.android.phoenix.watchyourself.library.PermissionResultCallback;

import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.SEND_SMS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.app.Activity.RESULT_OK;
import static android.provider.MediaStore.Audio.Media.RECORD_SOUND_ACTION;

public class ProviderService extends SAAgent {
    private static final String TAG = "HelloAccessory(P)";
    private static final Class<ServiceConnection> SASOCKET_CLASS = ServiceConnection.class;
    private final IBinder mBinder = new LocalBinder();
    private ServiceConnection mConnectionHandler = null;
    Handler mHandler = new Handler();
    String decodedDataUsingUTF8;
    public final String emergencyMsgNotification = "EmergencyMsg";
    public final String emergencyCallNotification = "EmergencyCall";
    public final String emergencyVideoNotification = "EmergencyVideo";
    public final String emergencyAudioNotification = "EmergencyAudio";
    boolean asyncTest = true;
    String EmergContact1 = "4162009613";

    private static final String LOG_TAG = "AudioRecordTest";
    private MediaRecorder mRecorder = null;
    String OUTPUT_FILE;

    String storagePath = null;
    String AudioSavePathInDevice = null;
    Random random;
    String RandomAudioFileName = "ABCDEFGHIJKLMNOP";
    public static final int RequestPermissionCode = 1;
    //final File dir = new File(this.getFilesDir() + "/nfs/guille/groce/users/nicholsk/workspace3/SQLTest");





    public ProviderService() {
        super(TAG, SASOCKET_CLASS);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SA mAccessory = new SA();
        try {
            mAccessory.initialize(this);

//            File audioFile = this.getFilesDir();
//            OUTPUT_FILE = audioFile.getPath()+"/"+"myAudioFile.3gp";

          // AudioSavePathInDevice = getFilesDir().getAbsolutePath() + "/" + "AudioRecording.3gp";

            //storagePath = Environment.getDataDirectory().getAbsolutePath();


//            Log.d(TAG, "AudioSavePathInDevice : result = " + AudioSavePathInDevice);
//            Toast.makeText(getBaseContext(), AudioSavePathInDevice, Toast.LENGTH_LONG).show();


        } catch (SsdkUnsupportedException e) {
            // try to handle SsdkUnsupportedException
            if (processUnsupportedException(e) == true) {
                return;
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            /*
             * Your application can not use Samsung Accessory SDK. Your application should work smoothly
             * without using this SDK, or you may want to notify user and close your application gracefully
             * (release resources, stop Service threads, close UI thread, etc.)
             */
            stopSelf();
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    protected void onFindPeerAgentsResponse(SAPeerAgent[] peerAgents, int result) {
        Log.d(TAG, "onFindPeerAgentResponse : result =" + result);
    }

    @Override
    protected void onServiceConnectionRequested(SAPeerAgent peerAgent) {
        if (peerAgent != null) {
            Toast.makeText(getBaseContext(), R.string.ConnectionAcceptedMsg, Toast.LENGTH_SHORT).show();
            acceptServiceConnectionRequest(peerAgent);
        }
    }

    @Override
    protected void onServiceConnectionResponse(SAPeerAgent peerAgent, SASocket socket, int result) {
        if (result == SAAgent.CONNECTION_SUCCESS) {
            if (socket != null) {
                mConnectionHandler = (ServiceConnection) socket;
            }
        } else if (result == SAAgent.CONNECTION_ALREADY_EXIST) {
            Log.e(TAG, "onServiceConnectionResponse, CONNECTION_ALREADY_EXIST");
        }
    }

    @Override
    protected void onAuthenticationResponse(SAPeerAgent peerAgent, SAAuthenticationToken authToken, int error) {
        /*
         * The authenticatePeerAgent(peerAgent) API may not be working properly depending on the firmware
         * version of accessory device. Please refer to another sample application for Security.
         */
    }

    @Override
    protected void onError(SAPeerAgent peerAgent, String errorMessage, int errorCode) {
        super.onError(peerAgent, errorMessage, errorCode);
    }

    private boolean processUnsupportedException(SsdkUnsupportedException e) {
        e.printStackTrace();
        int errType = e.getType();
        if (errType == SsdkUnsupportedException.VENDOR_NOT_SUPPORTED
                || errType == SsdkUnsupportedException.DEVICE_NOT_SUPPORTED) {
            /*
             * Your application can not use Samsung Accessory SDK. You application should work smoothly
             * without using this SDK, or you may want to notify user and close your app gracefully (release
             * resources, stop Service threads, close UI thread, etc.)
             */
            stopSelf();
        } else if (errType == SsdkUnsupportedException.LIBRARY_NOT_INSTALLED) {
            Log.e(TAG, "You need to install Samsung Accessory SDK to use this application.");
        } else if (errType == SsdkUnsupportedException.LIBRARY_UPDATE_IS_REQUIRED) {
            Log.e(TAG, "You need to update Samsung Accessory SDK to use this application.");
        } else if (errType == SsdkUnsupportedException.LIBRARY_UPDATE_IS_RECOMMENDED) {
            Log.e(TAG, "We recommend that you update your Samsung Accessory SDK before using this application.");
            return false;
        }
        return true;
    }

    public class LocalBinder extends Binder {
        public ProviderService getService() {
            return ProviderService.this;
        }
    }

    public class ServiceConnection extends SASocket {
        public ServiceConnection() {
            super(ServiceConnection.class.getName());
        }

        @Override
        public void onError(int channelId, String errorMessage, int errorCode) {
        }

        @Override
        public void onReceive(int channelId, byte[] data) {

            try {
                decodedDataUsingUTF8 = new String(data, "UTF-8");

                switch (decodedDataUsingUTF8) {
                    case emergencyMsgNotification:
                        //call emergencyMsgNotification related function
                       // Toast.makeText(getBaseContext(), "Emergency Message Sent", Toast.LENGTH_SHORT).show();


                        if(asyncTest) {

                            new AsyncTask<Void, Void, Boolean>() {

                                @Override
                                protected Boolean doInBackground(Void... params) {
                                    PermissionResponse response = null;
                                    try {
                                        response = PermissionEverywhere.getPermission(getApplicationContext(), new String[]{SEND_SMS},
                                                12, "Watch Yourself", "This app needs messaging permission", R.mipmap.ic_launcher).call();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    boolean isGranted = response.isGranted();

                                    return isGranted;
                                }

                                @Override
                                protected void onPostExecute(Boolean aBoolean) {
                                    super.onPostExecute(aBoolean);

                                    Toast.makeText(ProviderService.this, "is Granted " + aBoolean, Toast.LENGTH_SHORT).show();
                                    if(aBoolean)
                                    {
                                        try {

                                            String message = "Emergency! Please locate and help me! ";

                                            SmsManager smsManager = SmsManager.getDefault();
                                            smsManager.sendTextMessage(EmergContact1, null, message, null, null);


                                            Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();
                                        }
                                        catch (SecurityException e)
                                        {

                                        }
                                    }
                                }
                            }.execute();


                        }else {


                            PermissionEverywhere.getPermission(getApplicationContext(), new String[]{SEND_SMS},
                                    12, "Watch Yourself", "This app needs a permission", R.mipmap.ic_launcher).enqueue(new PermissionResultCallback() {
                                @Override
                                public void onComplete(PermissionResponse permissionResponse) {
                                    Toast.makeText(ProviderService.this, "is Granted " + permissionResponse.isGranted(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        break;
                    case emergencyCallNotification:
                        //call emergencyCallNotification related function
                        Toast.makeText(getBaseContext(), "Emergency Call Initiated", Toast.LENGTH_SHORT).show();

                        if(asyncTest) {

                            new AsyncTask<Void, Void, Boolean>() {

                                @Override
                                protected Boolean doInBackground(Void... params) {
                                    PermissionResponse response = null;
                                    try {
                                        response = PermissionEverywhere.getPermission(getApplicationContext(), new String[]{CALL_PHONE},
                                                12, "Watch Yourself", "This app needs call permission", R.mipmap.ic_launcher).call();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    boolean isGranted = response.isGranted();

                                    return isGranted;
                                }

                                @Override
                                protected void onPostExecute(Boolean aBoolean) {
                                    super.onPostExecute(aBoolean);

                                    Toast.makeText(ProviderService.this, "is Granted " + aBoolean, Toast.LENGTH_SHORT).show();
                                    if(aBoolean)
                                    {
                                        try {
                                            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + EmergContact1));
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);

                                        }
                                        catch (SecurityException e)
                                        {

                                        }
                                    }
                                }
                            }.execute();


                        }else {


                            PermissionEverywhere.getPermission(getApplicationContext(), new String[]{CALL_PHONE},
                                    12, "Watch Yourself", "This app needs a permission", R.mipmap.ic_launcher).enqueue(new PermissionResultCallback() {
                                @Override
                                public void onComplete(PermissionResponse permissionResponse) {
                                    Toast.makeText(ProviderService.this, "is Granted " + permissionResponse.isGranted(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        break;
                    case emergencyVideoNotification:
                        //call emergencyVideoNotification related function
                        Toast.makeText(getBaseContext(), "Video Recorded", Toast.LENGTH_SHORT).show();

                        stopRecording();
//                        if(asyncTest) {
//
//                            new AsyncTask<Void, Void, Boolean>() {
//
//                                @Override
//                                protected Boolean doInBackground(Void... params) {
//                                    PermissionResponse response = null;
//                                    try {
//                                        response = PermissionEverywhere.getPermission(getApplicationContext(), new String[]{CAMERA},
//                                                12, "Watch Yourself", "This app needs video recording permission", R.mipmap.ic_launcher).call();
//                                    } catch (InterruptedException e) {
//                                        e.printStackTrace();
//                                    }
//
//                                    boolean isGranted = response.isGranted();
//
//                                    return isGranted;
//                                }
//
//                                @Override
//                                protected void onPostExecute(Boolean aBoolean) {
//                                    super.onPostExecute(aBoolean);
//
//                                    Toast.makeText(ProviderService.this, "is Granted " + aBoolean, Toast.LENGTH_SHORT).show();
//                                    if(aBoolean)
//                                    {
//                                        try {
//
//                                            mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
//                                            mRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
//
//                                            CamcorderProfile cpHigh = CamcorderProfile
//                                                    .get(CamcorderProfile.QUALITY_HIGH);
//                                            mRecorder.setProfile(cpHigh);
//                                            mRecorder.setOutputFile(OUTPUT_FILE);
//                                            mRecorder.setMaxDuration(50000); // 50 seconds
//                                            mRecorder.setMaxFileSize(5000000); // Approximately 5 megabytes
//
//                                            try {
//                                                mRecorder.prepare();
//                                            } catch (IOException e) {
//                                                Log.e(LOG_TAG, "prepare() failed");
//                                            }
//
//                                            mRecorder.start();
//
//                                        }
//                                        catch (SecurityException e)
//                                        {
//
//                                        }
//                                    }
//                                }
//                            }.execute();
//
//
//                        }else {
//
//
//                            PermissionEverywhere.getPermission(getApplicationContext(), new String[]{CAMERA},
//                                    12, "Watch Yourself", "This app needs a permission", R.mipmap.ic_launcher).enqueue(new PermissionResultCallback() {
//                                @Override
//                                public void onComplete(PermissionResponse permissionResponse) {
//                                    Toast.makeText(ProviderService.this, "is Granted " + permissionResponse.isGranted(), Toast.LENGTH_SHORT).show();
//                                }
//                            });
//                        }
//

                        break;
                    case emergencyAudioNotification:
                        //call emergencyAudioNotification related function
                        Toast.makeText(getBaseContext(), "Audio Recorded", Toast.LENGTH_SHORT).show();

                        if(asyncTest) {

                            new AsyncTask<Void, Void, Boolean>() {

                                @Override
                                protected Boolean doInBackground(Void... params) {
                                    PermissionResponse response = null;
                                    try {
                                        response = PermissionEverywhere.getPermission(getApplicationContext(), new String[]{RECORD_AUDIO,WRITE_EXTERNAL_STORAGE},
                                                12, "Watch Yourself", "This app needs audio recording permission", R.mipmap.ic_launcher).call();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    boolean isGranted = response.isGranted();

                                    return isGranted;
                                }

                                @Override
                                protected void onPostExecute(Boolean aBoolean) {
                                    super.onPostExecute(aBoolean);

                                    Toast.makeText(ProviderService.this, "is Granted " + aBoolean, Toast.LENGTH_SHORT).show();
                                    if(aBoolean)
                                    {
                                        startRecording();
                                    }
                                }
                            }.execute();


                        }else {


                            PermissionEverywhere.getPermission(getApplicationContext(), new String[]{RECORD_AUDIO},
                                    12, "Watch Yourself", "This app needs a permission", R.mipmap.ic_launcher).enqueue(new PermissionResultCallback() {
                                @Override
                                public void onComplete(PermissionResponse permissionResponse) {
                                    Toast.makeText(ProviderService.this, "is Granted " + permissionResponse.isGranted(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        break;

                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
        public void MediaRecorderReady() {
            mRecorder  = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
            mRecorder.setOutputFile(AudioSavePathInDevice);
        }
        public void startRecording (){


                AudioSavePathInDevice =
                        getFilesDir().getAbsolutePath() + "/" +
                                "AudioRecording" + getCurrentTimeStamp() + ".3gp";

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



                Toast.makeText(getApplicationContext(), "Recording started",
                        Toast.LENGTH_LONG).show();

        }

        public void stopRecording() {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
public void requestAudioPermissions(){

        }
        @Override
        protected void onServiceConnectionLost(int reason) {
            mConnectionHandler = null;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getBaseContext(), R.string.ConnectionTerminateddMsg, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
      public String CreateRandomAudioFileName(int string) {
        random= new Random();
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
        return new SimpleDateFormat("_yyyyMMdd_HH:mm").format(new Date());
    }

}
