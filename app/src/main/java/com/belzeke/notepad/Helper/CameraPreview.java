package com.belzeke.notepad.Helper;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by marko on 1.7.2015.
 */
@SuppressWarnings("deprecation")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final int FOCUS_AREA_SIZE = 300;
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Context mContext;

    public static final int FLASH_MODE_OFF = 0;
    public static final int FLASH_MODE_ON = 1;


    public Camera.Size getPreviewSize() {
        return mPreviewSize;
    }

    Camera.Size mPreviewSize;
    List<Camera.Size> mSupportedPreviewSizes;


    public CameraPreview(Context context, Camera camera) {
        super(context);
        mContext = context;
        mCamera = camera;
        mHolder = getHolder();
        mHolder.addCallback(this);
        this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (mCamera != null) {
                mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
                requestLayout();
                setCameraParams();
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void refreshCamera(Camera camera) {
        if (mHolder.getSurface() == null) return;

        try {
            mCamera.stopPreview();
        } catch (Exception ignored) {
        }
        setCamera(camera);
        if (mCamera != null) {
            try {
                mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
                requestLayout();
                setCameraParams();
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private boolean canUseFlash() {
        if (mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            List<String> flashModes = mCamera.getParameters().getSupportedFlashModes();
            if (flashModes != null) {
                return true;
            }
        }
        return false;
    }

    private void setCameraParams() {
        Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Camera.Parameters parameters = mCamera.getParameters();
        if (display.getRotation() == Surface.ROTATION_0) {
            if (!CameraHelper.cameraFront) {
                parameters.setRotation(90);
                mCamera.setDisplayOrientation(90);
            } else {
                parameters.setRotation(270);
                mCamera.setDisplayOrientation(90);
            }
        }
        if (display.getRotation() == Surface.ROTATION_270) {
            if (!CameraHelper.cameraFront) {
                parameters.setRotation(90);
                mCamera.setDisplayOrientation(180);
            } else {
                parameters.setRotation(90);
                mCamera.setDisplayOrientation(0);
            }
        }



        parameters.set("iso", "ISO800");
        parameters.setColorEffect("none");
        parameters.setPreviewFrameRate(30);
        parameters.setExposureCompensation(4);
        parameters.setSceneMode("scene-mode=dusk-dawn");

        mCamera.setParameters(parameters);
    }

    private void setCamera(Camera camera) {
        mCamera = camera;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        refreshCamera(mCamera);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }

    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = CameraHelper.getOptimalPreviewSize2(mSupportedPreviewSizes, width, height);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {

            final int width = r - l;
            final int height = b - t;
            this.layout(0, 0, width, height);
        }
    }

    public void flashOn() {
        if (canUseFlash()) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(parameters);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void flashOff() {
        if (canUseFlash()) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(parameters);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void focus(MotionEvent event, final ImageView focusCursor) {
        if (mCamera != null) {
            mCamera.cancelAutoFocus();
            Rect focusRect = calculateTapArea(event.getX(), event.getY());

            Camera.Parameters parameters = mCamera.getParameters();
            if (!parameters.getFocusMode().equals(Camera.Parameters.FOCUS_MODE_AUTO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            if (parameters.getMaxNumFocusAreas() > 0) {
                List<Camera.Area> myList = new ArrayList<>();
                myList.add(new Camera.Area(focusRect, 1000));
                parameters.setFocusAreas(myList);
            }

            try {
                mCamera.cancelAutoFocus();
                mCamera.setParameters(parameters);
                mCamera.startPreview();
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (!camera.getParameters().getFocusMode().equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                            Parameters parameters = camera.getParameters();
                            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                            if (parameters.getMaxNumFocusAreas() > 0) {
                                parameters.setFocusAreas(null);
                            }
                            camera.setParameters(parameters);
                            camera.startPreview();
                        }
                        focusCursor.setVisibility(GONE);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Rect calculateTapArea(float x, float y) {
        int left = clamp(Float.valueOf((x / this.getWidth()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);
        int top = clamp(Float.valueOf((y / this.getHeight()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);

        return new Rect(left, top, left + FOCUS_AREA_SIZE, top + FOCUS_AREA_SIZE);
    }

    private int clamp(int touchCoordinateInCameraReper, int focusAreaSize) {
        int result;
        if (Math.abs(touchCoordinateInCameraReper) + focusAreaSize / 2 > 1000) {
            if (touchCoordinateInCameraReper > 0) {
                result = 1000 - focusAreaSize / 2;
            } else {
                result = -1000 + focusAreaSize / 2;
            }
        } else {
            result = touchCoordinateInCameraReper - focusAreaSize / 2;
        }
        return result;
    }
}
