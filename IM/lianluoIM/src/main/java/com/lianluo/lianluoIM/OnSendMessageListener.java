package com.lianluo.lianluoIM;

/**
 * Created by Tristan on 2016/11/9.
 */

public interface  OnSendMessageListener {
    public void onMessageSendSuccess();
    public void onMessageSendFailed();
    public void onDeviceMessageGot(String s);
}
