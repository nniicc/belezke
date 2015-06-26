package com.belzeke.notepad.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.belzeke.notepad.Config.AppConfig;
import com.belzeke.notepad.R;

import java.io.File;

public class HashTagNoteActivity extends AppCompatActivity {

    private ImageView mainImg;
    private EditText text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hash_tag_note);

        Intent intent = getIntent();
        int type = intent.getIntExtra(AppConfig.NoteType, AppConfig.NOTE_PICTURE_TYPE);
        init(type);
    }

    private void init(int type) {
        mainImg = (ImageView) findViewById(R.id.hashTagImg);
        text = (EditText) findViewById(R.id.hashTagText);

        if(type == AppConfig.NOTE_PICTURE_TYPE){
            mainImg.setVisibility(View.VISIBLE);
            Bitmap photo = BitmapFactory.decodeFile(new File(AppConfig.LastFilePathCreated).getAbsolutePath());
            mainImg.setImageBitmap(photo);
        }else if(type == AppConfig.NOTE_TEXT_TYPE){
            mainImg.setVisibility(View.GONE);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
                    ( RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            params.addRule(RelativeLayout.CENTER_VERTICAL);

            text.setLayoutParams(params);
            text.setHint(R.string.text_hashtag);
        }

    }
}
