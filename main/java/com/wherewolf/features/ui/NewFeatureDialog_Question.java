package com.wherewolf.features.ui;

import android.content.Context;

import com.wherewolf.controls.map.FeatureIcons;
import com.wherewolf.features.FeatureType;

/**
 * Created by Greg on 1/5/14.
 */
public class NewFeatureDialog_Question extends NewFeatureDialogBase
{
	public NewFeatureDialog_Question(Context context)
	{
		super(context);
        NewFeatureIcon1.setImageBitmap(FeatureIcons.GetIcon(FeatureType.Question, TitleIconSize));
        Title.setText("New Question");
		FeatureText.setHint("Ask everyone a question!");
		NewFeatureIcon2.setImageBitmap(FeatureIcons.GetIcon(FeatureType.Question, TitleIconSize));
	}
}
