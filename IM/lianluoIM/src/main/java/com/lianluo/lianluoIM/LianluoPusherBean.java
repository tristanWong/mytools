package com.lianluo.lianluoIM;

/**
 * Created by Tristan on 2016/10/9.
 */

public class LianluoPusherBean {
    String account = null; //APPkey
    String key = null;    //AppSecret
    String packageName = null;  //包名
    String clientID=null;

    public String getAccount() {
        return account;
    }

    public String getChannelID() {
        return clientID;
    }

    @Override
    public String toString() {
        String str = "pusher:  account ["+account+"] key ["+key+"] packagename " +packageName+"] channelID ["+clientID+"]";
        return str;
    }
}
