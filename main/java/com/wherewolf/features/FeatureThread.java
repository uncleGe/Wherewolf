package com.wherewolf.features;

import android.util.Log;
import android.util.Xml;

import com.wherewolf.controls.map.GeoPoint;
import com.wherewolf.controls.map.GeoRect;
import com.wherewolf.controls.map.MapView;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by Greg on 12/17/13.
 */
public class FeatureThread implements Runnable
{
    public static String BaseURL = "http://ec2-54-187-116-83.us-west-2.compute.amazonaws.com/";//http://www.naminosplaza.com/SampleXML.txt";
	public static String GetFeaturesURL = "http://ec2-54-187-116-83.us-west-2.compute.amazonaws.com/getfeatures/";//http://www.naminosplaza.com/SampleXML.txt";
	public static MapView mapView = null;
	public static Timestamp LastUpdate = new Timestamp(0);
	public static GeoRect FeatureBounds = GeoRect.Empty();
    public static Queue<QueueItem> FeatureQueue = new ArrayBlockingQueue<QueueItem>(5);
	private static int ElapsedTime = 0;
	private static int UpdateTime = 30000;
	private int LoopTime = 500;


	public static void QueueRefresh()
	{
		ElapsedTime = UpdateTime + 1;
	}
	public static String BuildURL()
	{
		if (mapView == null)
			return "";

		GeoRect Bounds = mapView.GetViewBounds();
		if (!FeatureBounds.Contains(Bounds))
		{
			Log.i("FeatureThread", "Invalidate Feature Time");
			LastUpdate = new Timestamp(0);
			FeatureBounds = Bounds;
			FeatureBounds.Expand(0.25f);
		}

		StringBuilder FeaturesURL = new StringBuilder(256);
		FeaturesURL.append(GetFeaturesURL);
		FeaturesURL.append("?La1=");
		FeaturesURL.append(FeatureBounds.p1.Latitude);
		FeaturesURL.append("&Lo1=");
		FeaturesURL.append(FeatureBounds.p1.Longitude);

		FeaturesURL.append("&La2=");
		FeaturesURL.append(FeatureBounds.p2.Latitude);
		FeaturesURL.append("&Lo2=");
		FeaturesURL.append(FeatureBounds.p2.Longitude);

		FeaturesURL.append("&LastUpdate=");
		FeaturesURL.append(LastUpdate.toString().replace(" ", "%20"));
        if(!FeatureQueue.isEmpty())
        {
            QueueItem qItem = FeatureQueue.remove();
            if(qItem instanceof QueueItemFeatureComment)
            {
                QueueItemFeatureComment q = (QueueItemFeatureComment)qItem;
                FeaturesURL.append("&FeatureID=");
                FeaturesURL.append(q.FeatureID);
            }
        }

		return FeaturesURL.toString();
	}

	@Override
	public void run()
	{
		for (; ; )
		{
			try
			{
				GeoRect Bounds = mapView.GetViewBounds();

				if (ElapsedTime > UpdateTime || !FeatureBounds.Contains(Bounds))
				{
					ElapsedTime = 0;
					int FeaturesReturned = 0;
					int FeaturesLoaded = 0;
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

					while (Parser.next() != XmlPullParser.END_DOCUMENT)
					{
                        if (Parser.getEventType() != XmlPullParser.START_TAG)
                        {
                            continue;
                        }

						if (Parser.getName() == null)
							continue;

						Log.e("Parser Name:", Parser.getName());
						if (Parser.getName().equalsIgnoreCase("Feature"))
						{
							if (Parser.getAttributeCount() <= 0)
								continue;
                            String IDString = Parser.getAttributeValue(null, "ID");
							String LatString = Parser.getAttributeValue(null, "Latitude");
							String LongString = Parser.getAttributeValue(null, "Longitude");
							String TypeString = Parser.getAttributeValue(null, "Type");
							String TextString = Parser.getAttributeValue(null, "ContentText");
							String UpdateTimeString = Parser.getAttributeValue(null, "Updated");
                            String UpdateTimeStringUnix = Parser.getAttributeValue(null, "UpdatedUnix");
                            String NameString = Parser.getAttributeValue(null, "Name");

                            String UserIDString = Parser.getAttributeValue(null, "UserID");
                            String CommentCountString = Parser.getAttributeValue(null, "Comments");

							Log.e("UpdateTimeString:", UpdateTimeString);
							Timestamp UpdateTime = Timestamp.valueOf(UpdateTimeString);
							if (UpdateTime.after(LastUpdate))
								LastUpdate = UpdateTime;

							float Lat = Float.valueOf(LatString);
							float Long = Float.valueOf(LongString);
							FeaturesReturned++;

							synchronized (MapView.MapFeatures)
							{
								if (!MapView.MapFeatures.HasFeature(Lat, Long))
								{
									FeaturesLoaded++;
									Log.w("AddFeature", "Feature Added!");
									Feature NewFeature = new Feature();
                                    NewFeature.FeatureID = Integer.parseInt(IDString);
									NewFeature.Location = new GeoPoint(Lat, Long);
									NewFeature.SetFeatureType(TypeString);
									NewFeature.Text = TextString;
                                    NewFeature.UserID = java.lang.Long.parseLong(UserIDString);
                                    NewFeature.Name = NameString;

                                    NewFeature.UpdateTimeUTC = java.lang.Long.valueOf(UpdateTimeStringUnix).longValue(); // new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(UpdateTimeString);
                                    NewFeature.FeatureComments = Integer.valueOf(CommentCountString);

									MapView.MapFeatures.add(NewFeature);
									MapView.lastProcessedZoom = 1000;
								}
                                else
                                {
                                    Feature CurFeature = MapView.MapFeatures.GetFeature(Lat, Long);
                                    CurFeature.FeatureComments = Integer.valueOf(CommentCountString);
                                }
							}
						}
					}

					FeaturesInputStream.close();
					Connection.disconnect();
					Log.e("FeatureThread", "Features Returned: " + FeaturesReturned + " Loaded: " + FeaturesLoaded);
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
			} catch (InterruptedException ex)
			{
				ex.printStackTrace();
			}
		}
	}
}
