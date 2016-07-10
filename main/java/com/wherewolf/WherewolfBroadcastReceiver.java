package com.wherewolf;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Greg on 5/6/2015.
 */
public class WherewolfBroadcastReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.i("BroadcastReceiver", "onReceive");
        Intent i= new Intent(context, MainService.class);
        context.startService(i);
    }
}
