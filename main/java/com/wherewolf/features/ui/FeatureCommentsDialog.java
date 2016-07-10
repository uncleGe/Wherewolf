package com.wherewolf.features.ui;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wherewolf.FacebookIntegration;
import com.wherewolf.R;
import com.wherewolf.controls.map.MapView;
import com.wherewolf.features.Feature;
import com.wherewolf.features.FeatureThread;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Greg on 3/17/2015.
 */
public class FeatureCommentsDialog extends Dialog implements View.OnClickListener
{
    TextView Comments;
    Button PostComment;
    EditText NewCommentText;
    Feature CurrentFeature;

    public FeatureCommentsDialog(Context context, Feature currentFeature)
    {
        super(context);
        CurrentFeature = currentFeature;

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.dialog_comments);

        PostComment = (Button)this.findViewById(R.id.FeatureComments_PostComment);
        PostComment.setOnClickListener(this);

        Comments = (TextView)findViewById(R.id.FeatureComments_Comments);

        NewCommentText = (EditText)findViewById(R.id.FeatureComments_NewCommentText);

        CurrentFeature.LoadComments();
        Comments.setText("");
        for(int i=0 ; i<CurrentFeature.Comments.size() ; i++)
        {
            Comments.setText(Comments.getText() + CurrentFeature.Comments.get(i).CommentText + "\r\n");
        }

        ListView lv = (ListView)findViewById(R.id.CommentsListView);
        lv.setAdapter(CurrentFeature.Comments);
    }

    @Override
    public void onClick(View view)
    {
        if(view == PostComment)
        {
            if(NewCommentText.getText().toString().equals(""))
            {
                Toast.makeText(getContext(), "Comment Text Required!", Toast.LENGTH_SHORT).show();
                return;
            }
            if(!FacebookIntegration.IsLoggedIn())
            {
                Toast.makeText(getContext(), "Please login to Facebook before commenting!", Toast.LENGTH_SHORT).show();
                return;
            }

            NewCommentPost();
        }
    }

    public void NewCommentPost()
    {

        try
        {
            StringBuilder FeaturesURL = new StringBuilder(256);
            FeaturesURL.append("http://ec2-54-187-116-83.us-west-2.compute.amazonaws.com/addcommentfb/");
            FeaturesURL.append("?AT=");
            FeaturesURL.append(FacebookIntegration.GetCurrentAccessToken());
            FeaturesURL.append("&FeatureID=");
            FeaturesURL.append(CurrentFeature.FeatureID);
            FeaturesURL.append("&Text=");
            FeaturesURL.append(NewCommentText.getText().toString().replace(" ", "%20")); // TODO: True URL Format

            Log.i("Commenting", FeaturesURL.toString());
            URL PostFeatureURL = new URL(FeaturesURL.toString());
            Log.i("Parser", FeaturesURL.toString());
            HttpURLConnection Connection = (HttpURLConnection) PostFeatureURL.openConnection();
            Connection.setDoInput(true);
            Connection.setUseCaches(false);
            Connection.addRequestProperty("Cache-Control", "no-cache");

            Connection.connect();
            String s = Connection.getResponseMessage();
            Log.i("Post Response", s);

            if(Connection.getResponseCode() == 200) {
                Toast.makeText(getContext(), "Posted Comment...", Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(getContext(), "Error Commenting...Code " + Connection.getResponseCode(), Toast.LENGTH_SHORT).show();

            FeatureThread.QueueRefresh();
            this.hide();

        }
        catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(getContext(), "Comment Posting Failed...", Toast.LENGTH_SHORT).show();
        }

    }
}

