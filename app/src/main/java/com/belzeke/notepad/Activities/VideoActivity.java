package com.belzeke.notepad.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.belzeke.notepad.Config.AppConfig;
import com.belzeke.notepad.Helper.CameraHelper;
import com.belzeke.notepad.Helper.CameraPreview;
import com.belzeke.notepad.R;

import java.io.File;
import java.io.IOException;
import java.util.List;

@SuppressWarnings("deprecation")
public class VideoActivity extends Activity{

    private Camera mCamera;
    private CameraPreview mPreview;
    private Button mainButton, switchCamera, backCameraButton, sendVideo;
    private LinearLayout cameraPreview;
    private MediaPlayer mediaPlayer;
    private File outputFile;
    private boolean recording = false;
    private TextView timer;
    private long startTime = 0;



    private Handler timerHandler = new Handler();
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            timer.setText(String.format("%d:%02d", minutes, seconds));

            timerHandler.postDelayed(this, 500);
        }
    };
    private String TAG = VideoActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_video);

        init();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(!CameraHelper.hasCamera(getApplicationContext())){
            Toast.makeText(this, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG).show();
            finish();
        }
        if(mCamera == null){
            if(CameraHelper.findFrontFacingCamera() < 0){
                Toast.makeText(this, "No front facing camera found.", Toast.LENGTH_LONG).show();
                switchCamera.setVisibility(View.GONE);
            }
            mCamera = Camera.open(CameraHelper.findBackFacingCamera());
            mPreview = new CameraPreview(this, mCamera);
            mPreview.refreshCamera(mCamera);
            cameraPreview.addView(mPreview);
        }
    }

    private void init() {
        cameraPreview = (LinearLayout) findViewById(R.id.videoPreview);

        timer = (TextView) findViewById(R.id.recordTimer);

        mediaPlayer = new MediaPlayer();

        mainButton = (Button) findViewById(R.id.mainButton);
        mainButton.setOnClickListener(captureListener);

        backCameraButton = (Button) findViewById(R.id.backVid);
        backCameraButton.setOnClickListener(backButtonListener);

        sendVideo = (Button) findViewById(R.id.sendVid);
        sendVideo.setOnClickListener(sendVideoListener);

        switchCamera = (Button) findViewById(R.id.changeCamera);
        switchCamera.setOnClickListener(switchCameraListener);
    }

    View.OnClickListener sendVideoListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.putExtra("File", AppConfig.LastFilePathCreated);
            setResult(RESULT_OK, intent);
            finish();
        }
    };

    View.OnClickListener backButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            File vid = new File(AppConfig.LastFilePathCreated);
            if(vid.exists()){
                if(!vid.delete()){
                    Log.d(TAG, "Couldn't delete file");
                }
            }
            releaseCamera();
            releaseMediaRecorder();
            cameraPreview.removeView(mPreview);
            chooseCamera();
            mPreview = new CameraPreview(VideoActivity.this, mCamera);
            mPreview.refreshCamera(mCamera);
            cameraPreview.addView(mPreview);
        }
    };

    View.OnClickListener switchCameraListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(!recording){
                int cameraNumbers = Camera.getNumberOfCameras();
                if(cameraNumbers > 1){
                    releaseCamera();
                    chooseCamera();
                }else{
                    Toast.makeText(getApplicationContext(), "Sorry, your phone has only one camera!", Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    public void chooseCamera(){
        if(CameraHelper.cameraFront){
            int cameraId = CameraHelper.findBackFacingCamera();
            if(cameraId >= 0){
                mCamera = Camera.open(cameraId);
                mPreview.refreshCamera(mCamera);
            }
        }else{
            int cameraId = CameraHelper.findFrontFacingCamera();
            if(cameraId >= 0){
                mCamera = Camera.open(cameraId);
                mPreview.refreshCamera(mCamera);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
        releaseMediaRecorder();
        cameraPreview.removeView(mPreview);
        timerHandler.removeCallbacks(timerRunnable);
        mediaPlayer.release();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        releaseCamera();
        releaseMediaRecorder();
        timerHandler.removeCallbacks(timerRunnable);
        mediaPlayer.release();
    }

    View.OnClickListener captureListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(recording){
                timerHandler.removeCallbacks(timerRunnable);
                mPreview.mediaRecorder.stop();
                releaseMediaRecorder();
                recording = false;
                CameraHelper.cameraFront = !CameraHelper.cameraFront;
                releaseCamera();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mediaPlayer.setDisplay(mPreview.getPrivateHolder());
                            mediaPlayer.setDataSource(AppConfig.LastFilePathCreated);
                            mediaPlayer.setLooping(true);
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                            backCameraButton.setVisibility(View.VISIBLE);
                            sendVideo.setVisibility(View.VISIBLE);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }else{
                outputFile = AppConfig.getOutputMediaFile(getApplicationContext(), AppConfig.MEDIA_TYPE_VIDEO);
                if(!prepareMediaRecorder()){
                    Toast.makeText(getApplicationContext(), "Fail in prepareMediaRecorder()!\n - Ended -", Toast.LENGTH_LONG).show();
                    finish();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            AppConfig.LastFilePathCreated = outputFile.getPath();
                            mPreview.mediaRecorder.start();
                            startTime = System.currentTimeMillis();
                            timerHandler.postDelayed(timerRunnable, 0);
                            recording = true;
                        }catch (final Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    };

    private void releaseMediaRecorder() {
        mPreview.releaseMediaRecorder();
    }

    private boolean prepareMediaRecorder() {
        try {
            if(mCamera == null)
                chooseCamera();

            Camera.Parameters parameters = mCamera.getParameters();
            List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
            Camera.Size optimalSize = mSupportedPreviewSizes.get(0);

            CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
            profile.videoFrameWidth = optimalSize.width;
            profile.videoFrameHeight = optimalSize.height;
            profile.videoBitRate = 1500000;
            profile.videoCodec = MediaRecorder.VideoEncoder.H264;
            profile.videoFrameRate = 30;
            profile.audioBitRate = 780000;

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                parameters.setAutoExposureLock(false);
                parameters.setAutoWhiteBalanceLock(false);
            }
            if(!CameraHelper.cameraFront) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            parameters.set("iso", "ISO800");
            parameters.setColorEffect("none");
            parameters.setPreviewFrameRate(30);
            parameters.setExposureCompensation(4);
            parameters.setSceneMode("scene-mode=dusk-dawn");


            parameters.setPreviewSize(profile.videoFrameWidth, profile.videoFrameHeight);
            mCamera.setParameters(parameters);
            Display display = ((WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

            mPreview.mediaRecorder = new MediaRecorder();

            mCamera.unlock();
            mPreview.mediaRecorder.setCamera(mCamera);

            mPreview.mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            mPreview.mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

            mPreview.mediaRecorder.setProfile(profile);


            if(display.getRotation() == Surface.ROTATION_0){
                if(!CameraHelper.cameraFront)
                    mPreview.mediaRecorder.setOrientationHint(90);
                else
                    mPreview.mediaRecorder.setOrientationHint(270);
            }
            if(display.getRotation() == Surface.ROTATION_270){
                if(!CameraHelper.cameraFront)
                    mPreview.mediaRecorder.setOrientationHint(180);
                else
                    mPreview.mediaRecorder.setOrientationHint(0);
            }
            mPreview.mediaRecorder.setOutputFile(outputFile.getAbsolutePath());

            try {
                mPreview.mediaRecorder.prepare();
            } catch (IllegalStateException e) {
                Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
                releaseMediaRecorder();
                return false;
            } catch (IOException e) {
                Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
                releaseMediaRecorder();
                return false;
            }
        }catch (Exception e){
            Log.d(TAG, "Unknown Exception: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void releaseCamera() {
        if(mCamera != null){
            mCamera.release();
            mCamera = null;
        }
    }


}
