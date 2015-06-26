package com.belzeke.notepad.Activities;

import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.belzeke.notepad.Asyncs.PostAudioAsyncTask;
import com.belzeke.notepad.Asyncs.PostVideoAsyncTask;
import com.belzeke.notepad.Config.AppConfig;
import com.belzeke.notepad.Helper.ProfileNavigationDrawerFragment;
import com.belzeke.notepad.Helper.SQLiteHandler;
import com.belzeke.notepad.Helper.SessionManager;
import com.belzeke.notepad.Listeners.archiveOnClickListener;
import com.belzeke.notepad.Listeners.audioOnClickListener;
import com.belzeke.notepad.Listeners.photoOnClickListener;
import com.belzeke.notepad.Listeners.textOnClickListener;
import com.belzeke.notepad.Listeners.videoOnClickListener;
import com.belzeke.notepad.R;
import com.belzeke.notepad.Services.AudioRecorderService;

import java.io.File;

public class MainScreenActivity extends AppCompatActivity {

    private final String TAG = MainScreenActivity.class.getSimpleName();

    private RelativeLayout videoButton;
    private RelativeLayout photoButton;
    private RelativeLayout audioButton;
    private RelativeLayout noteButton;

    private TextView noteArchive;
    private TextView pictureArchive;
    private TextView audioArchive;
    private TextView videoArchive;

    private RoundCornerProgressBar photoProgress;
    private RoundCornerProgressBar videoProgress;


    private SQLiteHandler db;
    private SessionManager session;
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.MainScreenTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        init();
    }

    private void init() {
        videoButton = (RelativeLayout) findViewById(R.id.video_button);
        photoButton = (RelativeLayout) findViewById(R.id.photo_button);
        noteButton = (RelativeLayout) findViewById(R.id.note_button);
        audioButton = (RelativeLayout) findViewById(R.id.audio_button);
        photoProgress = (RoundCornerProgressBar) findViewById(R.id.photoProgress);
        videoProgress = (RoundCornerProgressBar) findViewById(R.id.video_progress);
        noteArchive = (TextView) findViewById(R.id.note_archive);
        pictureArchive = (TextView) findViewById(R.id.picture_archive);
        audioArchive = (TextView) findViewById(R.id.audio_archive);
        videoArchive = (TextView) findViewById(R.id.video_archive);

        db = new SQLiteHandler(getApplicationContext());
        AppConfig.db = db;

        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        String title = "<b>BE</b><small><font color='#D3D3D3'>lezka</font></small>";
        toolbar.setTitle(Html.fromHtml(title));

        setSupportActionBar(toolbar);

        ProfileNavigationDrawerFragment drawerFragment = (ProfileNavigationDrawerFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp((DrawerLayout)findViewById(R.id.drawerLayout), toolbar, session);

        videoButton.setOnClickListener(new videoOnClickListener(this));
        photoButton.setOnClickListener(new photoOnClickListener(this));
        audioButton.setOnClickListener(new audioOnClickListener(this));
        noteButton.setOnClickListener(new textOnClickListener(this));

        noteArchive.setOnClickListener(new archiveOnClickListener(this, AppConfig.NOTE_TEXT_TYPE));
        pictureArchive.setOnClickListener(new archiveOnClickListener(this, AppConfig.NOTE_PICTURE_TYPE));
        audioArchive.setOnClickListener(new archiveOnClickListener(this, AppConfig.NOTE_AUDIO_TYPE));
        videoArchive.setOnClickListener(new archiveOnClickListener(this, AppConfig.NOTE_VIDEO_TYPE));


        final TextView timer = (TextView) findViewById(R.id.audio_timer);
        receiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                String result = intent.getStringExtra(AudioRecorderService.SERVICE_MESSAGE);
                if(result.equals("")){
                    RoundCornerProgressBar progressBar = (RoundCornerProgressBar) findViewById(R.id.audio_progress);
                    long noteId = db.addNote(AppConfig.NOTE_AUDIO_TYPE, AppConfig.LastFilePathCreated, "");
                    if(AppConfig.isNetworkAvailable(MainScreenActivity.this)){
                        PostAudioAsyncTask asyncTask = new PostAudioAsyncTask(MainScreenActivity.this, progressBar, db, noteId);
                        AppConfig.executeTask(asyncTask);
                    }
                }
                timer.setText(intent.getStringExtra(AudioRecorderService.SERVICE_MESSAGE));
            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(AudioRecorderService.SERVICE_RESULT));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(MainScreenActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_screen, menu);

        final MenuItem searchItem = menu.findItem(R.id.Search);

        SearchManager searchManager = (SearchManager)getSystemService(SEARCH_SERVICE);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchManager.getSearchableInfo(getComponentName());
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //adapter.getFilter().filter(s);
                RelativeLayout test = (RelativeLayout) findViewById(R.id.search_view);
                if(s.isEmpty()) {
                    test.setVisibility(View.GONE);
                }else{
                    test.setVisibility(View.VISIBLE);
                    test.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down));
                }
                return true;
            }
        });
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == AppConfig.CAMERA_REQUEST && resultCode == RESULT_OK){

            File pic = new File(AppConfig.LastFilePathCreated);
            if(pic.exists()){
                Intent intent = new Intent(MainScreenActivity.this, HashTagNoteActivity.class);
                intent.putExtra(AppConfig.NoteType, AppConfig.NOTE_PICTURE_TYPE);
                startActivity(intent);
                /*
                long noteId = db.addNote(AppConfig.NOTE_PICTURE_TYPE, pic.getAbsolutePath(), "");

                if(AppConfig.isNetworkAvailable(getApplicationContext())) {
                    Bitmap photo = BitmapFactory.decodeFile(pic.getAbsolutePath());

                    PostImageAsyncTask task = new PostImageAsyncTask(this, photoProgress, db, noteId);
                    AppConfig.executeTask(task, photo);
                }*/
            }

        }
        if(requestCode == AppConfig.VIDEO_REQUEST && resultCode == Activity.RESULT_OK){
            long noteId = db.addNote(AppConfig.NOTE_VIDEO_TYPE, new File(AppConfig.LastFilePathCreated).getAbsolutePath(), "");
            if(AppConfig.isNetworkAvailable(getApplicationContext())){
                PostVideoAsyncTask task = new PostVideoAsyncTask(this, videoProgress, db, noteId);
                AppConfig.executeTask(task, AppConfig.LastFilePathCreated);
            }
        }
    }
}
