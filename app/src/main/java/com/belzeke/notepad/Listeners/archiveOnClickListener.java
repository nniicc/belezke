package com.belzeke.notepad.Listeners;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.belzeke.notepad.Activities.GridViewActivity;
import com.belzeke.notepad.Activities.ListViewActivity;
import com.belzeke.notepad.Config.AppConfig;

/**
 * Created by marko on 19.6.2015.
 */
public class archiveOnClickListener implements View.OnClickListener{
    private Activity context;
    private int noteType;

    public archiveOnClickListener(Activity context, int noteType) {
        this.context = context;
        this.noteType = noteType;
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (noteType){
            case AppConfig.NOTE_TEXT_TYPE:
                intent = new Intent(context, ListViewActivity.class);
                intent.putExtra(AppConfig.NoteType, AppConfig.NOTE_TEXT_TYPE);
                context.startActivity(intent);
                break;
            case AppConfig.NOTE_PICTURE_TYPE:
                intent = new Intent(context, GridViewActivity.class);
                intent.putExtra(AppConfig.NoteType, AppConfig.NOTE_PICTURE_TYPE);
                context.startActivity(intent);
                break;
            case AppConfig.NOTE_AUDIO_TYPE:
                intent = new Intent(context, ListViewActivity.class);
                intent.putExtra(AppConfig.NoteType, AppConfig.NOTE_AUDIO_TYPE);
                context.startActivity(intent);
                break;
            case AppConfig.NOTE_VIDEO_TYPE:
                intent = new Intent(context, GridViewActivity.class);
                intent.putExtra(AppConfig.NoteType, AppConfig.NOTE_VIDEO_TYPE);
                context.startActivity(intent);
                break;
            default:
                break;
        }
    }
}
