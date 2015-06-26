package com.belzeke.notepad.Listeners;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.belzeke.notepad.Activities.CameraPreview;
import com.belzeke.notepad.Config.AppConfig;

/**
 * Created by marko on 11.4.2015.
 */
public class videoOnClickListener implements View.OnClickListener {
    private Activity context;
    public videoOnClickListener(Activity context) {
        this.context = context;
    }

    @Override
    public void onClick(View v) {
        if (!AppConfig.NavigationShown) {
            Intent intent = new Intent(context, CameraPreview.class);
            context.startActivityForResult(intent, AppConfig.VIDEO_REQUEST);
        }
    }
}
