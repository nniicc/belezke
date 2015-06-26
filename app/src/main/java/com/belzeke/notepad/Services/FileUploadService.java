package com.belzeke.notepad.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by marko on 11.6.2015.
 */
public class FileUploadService extends Service {
    private static String TAG = FileUploadService.class.getSimpleName();
    private List<File> files = new ArrayList<>();
    private LocalBroadcastManager broadcaster ;

    static final public String SERVICE_RESULT = "com.belzeke.notepad.Services.FileUploadService.REQUEST_PROCESSED";

    static final public String SERVICE_MESSAGE = "com.belzeke.notepad.Services.FileUploadService.SERVICE_MSG";


    @Override
    public void onCreate() {
        super.onCreate();
        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    public void sendResult(String message){
        Intent intent = new Intent(SERVICE_RESULT);
        if(message != null){
            intent.putExtra(SERVICE_MESSAGE, message);
        }
        broadcaster.sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sendResult("");
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
