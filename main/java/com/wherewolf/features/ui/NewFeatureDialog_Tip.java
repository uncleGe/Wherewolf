package com.wherewolf.features.ui;

import android.content.Context;

import com.wherewolf.controls.map.FeatureIcons;
import com.wherewolf.features.FeatureType;

/**
 * Created by Greg on 1/5/14.
 */
public class NewFeatureDialog_Tip extends NewFeatureDialogBase
{
	public NewFeatureDialog_Tip(Context context)
	{
		super(context);
        NewFeatureIcon1.setImageBitmap(FeatureIcons.GetIcon(FeatureType.Tip, TitleIconSize));
        Title.setText("New Tip");
		FeatureText.setHint("Give everyone a tip!");
		NewFeatureIcon2.setImageBitmap(FeatureIcons.GetIcon(FeatureType.Tip, TitleIconSize));

	}
}
