package com.wherewolf.features;

import java.util.ArrayList;

/**
 * Created by Greg on 12/16/13.
 */
public class Features extends ArrayList<Feature>
{
	public boolean HasFeature(float Lat, float Long) // TODO: FeatureID
	{
		for(int i=0;i<size();i++)
		{
			Feature CurFeature = get(i);
			if(CurFeature.Location.Latitude == Lat && CurFeature.Location.Longitude == Long)
			{
				return true;
			}
		}
		return false;
	}
    public boolean HasFeature(int FeatureID) // TODO: FeatureID
    {
        for(int i=0;i<size();i++)
        {
            Feature CurFeature = get(i);
            if(CurFeature.FeatureID == FeatureID)
            {
                return true;
            }
        }
        return false;
    }

    public Feature GetFeature(float Lat, float Long) // TODO: FeatureID
    {
        for(int i=0;i<size();i++)
        {
            Feature CurFeature = get(i);
            if(CurFeature.Location.Latitude == Lat && CurFeature.Location.Longitude == Long)
            {
                return CurFeature;
            }
        }
        return null;
    }
    public Feature GetFeature(int FeatureID) // TODO: FeatureID
    {
        for(int i=0;i<size();i++)
        {
            Feature CurFeature = get(i);
            if(CurFeature.FeatureID == FeatureID)
            {
                return CurFeature;
            }
        }
        return null;
    }
}
