package com.belzeke.notepad.Listeners;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

import com.belzeke.notepad.Activities.HashTagNoteActivity;
import com.belzeke.notepad.Config.AppConfig;

/**
 * Created by marko on 15.6.2015.
 */
public class textOnClickListener implements OnClickListener {
    private Activity activity;

    public textOnClickListener(Activity activity) {
        this.activity = activity;
    }
    @Override
    public void onClick(View v) {
        if(!AppConfig.NavigationShown) {
            Intent intent = new Intent(activity, HashTagNoteActivity.class);
            intent.putExtra(AppConfig.NoteType, AppConfig.NOTE_TEXT_TYPE);
            activity.startActivity(intent);
        }
    }
}
