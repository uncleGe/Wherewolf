package com.wherewolf;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.facebook.FacebookSdk;

/**
 * Created by Greg on 4/27/2015.
 */
public class MainService extends Service
{
    public boolean IsRunning = false;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if(IsRunning)
        {
            Log.e("MainService", "onStart but already running...");
            return super.onStartCommand(intent, flags, startId);
        }
        FacebookSdk.sdkInitialize(getApplicationContext());

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        MainServiceThread.LastUpdateSaved = settings.getLong("LastUpdate", (System.currentTimeMillis() / 1000) - 3600);
        MainServiceThread.LastUpdate = MainServiceThread.LastUpdateSaved;

        Log.e("MainService", "Start...");
        MainServiceThread t = new MainServiceThread(this);
        MainServiceThread.QueueRefresh();

        Thread WorkerThread = new Thread(t);
        WorkerThread.setName("Main Service Thread");
        WorkerThread.setPriority(Thread.NORM_PRIORITY - 1);
        WorkerThread.start();

        IsRunning = true;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void AddNotification(int id, float Latitude, float Longitude, String Title, String SubText, long UnixStamp)
    {
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Bundle Extras = new Bundle();
        Extras.putInt("com.wherewolf.FeatureID", id);
        Extras.putFloat("com.wherewolf.Latitude", Latitude);
        Extras.putFloat("com.wherewolf.Longitude", Longitude);
        Extras.putFloat("com.wherewolf.Longitude", Longitude);
        Extras.putLong("com.wherewolf.UnixStamp", UnixStamp);
        intent.putExtras(Extras);

        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder n  = new Notification.Builder(this)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(Title)
                .setContentText(SubText)
                .setSmallIcon(R.drawable.app_icon)
                .setContentIntent(pIntent)
                .setAutoCancel(true) ;

        notificationManager.notify(id, n.build());
    }
}