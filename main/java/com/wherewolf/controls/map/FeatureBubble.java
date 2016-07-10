package com.wherewolf.controls.map;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;
import com.wherewolf.R;
import com.wherewolf.features.Feature;
import com.wherewolf.features.ui.FeatureCommentsDialog;

/**
 * Created by Greg on 1/7/14.
 */
public class FeatureBubble extends RelativeLayout implements View.OnClickListener
{
    public ProfilePictureView PictureView = null;
	public TextView FeatureTitle = null;
    public TextView FeatureCreator = null;
    public TextView FeatureAge = null;
	public TextView FeatureText = null;
    public Button CommentsButton = null;
    public Feature CurrentFeature;

	public FeatureBubble(Context context)
	{
		super(context);
		Initialize();
	}

	public FeatureBubble(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		Initialize();
	}

	public FeatureBubble(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		Initialize();
	}

	private void Initialize()
	{
		LayoutInflater i = LayoutInflater.from(getContext());
		View v = i.inflate(R.layout.feature_bubble, this);
		v.setVisibility(View.INVISIBLE);

		FeatureTitle = (TextView)findViewById(R.id.Bubble_Feature_Title);
        FeatureCreator = (TextView)findViewById(R.id.Bubble_FeatureCreator);
		FeatureText = (TextView)findViewById(R.id.Bubble_FeatureText);
        FeatureAge = (TextView)findViewById(R.id.Bubble_FeatureAge);

        CommentsButton = (Button)findViewById(R.id.FeatureBubble_Comments);
        CommentsButton.setOnClickListener(this);

        PictureView = (ProfilePictureView)findViewById(R.id.Bubble_Picture);
        PictureView.setCropped(true);
        PictureView.setPresetSize(PictureView.SMALL);
	}

    @Override
    public void onClick(View v)
    {
        if(v == CommentsButton)
        {
            FeatureCommentsDialog FeatureComments = new FeatureCommentsDialog(getContext(), CurrentFeature);
            FeatureComments.show();
        }
    }
}
