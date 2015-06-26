package com.belzeke.notepad.Activities;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.belzeke.notepad.Adapters.ListViewAdapter;
import com.belzeke.notepad.Asyncs.GetItemsFromDBAsyncTask;
import com.belzeke.notepad.Config.AppConfig;
import com.belzeke.notepad.R;

public class ListViewActivity extends AppCompatActivity {

    private int gridType;
    private ProgressBar spinner;
    private ListView listView;
    private ListViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        gridType = intent.getIntExtra(AppConfig.NoteType, AppConfig.MEDIA_TYPE_AUDIO);
        if(gridType == AppConfig.NOTE_AUDIO_TYPE)
            setTheme(R.style.AudioTheme);
        else if(gridType == AppConfig.NOTE_TEXT_TYPE)
            setTheme(R.style.TextTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);


        init(gridType);

    }

    private void init(int type) {
        listView = (ListView)findViewById(R.id.listView);
        spinner = (ProgressBar)findViewById(R.id.listViewProgressBar);
        registerForContextMenu(listView);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        adapter = new ListViewAdapter(this, null, type);
        listView.setAdapter(adapter);

        GetItemsFromDBAsyncTask asyncTask = new GetItemsFromDBAsyncTask(ListViewActivity.this, type, adapter, spinner);
        AppConfig.executeTask(asyncTask);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_view, menu);

        MenuItem searchItem = menu.findItem(R.id.Search);

        SearchManager searchManager = (SearchManager)getSystemService(SEARCH_SERVICE);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
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
                adapter.getFilter().filter(s);
                return true;
            }
        });
        return true;
    }
}
