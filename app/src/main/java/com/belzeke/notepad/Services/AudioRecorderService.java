package com.belzeke.notepad.Services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.belzeke.notepad.Config.AppConfig;

import java.io.File;
import java.io.IOException;

/**
 * Created by marko on 7.6.2015.
 */
public class AudioRecorderService extends Service {

    public static final String TAG = AudioRecorderService.class.getSimpleName();
    private MediaRecorder mediaRecorder;
    private File outputFile;
    private long startTime = 0;
    private LocalBroadcastManager broadcaster ;

    static final public String SERVICE_RESULT = "com.belzeke.notepad.Services.AudioRecorderService.REQUEST_PROCESSED";

    static final public String SERVICE_MESSAGE = "com.belzeke.notepad.Services.AudioRecorderService.SERVICE_MSG";
    private Handler timerHandler = new Handler();

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            sendResult(String.format("%d:%02d", minutes, seconds));

            timerHandler.postDelayed(this, 500);

        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        broadcaster  =  LocalBroadcastManager.getInstance(this);
        startTime = System.currentTimeMillis();
    }

    public void sendResult(String message){
        Intent intent = new Intent(SERVICE_RESULT);
        if(message != null){
            intent.putExtra(SERVICE_MESSAGE, message);
        }
        broadcaster.sendBroadcast(intent);
    }

    private boolean prepareRecorder(){

        outputFile = AppConfig.getOutputMediaFile(this, AppConfig.NOTE_AUDIO_TYPE);
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        if (outputFile != null) {
            mediaRecorder.setOutputFile(outputFile.getAbsolutePath());
        }
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void releaseRecorder(){
        if(mediaRecorder != null){
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    @Override
    public synchronized int onStartCommand(Intent intent, int flags, int startId) {
        if(prepareRecorder()) {
            startTime = System.currentTimeMillis();
            timerHandler.postDelayed(timerRunnable, 0);
            try {
                mediaRecorder.start();
            }catch (IllegalStateException e){
                e.printStackTrace();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public synchronized void onDestroy() {
        super.onDestroy();

        timerHandler.removeCallbacks(timerRunnable);
        releaseRecorder();
        sendResult("");
        AppConfig.LastFilePathCreated = outputFile.getAbsolutePath();
        stopSelf();
    }
}
