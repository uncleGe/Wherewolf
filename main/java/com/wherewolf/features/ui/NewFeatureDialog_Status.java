package com.wherewolf.features.ui;

import android.content.Context;

import com.wherewolf.controls.map.FeatureIcons;
import com.wherewolf.features.FeatureType;

/**
 * Created by Greg on 1/5/14.
 */
public class NewFeatureDialog_Status extends NewFeatureDialogBase
{
	public NewFeatureDialog_Status(Context context)
	{
		super(context);
        NewFeatureIcon1.setImageBitmap(FeatureIcons.GetIcon(FeatureType.Status, TitleIconSize));
        Title.setText("New Status");
		FeatureText.setHint("Tell everyone your status!");
		NewFeatureIcon2.setImageBitmap(FeatureIcons.GetIcon(FeatureType.Status, TitleIconSize));
	}
}
