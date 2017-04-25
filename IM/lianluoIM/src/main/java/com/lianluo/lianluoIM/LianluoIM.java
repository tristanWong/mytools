package com.lianluo.lianluoIM;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tristan on 2016/8/15.
 */
public class LianluoIM {
    private static Context mContext;

    public static final String TAG ="LianluoIM";
    private static final int MESSAGE_CHECK_TO_BROADCAST = 0x1100;  //启动服务，广播
    private static final int MESSAGE_CHECK_TIMEOUT_OR_FAILED = 0x1101; //网络检测侧失败:认证出错等.
    private static final int MESSAGE_CHECK_TIMEOUT = 0x1102;//超时,可能是网络超时也可能是广播接收超时.
    private static final int MESSAGE_CHECK_SERVICE_TIMEOUT = 0x1103;


    private static String appkey = null;
    private static String appsecret = null;
    private static String packageName = null;
    private static String clientID = null;
    static OnMessageListener myListener = null;
    private static OnSendMessageListener myMessageListener = null;
    private static OnDeviceListOptionListener myDeviceOpListener = null;

    private static boolean bServiceStarted = false;
    private static boolean bClientIdGot = false;
    public static final String      VALID_CHECK_URL ="https://mops-push.lianluo.com";// "https://mops-dev.lianluo.com:880";


    public static final String      API_REPORT_DEVICES = "/api/client/v1/app-devices";
    public static boolean bIsConnected = false;

    public static void setDevOptionLister(OnDeviceListOptionListener listener){
        myDeviceOpListener = listener;

    }


    //设置标签
    public static void setTags(String tags){
        Intent broadCastIntent = new Intent(LianluoComunicatorMessages.SETTAGS_ACTION);
        broadCastIntent.putExtra("tags",tags);
        broadCastIntent.putExtra("packagename",packageName);
        mContext.sendBroadcast(broadCastIntent);
    }
    //删除标签
    public static void removeTags(String tags){
        Intent broadCastIntent = new Intent(LianluoComunicatorMessages.REMOVETAGS_ACTION);
        broadCastIntent.putExtra("tags",tags);
        broadCastIntent.putExtra("packagename",packageName);
        mContext.sendBroadcast(broadCastIntent);
    }
    //得到标签
    public static void getTags(){
        Intent broadCastIntent = new Intent(LianluoComunicatorMessages.GETTAGS_ACTION);
        broadCastIntent.putExtra("packagename",packageName);
        mContext.sendBroadcast(broadCastIntent);
    }


    private static void broadcastToStartPush(){
        if (clientID!=null) {
            Intent broadCastIntent = new Intent();
            broadCastIntent.putExtra("appkey", appkey);
            broadCastIntent.putExtra("appsecret", appsecret);
            broadCastIntent.putExtra("pkgname", packageName);
            broadCastIntent.putExtra("clientid", clientID);//设备ID.
            broadCastIntent.setAction(LianluoComunicatorMessages.STARTPUSH_ACTION);
            mContext.sendBroadcast(broadCastIntent);
        }
    }


    static BroadcastReceiver lianluoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String packagename = intent.getStringExtra("packagename");

            Log.d(TAG, "onReceive: intent.action = "+action);
            Log.d(TAG, "onReceive: packagename = "+packagename+" packageName = "+packageName);
            if (packagename.equals(packageName)) {

                if (action.equals(LianluoComunicatorMessages.SERVICE_STATED_ACTION)) {
                    //mqtt服务启动的广播

                        bServiceStarted = true;
                        myHandler.removeMessages(MESSAGE_CHECK_SERVICE_TIMEOUT);


                } else if (action.equals(LianluoComunicatorMessages.SERVICE_LIANLUO_IM_CONNECTED_ACTION)) {
                    //mqtt服务器连接上广播

                        myListener.onConnected();
                        bServiceStarted = true;
                        myHandler.removeMessages(MESSAGE_CHECK_SERVICE_TIMEOUT);
                        bIsConnected = true;



                }else if(action.equals(LianluoComunicatorMessages.SERVICE_LIANLUO_IM_DISCONNECTED_ACTION)){

                    myListener.onDisconnected();
                    bIsConnected = false;

                } else if (action.equals(packageName)) {
                    //推送消息广播--------------

                    String broadcastMessage = intent.getStringExtra("content");
                    if (broadcastMessage != null) {
                        Log.d(TAG, "BroadcastReceiver >>>>>>> onReceive: 收到消息广播 broadcastMessage = " + broadcastMessage);
                        if (myListener != null) {
                            myListener.onMessageGot(broadcastMessage);
                        }
                        if (myMessageListener != null) {

                            myMessageListener.onDeviceMessageGot(broadcastMessage);
                        }
                    }

//                String topString =  parseTopicString(broadcastMessage);
                } else if (action.equals(LianluoComunicatorMessages.GETTAGS_BACK_ACTION)) {
                    //获取TAGS返回的广播
                    String tagsString = intent.getStringExtra("tags");
                    Log.d(TAG, "onReceive:  获取到tags = " + tagsString);
                    if (tagsString != null && (myListener != null)) {
                        Log.d(TAG, "BroadcastReceiver >>>>>>> onReceive: 收到tag返回广播 tagsString = " + tagsString);
                        myListener.onTagsGot(tagsString);
                    }

                } else if (action.equals(LianluoComunicatorMessages.SERVICE_SERVICE_CHECK_BACK_ACTION)) {


                        bServiceStarted = true;
                        myHandler.removeMessages(MESSAGE_CHECK_SERVICE_TIMEOUT);

                } else if (action.equals(LianluoComunicatorMessages.SERVICE_GET_DEVICELIST_BACK_ACTION)) {

                        String devList = intent.getStringExtra("devices");
                        myListener.onDevListGot(devList);

                } else if (action.equals(LianluoComunicatorMessages.SERVICE_SEND_MESSAGE_FAILED_ACTION)) {
                    Log.d(TAG, "onReceive: [LianluoComunicatorMessages.SERVICE_SEND_MESSAGE_FAILED_ACTION ]:MyMessageLisener = " + myMessageListener);
                    if (myMessageListener != null) {

                        myMessageListener.onMessageSendFailed();
                    }

                } else if (action.equals(LianluoComunicatorMessages.SERVICE_SEND_MESSAGE_SUCESS_ACTION)) {
                    Log.d(TAG, "onReceive: [LianluoComunicatorMessages.SERVICE_SEND_MESSAGE_SUCESS_ACTION]:: ------------->>>>> myMessageListener = " + myMessageListener);
                    if (myMessageListener != null) {
                        myMessageListener.onMessageSendSuccess();
                    }

                } else if (action.equals(LianluoComunicatorMessages.SERVICE_ADD_DEVICELIST_FAILED_ACTION)) {
                    Log.d(TAG, "onReceive: 设备添加失败 ");
                    String tag = "add";
                    if (myDeviceOpListener != null) {
                        myDeviceOpListener.onDeviceOptionFailed(tag);
                    }

                } else if (action.equals(LianluoComunicatorMessages.SERVICE_ADD_DEVICELIST_SUCESS_ACTION)) {
                    Log.d(TAG, "onReceive: 设备添加成功 ");
                    String tag = "add";
                    if (myDeviceOpListener != null) {
                        myDeviceOpListener.onDeviceListChanged(tag);
                    }
                } else if (action.equals(LianluoComunicatorMessages.SERVICE_DEL_DEVICELIST_FAILED_ACTION)) {
                    Log.d(TAG, "onReceive: 设备删除失败 ");
                    String tag = "del";
                    if (myDeviceOpListener != null) {
                        myDeviceOpListener.onDeviceOptionFailed(tag);
                    }

                } else if (action.equals(LianluoComunicatorMessages.SERVICE_DEL_DEVICELIST_SUCESS_ACTION)) {
                    Log.d(TAG, "onReceive: 设备删除成功");
                    String tag = "del";
                    if (myDeviceOpListener != null) {
                        myDeviceOpListener.onDeviceListChanged(tag);
                    }
                }
            }
        }

    };

    static Handler myHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MESSAGE_CHECK_SERVICE_TIMEOUT:
                    Log.d(TAG, "handleMessage: 服务未启动，启动服务中。。");
                    startPushService(packageName);
                    break;
                case MESSAGE_CHECK_TO_BROADCAST:
                    // TODO: 2016/10/10 广播数据
                    checkDataStatus();
                    break;
                case MESSAGE_CHECK_TIMEOUT_OR_FAILED:
                    // TODO: 2016/10/10 超时或者失败.
                    break;

            }

            super.handleMessage(msg);
        }
    };

    private static void checkDataStatus() {

        Log.d(TAG, "checkDataStatus: bClientIdGot = "+bClientIdGot+" bServiceStarted = "+bServiceStarted);
        myHandler.removeMessages(MESSAGE_CHECK_TO_BROADCAST);
        if (bClientIdGot==true && bServiceStarted==true){
            broadcastToStartPush();
        }else{
            myHandler.sendEmptyMessageDelayed(MESSAGE_CHECK_TO_BROADCAST,1000);
        }

    }


    private static String getDeviceID(){

        String devid = null;
        devid = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
//        Log.d(TAG, "getDevid: ---------------------->>>>devid = "+devid+" length = "+devid.length());
        return devid;

    }

    public static void stopCommunicator(){
        stopPushService();
    }
    public static void stopIMWork(){
        Log.d(TAG, "stopPushWork: 调用设备下线》》》》》lianluoReceiver = "+lianluoReceiver+" mContext = "+mContext);
        if (lianluoReceiver != null && (mContext!=null)) {
            try {
                mContext.unregisterReceiver(lianluoReceiver);
            }catch (Exception e){
                e.printStackTrace();
            }
            stopPushService();
        }
    }

    private static void stopPushService() {
        Intent stopIntent = new Intent(LianluoComunicatorMessages.SERVICE_SERVICE_PUSH_STOP_CTION);
        stopIntent.putExtra("packagename",packageName);
        mContext.sendBroadcast(stopIntent);
    }


    private static void startPushService(String pkgName){
        Intent serviceIntent = new Intent();
        serviceIntent.putExtra("packagename",pkgName);
        serviceIntent.setClass(mContext,LianluoPushService.class);
        mContext.startService(serviceIntent);
    }

    private static void registBroadCastReceiver(){
        //注册广播接收..........
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(packageName);
        intentFilter.addAction(LianluoComunicatorMessages.SERVICE_STATED_ACTION);
        intentFilter.addAction(LianluoComunicatorMessages.SERVICE_LIANLUO_IM_CONNECTED_ACTION);
        intentFilter.addAction(LianluoComunicatorMessages.GETTAGS_BACK_ACTION);
        intentFilter.addAction(LianluoComunicatorMessages.SERVICE_SERVICE_CHECK_BACK_ACTION);
        intentFilter.addAction(LianluoComunicatorMessages.SERVICE_GET_DEVICELIST_BACK_ACTION);
        intentFilter.addAction(LianluoComunicatorMessages.SERVICE_SEND_MESSAGE_FAILED_ACTION);
        intentFilter.addAction(LianluoComunicatorMessages.SERVICE_SEND_MESSAGE_SUCESS_ACTION);
        intentFilter.addAction(LianluoComunicatorMessages.SERVICE_ADD_DEVICELIST_SUCESS_ACTION);
        intentFilter.addAction(LianluoComunicatorMessages.SERVICE_ADD_DEVICELIST_FAILED_ACTION);
        intentFilter.addAction(LianluoComunicatorMessages.SERVICE_DEL_DEVICELIST_FAILED_ACTION);
        intentFilter.addAction(LianluoComunicatorMessages.SERVICE_DEL_DEVICELIST_SUCESS_ACTION);

        mContext.registerReceiver(lianluoReceiver,intentFilter);

    }

    public static void startIMWork(Context context,String key,String secret,String pkgName,OnMessageListener listener){
        mContext = context;
        appkey = key;
        appsecret = secret;
        packageName = pkgName;
        myListener = listener;

        registBroadCastReceiver();

        // TODO: 2016/10/10 访问服务器，上报设备并返回数据
        new Thread(){
            @Override
            public void run() {
                Log.d(TAG, "checkValuesValidByHead >> Thread >> run: appkey = "+appkey+" appsecret = "+appsecret+" packagename = "+packageName);
                startcheckValidByHead(appkey,appsecret,packageName,getDeviceID());
            }
        }.start();

        //检查服务是否启动，如启动，直接发送，否则启动服务。
        checkServiceStartStatus();
        myHandler.sendEmptyMessageDelayed(MESSAGE_CHECK_SERVICE_TIMEOUT,5000);

        // TODO: 2016/10/10 启动定时监测。

        myHandler.sendEmptyMessage(MESSAGE_CHECK_TO_BROADCAST);
        myHandler.sendEmptyMessageDelayed(MESSAGE_CHECK_TIMEOUT_OR_FAILED,20000);
    }

    private static void checkServiceStartStatus() {

        Log.d(TAG, "checkServiceStartStatus: 联络推送服务是否已经启动。。。");
        Intent intent = new Intent(LianluoComunicatorMessages.SERVICE_SERVICE_CHECK_ACTION);
        intent.putExtra("external",packageName);
        mContext.sendBroadcast(intent);
    }





    private static void startcheckValidByHead(String appkey,
                                              String appsecret,
                                              String packagename,
                                              String devid) {

        Log.d(TAG, "startcheckValidByHead: ");
        HttpClient client = new DefaultHttpClient();
        String url = VALID_CHECK_URL+API_REPORT_DEVICES;
        Log.d(TAG, "startcheckValidByHead: url = "+url +" devid = "+devid);

        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("device_id", devid));
        nvps.add(new BasicNameValuePair("device_type", "0"));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String userName = appkey;
        String password = appsecret;
        byte[] encodedPassword = (userName + ":" + password).getBytes();
        Base64Encoder encoder = new Base64Encoder();
        httpPost.addHeader("Authorization","Basic "+encoder.encode(encodedPassword));
        httpPost.addHeader("X-PACKAGE-NAME",packagename);
        httpPost.addHeader("X-DEVICE-TYPE","android");
        Log.d(TAG, "startcheckValidByHead: postHeader = "+httpPost.getAllHeaders().toString());
        try {
            HttpResponse res = client.execute(httpPost);
            //返回大于200小于300的情况下默认为返回正确。

            if ((res.getStatusLine().getStatusCode() >= HttpStatus.SC_OK) && (res.getStatusLine().getStatusCode() < HttpStatus.SC_MULTIPLE_CHOICES)) {
                String strResult = EntityUtils.toString(res.getEntity());
                Log.d(TAG, "[SUCCESS ]startcheckValidByHead: strResult = "+strResult);
                clientID = getClientID(strResult);
                bClientIdGot = true;


            }else if ((res.getStatusLine().getStatusCode() >= HttpStatus.SC_BAD_REQUEST)){
                //错误识别

                String strResult = EntityUtils.toString(res.getEntity());
                Log.d(TAG, "[ERROR ]startcheckValidByHead: strResult = "+strResult);
            }

        } catch (Exception e) {
            e.printStackTrace();

//            throw new RuntimeException(e);
        } finally{
            //关闭连接 ,释放资源
            client.getConnectionManager().shutdown();
        }

    }

    private static String getClientID(String strResult) {
        try {
            JSONObject resultObj = new JSONObject(strResult);

            String clientID = resultObj.getString("client_id");
            return clientID;

        } catch (JSONException e) {
            e.printStackTrace();
            return null;

        }

    }
    public static void updateDeviceList(ArrayList<String> devlist){

        Intent intent = new Intent(LianluoComunicatorMessages.SERVICE_UPDATE_DEVICELIST_ACTION);
        intent.putExtra("packagename",packageName);
        intent.putExtra("devlist",devlist);
        mContext.sendBroadcast(intent);

    }

    public static void  getClientID() {
//        String clientIDString = "ClientID :"+clientID;
        myListener.onGetClientId(clientID);
    }

    public static void getDeviceList(){
        Log.d(TAG, "getDeviceList: 获取设备列表");
        Intent intent = new Intent(LianluoComunicatorMessages.SERVICE_GET_DEVICELIST_ACTION);
        intent.putExtra("packagename",packageName);
        mContext.sendBroadcast(intent);
    }

    /**
     * 订阅设备增加设备
     * @param deviceID
     */
    public static void addDevice(String deviceID){

        Log.d(TAG, "addDevice:  addDevices = "+deviceID);
        Intent intent = new Intent(LianluoComunicatorMessages.SERVICE_SUBCRIBE_DEVICEID_ACTION);
        intent.putExtra("packagename",packageName);
        intent.putExtra("devid",deviceID);
        mContext.sendBroadcast(intent);
    }

    /**
     * 删除设备
     * @param deviceID
     */
    public static void delDevice(String deviceID){
        Intent intent = new Intent(LianluoComunicatorMessages.SERVICE_DESUBCRIBE_DEVICEID_ACTION);
        intent.putExtra("packagename",packageName);
        intent.putExtra("devid",deviceID);
        mContext.sendBroadcast(intent);
    }
    public static void isDeviceOnline(String deviceID){

        Intent intent = new Intent(LianluoComunicatorMessages.SERVICE_SEND_MESSAGE_DEVICE_ONLINE_ACTION);
        intent.putExtra("packagename",packageName);
        intent.putExtra("devid",deviceID);
        mContext.sendBroadcast(intent);
    }



    /**
     * 发送消息
     * @param topic
     * @param message
     */
    public static void sendMessage(String appkey,String topic,String message,OnSendMessageListener listener){
        Log.d(TAG, "sendMessage: >>>>>>>>>>>>   listener = "+listener+" appkey = "+appkey+" topic = "+topic);
        String myAppkey = null;
        Intent intent = new Intent(LianluoComunicatorMessages.SERVICE_SEND_MESSAGE_ACTION);
        intent.putExtra("packagename",packageName);
        if (appkey==null){
            myAppkey ="";
        }else{
            myAppkey = appkey;
        }
        intent.putExtra("appkey",myAppkey);
        intent.putExtra("topic",topic);
        intent.putExtra("message",message);
        mContext.sendBroadcast(intent);

        myMessageListener = listener;
        Log.d(TAG, "sendMessage: >>>>>>>>>>>>   myMessageListener = "+myMessageListener);

    }


}
