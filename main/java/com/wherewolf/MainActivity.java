package com.wherewolf;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestAsyncTask;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.wherewolf.controls.IUpdateUI;
import com.wherewolf.controls.map.GeoPoint;
import com.wherewolf.controls.map.GeoSearch;
import com.wherewolf.controls.map.MapView;
import com.wherewolf.controls.map.TileFetcher;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends ActionBarActivity
{
    CallbackManager callbackManager;
    AccessTokenTracker accessTokenTracker;
	public static String CachePath;
    public static MapView MainMap = null;
    public static final String projectToken = "06e1ac21bb7d41d81de6d3cca4a89a80";
    GraphRequestAsyncTask graphAsync;

    public static boolean searching = false;
    public static boolean showSubmitMarkers = false;
    public static boolean showAnyMarkers = false;
    public static boolean showedBadDataMessage = false;
    public static boolean queryHasNumbers;
    public static boolean submitClicked = false;
    public static String venueName = "";
    public static String venueAddress = "";
    public static String plainAddressText = "";

    public static String primaryAddress = "";
    public static String primaryVenueAddress = "";
    public static String primaryVenueName = "";

    public static ArrayAdapter<String> searchAdapter;

    public static List<String> listToPopulate;
    public static GeoPoint markerGeoPoint = new GeoPoint (MapView.mapCenter.Latitude + 0.0001f, MapView.mapCenter.Longitude + 0.0001f);
    public static GeoPoint mapCenterPoint = new GeoPoint (MapView.mapCenter.Latitude + 0.0001f, MapView.mapCenter.Longitude + 0.0001f);
    public static GeoPoint mapCenterAtSubmitTime = new GeoPoint (MapView.mapCenter.Latitude + 0.0001f, MapView.mapCenter.Longitude + 0.0001f);
    public static HashMap<String, GeoPoint> markerLocHash;

    public static HashMap<GeoPoint, String> venueNameHash;
    public static HashMap<GeoPoint, String> venueAddressHash;
    public static HashMap<GeoPoint, String> addressHash;

    public static AsyncTask<String,Void,List> newFoursquareTask;
    public static AsyncTask<String,Void,HashMap> newSubmitTask;

    public CountDownTimer countDownTimer;
    public boolean isCountDownTimerRunning = false;

    private MixpanelAPI mixpanel;
    public static String userID = "";
    public static String userName = "";
    public static String userBirthday ="";
    public static String userEmail = "";
    public static String userGender = "";
    public static Boolean isFirstRun;

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        setIntent(intent);
    }

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
        CurrentActivity = this;
        showAnyMarkers = false;
        mixpanel = MixpanelAPI.getInstance(this, projectToken);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        MainActivity.UpdateUI(null);

                        if (graphAsync != null) {
                            graphAsync.cancel(true);
                        }
                        GraphRequest req = GraphRequest.
                                newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {

                                    userEmail = jsonObject.optString("email");
                                    userBirthday = jsonObject.optString("birthday");
                                    userGender = jsonObject.optString("gender");

                                    }
                                });
                        graphAsync = req.executeAsync();


                        Profile profile = Profile.getCurrentProfile();
                        if (profile != null) {
                            userID = profile.getId();
                            userName = profile.getName();
                        }


                        isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                                .getBoolean("isfirstrun", true);
                        if (isFirstRun) {
                            Toast.makeText(getApplicationContext(), "You're logged in!", Toast.LENGTH_LONG).show();
                            mixpanel.alias(userID, null);
                            getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putBoolean("isfirstrun", false).commit();
                        }
                        mixpanel.identify(userID);
                    }


                    @Override
                    public void onCancel() {
                        MainActivity.UpdateUI(null);
                    }

                    @Override
                    public void onError(FacebookException e) {
                        Toast.makeText(getApplicationContext(), "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                        Log.e("FBError", e.toString());
                        MainActivity.UpdateUI(null);
                    }
                }

        );

        accessTokenTracker=new

        AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged (AccessToken oldAccessToken, AccessToken
            currentAccessToken)
            {
                if (currentAccessToken != null) {
                    Log.e("", "TOKEN");
                    Log.e("", currentAccessToken.getToken());
                    Log.e("", "DONE");
                    FacebookIntegration.ValidateAccessToken();
                    MainActivity.UpdateUI(null);
                }
            }
        };

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        CachePath = getCacheDir().getAbsolutePath();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if(savedInstanceState==null)

        {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        ActionBar Action = getSupportActionBar();
        Action.setElevation(0f);
        Action.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar));
        Action.setDisplayShowTitleEnabled(false);
        Action.setDisplayShowHomeEnabled(true);
        Action.setLogo(R.mipmap.actionbar_logo);

        UpdateUI(null);

        Intent i = new Intent(getApplicationContext(), MainService.class);

        getApplicationContext().startService(i);
    }


    @Override
    protected void onDestroy() {
        mixpanel.flush();
        super.onDestroy();
        accessTokenTracker.stopTracking();
        showAnyMarkers = false;
        showSubmitMarkers = false;
        submitClicked = false;
    }

    @Override
    protected void onResume()
    {
        mixpanel.getPeople().identify(userID);
        mixpanel.getPeople().set("$name", userName);
        mixpanel.getPeople().set("gender", userGender);
        mixpanel.track("On Resume", null);
        mixpanel.flush();
        Log.i("MainActivity", "OnResume");

        ProcessIntent();
        super.onResume();
        UpdateUI(null);
    }

    public void ProcessIntent()
    {
        if(getIntent().hasExtra("com.wherewolf.FeatureID"))
        {
            Log.i("MainActivity", "OnResume has Extras");

            int FeatureID = getIntent().getIntExtra("com.wherewolf.FeatureID", -1);
            if(FeatureID != -1 && MainMap != null)
            {
                float Latitude = getIntent().getFloatExtra("com.wherewolf.Latitude", 0);
                float Longitude = getIntent().getFloatExtra("com.wherewolf.Longitude", 0);
                long UnixStamp = getIntent().getLongExtra("com.wherewolf.UnixStamp", 0);
                if(UnixStamp != 0)
                {
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor edit = settings.edit();
                    if(UnixStamp > MainServiceThread.LastUpdateSaved)
                    {
                        edit.putLong("LastUpdate", UnixStamp);
                        edit.apply();
                        MainServiceThread.LastUpdateSaved = UnixStamp;
                    }
                }
                MainMap.IntentFeatureComments(FeatureID, Latitude, Longitude);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        UpdateUI(null);
    }


    AsyncTask foursquareAsync;
    AsyncTask submitAsync;

    private CountDownTimer timedSearchQuery(long timeInMillis, final String searchFor){
        countDownTimer = new CountDownTimer(timeInMillis, timeInMillis/5) {
            @Override
            public void onTick(long millisUntilFinished) {
            }
            @Override
            public void onFinish() {

                newFoursquareTask = new AsyncTask<String, Void, List>() {
                    @Override
                    protected List doInBackground(String... params) {
                        System.out.println("In DIB");
                        if (foursquareAsync!=null) {
                            Context context = getApplicationContext();

                            // If query contains numbers use Mapbox to find address
                            if (params[0].matches("[a-zA-Z ]*\\d+.*")){
                                queryHasNumbers = true;
                                listToPopulate = GeoSearch.addressListBuilder(context, params[0]);

                            // Else use Foursquare to find venue
                            } else {
                                queryHasNumbers = false;
                                listToPopulate = GeoSearch.venueListBuilder(context, params[0]);
                                venueNameHash = GeoSearch.venueNameHash;
                                venueAddressHash = GeoSearch.venueAddressHash;
                            }
                        }
                        return listToPopulate;
                    }

                    @Override
                    protected void onPostExecute(List listToPopulate) {
                        super.onPostExecute(listToPopulate);

                        if ((!submitClicked && listToPopulate != null) ||
                                (showSubmitMarkers && listToPopulate != null)){

                            searchAdapter.clear();
                            searchAdapter.addAll(listToPopulate);

                            View searchListView = findViewById(R.id.listview_search);
                            searchListView.bringToFront();
                            searchListView.setVisibility(View.VISIBLE);
                        }
                    }
                };
                    foursquareAsync = newFoursquareTask.execute(searchFor);
            }
        };

        countDownTimer.start();
        isCountDownTimerRunning = true;

        return countDownTimer;
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.actionbar, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setQueryHint("POIs, Addresses");
        if (null != searchView) {
            searchView.setSearchableInfo(searchManager
                    .getSearchableInfo(getComponentName()));
        }


        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {
                if(isCountDownTimerRunning)                {
                    countDownTimer.cancel();
                }
                if (!newText.isEmpty()){
                    countDownTimer = timedSearchQuery(350, newText);
                }
                return false;
            }



            public boolean onQueryTextSubmit(final String query) {
                // Get the value "query" which is entered in the search box.
                Context context = getApplicationContext();

                System.out.print("the query is: " + query);
                invalidateOptionsMenu();

                newSubmitTask = new AsyncTask<String, Void, HashMap>() {
                    @Override
                    protected HashMap doInBackground(String... params) {
                        System.out.println("In DIB");
                        if (submitAsync!=null) {
                            Context context = getApplicationContext();

                            // If query contains numbers use Mapbox to find address
                            if (params[0].matches("[a-zA-Z ]*\\d+.*")){
                                GeoSearch.addressListBuilder(context, params[0]);
                                markerLocHash = GeoSearch.addressHash;
                                mapCenterPoint = GeoSearch.primaryAddressPoint;
                                primaryAddress = GeoSearch.primaryAddress;

                                primaryVenueAddress = "";
                                primaryVenueName = "";
                            // Else use Foursquare to get venue
                            } else {
                                GeoSearch.venueListBuilder(context, params[0]);
                                markerLocHash = GeoSearch.venueHash;
                                mapCenterPoint = GeoSearch.primaryVenuePoint;
                                primaryVenueAddress = GeoSearch.primaryVenueAddress;
                                primaryVenueName = GeoSearch.primaryVenueName;
                                primaryAddress = "";

                            }
                            mapCenterAtSubmitTime = MapView.mapCenter;
                        }

                        searching = true;
                        showAnyMarkers = true;
                        showSubmitMarkers = true;
                        submitClicked = true;
                        showedBadDataMessage = false;

                        return markerLocHash;
                    }

                    @Override
                    protected void onPostExecute(HashMap map) {
                        super.onPostExecute(map);

                        searchView.setVisibility(View.GONE);
                        searchView.invalidate();
                        searchView.setQuery("", false);


                        View searchListView = findViewById(R.id.listview_search);
                        searchAdapter.clear();
                        searchListView.setVisibility(View.GONE);
                    }
                };

                submitAsync = newSubmitTask.execute(query);

                return false;
            }
        };

        searchView.setOnQueryTextListener(queryTextListener);
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

	public static class PlaceholderFragment extends Fragment
	{

		public PlaceholderFragment()
		{
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState)
		{
			final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            String[] fakePlaceArray = {
                    "Joe's Spot - Atlas Street, Brooklyn, NY",
                    "Yummy's Hot - Jamima Place, New York, NY",
                    "Bro's Cot - Asshole Avenue, New York, NY",
                    "Gentleman's - Wherewolf Way, Wolftown, WI"
            };
            // Initialize adapter
            List<String> listOfFakePlaces = new ArrayList<String>(Arrays.asList(fakePlaceArray));

            searchAdapter = new ArrayAdapter<String>(
                    getActivity(),
                    R.layout.list_search_item,
                    R.id.search_item_textview,
                    listOfFakePlaces
            );
            listToPopulate = new ArrayList<String>();
            final ListView searchListView = (ListView) rootView.findViewById(R.id.listview_search);
            searchListView.setAdapter(searchAdapter);
            searchListView.bringToFront();
            searchListView.setVisibility(View.GONE);


			searchListView.setOnKeyListener(new View.OnKeyListener()
			{
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event)
				{
					if (keyCode == KeyEvent.KEYCODE_BACK)
					{
						if (searchListView.getVisibility() == View.VISIBLE)
						{
							searchListView.setVisibility(View.GONE);
						}
						return true;
					}
					return false;
				}
			});

					searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener()

				{
					@Override
					public void onItemClick (AdapterView < ? > adapter, View view, int position,
					long id)
					{
						Context context = getActivity();
						String clickedItemString = adapter.getItemAtPosition(position).toString();

						if (queryHasNumbers)
						{
							markerLocHash = GeoSearch.addressHash;
							markerGeoPoint = markerLocHash.get(clickedItemString);
							venueName = "";
							venueAddress = "";
						}
						else
						{
							markerLocHash = GeoSearch.venueHash;
							markerGeoPoint = markerLocHash.get(clickedItemString);
							venueName = venueNameHash.get(markerGeoPoint);
							venueAddress = venueAddressHash.get(markerGeoPoint);
						}
						plainAddressText = clickedItemString;
						searching = true;
						showAnyMarkers = true;
						showSubmitMarkers = false;
						submitClicked = false;
						searchListView.setVisibility(View.GONE);
                }
            });



            // Remove keyboard when user scrolls listview
            searchListView.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    try
                    {
                        InputMethodManager input = (InputMethodManager) getActivity()
                                .getSystemService(Activity.INPUT_METHOD_SERVICE);
                        input.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    return false;
                }
            });


            ImageView youAreHere = (ImageView) rootView.findViewById(R.id.youAreHere);
            youAreHere.bringToFront();

            // Load "You are here" animation
            Animation animFadeout = AnimationUtils.loadAnimation(getActivity(),
                    R.anim.you_are_here);

            youAreHere.startAnimation(animFadeout);
            youAreHere.setVisibility(View.INVISIBLE);

			return rootView;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public static MainActivity CurrentActivity;
    public static void UpdateUI(ViewGroup group)
    {
        if(group == null)
        {
            final View v = CurrentActivity.findViewById(R.id.MainMap);
            if(v == null)
                return;

            if(v instanceof ViewGroup)
            {
                CurrentActivity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run() {
                        UpdateUI((ViewGroup)v);
                    }
                });
                return;
            }
        }
        Log.v("ViewEnum", group.toString());
        for(int i=0 ; i<group.getChildCount() ; i++)
        {
            View child = group.getChildAt(i);
            if(child instanceof IUpdateUI)
            {
                Log.v("Child Update", child.toString());
                IUpdateUI ChildUI = (IUpdateUI)child;
                ChildUI.UpdateUI();
            }
            if(child instanceof ViewGroup)
            {
                UpdateUI((ViewGroup)child);
            }
        }
    }
}