package com.wherewolf.features.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.wherewolf.MainActivity;
import com.wherewolf.R;
import com.wherewolf.controls.map.MapView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Greg on 1/5/14.
 */
public class NewFeatureDialogBase extends Dialog implements View.OnClickListener, DatePickerDialog.OnDateSetListener
{
    public int TitleIconSize = 96;
    ImageView NewFeatureIcon1;
	ImageView NewFeatureIcon2;
    TextView Title;
	View DateStart;
	TextView DateStartText;
	Button Post;
	TextView FeatureText;
	private MixpanelAPI mixpanel;


	public NewFeatureDialogBase(Context context)
	{
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.dialog_newfeature);

        NewFeatureIcon1 = (ImageView)this.findViewById(R.id.NewFeatureDialog_NewFeatureIcon1);
		NewFeatureIcon2 = (ImageView)this.findViewById(R.id.NewFeatureDialog_NewFeatureIcon2);

		Title = (TextView)this.findViewById(R.id.NewFeatureDialog_Title);

		DateStart = this.findViewById(R.id.DateStart);
		DateStart.setOnClickListener(this);

		DateStartText = (TextView)findViewById(R.id.NewFeature_DateStartText);

		Post = (Button)findViewById(R.id.NewFeatureDialog_Post);
		Post.setOnClickListener(this);

		FeatureText = (TextView)findViewById(R.id.NewFeatureDialog_FeatureText);
	}

	@Override
	public void onDateSet(DatePicker datePicker, int year, int month, int day)
	{
			Calendar Picked = Calendar.getInstance();
			Picked.set(year, month, day);
			SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy");
			DateStartText.setText(sdf.format(Picked.getTime()));
	}

	@Override
	public void onClick(View view)
	{
		if(view == DateStart)
		{
			DatePickerDialog d = new DatePickerDialog(getContext(), this, 2011, 1, 1);
			d.show();
		}
		else if(view == Post)
		{
			Map.NewFeature.Text = FeatureText.getText().toString();
			if(Map.NewFeature.Text.equalsIgnoreCase(""))
			{
				Toast.makeText(getContext(), "You didn't type anything!", Toast.LENGTH_SHORT).show();
				return;
			}
			hide();

			mixpanel = MixpanelAPI.getInstance(getContext(), MainActivity.projectToken);
			mixpanel.getPeople().identify(MainActivity.userID);
			mixpanel.track("New Post", null);
			mixpanel.flush();

			Map.NewFeaturePost();
		}
	}

	private MapView Map;
	public void setMap(MapView map)
	{
		 Map = map;
	}
}
