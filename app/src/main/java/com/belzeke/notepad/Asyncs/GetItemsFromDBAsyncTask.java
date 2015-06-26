package com.belzeke.notepad.Asyncs;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ProgressBar;

import com.belzeke.notepad.Adapters.GridViewAdapter;
import com.belzeke.notepad.Adapters.ListViewAdapter;
import com.belzeke.notepad.Config.AppConfig;
import com.belzeke.notepad.Helper.SQLiteHandler;
import com.belzeke.notepad.Models.Note;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by marko on 23.5.2015.
 */
public class GetItemsFromDBAsyncTask extends AsyncTask<Void, Void, List<Note>> {

    private SQLiteHandler db;
    private Context mContext;
    private int type;
    private GridViewAdapter mAdapter;
    private ListViewAdapter mNoteListAdapter;
    private ProgressBar spinner;
    private List<Note> notes;

    public GetItemsFromDBAsyncTask(Context mContext, int type, GridViewAdapter mAdapter, ProgressBar spinner) {
        this.mAdapter = mAdapter;
        this.spinner = spinner;
        this.mContext = mContext;
        this.db = new SQLiteHandler(mContext);
        this.type = type;
        notes = new ArrayList<>();
    }

    public GetItemsFromDBAsyncTask(Context mContext, int type, ListViewAdapter mAdapter, ProgressBar spinner) {
        this.mNoteListAdapter = mAdapter;
        this.spinner = spinner;
        this.mContext = mContext;
        this.db = new SQLiteHandler(mContext);
        this.type = type;
        notes = new ArrayList<>();
    }

    @Override
    protected void onPostExecute(List<Note> notes) {
        super.onPostExecute(notes);
        if(type == AppConfig.NOTE_PICTURE_TYPE || type == AppConfig.NOTE_VIDEO_TYPE)
            mAdapter.setItems(notes);
        if(type == AppConfig.NOTE_AUDIO_TYPE || type == AppConfig.NOTE_TEXT_TYPE)
            mNoteListAdapter.setItems(notes);
        spinner.setVisibility(View.GONE);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        spinner.setVisibility(View.VISIBLE);
    }

    @Override
    protected List<Note> doInBackground(Void... params) {
        List<Note> array = db.getItems(type);
        if(array == null) return null;
        for(Note a : array){
            File diskFile = new File(a.getNoteName());
            if(diskFile.exists()){
                if(type == AppConfig.NOTE_VIDEO_TYPE) {
                    MediaPlayer player = MediaPlayer.create(mContext, Uri.parse(diskFile.getAbsolutePath()));
                    a.setDuration(player.getDuration());
                    player.reset();
                    player.release();
                    a.setBitmap(ThumbnailUtils.createVideoThumbnail(diskFile.getAbsolutePath(), MediaStore.Video.Thumbnails.MINI_KIND));
                }else if (type == AppConfig.NOTE_PICTURE_TYPE){
                    a.setBitmap(AppConfig.decodeFile(new File(a.getNoteName())));
                }else if(type == AppConfig.NOTE_AUDIO_TYPE){
                    MediaPlayer player = MediaPlayer.create(mContext, Uri.parse(diskFile.getAbsolutePath()));
                    if(player != null) {
                        a.setDuration(player.getDuration());
                        player.reset();
                        player.release();
                    }
                }
                notes.add(a);
            }
        }
        return array;
    }
}
