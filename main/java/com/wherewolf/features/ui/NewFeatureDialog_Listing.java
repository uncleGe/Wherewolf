package com.wherewolf.features.ui;

import android.content.Context;

import com.wherewolf.controls.map.FeatureIcons;
import com.wherewolf.features.FeatureType;

/**
 * Created by Greg on 1/5/14.
 */
public class NewFeatureDialog_Listing extends NewFeatureDialogBase
{
	public NewFeatureDialog_Listing(Context context)
	{
		super(context);
        NewFeatureIcon1.setImageBitmap(FeatureIcons.GetIcon(FeatureType.Listing, TitleIconSize));
        Title.setText("New Listing");
		FeatureText.setHint("What would you like to list?");
		NewFeatureIcon2.setImageBitmap(FeatureIcons.GetIcon(FeatureType.Listing, TitleIconSize));

	}
}
