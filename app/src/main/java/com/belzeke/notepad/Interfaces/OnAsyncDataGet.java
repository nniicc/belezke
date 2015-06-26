package com.belzeke.notepad.Interfaces;

import android.content.Context;

import com.belzeke.notepad.Models.Note;

import java.util.ArrayList;

/**
 * Created by marko on 4.3.2015.
 */
public interface OnAsyncDataGet {
    public void onDataGet(Context context, ArrayList<Note> list);
}
