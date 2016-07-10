package com.wherewolf.features.ui;

import android.content.Context;

import com.wherewolf.controls.map.FeatureIcons;
import com.wherewolf.features.FeatureType;

/**
 * Created by Greg on 1/5/14.
 */
public class NewFeatureDialog_ThingToDo extends NewFeatureDialogBase
{
	public NewFeatureDialog_ThingToDo(Context context)
	{
		super(context);
        NewFeatureIcon1.setImageBitmap(FeatureIcons.GetIcon(FeatureType.ThingToDo, TitleIconSize));
        Title.setText("New Thing to Do");
		FeatureText.setHint("Tell everyone about something to do!");
		NewFeatureIcon2.setImageBitmap(FeatureIcons.GetIcon(FeatureType.ThingToDo, TitleIconSize));
	}
}
