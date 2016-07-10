package com.wherewolf.controls.map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import com.wherewolf.features.FeatureType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

/**
 * Created by Greg on 12/27/13.
 */
public class FeatureIcons
{
	public static Hashtable<FeatureType, Bitmap> Icons = new Hashtable<FeatureType, Bitmap>();
	public static Bitmap EmptyIcon = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888);

	static
	{
		CreateEmptyBitmap();
	}
	private static void CreateEmptyBitmap()
	{
		Canvas c = new Canvas(EmptyIcon);
		Paint p = new Paint();
		p.setARGB(255, 100, 100, 100);

		c.drawRect(0, 0, EmptyIcon.getWidth(), EmptyIcon.getHeight(), p);
		p.setARGB(255, 0, 200, 255);
		c.drawText("Empty Icon", 10, 10, p);
	}

	public static Bitmap GetIcon(FeatureType type)
	{
		if(Icons.containsKey(type))
			return Icons.get(type);
		try
		{
			Bitmap b = getBitmapFromURL("http://ec2-54-187-116-83.us-west-2.compute.amazonaws.com/" + type.name() + ".png");
			Icons.put(type, b);
			return b;
		}
		catch(Exception ex)
		{
			Icons.put(type, EmptyIcon);
			ex.printStackTrace();
		}
		return EmptyIcon;
	}

	public static Bitmap GetIcon(FeatureType type, int Size)
	{
		Bitmap b = GetIcon(type);

		return Bitmap.createScaledBitmap(b, Size, Size,true);
	}

	public static Bitmap getBitmapFromURL(String src)
	{
		try
		{
			URL url = new URL(src);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();

			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			int nRead;
			byte[] data = new byte[16384];

			while ((nRead = input.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}

			buffer.flush();

			buffer.toByteArray();
			byte[] imageData = buffer.toByteArray();

			Log.d("src", "src: " + src);
			Log.d("Size", "Size: " + imageData.length);

			Bitmap myBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
			//Save to Disk cache

			return myBitmap;
		} catch (MalformedURLException mue)
		{
			mue.printStackTrace();
			return null;
		} catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

}
