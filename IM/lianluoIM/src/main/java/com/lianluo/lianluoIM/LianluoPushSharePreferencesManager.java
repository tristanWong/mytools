package com.lianluo.lianluoIM;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;


/**
 * Created by Tristan on 2016/10/11.
 */

public class LianluoPushSharePreferencesManager {
    private static final String TAG = "LianluoIM";
    public final static String LIANLUOPUSH_PARAM_DB = "lianluo_IM_param_db";
    SharedPreferences mPreferences = null;
    Context mContext = null;
    public LianluoPushSharePreferencesManager(Context context)
    {
        mContext = context;
        if(mContext != null)
        {
            mPreferences = context.getSharedPreferences(LIANLUOPUSH_PARAM_DB, Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
        }
    }

    //标签相关操作.
    public void saveTags(String key, String tags){
        if(mPreferences != null){
            Editor editor = mPreferences.edit();
            editor.putString(key+"_tags", tags);
            editor.apply();
        }
    }
    public String getTags(String packagename){
        if (mPreferences!=null) {
            return mPreferences.getString(packagename+"_tags",null);
        }else
            return null;
    }

    //推送客户端相关操作.
    public void  savePushList(String pushers){

        if(mPreferences != null){
            Editor editor = mPreferences.edit();
            editor.putString("appkeys",pushers);
            editor.commit();
        }
    }

    public String getPushList(){
        if(mPreferences != null){
            return mPreferences.getString("appkeys",null);
        }else{
            return null;
        }
    }

    //设备列表相关操作。

    public String getDeviceList(String packagename){
        if (mPreferences!=null) {
            return mPreferences.getString(packagename+"_devices",null);
        }else
            return null;

    }
    public void saveDeviceList(String key, String devices){
        Log.d(TAG, "saveDeviceList: mPreferences = "+mPreferences);
        if(mPreferences != null){
            Editor editor = mPreferences.edit();
            editor.putString(key+"_devices", devices);
            editor.commit();
        }
    }



}
