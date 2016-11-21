package mapd.android.phoenix.watchyourself.library;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

/**
 * Created by Farruxx on 30.04.2016.
 */
public class PermissionRequest {
    Context context;
    String[] permissions;
    int requestCode;
    String notificationTitle;
    String notificationText;
    int icon;

    PermissionResponse response;

    public PermissionRequest(Context context, String[] permissions, int requestCode, String notificationTitle, String notificationText, int icon) {
        this.context = context;
        this.permissions = permissions;
        this.requestCode= requestCode;
        this.notificationTitle = notificationTitle;
        this.notificationText = notificationText;
        this.icon = icon;

    }

    public PermissionResponse call() throws InterruptedException{
        Log.e("Util.hasPermission","*");
        if(!Util.hasPermission(context, permissions)) {
            Log.e("Util.hasPermission","*");
                final Object lock = new Object();
                NotificationHelper.sendNotification(context, permissions, requestCode,
                        notificationTitle, notificationText, icon, new ResultReceiver(new Handler(Looper.getMainLooper())) {
                            @Override
                            protected void onReceiveResult(int resultCode, Bundle resultData) {
                                super.onReceiveResult(resultCode, resultData);
                                int[] grantResult = resultData.getIntArray(Const.GRANT_RESULT);
                                String[] permissions = resultData.getStringArray(Const.PERMISSIONS_ARRAY);
                                response = new PermissionResponse(permissions, grantResult, resultCode);

                                synchronized (lock) {
                                    lock.notifyAll();
                                }
                            }
                        });
                synchronized (lock) {
                    lock.wait();
                }
        }else {
            Log.e("Util.hasPermission","else");
            response = new PermissionResponse(permissions,
                    new int[]{PackageManager.PERMISSION_GRANTED}, requestCode);
        }
        return response;
    }

    public void enqueue(final PermissionResultCallback callback){
        if(!Util.hasPermission(context, permissions)) {
            NotificationHelper.sendNotification(context, permissions, requestCode,
                    notificationTitle, notificationText, icon, new ResultReceiver(new Handler()) {
                        @Override
                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                            super.onReceiveResult(resultCode, resultData);
                            int[] grantResult = resultData.getIntArray(Const.GRANT_RESULT);
                            String[] permissions = resultData.getStringArray(Const.PERMISSIONS_ARRAY);
                            response = new PermissionResponse(permissions, grantResult, resultCode);
                            callback.onComplete(new PermissionResponse(permissions, grantResult, resultCode));
                        }
                    });
        }else {
            callback.onComplete(new PermissionResponse(permissions,
                    new int[]{PackageManager.PERMISSION_GRANTED}, requestCode));
        }
    }
}
