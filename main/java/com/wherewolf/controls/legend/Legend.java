package com.wherewolf.controls.legend;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.widget.LoginButton;
import com.wherewolf.FacebookIntegration;
import com.wherewolf.R;
import com.wherewolf.controls.IUpdateUI;
import com.wherewolf.controls.map.FeatureIcons;
import com.wherewolf.controls.map.MapState;
import com.wherewolf.controls.map.MapView;
import com.wherewolf.features.Feature;
import com.wherewolf.features.FeatureType;

/**
 * Created by Greg on 1/1/14.
 */
public class Legend extends TableLayout implements View.OnClickListener, GestureDetector.OnGestureListener, IUpdateUI
{
	private MapView Map;
	private GestureDetector Detector = null;
    boolean IconsVisible = false;

	public Legend(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		Initialize(context);
	}

	public Legend(Context context, MapView map)
	{
		super(context);
		Map = map;
		Initialize(context);
	}

	private Button PostSomething;
	private TableRow IconsRow;
	private TableRow LabelRow;
	private ImageButton StatusButton;
	private ImageButton ThingToDoButton;
	private ImageButton TipButton;
	private ImageButton QuestionButton;
	private ImageButton ListingButton;
	private ImageButton AlertButton;
    private LoginButton Login;

	private boolean IsOpen = false;

	public void Initialize(Context context)
	{
		if(Detector == null)
		{
			Detector = new GestureDetector(getContext(), this);
		}

		LayoutInflater i = LayoutInflater.from(getContext());
		View v = i.inflate(R.layout.legend, this);
		IconsRow = (TableRow)findViewById(R.id.IconRow);
		int bSize = 90;

        if(isInEditMode())
            return;

		LabelRow = (TableRow)findViewById(R.id.LabelRow);

		PostSomething = (Button)findViewById(R.id.PostSomething);
		PostSomething.setOnClickListener(this);

		StatusButton = (ImageButton)findViewById(R.id.StatusButton);
		StatusButton.setOnClickListener(this);
		StatusButton.setImageBitmap(FeatureIcons.GetIcon(FeatureType.Status, bSize));

		ThingToDoButton = (ImageButton)findViewById(R.id.ThingToDoButton);
		ThingToDoButton.setOnClickListener(this);
		ThingToDoButton.setImageBitmap(FeatureIcons.GetIcon(FeatureType.ThingToDo, bSize));

		TipButton = (ImageButton)findViewById(R.id.TipButton);
		TipButton.setOnClickListener(this);
		TipButton.setImageBitmap(FeatureIcons.GetIcon(FeatureType.Tip, bSize));

		QuestionButton = (ImageButton)findViewById(R.id.QuestionButton);
		QuestionButton.setOnClickListener(this);
		QuestionButton.setImageBitmap(FeatureIcons.GetIcon(FeatureType.Question, bSize));

		ListingButton = (ImageButton)findViewById(R.id.ListingButton);
		ListingButton.setOnClickListener(this);
		ListingButton.setImageBitmap(FeatureIcons.GetIcon(FeatureType.Listing, bSize));

		AlertButton = (ImageButton)findViewById(R.id.AlertButton);
		AlertButton.setOnClickListener(this);
		AlertButton.setImageBitmap(FeatureIcons.GetIcon(FeatureType.Alert, bSize));

        Login = (LoginButton)findViewById(R.id.login_button);
        Login.setReadPermissions("public_profile", "email", "user_friends");

	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		return Detector.onTouchEvent(event);
	}

	public void AddFeature(TableRow iRow, TableRow lRow, FeatureType type, String text)
	{
		ImageButton iButton = new ImageButton(getContext());
		iButton.setTag(type);
		iButton.setImageBitmap(FeatureIcons.GetIcon(type));
		iButton.setOnClickListener(this);
		iRow.addView(iButton);

		TextView v = new TextView(getContext());
		v.setText(text);
		lRow.addView(v);
	}

	@Override
	public void onClick(View view)
	{
		if(view == PostSomething)
		{

			IsOpen = !IsOpen;
			if(IsOpen)
			{
                IconsVisible = true;
				PostSomething.setText("Post Something");
				PostSomething.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.menu_arrow_down, 0, R.mipmap.menu_arrow_down, 0);
			}
			else
			{
                IconsVisible = false;
				PostSomething.setText("Happening Nearby");
				PostSomething.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.menu_arrow_up, 0, R.mipmap.menu_arrow_up, 0);
			}
            UpdateUI();
		}
		else if(view == StatusButton || view == ThingToDoButton || view == TipButton || view ==  QuestionButton || view == ListingButton || view == AlertButton)
		{

			Map.NewFeature = new Feature();

			if(view == StatusButton)
				Map.NewFeature.Type = FeatureType.Status;
			else if(view == ThingToDoButton)
				Map.NewFeature.Type = FeatureType.ThingToDo;
			else if(view == TipButton)
				Map.NewFeature.Type = FeatureType.Tip;
			else if(view == QuestionButton)
				Map.NewFeature.Type = FeatureType.Question;
			else if(view == ListingButton)
				Map.NewFeature.Type = FeatureType.Listing;
			else if(view == AlertButton)
				Map.NewFeature.Type = FeatureType.Alert;

			Map.SetCenterBitmap(FeatureIcons.GetIcon(Map.NewFeature.Type, 115), 115);
			Map.State = MapState.SettingPostPosition;

			Toast.makeText(getContext(), "Position your marker, then tap it!", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public boolean onDown(MotionEvent motionEvent)
	{
		return false;
	}

	@Override
	public void onShowPress(MotionEvent motionEvent)
	{

	}

	@Override
	public boolean onSingleTapUp(MotionEvent motionEvent)
	{
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2)
	{
		return false;
	}

	@Override
	public void onLongPress(MotionEvent motionEvent)
	{

	}

	@Override
	public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2)
	{
		return true;
	}

    @Override
    public void UpdateUI()
    {
        if(FacebookIntegration.IsLoggedIn())
        {
            Login.setVisibility(GONE);
            if(IconsVisible)
            {
                IconsRow.setVisibility(View.VISIBLE);
                LabelRow.setVisibility(View.VISIBLE);
            }
            else
            {
                IconsRow.setVisibility(View.GONE);
                LabelRow.setVisibility(View.GONE);
            }
        }
        else
        {
            Login.setVisibility(VISIBLE);
            IconsRow.setVisibility(View.GONE);
            LabelRow.setVisibility(View.GONE);
        }

    }
}