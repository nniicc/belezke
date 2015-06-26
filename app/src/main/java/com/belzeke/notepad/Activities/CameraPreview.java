package com.belzeke.notepad.Activities;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.belzeke.notepad.Helper.Preview;
import com.belzeke.notepad.R;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mPreview = new Preview(this);
        setContentView(mPreview);

        numberOfCameras = Camera.getNumberOfCameras();

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if(cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK)
                defaultCameraId = i;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCamera = Camera.open(defaultCameraId);
        cameraCurrentlyLocked = defaultCameraId;
        mPreview.setCamera(mCamera);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mCamera != null){
            mPreview.setCamera(null);
            mCamera.release();
            mCamera = null;
        }
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
