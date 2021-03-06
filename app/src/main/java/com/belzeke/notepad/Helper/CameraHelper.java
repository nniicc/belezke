package com.belzeke.notepad.Helper;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;

import java.util.List;

/**
 * Created by marko on 19.5.2015.
 */
@SuppressWarnings("deprecation")
public class CameraHelper {

    public static boolean cameraFront = false;
    public static int cameraId = -1;

    public static boolean hasCamera(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public static int findFrontFacingCamera() {
        cameraId = -1;

        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return cameraId;
    }

    public static int findBackFacingCamera() {
        cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;
            }
        }
        return cameraId;
    }

    public static Size getOptimalPreviewSize2(List<Camera.Size> sizes, int width, int height) {
        Camera.Size result = null;
        double PREVIEW_SIZE_FACTOR = 1.30;
        Log.i(CameraPreview.class.getSimpleName(), "window width: " + width + ", height: " + height);
        for (final Camera.Size size : sizes) {
            if (size.width <= width * PREVIEW_SIZE_FACTOR && size.height <= height * PREVIEW_SIZE_FACTOR) {
                if (result == null) {
                    result = size;
                } else {
                    final int resultArea = result.width * result.height;
                    final int newArea = size.width * size.height;

                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }
        if (result == null) {
            result = getOptimalPreviewSize(720, 480, sizes);
        }
        Log.i(CameraPreview.class.getSimpleName(), "Using PreviewSize: " + result.width + " x " + result.height);
        return result;
    }

    public static Size getOptimalPreviewSize(int width, int height, List<Camera.Size> sizes) {
        Camera.Size result = null;

        for (Camera.Size size : sizes) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }
        return result;

    }

    public static boolean hasFrontCamera() {
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return true;
            }
        }
        return false;
    }

    public static boolean isCameraUsedByOtherApp() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (RuntimeException e) {
            return true;
        } finally {
            if (camera != null) camera.release();
        }
        return false;
    }

}
