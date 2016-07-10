package com.wherewolf;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Greg on 12/17/13.
 */
public class MainServiceThread implements Runnable
{
    public static String GetNotificationsURL = "http://ec2-54-187-116-83.us-west-2.compute.amazonaws.com/getnotifications/";//http://www.naminosplaza.com/SampleXML.txt";
    public static long LastUpdate = 0;
    public static long LastUpdateSaved = 0;
    private static int ElapsedTime = 0;
    private static int UpdateTime = 30000;
    private int LoopTime = 500;
    MainService mService = null;

    public MainServiceThread(MainService svc)
    {
        mService = svc;
    }

    public static void QueueRefresh()
    {
        ElapsedTime = UpdateTime + 1;
    }
    public static String BuildURL()
    {
        StringBuilder FeaturesURL = new StringBuilder(256);
        FeaturesURL.append(GetNotificationsURL);
        FeaturesURL.append("?AT=");
        FeaturesURL.append(FacebookIntegration.GetCurrentAccessToken());
        FeaturesURL.append("&UnixStamp=");
        FeaturesURL.append(Long.toString(LastUpdate));

        return FeaturesURL.toString();
    }

    @Override
    public void run()
    {
        for (; ; )
        {
            try
            {
                Thread.sleep(100);

                if (ElapsedTime > UpdateTime)
                {
                    ElapsedTime = 0;
                    int NotificationsReturned = 0;
                    URL NotificationsURL = new URL(BuildURL());
                    Log.i("Notifications Parser", NotificationsURL.toString());

                    HttpURLConnection Connection = (HttpURLConnection) NotificationsURL.openConnection();
                    Connection.setDoInput(true);
                    Connection.setUseCaches(false);
                    Connection.addRequestProperty("Cache-Control", "no-cache");
                    Connection.connect();

                    Log.i("Notifications", "Content Length: " + Connection.getContentLength());
                    Log.i("Notifications", "Response Code: " + Connection.getResponseCode());
                    Log.i("Notifications", "Response Message: " + Connection.getResponseMessage());
                    InputStream FeaturesInputStream = Connection.getInputStream();
                    XmlPullParser Parser = Xml.newPullParser();

                    Parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    Parser.setFeature(XmlPullParser.FEATURE_PROCESS_DOCDECL, false);

                    Parser.setInput(FeaturesInputStream, null);

                    while (Parser.next() != XmlPullParser.END_DOCUMENT)
                    {
                        if (Parser.getEventType() != XmlPullParser.START_TAG)
                        {
                            continue;
                        }

                        if (Parser.getName() == null)
                            continue;

                        Log.e("Parser Name:", Parser.getName());
                        if (Parser.getName().equalsIgnoreCase("Notification"))
                        {
                            if (Parser.getAttributeCount() <= 0)
                                continue;
                            String FeatureIDString = Parser.getAttributeValue(null, "FeatureID");
                            String NotificationString = Parser.getAttributeValue(null, "NotificationText");
                            String UnixStampString = Parser.getAttributeValue(null, "UnixStamp");
                            String LatitudeString = Parser.getAttributeValue(null, "Latitude");
                            String LongitudeString = Parser.getAttributeValue(null, "Longitude");

                            Log.e("UpdateTimeString:", UnixStampString);
                            long UpdateTime = Long.valueOf(UnixStampString);
                            if (UpdateTime > LastUpdate)
                                LastUpdate = UpdateTime;

                            float Latitude = Float.parseFloat(LatitudeString);
                            float Longitude = Float.parseFloat(LongitudeString);
                            int FeatureID = Integer.valueOf(FeatureIDString);
                            mService.AddNotification(FeatureID, Latitude, Longitude, "Wherewolf Notification", NotificationString, UpdateTime);

                            NotificationsReturned++;
                        }
                    }

                    FeaturesInputStream.close();
                    Connection.disconnect();
                    Log.e("NotificationThread", "Notifications Returned: " + NotificationsReturned);
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

            try
            {
                Thread.sleep(LoopTime); //TODO: Determine Sleep Time
                ElapsedTime += LoopTime;
            }
            catch (InterruptedException ex)
            {
                ex.printStackTrace();
            }
        }
    }
}
