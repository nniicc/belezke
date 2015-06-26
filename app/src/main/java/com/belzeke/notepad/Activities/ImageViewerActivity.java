package com.belzeke.notepad.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.belzeke.notepad.Config.AppConfig;
import com.belzeke.notepad.Models.Note;
import com.belzeke.notepad.R;
import com.polites.android.GestureImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageViewerActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        Intent intent = getIntent();
        int position = intent.getIntExtra("position", 0);
        List<Note> items = AppConfig.AdapterItems;

        ViewPager pager = (ViewPager) findViewById(R.id.view_pager);
        ImagePagerAdapter adapter = new ImagePagerAdapter(items);
        pager.setAdapter(adapter);
        pager.setCurrentItem(position,true);
    }

    private class ImagePagerAdapter extends PagerAdapter {

        private List<Bitmap> items;

        public ImagePagerAdapter(List<Note> notes) {
            items = new ArrayList<>();
            for (int i = 0; i < notes.size(); i++) {
                File file = new File(notes.get(i).getNoteName());
                if(file.exists()){
                    items.add(AppConfig.decodeFile(file));
                }
            }
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((GestureImageView)object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Context context = ImageViewerActivity.this;
            GestureImageView imageView = new GestureImageView(context);
            int padding = container.getResources().getDimensionPixelSize(R.dimen.padding_medium);
            imageView.setPadding(padding, padding, padding, padding);
            imageView.setAdjustViewBounds(true);
            imageView.setImageBitmap(items.get(position));
            container.addView(imageView, 0);
            return imageView;
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((GestureImageView) object);
        }
    }
}
