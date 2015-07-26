package com.belzeke.notepad.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.belzeke.notepad.Config.AppConfig;
import com.belzeke.notepad.Helper.CameraHelper;
import com.belzeke.notepad.Helper.CameraPreview;
import com.belzeke.notepad.R;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

@SuppressWarnings("deprecation")
public class PictureActivity extends Activity {

    private String TAG = PictureActivity.class.getSimpleName();

    private Camera mCamera;
    private CameraPreview mPreview;
    private Context mContext;

    private int flashMode = CameraPreview.FLASH_MODE_OFF;

    private LinearLayout cameraPreview;
    private boolean previewing;
    private Button backCameraButton;
    private Button sendVideo;
    private Button switchCamera;
    private Button flashButton;
    private Button mainButton;
    private ImageView focusCursor;
    private EditText hashTags;
    private SubsamplingScaleImageView imagePreview;
    private byte[] imageData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mContext = this;
        init();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!CameraHelper.hasCamera(mContext)) {
            Toast.makeText(mContext, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG).show();
            finish();
        }
        if(CameraHelper.isCameraUsedByOtherApp()){
            hideAllPosition();
            hashTags.setText("Can't access camera");
        }else {
            if (mCamera == null) {
                if (!CameraHelper.hasFrontCamera()) {
                    Toast.makeText(this, "No front facing camera found.", Toast.LENGTH_LONG).show();
                    switchCamera.setVisibility(View.GONE);
                }
                if (cameraPreview == null || cameraPreview.getChildCount() == 0) {
                    cameraPreview = (LinearLayout) findViewById(R.id.videoPreview);
                    cameraPreview.addView(mPreview);
                }
                mCamera = Camera.open(CameraHelper.cameraId == -1 ? CameraHelper.findBackFacingCamera() : CameraHelper.cameraId);
                mPreview.refreshCamera(mCamera);
            }
        }
    }

    private void init() {
        RelativeLayout screen = (RelativeLayout) findViewById(R.id.videoScreen);
        screen.setOnTouchListener(touchFocusListener);

        focusCursor = (ImageView) findViewById(R.id.cameraFocusCursor);

        hashTags = (EditText) findViewById(R.id.videoHashTag);

        cameraPreview = (LinearLayout) findViewById(R.id.videoPreview);
        mPreview = new CameraPreview(mContext, mCamera);
        cameraPreview.addView(mPreview);

        flashButton = (Button) findViewById(R.id.flashMode);
        flashButton.setOnClickListener(flashModeListener);
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            flashButton.setVisibility(View.GONE);
        }
        if (CameraHelper.cameraFront) flashButton.setVisibility(View.GONE);

        imagePreview = (SubsamplingScaleImageView) findViewById(R.id.imagePreview);

        mainButton = (Button) findViewById(R.id.mainButton);
        mainButton.setOnClickListener(captureListener);

        backCameraButton = (Button) findViewById(R.id.backVid);
        backCameraButton.setOnClickListener(backButtonListener);

        sendVideo = (Button) findViewById(R.id.sendVid);
        sendVideo.setOnClickListener(sendVideoListener);

        switchCamera = (Button) findViewById(R.id.changeCamera);
        switchCamera.setOnClickListener(switchCameraListener);
    }

    private void resetPosition(){
        previewing = false;
        sendVideo.setVisibility(View.GONE);
        switchCamera.setVisibility(View.VISIBLE);
        backCameraButton.setVisibility(View.GONE);
        hashTags.setVisibility(View.GONE);
        if(CameraHelper.cameraFront)
            flashButton.setVisibility(View.GONE);
        else
            flashButton.setVisibility(View.VISIBLE);
    }

    private void hideAllPosition(){
        mainButton.setVisibility(View.GONE);
        sendVideo.setVisibility(View.GONE);
        switchCamera.setVisibility(View.GONE);
        backCameraButton.setVisibility(View.GONE);
        flashButton.setVisibility(View.GONE);
        hashTags.setVisibility(View.VISIBLE);
    }

    private void previewPosition(){
        sendVideo.setVisibility(View.VISIBLE);
        switchCamera.setVisibility(View.GONE);
        backCameraButton.setVisibility(View.VISIBLE);
        flashButton.setVisibility(View.GONE);
        hashTags.setVisibility(View.VISIBLE);
    }

    View.OnTouchListener touchFocusListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(previewing){
                InputMethodManager imm = (InputMethodManager)getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(hashTags.getWindowToken(), 0);
            }
            if (!CameraHelper.cameraFront) {
                if(!previewing) {
                    mPreview.focus(event, focusCursor);
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            focusCursor.setVisibility(View.VISIBLE);
                            RelativeLayout.LayoutParams mParams = (RelativeLayout.LayoutParams) focusCursor.getLayoutParams();
                            int x = (int) event.getRawX();
                            int y = (int) event.getRawY();
                            mParams.leftMargin = x - (focusCursor.getWidth() / 2);
                            mParams.topMargin = y - focusCursor.getHeight();
                            focusCursor.setLayoutParams(mParams);


                            final Animation animation = new AlphaAnimation(1, 0);
                            animation.setDuration(1200);
                            animation.setInterpolator(new LinearInterpolator());
                            animation.setRepeatMode(Animation.REVERSE);
                            focusCursor.startAnimation(animation);

                            break;
                    }
                }
            }
            return true;
        }
    };

    View.OnClickListener flashModeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (flashMode == CameraPreview.FLASH_MODE_OFF) {
                mPreview.flashOn();
                flashMode = CameraPreview.FLASH_MODE_ON;
                flashButton.setText("ON");
            } else if (flashMode == CameraPreview.FLASH_MODE_ON) {
                mPreview.flashOff();
                flashMode = CameraPreview.FLASH_MODE_OFF;
                flashButton.setText("OFF");
            }
        }
    };


    View.OnClickListener captureListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            capture();
        }
    };

    private void capture() {
        mCamera.takePicture(null, null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                previewPosition();
                imageData = data;
                imagePreview.setVisibility(View.VISIBLE);
                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                imagePreview.setImage(ImageSource.bitmap(bmp));
                releaseCamera();
                cameraPreview.removeView(mPreview);
            }
        });
    }


    View.OnClickListener sendVideoListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            File pictureFile = AppConfig.getOutputMediaFile(mContext, AppConfig.NOTE_PICTURE_TYPE);
            if(pictureFile != null) {
                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(imageData);
                    fos.close();
                } catch (FileNotFoundException e) {
                    Log.d(TAG, "File not found: " + e.getMessage());
                } catch (IOException e) {
                    Log.d(TAG, "Error accessing file: " + e.getMessage());
                }finally {
                    AppConfig.LastFilePathCreated = pictureFile.getAbsolutePath();
                    Intent intent = new Intent();
                    intent.putExtra("File", AppConfig.LastFilePathCreated);
                    intent.putExtra("HashTag", hashTags.getText());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        }
    };
    View.OnClickListener backButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            File vid = new File(AppConfig.LastFilePathCreated);
            if (vid.exists()) {
                if (!vid.delete()) {
                    Log.d(TAG, "Couldn't delete file");
                }
            }
            releaseCamera();
            cameraPreview.removeView(mPreview);
            chooseCamera();
            mPreview = new CameraPreview(mContext, mCamera);
            mPreview.refreshCamera(mCamera);
            cameraPreview.addView(mPreview);
            resetPosition();
        }
    };


    View.OnClickListener switchCameraListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!previewing) {
                int cameraNumbers = Camera.getNumberOfCameras();
                if (cameraNumbers > 1) {
                    releaseCamera();
                    chooseCamera();
                } else {
                    Toast.makeText(getApplicationContext(), "Sorry, your phone has only one camera!", Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    private void chooseCamera() {
        if (!previewing) {
            int cameraId;
            if (CameraHelper.cameraFront) {
                cameraId = CameraHelper.findBackFacingCamera();
                flashButton.setVisibility(View.VISIBLE);
            } else {
                cameraId = CameraHelper.findFrontFacingCamera();
                flashButton.setVisibility(View.GONE);
            }
            if (cameraId >= 0) {
                mCamera = Camera.open(cameraId);
                mPreview.refreshCamera(mCamera);
            }
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
        cameraPreview.removeView(mPreview);
        resetPosition();
    }

}
