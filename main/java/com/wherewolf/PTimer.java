package com.wherewolf;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Created by Greg on 12/11/13.
 */
public class PTimer
{
	public static final float AvgFactor = 0.025f;
	private static Dictionary<String, Long> Timers = new Hashtable<String, Long>();
	private static Dictionary<String, Long> Averages = new Hashtable<String, Long>();
	private static Dictionary<String, Integer> Counts = new Hashtable<String, Integer>();

	public static void Start(String Name)
	{
		if (Counts.get(Name) == null)
			Counts.put(Name, 1);
	}
}
