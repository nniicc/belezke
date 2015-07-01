package com.belzeke.notepad.Helper;

import android.content.Context;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Build;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import java.io.IOException;
import java.util.List;

/**
 * Created by marko on 6.5.2015.
 */

@SuppressWarnings("deprecation")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Context mContext;
    private int width, height;

    public MediaRecorder mediaRecorder;
    private List<Camera.Size> mSupportedPreviewSizes;
    private List<String> mSupportedFlashModes;
    private Camera.Size mPreviewSize;


    public CameraPreview(Context context, Camera mCamera) {
        super(context);
        this.mContext = context;
        this.mCamera = mCamera;
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public SurfaceHolder getPrivateHolder() { return mHolder; }


    public void surfaceCreated(SurfaceHolder holder) {
        try{
            if(mCamera != null){
                mHolder = holder;
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void refreshCamera(Camera camera){
        mCamera = camera;
        if(mCamera != null){
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            Camera.Parameters parameters = camera.getParameters();
            parameters.setExposureCompensation(parameters.getMaxExposureCompensation());


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                parameters.setAutoExposureLock(false);
                parameters.setAutoWhiteBalanceLock(false);
            }
            parameters.set("iso", "ISO800");
            parameters.setColorEffect("none");
            parameters.setPreviewFrameRate(30);
            parameters.setExposureCompensation(4);
            parameters.setSceneMode("scene-mode=dusk-dawn");
            if(!CameraHelper.cameraFront) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            camera.setParameters(parameters);
        }
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if(mSupportedPreviewSizes != null){
            Camera.Size optimalSize = CameraHelper.getOptimalPreviewSize(720, 480, mSupportedPreviewSizes);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if(changed){
            final View cameraView = this;

            int width = right - left;
            int height = bottom - top;

            int previewWidth = width;
            int previewHeight = height;

            if(mPreviewSize != null){
                Display display = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

                switch (display.getRotation()){
                    case Surface.ROTATION_0:
                        previewWidth = mPreviewSize.height;
                        previewHeight = mPreviewSize.width;
                        mCamera.setDisplayOrientation(90);
                        break;
                    case Surface.ROTATION_90:
                        previewWidth = mPreviewSize.width;
                        previewHeight = mPreviewSize.height;
                        break;
                    case Surface.ROTATION_180:
                        previewWidth = mPreviewSize.height;
                        previewHeight = mPreviewSize.width;
                        break;
                    case Surface.ROTATION_270:
                        previewWidth = mPreviewSize.width;
                        previewHeight = mPreviewSize.height;
                        mCamera.setDisplayOrientation(180);
                        break;
                }
            }

            cameraView.layout(0, 0, previewWidth, previewHeight);
            try{
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public void refreshCamera1(Camera camera){
        if(mHolder == null) return;
        try {
            Camera.Parameters parameters = camera.getParameters();
            List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
            Camera.Size optimalSize = CameraHelper.getOptimalPreviewSize(720, 480, mSupportedPreviewSizes);

            parameters.setExposureCompensation(parameters.getMaxExposureCompensation());


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                parameters.setAutoExposureLock(false);
                parameters.setAutoWhiteBalanceLock(false);
            }
            parameters.set("iso", "ISO800");
            parameters.setColorEffect("none");
            parameters.setPreviewFrameRate(30);
            parameters.setExposureCompensation(4);
            parameters.setSceneMode("scene-mode=dusk-dawn");
            if(!CameraHelper.cameraFront) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }

            parameters.setPreviewSize(optimalSize.width, optimalSize.height);
            camera.setParameters(parameters);
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            camera.stopPreview();
        }catch (Exception e){
            e.printStackTrace();
        }
        Display display = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        if(display.getRotation() == Surface.ROTATION_0){
            camera.setDisplayOrientation(90);
        }
        if(display.getRotation() == Surface.ROTATION_270){
            camera.setDisplayOrientation(180);
        }

        setCamera(camera);
        try{
            camera.setPreviewDisplay(mHolder);
            camera.startPreview();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int w, int h) {
        width = w;
        height = h;
        refreshCamera(mCamera);
    }

    private void setCamera(Camera camera) {
        mCamera = camera;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        releaseCamera();
        releaseMediaRecorder();
    }
    public void releaseMediaRecorder(){
        if(mediaRecorder != null){
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }
    public void releaseCamera(){
        if(mCamera != null){
            mCamera.release();
            mCamera = null;
        }
    }
}
