package com.belzeke.notepad.Helper;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.belzeke.notepad.Activities.LoginActivity;
import com.belzeke.notepad.Config.AppConfig;
import com.belzeke.notepad.R;

/**
 * Created by marko on 18.6.2015.
 */
public class ProfileNavigationDrawerFragment extends Fragment {
    private DrawerLayout mDrawerLayout;
    private boolean mDrawerOpend = false;

    public ProfileNavigationDrawerFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.navigation_drawer_profile, container, false);
    }

    public void setUp(DrawerLayout drawerLayout, final Toolbar toolbar, final SessionManager session){
        mDrawerLayout = drawerLayout;
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                if(slideOffset >= 0.9f)
                    mDrawerOpend = true;
                else if(slideOffset <= 0.1f)
                    mDrawerOpend = false;
            }
        };
        mDrawerLayout.setDrawerListener(drawerToggle);


        Button logOutButton = (Button) mDrawerLayout.findViewById(R.id.profile_button_log_out);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                session.setLogin(false);
                AppConfig.db.deleteUsers();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                getActivity().startActivity(intent);
                getActivity().finish();
            }
        });


        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDrawerOpend) {
                    mDrawerLayout.closeDrawers();
                    mDrawerOpend = !mDrawerOpend;
                    AppConfig.NavigationShown = false;
                } else {
                    mDrawerLayout.openDrawer(Gravity.LEFT);
                    mDrawerOpend = !mDrawerOpend;
                    AppConfig.NavigationShown = true;
                }
            }
        });
    }
}
