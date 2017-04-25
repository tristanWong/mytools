package com.lianluo.lianluoIM;

/**
 * Created by Tristan on 2016/10/25.
 */

public class LianluoComunicatorMessages {
    //广播Action
    public static final String STARTPUSH_ACTION = "com.lianluo.im.STARTPUSH";
    public static final String SETTAGS_ACTION = "com.lianluo.im.SETTAGS";  //设置标签。
    public static final String REMOVETAGS_ACTION = "com.lianluo.im.REMOVETAGS"; //删除标签
    public static final String GETTAGS_ACTION = "com.lianluo.im.GETTAGS";  //获取标签
    public static final String GETTAGS_BACK_ACTION = "com.lianluo.im.GETTAGS_BACK"; //获取标签返回。


    public static final String SERVICE_STATED_ACTION = "com.lianluo.im.SERVICE_STATED";
    public static final String SERVICE_LIANLUO_IM_CONNECTED_ACTION = "com.lianluo.im.SERVICE_CONNECTED";
    public static final String SERVICE_LIANLUO_IM_DISCONNECTED_ACTION="com.lianluo.im.SERVICE_DISCONNECTED";
    public static final String SERVICE_SERVICE_CHECK_ACTION = "com.lianluo.im.SERVICE_CHECK";
    public static final String SERVICE_SERVICE_CHECK_BACK_ACTION = "com.lianluo.im.SERVICE_CHECK_BACK";
    public static final String SERVICE_SERVICE_PUSH_STOP_CTION = "com.lianluo.im.STOP_PUSHER";
    public static final String SERVICE_SERVICE_PUSH_STOP_BACK_CTION = "com.lianluo.im.STOP_PUSHER_BACK";
    public static final String SERVICE_SERVICE_PUSH_START_BACK = "com.lianluo.im.START_PUSHER_BACK";


    public static final String SERVICE_SUBCRIBE_DEVICEID_ACTION = "com.lianluo.im.SUBCRIBE_DEVID";
    public static final String SERVICE_DESUBCRIBE_DEVICEID_ACTION = "com.lianluo.im.DESUBCRIBE_DEVID";
    public static final String SERVICE_GET_DEVICELIST_ACTION = "com.lianluo.im.GET_DEVLIST";

    //设备数据
    public static final String SERVICE_UPDATE_DEVICELIST_ACTION = "com.lianluo.im.UPDATE_DEVLIST";
    public static final String SERVICE_GET_DEVICELIST_BACK_ACTION = "com.lianluo.im.GET_DEVLIST_BACK";
    public static final String SERVICE_ADD_DEVICELIST_SUCESS_ACTION = "com.lianluo.im.ADD_DEVLIST_SUCESS";
    public static final String SERVICE_ADD_DEVICELIST_FAILED_ACTION = "com.lianluo.im.ADD_DEVLIST_FAILEd";
    public static final String SERVICE_DEL_DEVICELIST_SUCESS_ACTION = "com.lianluo.im.DEL_DEVLIST_SUCESS";
    public static final String SERVICE_DEL_DEVICELIST_FAILED_ACTION = "com.lianluo.im.DEL_DEVLIST_FAILEd";


    //消息发送内容

    public static final String SERVICE_SEND_MESSAGE_ACTION = "com.lianluo.im.SEND_MESSAGE";
    public static final String SERVICE_SEND_MESSAGE_SUCESS_ACTION = "com.lianluo.im.SEND_MESSAGE_SUCESS";
    public static final String SERVICE_SEND_MESSAGE_FAILED_ACTION = "com.lianluo.im.SEND_MESSAGE_FAILED";
    public static final String SERVICE_SEND_MESSAGE_DEVICE_ONLINE_ACTION = "com.lianluo.im.SEND_ONLINE_CHECK_MESSAGE";
}
