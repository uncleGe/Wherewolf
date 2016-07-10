package com.wherewolf.controls.map;

/**
 * Created by Greg on 12/11/13.
 */
public class GeoPoint
{
	public final float Latitude;
	public final float Longitude;

	public GeoPoint(float Lat, float Long)
	{
		Latitude = Lat;
		Longitude = Long;
	}

	@Override
	public String toString()
	{
		return "GeoP{" +
				"Lat=" + Latitude +
				", Long=" + Longitude +
				'}';
	}
}