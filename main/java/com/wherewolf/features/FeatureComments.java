package com.wherewolf.features;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;
import com.wherewolf.ArrayListAdaptor;
import com.wherewolf.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Greg on 3/18/2015.
 */
public class FeatureComments extends ArrayListAdaptor<FeatureComment>
{

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        System.out.println("getView " + position + " " + convertView);
        FeatureCommentListViewItem holder = null;
        if (convertView == null)
        {
            convertView = mInflater.inflate(R.layout.comment_list_item, null);
            holder = new FeatureCommentListViewItem();
            holder.UserPicture = (ProfilePictureView)convertView.findViewById(R.id.CommentItemPicture);
            holder.User = (TextView)convertView.findViewById(R.id.CommentItemName);
            holder.Comment = (TextView)convertView.findViewById(R.id.CommentItemText);
            holder.PostDateTime = (TextView)convertView.findViewById(R.id.CommentItemTime);

            convertView.setTag(holder);
        } else
        {
            holder = (FeatureCommentListViewItem)convertView.getTag();
        }
        holder.User.setText(get(position).Name);
        holder.UserPicture.setProfileId(Long.toString(get(position).UserID));
        holder.Comment.setText(get(position).CommentText);
        holder.PostDateTime.setText(get(position).GetCommentAge());
        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }
}
