package com.lianluo.lianluoIM;

/**
 * Created by Tristan on 2016/10/24.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.hawtdispatch.Dispatch;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.Listener;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.fusesource.mqtt.client.Tracer;
import org.fusesource.mqtt.codec.MQTTFrame;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 联络IM
 *
 */
public class LianluoComunicator {

    MQTT myMqttClient = null;
    public LianluoPusherBean pusherBean = null;
    Context mContext = null;
    LianluoPushSharePreferencesManager lianlSpManager = null;
    List<String> tagList = new ArrayList<String>();
    List<String> devList = new ArrayList<String>();

    public static final String		TAG = "LianluoIM";
    public boolean isbIsConnected = false;


    static CallbackConnection callbackConnection = null;
    // the IP address, where your MQTT broker is running.
    private static final String		MQTT_HOST = "139.198.9.41";   //"139.198.9.141";  -----test server
    // the port at which the broker is running.
    private static int				MQTT_BROKER_PORT_NUM      = 1883;
    // Let's not use the MQTT persistence.

    // We don't need to remember any state between the connections, so we use a clean start.
    private static boolean			MQTT_CLEAN_START          = false;
    // Let's set the internal keep alive for MQTT to 15 mins. I haven't tested this value much. It could probably be increased.
    private static short			MQTT_KEEP_ALIVE           = 60 * 3;
    // Set quality of services to 0 (at most once delivery), since we don't want push notifications
    // arrive more than once. However, this means that some messages might get lost (delivery is not guaranteed)
    private static int[]			MQTT_QUALITIES_OF_SERVICE = { 2 } ;
    private static int				MQTT_QUALITY_OF_SERVICE   = 2;
    // The broker should not retain any messages.
    private static boolean			MQTT_RETAINED_PUBLISH     = false;

    // MQTT client ID, which is given the broker. In this example, I also use this for the topic header.
    // 客户端标识.
    public static String			MQTT_CLIENT_ID = "lianluo";


    private static final String     MQTT_SYS_DEVICE_PREFIX = "$SYS/brokers/+/clients/";
    private static final String     MQTT_SYS_DEVICE_CLIENTS = "/clients/";
    private static final String     MQTT_ONLINE = "/connected";
    private static final String     MQTT_OFFONLINE = "/disconnected";
    public boolean bIsConnected = false;
    public int connectStatus = 0;  //连接状态更新， 0-未连接；1-正在连接；2-连接上。

    private void loadtagList(){
        String taglistString = lianlSpManager.getTags(pusherBean.packageName);
        if (taglistString!=null&&taglistString.length()>0) {
            String[] array = taglistString.split(",");
            for (int index = 0; index < array.length; index++) {
                tagList.add(array[index]);
            }
        }
    }


    public void reconnect(){

        connect();
    }

    public LianluoComunicator(Context context, String appkey, String appsecret, String packagename, String clientId){
        Log.d(TAG, "LianluoPusher: packagename = "+packagename);
        pusherBean = new LianluoPusherBean();
        pusherBean.account = appkey;
        pusherBean.key = appsecret;
        pusherBean.packageName = packagename;
        pusherBean.clientID = clientId;
        mContext = context;
        lianlSpManager = new LianluoPushSharePreferencesManager(mContext);
        loadtagList();
        loadDevList();

        initMqttClient();
    }

    private void loadDevList() {

        String devlistString = lianlSpManager.getDeviceList(pusherBean.packageName);
        Log.d(TAG, "loadDevList: >>>>>>> devlistString = "+devlistString);
        if (devlistString!=null && devlistString.length()>0){
            String[] myArray = devlistString.split(",");
            for (int index1 = 0;index1<myArray.length;index1++){
                devList.add(myArray[index1]);
            }
        }
        Log.d(TAG, "loadDevList: >>>>>>>>>>>>设备列表大小：：  devList = "+devList.size());
    }




    private void connect() {
        Log.d(TAG, "connect: ");
        if (myMqttClient!=null){
            try {
                connectStatus = 1;
                final String mqttConnSpec = "tcp://" + MQTT_HOST + ":" + MQTT_BROKER_PORT_NUM;
                Log.d(TAG, "connect: "+mqttConnSpec);
                myMqttClient.setHost(mqttConnSpec);
                myMqttClient.setClientId(pusherBean.clientID); //用于设置客户端会话的ID。在setCleanSession(false);被调用时，MQTT服务器利用该ID获得相应的会话。此ID应少于23个字符，默认根据本机地址、端口和时间自动生成
                myMqttClient.setCleanSession(false); //若设为false，MQTT服务器将持久化客户端会话的主体订阅和ACK位置，默认为true
                myMqttClient.setKeepAlive((short) 60);//定义客户端传来消息的最大时间间隔秒数，服务器可以据此判断与客户端的连接是否已经断开，从而避免TCP/IP超时的长时间等待
                Log.d(TAG, "connect: account = "+pusherBean.account+" key = "+pusherBean.key);
                myMqttClient.setUserName(pusherBean.account);//服务器认证用户名
                myMqttClient.setPassword(pusherBean.key);//服务器认证密码

                myMqttClient.setWillTopic("willTopic");//设置“遗嘱”消息的话题，若客户端与服务器之间的连接意外中断，服务器将发布客户端的“遗嘱”消息
                myMqttClient.setWillMessage("willMessage");//设置“遗嘱”消息的内容，默认是长度为零的消息
                myMqttClient.setWillQos(QoS.AT_MOST_ONCE);//设置“遗嘱”消息的QoS，默认为QoS.ATMOSTONCE
                myMqttClient.setWillRetain(false);//若想要在发布“遗嘱”消息时拥有retain选项，则为true
                myMqttClient.setVersion("3.1.1");

                //失败重连接设置说明
                myMqttClient.setConnectAttemptsMax(10L);//客户端首次连接到服务器时，连接的最大重试次数，超出该次数客户端将返回错误。-1意为无重试上限，默认为-1
                myMqttClient.setReconnectAttemptsMax(3L);//客户端已经连接到服务器，但因某种原因连接断开时的最大重试次数，超出该次数客户端将返回错误。-1意为无重试上限，默认为-1
                myMqttClient.setReconnectDelay(10L);//首次重连接间隔毫秒数，默认为10ms
                myMqttClient.setReconnectDelayMax(30000L);//重连接间隔毫秒数，默认为30000ms
                myMqttClient.setReconnectBackOffMultiplier(1);//设置重连接指数回归。设置为1则停用指数回归，默认为2

                myMqttClient.setReceiveBufferSize(65536);//设置socket接收缓冲区大小，默认为65536（64k）
                myMqttClient.setSendBufferSize(65536);//设置socket发送缓冲区大小，默认为65536（64k）
                myMqttClient.setTrafficClass(8);//设置发送数据包头的流量类型或服务类型字段，默认为8，意为吞吐量最大化传输

                //带宽限制设置说明
                myMqttClient.setMaxReadRate(0);//设置连接的最大接收速率，单位为bytes/s。默认为0，即无限制
                myMqttClient.setMaxWriteRate(0);//设置连接的最大发送速率，单位为bytes/s。默认为0，即无限制

                //选择消息分发队列
                //若没有调用方法setDispatchQueue，客户端将为连接新建一个队列。如果想实现多个连接使用公用的队列，显式地指定队列是一个非常方便的实现方法

                myMqttClient.setDispatchQueue(Dispatch.createQueue(pusherBean.clientID));


                myMqttClient.setTracer(new Tracer(){
                    @Override
                    public void onReceive(MQTTFrame frame) {
                        System.out.println("setTracer： onReceive: "+frame);
                    }

                    @Override
                    public void onSend(MQTTFrame frame) {
                        System.out.println("setTracer： onSend: "+frame);
                    }

                    @Override
                    public void debug(String message, Object... args) {
                        System.out.println(String.format("debug: "+message, args));
                    }
                });


                //使用回调式API
                callbackConnection = myMqttClient.callbackConnection();

                //连接监听
                callbackConnection.listener(new Listener() {

                    //接收订阅话题发布的消息
                    @Override
                    public void onPublish(UTF8Buffer topic, Buffer payload, Runnable onComplete) {
                        String topicString = topic.toString();

                        Log.d(TAG, "onPublish:收到订阅主题发布的消息receive   topicString = "+ topicString );

//                      System.out.println("=============收到订阅主题发布的消息receive msg================>>>>"+new String(payload.toByteArray()));
                        String mqttMessage = getMqttMessage(topic,payload);
                        if (mqttMessage !=null) {

//                        String mqttMessage = new String(payload.toByteArray());//getMqttMessage(topic,payload);
                            Log.d(TAG, "onPublish:receive   mqttMessage = " + mqttMessage);
                            showNotification(mqttMessage);

                            Intent broadCastIntent = new Intent();
                            Log.d(TAG, "onPublish: 发送广播----Action = " + pusherBean.packageName);
                            broadCastIntent.setAction(pusherBean.packageName);
                            broadCastIntent.putExtra("content", mqttMessage);
                            broadCastIntent.putExtra("packagename", pusherBean.packageName);
                            mContext.sendBroadcast(broadCastIntent);
                        }

                    }

                    //连接失败
                    @Override
                    public void onFailure(Throwable value) {
                        System.out.println("===========mqtt 失败 ===========>>>value >>>>"+value.getLocalizedMessage()+"@@@@@@");
                        if (callbackConnection != null ){
                            callbackConnection.disconnect(null);
                        }
                    }

                    //连接断开
                    @Override
                    public void onDisconnected() {
                        isbIsConnected = false;
                        System.out.println("====mqtt 连接断开====="+pusherBean.account+" key "+pusherBean.key+" packagename = "+pusherBean.packageName);
                        Intent connectIntent = new Intent(LianluoComunicatorMessages.SERVICE_LIANLUO_IM_DISCONNECTED_ACTION);
                        connectIntent.putExtra("packagename",pusherBean.packageName);
                        mContext.sendBroadcast(connectIntent);
                    }

                    //连接成功
                    @Override
                    public void onConnected() {
                        isbIsConnected = true;
                        System.out.println("====mqtt 已经连接====="+pusherBean.account+" key "+pusherBean.key+" packagename = "+pusherBean.packageName);
                    }
                });
                //连接
                callbackConnection.connect(new Callback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: MQTT连接成功");
//                        myListener.onConnected();
                        bIsConnected =true;
                        connectStatus = 2;
                        Intent connectIntent = new Intent(LianluoComunicatorMessages.SERVICE_LIANLUO_IM_CONNECTED_ACTION);
                        connectIntent.putExtra("packagename",pusherBean.packageName);
                        mContext.sendBroadcast(connectIntent);
                        //订阅消息
                        subscribeInnerTopics();
                        subscribePreTags();
                        subscribeDeviceOnlineOffline();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.d(TAG, "onFailure: ============连接失败：\n"+throwable.getLocalizedMessage()+"===========");
                        callbackConnection.disconnect(null);//先断开。
                    }
                });

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }
    public void setConnectStatus(boolean status){
        bIsConnected = status;
        if (status == false){
            connectStatus = 0;
        }

    }
    private void subscribeDeviceOnlineOffline() {
        Log.d(TAG, "subscribeDeviceOnlineOffline: 设备列表： "+devList.size());
        if (devList.size()>0) {
            Topic[] myTopics = new Topic[devList.size() * 2];
            for (int index = 0; index < devList.size(); index++) {
                String tDevid = devList.get(index);
                Log.d(TAG, "subscribeDeviceOnlineOffline: 设备ID ： tDevid = " + tDevid);
                myTopics[index * 2] = new Topic(MQTT_SYS_DEVICE_PREFIX + tDevid + MQTT_ONLINE, QoS.AT_MOST_ONCE);
                myTopics[index * 2 + 1] = new Topic(MQTT_SYS_DEVICE_PREFIX + tDevid + MQTT_OFFONLINE, QoS.AT_MOST_ONCE);
            }

            if (callbackConnection != null && myTopics.length > 0) {
                callbackConnection.subscribe(myTopics, new Callback<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG, "subscribePreTags--->onSuccess: 订阅之前设备上下线主题成功");
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.d(TAG, "subscribePreTags--->onSuccess: 订阅之前订阅的主题失败 !!!!");
                    }
                });
            }
        }
    }




    //获取标签列表
    public  String getthisTags(){
        if (tagList==null) {
            return null;
        }
        StringBuilder result=new StringBuilder();
        boolean flag=false;
        for (String string : tagList) {
            if (flag) {
                result.append(",");
            }else {
                flag=true;
            }
            result.append(string);
        }
        return result.toString();
    }

    private String onLineMessage(String deviceid,String onlinestat) {
        MQTTMessage msg = new MQTTMessage();
        msg.notification_builder_id = 1;
        msg.desc = "";
        msg.notification_basic_style = 0x01;
        msg.open_type = 0;
        msg.title = "测试消息";
        msg.url = "";
        msg.from = deviceid;

        msg.custom_content = new HashMap();


        msg.custom_content.put("op","feedback");
        msg.custom_content.put("onlinestat",onlinestat);
        msg.custom_content.put("bindstat","TRUE");

        Gson gson = new Gson();
        String outString = gson.toJson(msg);
        Log.d(TAG, "onLineMessage: outMessage = "+outString);
        return  outString+"\n";

    }

    /**
     * 根据返回的topic以及payload返回新字符串。
     * @param topic   ---返回消息的主题
     * @param payload ---返回数据
     * @return
     */
    private String getMqttMessage(UTF8Buffer topic, Buffer payload) {

        String result = null;
        String topicString = topic.toString();
        String devidString = null;
        String onlineStatString = null;
        Log.d(TAG, "getMqttMessage: topicString = "+topicString);
        if (topicString.contains(MQTT_SYS_DEVICE_CLIENTS)){
            if (topicString.endsWith(MQTT_ONLINE)){
                devidString = topicString.substring(topicString.indexOf(MQTT_SYS_DEVICE_CLIENTS)+MQTT_SYS_DEVICE_CLIENTS.length(),topicString.indexOf(MQTT_ONLINE));
                Log.d(TAG, "getMqttMessage: devidString = "+devidString);
                onlineStatString = "ON";//设备上线
            }else if (topicString.endsWith(MQTT_OFFONLINE)){

                devidString = topicString.substring(topicString.indexOf(MQTT_SYS_DEVICE_CLIENTS)+MQTT_SYS_DEVICE_CLIENTS.length(),topicString.indexOf(MQTT_OFFONLINE));
                Log.d(TAG, "getMqttMessage: devidString = "+devidString);
                onlineStatString = "OFF";//设备下线
            }
            result =onLineMessage(devidString,onlineStatString);

        }else {
            result =new String(payload.toByteArray());
            if (autoSendBack(result) == true){
                result = null;
            }

        }
        return result;

    }

    private boolean autoSendBack(String result) {
        try {
            JSONObject resultObj = new JSONObject(result);
            if (resultObj.has("from")) {
                String fromDeviceID = resultObj.getString("from");
                if (!fromDeviceID.contains("System")) {
                    if (resultObj.has("custom_content")) {
                        JSONObject custoncontentObj = resultObj.getJSONObject("custom_content");
                        if (custoncontentObj.has("op")) {
                            String option = custoncontentObj.getString("op");
                            if (option.equals("get")) {
                                if (custoncontentObj.has("onlinestat")) {
                                    sendBackOnlinStatus(fromDeviceID);
                                    return true;

                                } else if (custoncontentObj.has("prop")) {
                                    String prop = custoncontentObj.getString("prop");
                                    if (prop.equalsIgnoreCase("online")) {

                                        sendBackOnlinStatus(fromDeviceID);
                                        return true;
                                    }

                                } else {
                                    return false;
                                }
                            } else {
                                return false;
                            }
                        }else{
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            }else{
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void sendBackOnlinStatus(String fromID) {

        String sendOutString = onLineMessage(pusherBean.clientID,"ON");
        String topic = pusherBean.getAccount()+"/"+fromID;
        if (callbackConnection!=null){
            callbackConnection.publish(topic, sendOutString.getBytes(), QoS.AT_LEAST_ONCE, true, new Callback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                }

                @Override
                public void onFailure(Throwable throwable) {
                }
            });
        }
    }


    public void sendMessage(String appkey,String topic, final String message){
        String outMesage = getOutMessage(message);
        String outTopic = null;

        Log.d(TAG, "sendMessage: appkey = "+appkey);
        if (appkey != null && appkey.length()>0){
            outTopic = appkey+"/"+topic;
        }else{
            outTopic = pusherBean.getAccount()+"/"+topic;
        }



        Log.d(TAG, "sendMessage: outTopic = "+outTopic+"  message = "+outMesage + "callbackConnection "+callbackConnection);

        if (callbackConnection!=null) {
            callbackConnection.publish(outTopic, outMesage.getBytes(), QoS.AT_LEAST_ONCE,true,new Callback<Void>() {

                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "onSuccess: 发送消息成功 【 "+message+" 】");
                    sendMessageSucess();
                }

                @Override
                public void onFailure(Throwable throwable) {
                    Log.e(TAG, "onSuccess: 发送消息失败  【 "+message+" 】");
                    sendMessageFail();
                }
            });
        }
    }
    private boolean isMQTTJson(String s){

        try {
            JSONObject obj= new JSONObject(s);
            if (obj.has("notification_builder_id")) {
                return true;
            }else{
                return false;
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    

    private String getOutMessage(String message) {
        if (isMQTTJson(message)==false){
            //重新组织
            return getnewOutMessage(message);

        }else{
            return message;
        }

    }
    public void updateDevicelist(String devlist){
        String[] array = devlist.split(",");
        List<String> inDevList = new ArrayList<String>();
        devList.clear();
        for (int i = 0;i<array.length;i++){
            inDevList.add(array[i]);
        }
        devList.addAll(inDevList);
    }

    private String getnewOutMessage(String message) {
        Log.d(TAG, "getnewOutMessage: 重组数据，进行发送 ");
        MQTTMessage msg = new MQTTMessage();
        msg.notification_builder_id = 1;
        msg.desc = "";
        msg.notification_basic_style = 0x01;
        msg.open_type = 0;
        msg.title = "测试消息";
        msg.url = "";
        msg.from = pusherBean.clientID;

        msg.custom_content = new HashMap();

        msg.custom_content.put("devid",pusherBean.clientID);
        msg.custom_content.put("content",message);

        Gson gson = new Gson();
        String outString = gson.toJson(msg);
        Log.d(TAG, "onLineMessage: outMessage = "+outString);
        return outString;
    }

    private void sendMessageFail() {
        Log.d(TAG, "sendMessageSucess: 发送消息失败   **JUKJIK、。。。。。。。。。。。。。。。。");
        Intent intent = new Intent(LianluoComunicatorMessages.SERVICE_SEND_MESSAGE_FAILED_ACTION);
        intent.putExtra("packagename",pusherBean.packageName);
        mContext.sendBroadcast(intent);
    }

    private void sendMessageSucess() {
        Log.d(TAG, "sendMessageSucess: 发送消息成功。。。。。。。。。。。。。。。。");
        Intent intent = new Intent(LianluoComunicatorMessages.SERVICE_SEND_MESSAGE_SUCESS_ACTION);
        intent.putExtra("packagename",pusherBean.packageName);
        mContext.sendBroadcast(intent);
    }

    //删除标签
    public void removeTags(String tags){
        String[] array = tags.split(",");
        Log.d(TAG, "removeTags: arraylength = "+array.length);
        List<String> remTags = new ArrayList<String>();


        remTags.clear();
        boolean hasChanged = false;

        for (int index = 0; index< array.length;index++){
            if (tagList.contains(array[index])){
                //list中删除
                hasChanged = true;
                tagList.remove(array[index]);
                remTags.add(array[index]);
            }
        }
        //保存数据
        if (hasChanged == true) {

            saveTags();

        }

        if (remTags.size()>0) {
            UTF8Buffer[] topics = new UTF8Buffer[remTags.size()];
            for (int i= 0;i<remTags.size();i++){
                topics[i] = new UTF8Buffer(pusherBean.account+"/AndroidTags/"+remTags.get(i));
            }

            if (callbackConnection != null) {
                callbackConnection.unsubscribe(topics, new Callback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: 退订标签成功");
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.d(TAG, "onFailure: 退订标签失败");
                    }
                });
            }
        }
    }

    private void saveTags(){
        //将taglis转化成String
        String tags = getthisTags();
        Log.d(TAG, "subscribeTags: 保存tags------------>>>>>>>>>>>>>>tags = "+tags);

        lianlSpManager.saveTags(pusherBean.packageName,tags);
    }



    //订阅标签主题。
    public void setTags(String tags){

        String[] array = tags.split(",");
        Log.d(TAG, "subscribePreTags: array.size = "+array.length+"  tags = "+tags);

        List<String> addTags = new ArrayList<String>();
        addTags.clear();
        boolean haschanged = false;
        for (int i = 0; i < array.length; i++) {
            Log.d(TAG, "subscribeTags: 订阅tag》》》》》 name = "+array[i]);
            if (array[i].length()>0 && (tagList.contains(array[i])==false)) {
                Log.d(TAG, "setTags: subscribeTags: 订阅22222    tag》》》》》"+array[i]);
                haschanged = true;
                addTags.add(array[i]);
                tagList.add(array[i]);
                Log.d(TAG, "subscribePreTags: tagTopic:::>>>> "+pusherBean.account + "/" + array[i]);
            }
        }
        if (haschanged==true) {
            saveTags();
        }
        Log.d(TAG, "setTags: addtags = "+addTags.size());


        if (addTags.size()>0) {
            final Topic[] tagTopics = new Topic[addTags.size()];
            //每一个addtag进行订阅，
            for (int index = 0;index<addTags.size();index++){
                tagTopics[index] = new Topic(pusherBean.account+"/AndroidTags/"+addTags.get(index),QoS.AT_MOST_ONCE);
            }

            if (callbackConnection != null) {
                callbackConnection.subscribe(tagTopics, new Callback<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        for (Topic tpc : tagTopics) {
                            Log.d(TAG, "onSuccess: >>>>订阅标签成功》》》》" + tpc.name().toString());
                        }
                    }

                    @Override
                    public void onFailure(Throwable throwable) {

                        Log.d(TAG, "onSuccess: >>>>订阅标签失败》》》》" + throwable.getLocalizedMessage());

                    }
                });
            }
        }

    }

    //订阅之前订阅的标签专题.
    private void subscribePreTags(){
        if (tagList.size()>0){
            Topic[] topics = new Topic[tagList.size()];
            for (int index=0; index<tagList.size();index++){
                topics[index] = new Topic(pusherBean.account+"/AndroidTags/"+tagList.get(index),QoS.AT_MOST_ONCE);
            }

            if (callbackConnection!=null){
                callbackConnection.subscribe(topics, new Callback<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG, "subscribePreTags--->onSuccess: 订阅之前订阅的主题成功");
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.d(TAG, "subscribePreTags--->onSuccess: 订阅之前订阅的主题失败 !!!!");
                    }
                });
            }
        }
    }



    //显示通知栏
    private  void showNotification(String topicStr) {
        Log.d(TAG, "showNotification: >>>显示通知栏。topicStr = "+topicStr);
        String notificationContent;
        String notificationTitle;
        String pkgContent = null;
        int openType = -1;

        try {
            JSONObject notificationObj =new JSONObject(topicStr);

            Log.d(TAG, "showNotification: topicObj.getInt(\"notification_builder_id\") ="+notificationObj.getInt("notification_builder_id"));

            if (notificationObj.has("notification_builder_id") && (notificationObj.getInt("notification_builder_id") == 0)) {
                notificationContent = notificationObj.getString("description");
                notificationTitle = notificationObj.getString("title");
                openType = notificationObj.getInt("open_type");
                Log.d(TAG, "[通知栏]showNotification: notificationContent = " + notificationContent);
                Log.d(TAG, "[通知栏]showNotification: notificationTitle = " + notificationTitle);
                Log.d(TAG, "[通知栏]showNotification: openType = " + openType);


                Notification.Builder notifyBuilder = new Notification.Builder(mContext);
                notifyBuilder.setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏中的小图片，尺寸一般建议在24×24， 这里也可以设置大图标
                        .setTicker(notificationTitle) // 设置显示的提示文字
                        .setContentTitle(notificationTitle)// 设置显示的标题
                        .setContentText(notificationContent)// 消息的详细内容
                        .getNotification(); // 需要注意build()是在API level16及之后增加的，在API11中可以使用getNotificatin()来代替
                if (openType == 1) {  //打开URL
                    Log.d(TAG, "showNotification: 打开网页-------------------》》》》");
                    String url = notificationObj.getString("url");
                    Log.d(TAG, "showNotification: 打开网页-------------------》》》》"+url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
                    notifyBuilder.setContentIntent(pendingIntent);
                } else if (openType == 2) {

                    pkgContent = notificationObj.getString("pkg_content");
                    Log.d(TAG, "[通知栏]showNotification: 打开app  pkgContent= " + pkgContent + " mContext.getPackageName() = " + mContext.getPackageName());

                    Intent intent = new Intent();

                    if (pkgContent == null || pkgContent.length() == 0) {
                        intent.setPackage(mContext.getPackageName());
                    } else {
                        String content = getPkgContent(pkgContent);
                        intent.setClassName(mContext.getPackageName(), mContext.getPackageName() + content);
                    }

                    PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
                    notifyBuilder.setContentIntent(pendingIntent);
                } else if (openType == 0) {

                    Log.d(TAG, "showNotification: 打开app");
//                Intent intent = new Intent();
                    Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(pusherBean.packageName);
                    if (intent!=null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        notifyBuilder.setContentIntent(pendingIntent);
                    }
                }

                Notification notify = notifyBuilder.getNotification();
                notify.defaults = Notification.DEFAULT_SOUND;
                notify.flags |= Notification.FLAG_AUTO_CANCEL;
                NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(1, notify);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
    //转化全名
    private static String getPkgContent(String pkgContent) {
        if (pkgContent.startsWith(".")){
            return pkgContent;
        }else{
            return "."+pkgContent;
        }
    }
    //END    显示通知.....


    /**
     * 自动订阅当前设备ID的相关topic
     *
     */
    private void subscribeInnerTopics() {
        Log.d(TAG, "subscribeInnerTopics: 订阅设备ID的主题");
        final Topic[] topics = {new Topic(pusherBean.account+"/"+pusherBean.clientID, QoS.AT_MOST_ONCE),
                new Topic(pusherBean.account, QoS.AT_MOST_ONCE),
                new Topic(pusherBean.account+"/pushnotification",QoS.AT_MOST_ONCE)};

        if (callbackConnection!=null){

            callbackConnection.subscribe(topics, new Callback<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    for(Topic tpc:topics) {
                        Log.d(TAG, "onSuccess: >>>>订阅消息成功》》》》"+tpc.name().toString());
                    }
                }

                @Override
                public void onFailure(Throwable throwable) {
                    Log.d(TAG, "onSuccess: >>>>订阅消息失败》》》》"+throwable.getLocalizedMessage());

                }
            });
        }
    }



    private void initMqttClient() {
        Log.d(TAG, ">>>>>>>>>>>>>>>>>>initMqttClient: ");
        myMqttClient = new MQTT();
        connect();
    }


    public void disconnect() {

        Log.d(TAG, "disconnect: myMqttClient = "+myMqttClient+" callbackConnection = "+callbackConnection);
        if (myMqttClient!=null){
            if (callbackConnection!=null){
                callbackConnection.disconnect(new Callback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        disconnectSucess();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        disconnectFailure();
                    }
                });
            }
        }
    }

    private void disconnectFailure() {
        Intent itent = new Intent(LianluoComunicatorMessages.SERVICE_SERVICE_PUSH_STOP_BACK_CTION);
        itent.putExtra("packagename",pusherBean.packageName);
        itent.putExtra("result",false);
        mContext.sendBroadcast(itent);
    }

    private void disconnectSucess() {
        Intent itent = new Intent(LianluoComunicatorMessages.SERVICE_SERVICE_PUSH_STOP_BACK_CTION);
        itent.putExtra("packagename",pusherBean.packageName);
        itent.putExtra("result",true);
        mContext.sendBroadcast(itent);

    }

    public static void subcribeTopics(final Topic[] myTopics) {

        if (callbackConnection!=null) {
            callbackConnection.subscribe(myTopics, new Callback<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    for (Topic tpc : myTopics) {
                        Log.d(TAG, "onSuccess: 订阅消息成功》》》" + tpc.toString());
                    }
                }

                @Override
                public void onFailure(Throwable throwable) {
                    Log.d(TAG, "onSuccess: 订阅消息失败》》》");
                }
            });

        }

    }

    static String[] tmpTopics = null;


    public  void desubcribeTopics(String[] topics) {
        tmpTopics = topics;
        UTF8Buffer[] buffer = new UTF8Buffer[topics.length];
        for (int bufferIndex = 0; bufferIndex<topics.length;bufferIndex++){

            buffer[bufferIndex]=new UTF8Buffer(topics[bufferIndex]);
        }

        if (callbackConnection!=null) {

            callbackConnection.unsubscribe(buffer, new Callback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    for (String tpc : tmpTopics) {
                        Log.d(TAG, "onSuccess: 退订消息成功》》》" + tpc);
                    }
                }

                @Override
                public void onFailure(Throwable throwable) {
                    Log.d(TAG, "onSuccess: 退订消息失败》》》" + throwable.getLocalizedMessage());
                }
            });

        }

    }

    /**订阅设备上下线消息
     *
     * 功能： 订阅上下线功能，增加到设备列表，保存设备列表。
     * @param deviceId
     */

    public  void subcribeOnlineStatusByDevids(String deviceId) {
        Log.d(TAG, "subcribeOnlineStatusByDevids: deviceId = "+deviceId);
        String[] devids = deviceId.split(",");
        final List<String> addList = new ArrayList<String>();

        if (devids.length>0) {

            boolean haschanged = false;
            for (int index = 0; index < devids.length; index++) {
                String devidStr = devids[index];
                if (devidStr.length() > 0 && (devList.contains(devidStr) == false)) {
                    //不存在在devlist中的才订阅和添加，否则认为不订阅
                    addList.add(devidStr);
                    haschanged = true;
                }
            }

            if (haschanged == true) {

                final Topic[] onlineTopics = new Topic[devids.length * 2];
                for (int i =0;i<addList.size();i++){
                    String deviceID = addList.get(i);
                    onlineTopics[i * 2] = new Topic(MQTT_SYS_DEVICE_PREFIX + deviceID + MQTT_ONLINE, QoS.AT_MOST_ONCE);
                    onlineTopics[i * 2 + 1] = new Topic(MQTT_SYS_DEVICE_PREFIX + deviceID + MQTT_OFFONLINE, QoS.AT_MOST_ONCE);
                }

                if (callbackConnection!=null) {
                    callbackConnection.subscribe(onlineTopics, new Callback<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            for (Topic tpc : onlineTopics) {
                                Log.d(TAG, "onSuccess: 订阅消息成功》》》" + tpc.toString());
                            }
                            devList.addAll(addList);
                            addDevicesSucess();
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            Log.d(TAG, "onFailure: 订阅消息失败》》》");
                            addDevicesFailed();
                        }
                    });

                }
            }else{
                //此处有bug，设备列表中已经存在，则不需要返回错误，直接返回正确。
//                addDevicesFailed();
                Log.d(TAG, "subcribeOnlineStatusByDevids: 11111>>>>>>>>>>数据为空。。。");
            }
        }else{
            Log.d(TAG, "subcribeOnlineStatusByDevids: 22222>>>>>>>>>>数据为空。。。");
            addDevicesFailed();

        }


    }

    /**
     * 添加设备成功
     */
    private void addDevicesFailed() {
        Intent intent = new Intent(LianluoComunicatorMessages.SERVICE_ADD_DEVICELIST_FAILED_ACTION);
        intent.putExtra("packagename",pusherBean.packageName);
        mContext.sendBroadcast(intent);

    }

    /**
     * 添加设备失败
     */
    private void addDevicesSucess() {
        saveDevList();
        Intent intent = new Intent(LianluoComunicatorMessages.SERVICE_ADD_DEVICELIST_SUCESS_ACTION);
        intent.putExtra("packagename",pusherBean.packageName);
        mContext.sendBroadcast(intent);
    }

    private String getDevListString(){

        String devs = null;
        Log.d(TAG, "getDevListString: devList = "+devList);
        if (devList!=null) {
            StringBuilder result=new StringBuilder();
            boolean flag=false;
            for (String string : devList) {
                if (flag) {
                    result.append(",");
                }else {
                    flag=true;
                }
                result.append(string);
            }
            devs = result.toString();

        }
        return devs;
    }

    /**
     * 保存设备列表
     */
    private void saveDevList() {
        String devsString = getDevListString();
        Log.d(TAG, "saveDevList: 保存数据设备列表》》》》》》 devsString = "+devsString);
        if (devsString != null) {
            lianlSpManager.saveDeviceList(pusherBean.packageName, devsString);
        }

    }

    /**
     *
     * 设备删除， 功能： 取消订阅上下线，删除列表中存在，保存数据。
     * @param deviceId
     */

    public  void deSubcribeOnlineStatByDevids(String deviceId) {

        String[] devids = deviceId.split(",");

        final List<String> remDevs = new ArrayList<String>();
        remDevs.clear();

        boolean hasChanged = false;

        //组织数据
        for (int index = 0; index< devids.length;index++){
            String devstr = devids[index];
            if (devList.contains(devstr)){
                //list中删除
                hasChanged = true;
                remDevs.add(devstr);
            }
        }
        //保存数据
        if (hasChanged == true) {

            //重新刷新并删除订阅
            String[] onlineTopicStrings = new String[remDevs.size() * 2];
            for (int index = 0;index<remDevs.size();index++){

                String devidStr = remDevs.get(index);
                Log.d(TAG, "deSubcribeOnlineStatByDevids: devidStr = "+devidStr);
                onlineTopicStrings[index*2]=new String(MQTT_SYS_DEVICE_PREFIX+devidStr+MQTT_ONLINE);
                onlineTopicStrings[index*2+1]=new String(MQTT_SYS_DEVICE_PREFIX+devidStr+MQTT_OFFONLINE);

            }
            tmpTopics = onlineTopicStrings;
            Log.d(TAG, "deSubcribeOnlineStatByDevids:onlineTopicStrings.length =  "+onlineTopicStrings.length);
            UTF8Buffer[] buffer = new UTF8Buffer[onlineTopicStrings.length];
            for (int bufferIndex = 0; bufferIndex<onlineTopicStrings.length;bufferIndex++){
                buffer[bufferIndex] = new UTF8Buffer(onlineTopicStrings[bufferIndex]);
                Log.d(TAG, "deSubcribeOnlineStatByDevids: buffer = "+buffer[bufferIndex].toString());
            }

            Log.d(TAG, "deSubcribeOnlineStatByDevids: ------>>>>   buffer = "+buffer.length);

            if (callbackConnection!=null) {

                callbackConnection.unsubscribe(buffer, new Callback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        for (String tpc : tmpTopics) {

                            Log.d(TAG, "onSuccess: 退订消息成功》》》" + tpc);
                        }
                        devList.removeAll(remDevs);
                        saveDevList();
                        delDeviceSucess();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        delDeviceFailed();
                        Log.d(TAG, "onSuccess: 退订消息失败》》》" + throwable.getLocalizedMessage());
                    }
                });

            }

        }else{
            Log.d(TAG, "deSubcribeOnlineStatByDevids: 参数为空 不能退订 -----》》》");
        }
            
    }

    private void delDeviceFailed() {
        if (mContext!=null) {
            Intent intent = new Intent(LianluoComunicatorMessages.SERVICE_DEL_DEVICELIST_FAILED_ACTION);
            intent.putExtra("packagename", pusherBean.packageName);
            mContext.sendBroadcast(intent);
        }
    }

    private void delDeviceSucess() {
        if (mContext!=null) {
            Intent intent = new Intent(LianluoComunicatorMessages.SERVICE_DEL_DEVICELIST_SUCESS_ACTION);
            intent.putExtra("packagename", pusherBean.packageName);
            mContext.sendBroadcast(intent);
        }
    }
    private String isOnlineString(){
        String outStr = null;
        MQTTMessage message = new MQTTMessage();
        message.notification_builder_id = 1;
        message.desc = "";
        message.notification_basic_style = 0x01;
        message.open_type = 0;
        message.title = "测试消息";
        message.url = "";
        message.from = pusherBean.getChannelID();
        message.custom_content = new HashMap();
        message.custom_content.put("op","feedback");
        message.custom_content.put("prop","online");

        Gson gson = new Gson();
        outStr = gson.toJson(message);
        Log.d(TAG, "getOnlineCheckDevice: outStr = "+outStr);

        return outStr;

    }

    private String getOnlineCheckDevice(){
        String outStr = null;
        MQTTMessage message = new MQTTMessage();
        message.notification_builder_id = 1;
        message.desc = "";
        message.notification_basic_style = 0x01;
        message.open_type = 0;
        message.from = pusherBean.getChannelID();
        message.title = "测试消息";
        message.url = "";
        message.custom_content = new HashMap();
        message.custom_content.put("op","get");
        message.custom_content.put("onlinestat","true");

        Gson gson = new Gson();
        outStr = gson.toJson(message);
        Log.d(TAG, "getOnlineCheckDevice: outStr = "+outStr);

        return outStr;

    }

    public String getDevicelist() {
        if (devList==null) {
            return null;
        }
        StringBuilder result=new StringBuilder();
        boolean flag=false;
        for (String string : devList) {
            if (flag) {
                result.append(",");
            }else {
                flag=true;
            }
            result.append(string);
        }
        return result.toString();
    }

    public void checkDeviceOnlineStatus(String devid) {
        String message = getOnlineCheckDevice();
        String topic = pusherBean.account+"/"+devid;
        Log.d(TAG, "checkDeviceOnlineStatus: topic = "+topic);
        if (callbackConnection!=null){

            callbackConnection.publish(topic, message.getBytes(),QoS.AT_LEAST_ONCE,true,new Callback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                }

                @Override
                public void onFailure(Throwable throwable) {

                }
            });

        }


    }
}
