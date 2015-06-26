package com.belzeke.notepad.Asyncs;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.ListView;

import com.belzeke.notepad.Interfaces.OnAsyncDataGet;
import com.belzeke.notepad.Models.Note;

import java.util.ArrayList;

/**
 * Created by marko on 4.3.2015.
 */
public class GetNotesAsyncTask extends AsyncTask<Void, Void, ArrayList<Note>> {
    private Context context;
    private OnAsyncDataGet listener;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listView;

    public GetNotesAsyncTask(Context context, OnAsyncDataGet listener, SwipeRefreshLayout swipeRefreshLayout, ListView listView) {
        this.context = context;
        this.listener = listener;
        this.swipeRefreshLayout = swipeRefreshLayout;
        this.listView = listView;
    }

    @Override
    protected void onPreExecute() {
        listView.setEnabled(false);
    }

    @Override
    protected void onPostExecute(ArrayList<Note> notes) {
        swipeRefreshLayout.setRefreshing(false);
        listView.setEnabled(true);
        if(notes == null)
            notes = new ArrayList<>();
        listener.onDataGet(context, notes);
    }

    @Override
    protected ArrayList<Note> doInBackground(Void... params) {
        ArrayList<Note> list = null;
        /*
        Cursor cursor = db.getAllRows();
        if (cursor.moveToFirst()) {
            list = new ArrayList<>();
            do {
                Note note = new Note(
                                    cursor.getString(DBAdapter.COL_NOTETEXT), //Note text
                                    cursor.getString(DBAdapter.COL_HASHTAG),  //Hashtag
                                    cursor.getInt(DBAdapter.COL_NOTETYPE));   //Note type
                note.setId(cursor.getLong(DBAdapter.COL_ROWID));
                list.add(note);

            }while(cursor.moveToNext());
        }*/
        return list;
    }
}
