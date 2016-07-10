package com.wherewolf.controls.map;

/**
 * Created by Greg on 12/24/13.
 */
public class GeoRect
{
	public GeoPoint p1;
	public GeoPoint p2;


	public float getWidth()
	{
		return Math.abs(p1.Longitude - p2.Longitude);
	}

	public float getHeight()
	{
		return Math.abs(p1.Latitude - p2.Latitude);
	}

	public void Normalize()
	{
		GeoPoint Min = new GeoPoint(Math.min(p1.Latitude, p2.Latitude), Math.min(p1.Longitude, p2.Longitude));
		GeoPoint Max = new GeoPoint(Math.max(p1.Latitude, p2.Latitude), Math.max(p1.Longitude, p2.Longitude));

		p1 = Min;
		p2 = Max;
	}

	public boolean Contains(GeoRect gRect)
	{
		Normalize();
		gRect.Normalize();
		if(	(gRect.p1.Latitude > p1.Latitude && gRect.p1.Latitude < p2.Latitude) &&
			(gRect.p2.Latitude > p1.Latitude && gRect.p2.Latitude < p2.Latitude) &&
			(gRect.p1.Longitude > p1.Longitude && gRect.p1.Longitude < p2.Longitude) &&
			(gRect.p2.Longitude > p1.Longitude && gRect.p2.Longitude < p2.Longitude))
		{
			return true;
		}
		return false;
	}

	public void Expand(float sMult)
	{
		Normalize();
		float width = getWidth();
		float height = getHeight();
		p1 = new GeoPoint(p1.Latitude - (height * sMult), p1.Longitude - (width * sMult));
		p2 = new GeoPoint(p2.Latitude + (height * sMult), p2.Longitude + (width * sMult));
	}

	@Override
	public String toString()
	{
		return "GR{" +
				"p1=" + p1 +
				"\r\n, p2=" + p2 +
				'}';
	}

	public static GeoRect Empty()
	{
		GeoRect r = new GeoRect();
		r.p1 = new GeoPoint(0, 0);
		r.p2 = new GeoPoint(0, 0);
		return r;
	}
}
