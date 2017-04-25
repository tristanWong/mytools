package com.lianluo.lianluoimdemo;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Tristan on 2016/11/11.
 */
public class DevicelistAdapter extends BaseAdapter{
    private static final String TAG = "ADAPTER";
    Context mContext = null;
    List<String> mDeviceList = null;
    LayoutInflater mInflater  = null;
    DevicelistAdapter(Context context, List<String> deviceList){
        Log.d(TAG, "DevicelistAdapter: Adapter 初始化〉〉〉〉〉〉〉〉〉〉〉〉");
        mContext = context;
        mDeviceList = deviceList;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        Log.d(TAG, "getCount: mDeviceList.size() ");
        return ((mDeviceList!=null)?mDeviceList.size():0);
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Log.d(TAG, "getView: >>>>>>>>>>>>>>>");
        ViewHolder holder;
        if(view == null)
        {
            holder = new ViewHolder();
            view = mInflater.inflate(R.layout.adapter_layout, null);
            holder.tv_deviceID = (TextView)view.findViewById(R.id.tv_devid);
            view.setTag(holder);
        }else
        {
            holder = (ViewHolder)view.getTag();
        }

//        Log.d(TAG, "getView: holder.title = "+holder.title+" devlist =  "+devlist);
        holder.tv_deviceID.setText(mDeviceList.get(i));
        return view;
    }

    public class ViewHolder{
        TextView tv_deviceID;
    }


}
