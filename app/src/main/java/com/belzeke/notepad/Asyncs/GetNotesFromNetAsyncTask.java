package com.belzeke.notepad.Asyncs;

import android.os.AsyncTask;

import com.belzeke.notepad.Models.Note;

import java.util.ArrayList;

/**
 * Created by marko on 4.3.2015.
 */
public class GetNotesFromNetAsyncTask extends AsyncTask<Void, Integer, ArrayList<Note>> {



    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(ArrayList<Note> notes) {
        super.onPostExecute(notes);
    }

    @Override
    protected ArrayList<Note> doInBackground(Void... params) {

        return null;
    }
}
