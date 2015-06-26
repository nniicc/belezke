package com.belzeke.notepad.Models;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by marko on 4.3.2015.
 */
public class Note implements Serializable {
    private long id;
    private String created;
    private int userId;
    private int noteType;
    private String noteName;
    private String noteText;
    private boolean uploaded;
    private Bitmap bitmap;
    private long duration = 0;

    public String getHashTag() {
        return hashTag;
    }

    public void setHashTag(String hashTag) {
        this.hashTag = hashTag;
    }

    private String hashTag;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Note() {
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setNoteName(String noteName) {
        this.noteName = noteName;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    public void setNoteType(int noteType) {
        this.noteType = noteType;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCreated() {
        return created;
    }

    public long getId() {
        return id;
    }

    public String getNoteName() {
        return noteName;
    }

    public String getNoteText() {
        return noteText;
    }

    public int getNoteType() {
        return noteType;
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public int getUserId() {
        return userId;
    }

    public String getDuration() {
        if(duration == 0) return "";
        int seconds = (int) (duration / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;

        return String.format("%d:%02d", minutes, seconds);
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
