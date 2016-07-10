package com.wherewolf;

import android.app.Application;
import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ListAdapter;

import java.util.ArrayList;

/**
 * Created by Greg on 5/23/2015.
 */
public abstract class ArrayListAdaptor<T> extends ArrayList<T> implements ListAdapter
{
    protected LayoutInflater mInflater;

    public ArrayListAdaptor()
    {
        super();
        mInflater = (LayoutInflater) MainActivity.CurrentActivity.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        return size();
    }

    @Override
    public Object getItem(int position) {
        return get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
