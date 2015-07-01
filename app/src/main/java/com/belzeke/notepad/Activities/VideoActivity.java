package com.belzeke.notepad.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
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
import android.widget.TextView;
import android.widget.Toast;

import com.belzeke.notepad.Config.AppConfig;
import com.belzeke.notepad.Helper.CameraHelper;
import com.belzeke.notepad.Helper.CameraPreview;
import com.belzeke.notepad.R;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("deprecation")
public class VideoActivity extends Activity {

    private String TAG = VideoActivity.class.getSimpleName();

    private Camera mCamera;
    private CameraPreview mPreview;
    private Context mContext;

    private int flashMode = CameraPreview.FLASH_MODE_OFF;

    private LinearLayout cameraPreview;
    private boolean recording;
    private boolean previewing;
    private MediaPlayer mediaPlayer;
    private MediaRecorder mediaRecorder;
    private Button backCameraButton;
    private Button sendVideo;
    private Button switchCamera;
    private Button flashButton;
    private Button mainButton;
    private File outputFile;
    private long startTime = 0;
    private ImageView focusCursor;
    private EditText hashTags;


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

    private void resetPosition(){
        previewing = false;
        sendVideo.setVisibility(View.GONE);
        switchCamera.setVisibility(View.VISIBLE);
        backCameraButton.setVisibility(View.GONE);
        timer.setText("");
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

    private void recordingPosition(){
        sendVideo.setVisibility(View.GONE);
        switchCamera.setVisibility(View.GONE);
        backCameraButton.setVisibility(View.GONE);
        flashButton.setVisibility(View.GONE);
        hashTags.setVisibility(View.GONE);
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
                if(!recording && !previewing) {
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
            if (recording) {
                timerHandler.removeCallbacks(timerRunnable);
                mediaRecorder.stop();
                releaseMediaRecorder();
                recording = false;
                CameraHelper.cameraFront = !CameraHelper.cameraFront;
                releaseCamera();
                previewPosition();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            previewing = true;
                            mediaPlayer.setDisplay(mPreview.getHolder());
                            mediaPlayer.setDataSource(AppConfig.LastFilePathCreated);
                            mediaPlayer.setLooping(true);
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                            backCameraButton.setVisibility(View.VISIBLE);
                            sendVideo.setVisibility(View.VISIBLE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                outputFile = AppConfig.getOutputMediaFile(getApplicationContext(), AppConfig.MEDIA_TYPE_VIDEO);
                if (!prepareMediaRecorder()) {
                    Toast.makeText(getApplicationContext(), "Fail in prepareMediaRecorder()!\n - Ended -", Toast.LENGTH_LONG).show();
                    finish();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            AppConfig.LastFilePathCreated = outputFile.getPath();
                            mediaRecorder.start();
                            startTime = System.currentTimeMillis();
                            timerHandler.postDelayed(timerRunnable, 0);
                            recording = true;
                            recordingPosition();
                        } catch (final Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    };

    private boolean prepareMediaRecorder() {
        try {
            if (mCamera == null)
                chooseCamera();

            Display display = ((WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            Camera.Size optimalSize = mPreview.getPreviewSize();

            CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
            profile.videoFrameWidth = optimalSize.width;
            profile.videoFrameHeight = optimalSize.height;
            profile.videoBitRate = 1500000;
            profile.videoCodec = MediaRecorder.VideoEncoder.H264;
            profile.videoFrameRate = 30;
            profile.audioBitRate = 780000;

            Camera.Parameters parameters = mCamera.getParameters();
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

            mCamera.setParameters(parameters);

            mediaRecorder = new MediaRecorder();

            mCamera.unlock();
            mediaRecorder.setCamera(mCamera);

            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mediaRecorder.setOutputFile(outputFile.getAbsolutePath());

            mediaRecorder.setProfile(profile);
/*
            Camera.Parameters parameters = mCamera.getParameters();

            parameters.set("iso", "ISO800");
            parameters.setColorEffect("none");
            parameters.setPreviewFrameRate(30);
            parameters.setExposureCompensation(4);
            parameters.setSceneMode("scene-mode=dusk-dawn");

            mCamera.setParameters(parameters);

            mediaRecorder = new MediaRecorder();

            mCamera.unlock();
            mediaRecorder.setCamera(mCamera);

            Camera.Size optimalSize = mPreview.getPreviewSize();

            mediaRecorder.setVideoSource(0);
            mediaRecorder.setAudioSource(0);
            mediaRecorder.setOutputFormat(2);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mediaRecorder.setVideoEncodingBitRate(1500000);
            mediaRecorder.setVideoFrameRate(30);
            mediaRecorder.setVideoSize(optimalSize.width, optimalSize.height);
            mediaRecorder.setAudioChannels(2);
            mediaRecorder.setAudioEncoder(3);
            mediaRecorder.setAudioEncodingBitRate(780000);
            mediaRecorder.setAudioSamplingRate(44100);
            mediaRecorder.setOutputFile(outputFile.getAbsolutePath());
*/

            if (display.getRotation() == Surface.ROTATION_0) {
                if (!CameraHelper.cameraFront) {
                    mediaRecorder.setOrientationHint(90);
                } else {
                    mediaRecorder.setOrientationHint(270);
                }
            }
            if (display.getRotation() == Surface.ROTATION_270) {
                if (!CameraHelper.cameraFront) {
                    mediaRecorder.setOrientationHint(180);
                } else {
                    mediaRecorder.setOrientationHint(0);
                }
            }

            try {
                mediaRecorder.prepare();
            } catch (IllegalStateException e) {
                Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
                releaseMediaRecorder();
                return false;
            } catch (IOException e) {
                Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
                releaseMediaRecorder();
                return false;
            }
        } catch (Exception e) {
            Log.d(TAG, "Unknown Exception: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    View.OnClickListener sendVideoListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.putExtra("File", AppConfig.LastFilePathCreated);
            intent.putExtra("HashTag", hashTags.getText());
            setResult(RESULT_OK, intent);
            finish();
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
            releaseMediaRecorder();
            cameraPreview.removeView(mPreview);
            chooseCamera();
            mPreview = new CameraPreview(mContext, mCamera);
            mPreview.refreshCamera(mCamera);
            cameraPreview.addView(mPreview);
            resetPosition();
        }
    };

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    View.OnClickListener switchCameraListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!recording) {
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
        if (!recording) {
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
        releaseMediaRecorder();
        cameraPreview.removeView(mPreview);
        timerHandler.removeCallbacks(timerRunnable);
        mediaPlayer.release();
        resetPosition();
    }
}
