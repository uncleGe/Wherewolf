package com.wherewolf.features.ui;

import android.content.Context;

import com.wherewolf.controls.map.FeatureIcons;
import com.wherewolf.features.FeatureType;

/**
 * Created by Greg on 1/5/14.
 */
public class NewFeatureDialog_Alert extends NewFeatureDialogBase
{
	public NewFeatureDialog_Alert(Context context)
	{
		super(context);
        NewFeatureIcon1.setImageBitmap(FeatureIcons.GetIcon(FeatureType.Alert, TitleIconSize));
        Title.setText("New Alert");
		FeatureText.setHint("What would you like to alert everyone to?");
		NewFeatureIcon2.setImageBitmap(FeatureIcons.GetIcon(FeatureType.Alert, TitleIconSize));

	}
}
