package com.wherewolf.controls.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import com.wherewolf.MainActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by Greg on 12/11/13.
 */
public class TileFetcher implements Runnable
{
	public static final int MaxTilesInCache = 500;
	public static Dictionary<Long, Bitmap> Cache = new Hashtable<Long, Bitmap>();
	public static List<Long> WorkerSpawned = new ArrayList<Long>();
	public static Bitmap EmptyTile = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
	public static Bitmap TempTile = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
	public static String BaseURL = "https://api.tiles.mapbox.com/v4/geisman.cieiosomy00mqsim1wxasv8di/";
    public static String AerialBaseURL = "http://otile1.mqcdn.com/tiles/1.0.0/sat/";//"http://www.naminosplaza.com/osm/";
    public static boolean Aerial = false;
	static
	{
		CreateEmptyBitmap();
	}

	private static int MaxThreads = 20;
	private static int NumberThreads = 0;
	private TileRequest tileRequest;

	public TileFetcher(TileRequest request)
	{
		tileRequest = request;
	}

	private static void CreateEmptyBitmap()
	{
		Canvas c = new Canvas(EmptyTile);
		Paint p = new Paint();
		p.setARGB(255, 100, 100, 100);

		c.drawRect(0, 0, EmptyTile.getWidth(), EmptyTile.getHeight(), p);
		p.setARGB(255, 0, 200, 255);
		c.drawText("Empty Tile", 10, 10, p);
	}

	private static long GetCacheKey(int x, int y, int zoom)
	{
        if(!Aerial)
		    return ((long) zoom << 40) | ((long) x << 20) | (long) (y & 0x3FFFF);
        else
            return ((long)1 << 45 | (long) zoom << 40) | ((long) x << 20) | (long) (y & 0x3FFFF);
	}

	public static Bitmap GetTile(Context context, int x, int y, int zoom)
	{
		if(Cache.size() > MaxTilesInCache)
		{
			Cache.remove(Cache.keys().nextElement());
		}

		if (y < 0)
			return EmptyTile;

		long Key = GetCacheKey(x, y, zoom);

		while (x < 0)
			x += (int) Math.pow(2, (int) zoom);

		while (x >= (int) Math.pow(2, (int) zoom))
			x -= (int) Math.pow(2, (int) zoom);

		if (Cache.get(Key) != null) // TODO: More efficient cache
		{
			return Cache.get(Key);
		}
		//TODO - IF CACHE HIT, RETURN FROM CACHE
		File TileFile = new File(GetTilePath(x, y, zoom, Aerial));
		if (TileFile.exists())
		{
			try
			{
				FileInputStream input = new FileInputStream(TileFile);
				Bitmap b = BitmapFactory.decodeStream(input);
				if (b != null)
				{
					synchronized (Cache)
					{
						Cache.put(Key, b);
					}
					return b;
				}
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}

		if (!WorkerSpawned.contains(Key) && NumberThreads < MaxThreads)
		{
			WorkerSpawned.add(Key);
			SpawnWorker(context, x, y, zoom, Key);
		}

        return GetTempTile(context, x, y, zoom, 2);
	}

    private static Bitmap GetTempTile(Context context,int x, int y, int zoom, float factor)
    {
        int newX = x / (int) factor;
        int newY = y / (int) factor;
        int newZoom = zoom - (int) (Math.log(factor) / Math.log(2));

        if (newZoom < MapView.MinZoom)
            return EmptyTile;

        float subTileX = (x / factor) - newX;
        float subTileY = (y / factor) - newY;

        GetTile(context, newX, newY, newZoom);
        long Key = GetCacheKey(newX, newY, newZoom);
        if (Cache.get(Key) != null)
        {
            Bitmap uTempTile = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(uTempTile);
            Paint p = new Paint();
            Rect SourceRect = new Rect((int) (MapView.TileSize * subTileX),
                    (int) (MapView.TileSize * subTileY),
                    (int) (MapView.TileSize * subTileX) + MapView.TileSize / (int) factor,
                    (int) (MapView.TileSize * subTileY) + MapView.TileSize / (int) factor);
            c.drawBitmap(Cache.get(Key), SourceRect, new Rect(0, 0, MapView.TileSize, MapView.TileSize), p);

            return uTempTile;
        }
        else
        {
            return GetTempTile(context, x, y, zoom, factor * 2);
        }
    }

	private static boolean FillTempTile(Context context,int x, int y, int zoom, float factor)
	{
		int newX = x / (int) factor;
		int newY = y / (int) factor;
		int newZoom = zoom - (int) (Math.log(factor) / Math.log(2));

		if (newZoom < MapView.MinZoom)
			return false;

		float subTileX = (x / factor) - newX;
		float subTileY = (y / factor) - newY;
		GetTile(context, newX, newY, newZoom);
		long Key = GetCacheKey(newX, newY, newZoom);
		if (Cache.get(Key) != null)
		{
			Canvas c = new Canvas(TempTile);
			Paint p = new Paint();
			Rect SourceRect = new Rect((int) (MapView.TileSize * subTileX),
					(int) (MapView.TileSize * subTileY),
					(int) (MapView.TileSize * subTileX) + MapView.TileSize / (int) factor,
					(int) (MapView.TileSize * subTileY) + MapView.TileSize / (int) factor);
			c.drawBitmap(Cache.get(Key), SourceRect, new Rect(0, 0, MapView.TileSize, MapView.TileSize), p);

			return true;
		} else
		{
			return FillTempTile(context, x, y, zoom, factor * 2);
		}
	}

	private static void SpawnWorker(Context context, int x, int y, int zoom, long CacheKey)
	{
		NumberThreads++;

		TileRequest Request = new TileRequest();
		Request.Context = context;
		Request.x = x;
		Request.y = y;
		Request.zoom = zoom;
        Request.Aerial = Aerial;
		Request.CacheKey = CacheKey;

		//TODO: Fix Threading
		TileFetcher workerFetcher = new TileFetcher(Request);

		Thread WorkerThread = new Thread(workerFetcher);
		WorkerThread.setName("TileFetcher");
		WorkerThread.setPriority(Thread.NORM_PRIORITY - 1);
		WorkerThread.start();
	}

	private static String GetTilePath(int x, int y, int zoom, boolean AerialRequest)
	{
        if(!AerialRequest)
		    return MainActivity.CachePath + File.separator + zoom + "-" + x + "_" + y + ".tile";
        else
            return MainActivity.CachePath + File.separator + zoom + "-" + x + "_" + y + "_Aerial.tile";
	}

	public static Bitmap getBitmapFromURL(String src, TileRequest request)
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
			File TileFile = new File(GetTilePath(request.x, request.y, request.zoom, request.Aerial));
			if (TileFile.exists())
				TileFile.delete();

			TileFile.createNewFile();
			FileOutputStream outFile = new FileOutputStream(TileFile);// request.Context.openFileOutput(TileFile.getAbsolutePath(), 1);
			outFile.write(imageData, 0, imageData.length);
			outFile.close();

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

	@Override
	public void run()
	{
		try
		{
			String URL = BaseURL + tileRequest.zoom + "/" + tileRequest.x + "/" + tileRequest.y + ".png" + "?access_token=pk.eyJ1IjoiZ2Vpc21hbiIsImEiOiJjaWVpb3NveWgwMG9kc3ZrZ3FxZ2pyNzJpIn0.Zhw4A_KoSlxWaC3-qM8gMA";
            if(tileRequest.Aerial)
            {
                URL = AerialBaseURL + tileRequest.zoom + "/" + tileRequest.x + "/" + tileRequest.y + ".png";
            }
			Bitmap b = getBitmapFromURL(URL, tileRequest);
			if (b != null)
			{
				synchronized (Cache)
				{
					Cache.put(tileRequest.CacheKey, b);
				}
			} else
			{
				synchronized (WorkerSpawned)
				{
					WorkerSpawned.remove((Object) tileRequest.CacheKey);
				}
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		} finally
		{
			NumberThreads--;
		}
	}
}