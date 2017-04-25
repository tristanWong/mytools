package com.lianluo.lianluoIM;

/**
 * Created by Tristan on 2016/8/29.
 */
public interface OnMessageListener {

    public void onConnected();
    public void onDisconnected();
    public void onMessageGot(String s);
    public void onTagsGot(String s);
    public void onGetClientId(String clientID);
    public void onDevListGot(String devlist);



}
