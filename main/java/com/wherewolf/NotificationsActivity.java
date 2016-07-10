package com.wherewolf;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.wherewolf.controls.map.TileFetcher;

/**
 * Created by Greg on 08/23/15.
 */
public class NotificationsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new NotificationsFragment())
                    .commit();
        }

        ActionBar Action = getSupportActionBar();
        Action.setElevation(0f);
        Action.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar));
        Action.setDisplayShowTitleEnabled(false);
        Action.setDisplayShowHomeEnabled(false);
        Action.setLogo(R.mipmap.actionbar_logo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.actionbar, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setQueryHint("POIs, Addresses");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks
        switch (item.getItemId())
        {
            case R.id.login_logout:
                return true;
            case R.id.action_map_type:
                TileFetcher.Aerial = !TileFetcher.Aerial;
                return true;
            case R.id.help_item:
                startActivity(new Intent(this, HelpActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class NotificationsFragment extends Fragment {

        private static final String LOG_TAG = NotificationsFragment.class.getSimpleName();

        public NotificationsFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_notifications, container, false);

            return rootView;
        }
    }
}