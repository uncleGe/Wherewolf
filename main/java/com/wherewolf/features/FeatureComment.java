package com.wherewolf.features;

/**
 * Created by Greg on 3/18/2015.
 */
public class FeatureComment
{
    public long UserID;
    public String Name;
    public String CommentText;
    public long UnixStamp;

    public long GetSecondsOld() {
        long timeSpan = (System.currentTimeMillis() / 1000) - UnixStamp;
        return timeSpan;
    }

    public String GetCommentAge()
    {
        try {
            long timeSpan = (System.currentTimeMillis() / 1000) - UnixStamp;

            long diffSeconds = timeSpan % 60;
            long diffMinutes = timeSpan / (60);
            long diffHours = timeSpan / (60 * 60);
            long diffDays = timeSpan / (24 * 60 * 60);

            if(diffDays > 1)
                return diffDays + " days ago";
            if(diffDays > 0)
                return diffDays + " day ago";

            if(diffHours > 1)
                return diffHours + " hours ago";
            if(diffHours > 0)
                return diffHours + " hour ago";

            if(diffMinutes > 1)
                return diffMinutes + " minutes ago";
            if(diffMinutes > 0)
                return diffMinutes + " minute ago";

            if(diffSeconds > 1)
                return diffSeconds + " seconds ago";
            if(diffSeconds > 0)
                return diffSeconds + " second ago";
        }
        catch(Exception ex) {
            return "N/A";
        }
        return "N/A";
    }
}
