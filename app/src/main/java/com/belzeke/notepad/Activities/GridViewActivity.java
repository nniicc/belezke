package com.belzeke.notepad.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.belzeke.notepad.Adapters.GridViewAdapter;
import com.belzeke.notepad.Asyncs.GetItemsFromDBAsyncTask;
import com.belzeke.notepad.Asyncs.PostImageAsyncTask;
import com.belzeke.notepad.Asyncs.PostVideoAsyncTask;
import com.belzeke.notepad.Config.AppConfig;
import com.belzeke.notepad.Models.Note;
import com.belzeke.notepad.R;

import java.io.File;

public class GridViewActivity extends AppCompatActivity {

    private GridView mGridView;
    private VideoView mVideoViw;
    private boolean mediaShown = false;
    private Integer gridType;
    private static String TAG = GridViewActivity.class.getSimpleName();
    private MenuItem shareItem;
    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        gridType = intent.getIntExtra(AppConfig.NoteType, AppConfig.NOTE_PICTURE_TYPE);
        if(gridType == AppConfig.NOTE_VIDEO_TYPE)
            setTheme(R.style.VideoTheme);
        else if (gridType == AppConfig.NOTE_PICTURE_TYPE)
            setTheme(R.style.PhotoTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_view);

        init(gridType);
    }

    @Override
    public void onBackPressed() {
        if (!mediaShown) {
            super.onBackPressed();
        } else {
            mGridView.setVisibility(View.VISIBLE);
            mVideoViw.setVisibility(View.GONE);
            mediaShown = !mediaShown;
            shareItem.setVisible(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_grid_view, menu);

        shareItem = menu.findItem(R.id.menu_item_share);


        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);

        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Options");
        menu.add(0, v.getId(), 0, "Share");
        menu.add(0, v.getId(), 0, "Show in browser");
        menu.add(0, v.getId(), 0, "Upload");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        GridViewAdapter adapter = (GridViewAdapter) mGridView.getAdapter();
        Note note = adapter.getItem(info.position);
        File noteFile = new File(note.getNoteName());
        if (item.getTitle() == "Share") {
            if (noteFile.exists() && note.isUploaded() && AppConfig.isNetworkAvailable(getApplicationContext())) {
                String url = AppConfig.URLFiles + AppConfig.userId + File.separator + noteFile.getName();
                if (!url.startsWith("http://") && !url.startsWith("https://"))
                    url = "http://" + url;
                Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, url);
                startActivity(Intent.createChooser(intent, "Share via"));
            }
        } else if (item.getTitle() == "Show in browser") {
            if (noteFile.exists() && note.isUploaded() && AppConfig.isNetworkAvailable(getApplicationContext())) {
                String url = AppConfig.URLFiles + AppConfig.userId + File.separator + noteFile.getName();
                if (!url.startsWith("http://") && !url.startsWith("https://"))
                    url = "http://" + url;
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        } else if (item.getTitle() == "Upload") {
            if (!note.isUploaded()) {
                if (AppConfig.isNetworkAvailable(getApplicationContext())) {
                    ViewGroup parent = (ViewGroup) info.targetView;
                    if (gridType == AppConfig.NOTE_PICTURE_TYPE) {
                        if (noteFile.exists()) {
                            Bitmap photo = BitmapFactory.decodeFile(noteFile.getAbsolutePath());
                            PostImageAsyncTask task = new PostImageAsyncTask(this, parent, AppConfig.db, note.getId());
                            AppConfig.executeTask(task, photo);
                        }
                    } else if (gridType == AppConfig.NOTE_VIDEO_TYPE) {
                        if (noteFile.exists()) {
                            PostVideoAsyncTask task = new PostVideoAsyncTask(this, parent, AppConfig.db, note.getId());
                            AppConfig.executeTask(task, noteFile.getAbsolutePath());
                        }
                    }
                }
            }
        } else
            return false;
        return true;
    }

    private void init(final int type) {
        mGridView = (GridView) findViewById(R.id.gridView);
        ProgressBar spinner = (ProgressBar) findViewById(R.id.gridViewProgressBar);
        mVideoViw = (VideoView) findViewById(R.id.gridVideoView);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);


        setSupportActionBar(toolbar);


        registerForContextMenu(mGridView);

        final GridViewAdapter adapter = new GridViewAdapter(GridViewActivity.this, null);
        mGridView.setAdapter(adapter);

        GetItemsFromDBAsyncTask asyncTask = new GetItemsFromDBAsyncTask(GridViewActivity.this, type, adapter, spinner);
        AppConfig.executeTask(asyncTask);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (type == AppConfig.NOTE_VIDEO_TYPE) {
                    Note file = adapter.getItem(position);
                    mGridView.setVisibility(View.GONE);
                    mVideoViw.setVisibility(View.VISIBLE);

                    MediaController mediaController = new MediaController(GridViewActivity.this);
                    mVideoViw.setMediaController(mediaController);
                    mVideoViw.setVideoPath(file.getNoteName());
                    mVideoViw.start();
                    mediaShown = true;
                    shareItem.setVisible(true);
                    if (mShareActionProvider != null && AppConfig.isNetworkAvailable(getApplicationContext())) {
                        String noteFile = new File(file.getNoteName()).getName();
                        int pos = noteFile.lastIndexOf('.');
                        if(pos > 0){
                            noteFile = noteFile.substring(0, pos);
                        }
                        String url = AppConfig.URLFiles + AppConfig.userId + File.separator + AppConfig.URLVideoView + noteFile;
                        if (!url.startsWith("http://") && !url.startsWith("https://"))
                            url = "http://" + url;
                        Intent shareIntent = ShareCompat.IntentBuilder.from(GridViewActivity.this)
                                .setType("text/plain").setText(url).getIntent();

                        mShareActionProvider.setShareIntent(shareIntent);
                    }

                } else if (type == AppConfig.NOTE_PICTURE_TYPE) {
                    Intent intent = new Intent(GridViewActivity.this, ImageViewerActivity.class);
                    AppConfig.AdapterItems = adapter.getItems();
                    intent.putExtra("position", position);
                    startActivity(intent);
                }

            }
        });
    }
}
