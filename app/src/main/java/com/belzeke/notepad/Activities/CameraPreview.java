package com.belzeke.notepad.Activities;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.belzeke.notepad.Config.AppConfig;
import com.belzeke.notepad.Helper.CameraHelper;
import com.belzeke.notepad.Helper.Preview;
import com.belzeke.notepad.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraPreview extends Activity {

    private String TAG = CameraPreview.class.getSimpleName();
    private Preview mPreview;
    Camera mCamera;
    int numberOfCameras;
    int cameraCurrentlyLocked;
    int defaultCameraId;
    private boolean recording;

    private MediaPlayer mediaPlayer;
    private MediaRecorder mediaRecorder;
    private Button mainButton;
    private Button backCameraButton;
    private Button sendVideo;
    private Button switchCamera;
    private File outputFile;
    private long startTime = 0;
    private TextView timer;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mPreview = new Preview(this);
        setContentView(mPreview);

        View child = getLayoutInflater().inflate(R.layout.activity_video, null);

        addContentView(child, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        init();
        numberOfCameras = Camera.getNumberOfCameras();

        defaultCameraId = CameraHelper.findBackFacingCamera();

    }

    private void init() {
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
            cameraCurrentlyLocked = defaultCameraId;
            mPreview.setCamera(mCamera);
        }
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
            chooseCamera();
        }
    };
    View.OnClickListener captureListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {

        }
    };

    private void releaseMediaRecorder() {
        if(mediaRecorder != null){
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    private void chooseCamera() {
        if(CameraHelper.cameraFront){
            int cameraId = CameraHelper.findBackFacingCamera();
            if(cameraId >= 0){
                defaultCameraId = cameraId;
                mCamera = Camera.open(cameraId);
                mPreview.switchCamera(mCamera);
            }
        }else{
            int cameraId = CameraHelper.findFrontFacingCamera();
            if(cameraId >= 0){
                defaultCameraId = cameraId;
                mCamera = Camera.open(cameraId);
                mPreview.switchCamera(mCamera);
            }
        }
        try {
            mCamera.startPreview();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void releaseCamera() {
        if(mCamera != null){
            mPreview.setCamera(null);
            mCamera.release();
            mCamera = null;
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_camera_prview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.switchCam:
                //Check for availability of multiple cameras
                if (numberOfCameras == 1) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("TEST").setNeutralButton("Close", null);
                    AlertDialog alert = builder.create();
                    alert.show();
                    return true;
                }

                //OK, we have multiple cameras.
                //Release this camera -> cameraCurrentlyLocked
                if (mCamera != null) {
                    mCamera.stopPreview();
                    mPreview.setCamera(null);
                    mCamera.release();
                    mCamera = null;
                }

                //Acquire the next camera and request Preview to reconfigure parameters.
                mCamera = Camera.open((cameraCurrentlyLocked + 1) % numberOfCameras);
                cameraCurrentlyLocked = (cameraCurrentlyLocked + 1) % numberOfCameras;
                mPreview.switchCamera(mCamera);

                //Start the preview
                mCamera.startPreview();
                return true;

            case R.id.takePicture:
                mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {

        }
    };
    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
        }
    };
    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            FileOutputStream outStream = null;
            try {
                // Write to SD Card
                outStream = new FileOutputStream(String.format("/sdcard/%d.jpg",
                        System.currentTimeMillis()));
                outStream.write(data);
                outStream.close();
            }
            catch (FileNotFoundException e) {
                Log.e(TAG, "IOException caused by PictureCallback()", e);
            }
            catch (IOException e) {
                Log.e(TAG, "IOException caused by PictureCallback()", e);
            }
        }
    };
}
