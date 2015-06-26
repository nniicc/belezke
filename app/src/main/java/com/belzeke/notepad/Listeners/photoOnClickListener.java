package com.belzeke.notepad.Listeners;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;

import com.belzeke.notepad.Config.AppConfig;

import java.io.File;

/**
 * Created by marko on 11.4.2015.
 */
public class photoOnClickListener implements View.OnClickListener {

    private Activity activity;

    public photoOnClickListener(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onClick(View v) {
        if(!AppConfig.NavigationShown) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File savePath = AppConfig.getOutputMediaFile(activity, AppConfig.MEDIA_TYPE_IMAGE);
            if (savePath != null) {
                AppConfig.LastFilePathCreated = savePath.getAbsolutePath();
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(savePath));
                activity.startActivityForResult(intent, AppConfig.CAMERA_REQUEST);
            }
        }

        //ARCHIVE
        /*{
            //view
            Intent intent = new Intent(activity, GridViewActivity.class);
            intent.putExtra("GRID_TYPE", AppConfig.NOTE_PICTURE_TYPE);
            activity.startActivity(intent);
        }*/
    }
}
