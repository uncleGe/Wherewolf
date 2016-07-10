package com.wherewolf.features;

import android.util.Log;
import android.util.Xml;

import com.wherewolf.controls.map.GeoPoint;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Greg on 12/15/13.
 */
public class Feature
{
    public int FeatureID;
	public GeoPoint Location;
	public FeatureType Type;
	public String Text;
    public long UserID;
    public String Name = "";
    public long UpdateTimeUTC;
    public int FeatureComments = 0;
    public int FeaturePointX = 0;
    public int FeaturePointY = 0;
    public boolean isObscured = false;
    public boolean offScreen = false;

    public FeatureComments Comments = new FeatureComments();

	public Feature()
	{

	}

	public Feature(GeoPoint location, FeatureType type, String text)
	{
		Location = location;
		Type = type;
		Text = text;
	}

	public void SetFeatureType(String Text)
	{
		if(Text == null)
		{
			Type = FeatureType.Unknown;
			return;
		}

		for(FeatureType F : FeatureType.values())
		{
			if(F.toString().equalsIgnoreCase(Text))
			{
				Type = F;
				return;
			}
		}

		Type = FeatureType.Unknown;
	}

    public long GetSecondsOld() {
        long timeSpan = (System.currentTimeMillis() / 1000) - UpdateTimeUTC;
        return timeSpan;
    }
    public String GetFeatureAge()
    {
        try {
            long timeSpan = (System.currentTimeMillis() / 1000) - UpdateTimeUTC;

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

    public String BuildURL()
    {
        StringBuilder FeaturesURL = new StringBuilder(256);
        FeaturesURL.append(FeatureThread.BaseURL + "getcomments/");
        FeaturesURL.append("?FeatureID=");
        FeaturesURL.append(FeatureID);

        return FeaturesURL.toString();
    }

    public void LoadComments()
    {
        try
        {
                URL FeaturesURL = new URL(BuildURL());
                Log.i("Parser", FeaturesURL.toString());
                HttpURLConnection Connection = (HttpURLConnection) FeaturesURL.openConnection();
                Connection.setDoInput(true);
                Connection.setUseCaches(false);
                Connection.addRequestProperty("Cache-Control", "no-cache");
                Connection.connect();
                Log.i("Feature", "Content Length: " + Connection.getContentLength());
                Log.i("Feature", "Response Code: " + Connection.getResponseCode());
                Log.i("Feature", "Response Message: " + Connection.getResponseMessage());
                InputStream FeaturesInputStream = Connection.getInputStream();
                XmlPullParser Parser = Xml.newPullParser();

                Parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                Parser.setFeature(XmlPullParser.FEATURE_PROCESS_DOCDECL, false);

                Parser.setInput(FeaturesInputStream, null);

                Comments.clear();

                while (Parser.next() != XmlPullParser.END_DOCUMENT)
                {
                    if (Parser.getEventType() != XmlPullParser.START_TAG)
                    {
                        continue;
                    }
                    if (Parser.getName() == null)
                        continue;

                    Log.e("Parser Name:", Parser.getName());
                    if (Parser.getName().equalsIgnoreCase("Comment")) {
                        if (Parser.getAttributeCount() <= 0)
                            continue;
                        String UserIDString = Parser.getAttributeValue(null, "UserID");
                        String NameString = Parser.getAttributeValue(null, "Name");
                        String TextString = Parser.getAttributeValue(null, "Text");
                        String TimeStampString = Parser.getAttributeValue(null, "UnixStamp");

                        Log.w("AddComment", "Comment Added!");
                        FeatureComment NewFeatureComment = new FeatureComment();
                        NewFeatureComment.UserID = Long.parseLong(UserIDString);
                        NewFeatureComment.Name = NameString;
                        NewFeatureComment.CommentText = TextString;
                        NewFeatureComment.UnixStamp = Long.parseLong(TimeStampString);

                        Comments.add(NewFeatureComment);
                    }
                }

                FeaturesInputStream.close();
                Connection.disconnect();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
