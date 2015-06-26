package com.belzeke.notepad.Activities;

import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.Toast;

import com.belzeke.notepad.Helper.CameraSurfaceView;
import com.belzeke.notepad.R;

import java.util.List;

public class CameraTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        CameraSurfaceView surface_view = new CameraSurfaceView(this);
        setContentView(surface_view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        ActionBar toolbar = getSupportActionBar();
        if(toolbar != null) {
            toolbar.setDisplayShowHomeEnabled(false);
            toolbar.setBackgroundDrawable(new ColorDrawable(R.color.videoColor));
            View appBar = getLayoutInflater().inflate(R.layout.video_app_bar, null);
            toolbar.setCustomView(appBar);
            toolbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

            final Spinner spinner = (Spinner) appBar.findViewById(R.id.cameraResolutions);
            ImageButton changeCamera = (ImageButton) appBar.findViewById(R.id.changeCamera);
            changeCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getApplicationContext(), "Change Camera", Toast.LENGTH_LONG).show();
                }
            });

            List<Size> sizes = surface_view.getmSupportedPreviewSizes();
            String[] resolutions = new String[sizes.size()];
            for (int i = 0; i < sizes.size(); i++) {
                resolutions[i] = sizes.get(i).width + "x" + sizes.get(i).height;
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, resolutions);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    String resolution = (String) spinner.getItemAtPosition(position);
                    Toast.makeText(getApplicationContext(), resolution, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
    }

}
