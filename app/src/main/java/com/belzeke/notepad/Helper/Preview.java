package com.belzeke.notepad.Helper;

/**
 * Created by marko on 26.6.2015.
 */

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.io.IOException;

@SuppressWarnings("deprecation")
public class Preview extends ViewGroup implements SurfaceHolder.Callback {

    private final String TAG = "Preview";
    SurfaceView mSurfaceView;
    SurfaceHolder mHolder;
    Size mPreviewSize;
    List<Size> mSupportedPreviewSizes;
    Camera mCamera;

    public Preview(Context context) {
        super(context);

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        this.setLayoutParams(lp);

        mSurfaceView = new SurfaceView(context);
        mSurfaceView.setLayoutParams(lp);
        addView(mSurfaceView);

        //Install a SurfaceHolder.Callback so we get notified when the
        //underlying surface is created and destroyed.
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void setCamera(Camera camera) {
        mCamera = camera;
        if(mCamera != null) {
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            requestLayout();
        }
    }

    public void switchCamera(Camera camera) {
        setCamera(camera);
        try {
            camera.setPreviewDisplay(mHolder);
        }
        catch (IOException exception) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
        }
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        requestLayout();

        camera.setParameters(parameters);
    }

    /**
     * Called to determine the size requirements for this view and all of its children.
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //We purposely disregard child measurements because act as a
        //Wrapper to a SurfaceView that centers the camera preview instead of stretching it.
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if(mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }
    }

    /**
     * Called when this view should assign a size and position to all of its children.
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(changed && getChildCount() > 0) {
            final View child = getChildAt(0);

            final int width = r - l;
            final int height = b - t;

            int previewWidth = width;
            int previewHeight = height;
            if(mPreviewSize != null) {
                previewWidth = mPreviewSize.width;
                previewHeight = mPreviewSize.height;
            }

            // Center the child SurfaceView within the parent.
            if(width * previewHeight > height * previewWidth) {
                final int scaledChildWidth = previewWidth * height / previewHeight;
                child.layout((width - scaledChildWidth) / 2, 0, (width + scaledChildWidth) / 2, height);
            }
            else {
                final int scaledChildHeight = previewHeight * width / previewWidth;
                child.layout(0, 0, width, height);
            }
        }
    }

    /**
     * This is called immediately after the surface is first created
     * @param holder
     */
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if(mCamera != null) {
                mCamera.setPreviewDisplay(holder);
            }
        }
        catch (IOException e) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", e);
        }
    }

    /**
     * This is called immediately before a surface is being destroyed
     * @param holder
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
        //Surface will be destroyed when we return, so stop the preview.
        if(mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
        }
    }

    /**
     * This is called immediately after any structural changes (format or size) have been made to the surface
     * @param holder
     * @param format
     * @param w
     * @param h
     */
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        mCamera.setDisplayOrientation(90);
        requestLayout();

        mCamera.setParameters(parameters);
        mCamera.startPreview();
    }

    /**
     * Returns the best preview size
     * @param sizes
     * @param w
     * @param h
     * @return Size
     */
    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for(Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if(Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if(Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if(optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if(Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        return optimalSize;
    }
}
