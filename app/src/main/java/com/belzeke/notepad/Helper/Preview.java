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
import android.widget.RelativeLayout;

import java.io.IOException;
import java.util.List;

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

        RelativeLayout relativeLayout = new RelativeLayout(context);
        relativeLayout.setLayoutParams(lp);


        mSurfaceView = new SurfaceView(context);
        mSurfaceView.setLayoutParams(lp);
        mSurfaceView.bringToFront();
        relativeLayout.addView(mSurfaceView);

      /*  Button mainButton = new Button(context);
        mainButton.setText("Start/Stop");
        RelativeLayout.LayoutParams mainButtonLp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mainButtonLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mainButton.setLayoutParams(mainButtonLp);

        relativeLayout.addView(mainButton);
*/
        addView(relativeLayout);

        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void setCamera(Camera camera) {
        mCamera = camera;
        if (mCamera != null) {
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            requestLayout();
        }
    }

    public void switchCamera(Camera camera) {
        setCamera(camera);
        try {
            camera.setPreviewDisplay(mHolder);
        } catch (IOException exception) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
        }
        requestLayout();
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);

        camera.setParameters(parameters);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed && getChildCount() > 0) {
            ViewGroup parent = (ViewGroup) getChildAt(0);
            final View child = parent.getChildAt(0);


            final int width = r - l;
            final int height = b - t;

            parent.layout(0, 0, width, height);
            child.layout(0, 0, width, height);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", e);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //Surface will be destroyed when we return, so stop the preview.
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        mCamera.setDisplayOrientation(90);
        requestLayout();

        mCamera.setParameters(parameters);
        mCamera.startPreview();
    }


    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        return optimalSize;
    }
}
