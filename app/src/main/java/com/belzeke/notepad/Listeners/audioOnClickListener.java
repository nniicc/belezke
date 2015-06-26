package com.belzeke.notepad.Listeners;

import android.content.Intent;
import android.view.View;

import com.belzeke.notepad.Activities.MainScreenActivity;
import com.belzeke.notepad.Config.AppConfig;
import com.belzeke.notepad.Services.AudioRecorderService;

/**
 * Created by marko on 22.5.2015.
 */
public class audioOnClickListener implements View.OnClickListener {
    private MainScreenActivity context;
    private boolean isRecording = false;



    public audioOnClickListener(MainScreenActivity context) {
        this.context = context;
    }

    @Override
    public void onClick(View v) {
        if(!AppConfig.NavigationShown) {
            //record
            if(isRecording) {
                context.stopService(new Intent(context, AudioRecorderService.class));
                isRecording = !isRecording;
            }else{
                context.startService(new Intent(context, AudioRecorderService.class));
                isRecording = !isRecording;
            }

        }

        //Archive
        /*{
            //view
            Intent intent = new Intent(context, ListViewActivity.class);
            intent.putExtra("GRID_TYPE", AppConfig.NOTE_AUDIO_TYPE);
            context.startActivity(intent);
        }*/
    }
}
