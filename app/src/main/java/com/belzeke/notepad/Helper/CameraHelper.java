package com.belzeke.notepad.Helper;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Size;

import java.util.List;

/**
 * Created by marko on 19.5.2015.
 */
@SuppressWarnings("deprecation")
public class CameraHelper {

    public static boolean cameraFront = false;
    private final static double epsilon = 0.17;

    public static boolean hasCamera(Context context){
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public static int findFrontFacingCamera(){
        int cameraId = -1;

        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++){
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if(info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return  cameraId;
    }

    public static int findBackFacingCamera(){
        int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++){
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if(info.facing == Camera.CameraInfo.CAMERA_FACING_BACK){
                cameraId = i;
                cameraFront = false;
                break;
            }
        }
        return  cameraId;
    }

    public static Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h){

        Size optimalSize = null;
        for (Size currSize : sizes) {
            if(currSize.width == 720){
                if(currSize.height == 480){
                    optimalSize = currSize;
                }
            }
        }/*
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio=(double)h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }*/
        return optimalSize;
    }
}
