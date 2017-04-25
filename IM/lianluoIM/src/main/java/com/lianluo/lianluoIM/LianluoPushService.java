package com.lianluo.lianluoIM;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import static com.lianluo.lianluoIM.LianluoIM.TAG;

public class LianluoPushService extends Service {
    private static final int NETWORK_NONE = -1;
    /**
     * 移动网络
     */
    private static final int NETWORK_MOBILE = 0;
    /**
     * 无线网络
     */
    private static final int NETWORK_WIFI = 1;
    BroadcastReceiver pushStartReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: 接收到打开客户端的广播》》》》》intent = "+intent.getAction());
            String action = intent.getAction();
            if (action.equals(LianluoComunicatorMessages.STARTPUSH_ACTION)) {

                String appKey = intent.getStringExtra("appkey");
                String appSecret = intent.getStringExtra("appsecret");
                String packageName = intent.getStringExtra("pkgname");
                String clientID = intent.getStringExtra("clientid");
                startPushClient(appKey, appSecret, packageName, clientID);
            }else if (action.equals(LianluoComunicatorMessages.SETTAGS_ACTION)){
                // TODO: 2016/10/12 设置Tags
                String packageName= intent.getStringExtra("packagename");
                String tags = intent.getStringExtra("tags");
                Log.d(TAG, "onReceive: [LianluoPush.SETTAGS_ACTION]  >>> "+packageName+"  ["+tags+"]");

                setTagsByPkgName(packageName,tags);

            }else if (action.equals(LianluoComunicatorMessages.REMOVETAGS_ACTION)){
                // TODO: 2016/10/12 删除Tags
                String packageName= intent.getStringExtra("packagename");
                String tags = intent.getStringExtra("tags");
                Log.d(TAG, "onReceive: [LianluoPush.REMOVE_ACTION]  >>> "+packageName+"  ["+tags+"]");
                removeTagsByPkgName(packageName,tags);

            }else if (action.equals(LianluoComunicatorMessages.GETTAGS_ACTION)){
                // TODO: 2016/10/12 获取TAGS.
                Log.d(TAG, "onReceive: LianluoPush.GETTAGS_ACTION>>>>>>>>>");
                String packageName= intent.getStringExtra("packagename");
                getTagsByPackageNam(packageName);

            }else if (action.equals(LianluoComunicatorMessages.SERVICE_SERVICE_CHECK_ACTION)){

                String externalString = intent.getStringExtra("external");
                Log.d(TAG, "onReceive: 收到广播----->>>>  action :: SERVICE_SERVICE_CHECK_ACTION = externalString = "+externalString);
                sendCheckActionBack(externalString);
                
            }else if(action.equals(LianluoComunicatorMessages.SERVICE_SERVICE_PUSH_STOP_CTION)){
                Log.d(TAG, "onReceive: 断开");
                String stoppackageName = intent.getStringExtra("packagename");
                tryToStopPusherByPackagename(stoppackageName);


            }else if (action.equals(LianluoComunicatorMessages.SERVICE_SEND_MESSAGE_ACTION)){
                //发送消息
                Log.d(TAG, "onReceive: 发送消息");

                String sendPackageName = intent.getStringExtra("packagename");
                String sendAppkey = intent.getStringExtra("appkey");
                String sendTopic = intent.getStringExtra("topic");
                String messageString = intent.getStringExtra("message");

                sendMessages(sendAppkey,sendPackageName,sendTopic,messageString);

            }else if (action.equals(LianluoComunicatorMessages.SERVICE_SUBCRIBE_DEVICEID_ACTION)){
                Log.d(TAG, "onReceive: 订阅上下线消息 ");
                String subCribePackageName = intent.getStringExtra("packagename");
                String subcribeDevid = intent.getStringExtra("devid");
                subcribeOnOFFlineById(subCribePackageName,subcribeDevid);

            }else if (action.equals(LianluoComunicatorMessages.SERVICE_DESUBCRIBE_DEVICEID_ACTION)){


                Log.d(TAG, "onReceive: 退订上下线消息");
                String desubCribePackageName = intent.getStringExtra("packagename");
                String desubcribeDevid = intent.getStringExtra("devid");
                desubcribeOnOFFlineById(desubCribePackageName,desubcribeDevid);

            }else if (action.equals(LianluoComunicatorMessages.SERVICE_GET_DEVICELIST_ACTION)){
                Log.d(TAG, "onReceive: 获取设备列表");
                String devlistPackagename = intent.getStringExtra("packagename");
                getDevListStringByPackageName(devlistPackagename);
                
            }else if (action.equals(Intent.ACTION_SCREEN_OFF)){

                Log.d(TAG, "onReceive:锁屏----启动Activity");
                try{
                    Intent keepintent = new Intent();
                    keepintent.setClass(context,KeepAliveActivity.class);
                    keepintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(keepintent);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }else if (action.equals(Intent.ACTION_USER_PRESENT)){
                Log.d(TAG, "onReceive: 解锁------------》》》关闭Activity");
                try {
                    checkIMconnection();
                    KeepAliveActivity.getInstance().finish();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }else if (action.equals(LianluoComunicatorMessages.SERVICE_UPDATE_DEVICELIST_ACTION)){
                String devlistPackagename = intent.getStringExtra("packagename");
                String devlist = intent.getStringExtra("devlist");
                updateDevListByPackageName(devlistPackagename,devlist);
            }else if (action.equals(LianluoComunicatorMessages.SERVICE_SEND_MESSAGE_DEVICE_ONLINE_ACTION)){
                String devlistPackagename = intent.getStringExtra("packagename");
                String devid = intent.getStringExtra("devid");
                getDeviceOnlineStatusByPackageName(devlistPackagename,devid);

            }else if (action.equals(LianluoComunicatorMessages.SERVICE_SERVICE_PUSH_STOP_BACK_CTION)){

                boolean result = intent.getBooleanExtra("result",false);
                if (result==true){
                    //成功

                    String packagename = intent.getStringExtra("packagename");
                    removePushersByPackagename(packagename);

                }else{
                    Log.d(TAG, "onReceive: 断开连接失败");
                }
            }else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)){
                getNetWorkState(LianluoPushService.this);
            }

        }
    };

    public void checkIMconnection(){
        for (LianluoComunicator comm:pushers){
            if (comm.bIsConnected==false){
                comm.reconnect();
            }
        }
    }

    public int getNetWorkState(Context context) {
        // 得到连接管理器对象
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            Log.d(TAG, "getNetWorkState: 网络连接，重新连接 ");
//            startPrePushers();
            startPushers();
        } else {
            stopPushers();
            return NETWORK_NONE;
        }
        return NETWORK_NONE;
    }

    private void startPushers(){
        for (LianluoComunicator comm:pushers){
            if (comm.connectStatus==0) {
                comm.reconnect();
            }
        }
    }


    private void stopPushers() {
        Log.d(TAG, "stopPushers: 网络断开，强制关闭连接");
        for (LianluoComunicator comm:pushers){
            comm.setConnectStatus(false);
        }
    }


    private void removePushersByPackagename(String packagename){
        LianluoComunicator pusher = getPusherByPackageName(packagename);
        if (pusher!=null){
            pushers.remove(pusher);
        }
    }

    private void getDeviceOnlineStatusByPackageName(String devlistPackagename, String devid) {
        LianluoComunicator pusher = getPusherByPackageName(devlistPackagename);
        if (pusher!=null){
            pusher.checkDeviceOnlineStatus(devid);

        }

    }

    private void updateDevListByPackageName(String devlistPackagename, String devlist) {

        LianluoComunicator pusher = getPusherByPackageName(devlistPackagename);
        if (pusher!=null){
            pusher.updateDevicelist(devlist);
        }
    }


    /**
     * 退订目标设备上下线消息
     *
     * @param subCribePackageName
     * @param subcribeDevid
     */
    private void desubcribeOnOFFlineById(String subCribePackageName, String subcribeDevid){
        Log.d(TAG, "desubcribeOnOFFlineById: >>>>>>退订  subcribeDevid  = "+subcribeDevid);
        LianluoComunicator pusher = getPusherByPackageName(subCribePackageName);
        if (pusher!=null){
            pusher.deSubcribeOnlineStatByDevids(subcribeDevid);
        }

    }

    /**
     * 订阅目标设备上下线消息
     *
     * @param subCribePackageName
     * @param subcribeDevid
     */
    private void subcribeOnOFFlineById(String subCribePackageName, String subcribeDevid) {

        LianluoComunicator pusher = getPusherByPackageName(subCribePackageName);
        if (pusher!=null){
            pusher.subcribeOnlineStatusByDevids(subcribeDevid);
        }

    }

    /**
     * 发布消息
     * @param sendPackageName
     * @param sendTopic
     * @param messageString
     */
    private void sendMessages(String appkey,String sendPackageName, String sendTopic, String messageString) {
        Log.d(TAG, "sendMessages: appkey = "+appkey+" sendpackagename = "+sendPackageName);
        LianluoComunicator pusher = getPusherByPackageName(sendPackageName);
        String sendoutAppkey = null;
        String sendoutTopic = null;
        if (appkey == null){
            sendoutAppkey = "";
        }else{
            sendoutAppkey = appkey;
        }

        if (sendTopic == null){
            sendoutTopic = "";
        }else{
            sendoutTopic = sendTopic;
        }

        if (pusher!=null){
            pusher.sendMessage(sendoutAppkey,sendoutTopic,messageString);
        }

    }

    /**
     * 停用相关的mqtt客户端
     * @param stoppackageName
     */
    private void tryToStopPusherByPackagename(String stoppackageName) {
        
        LianluoComunicator pusher = getPusherByPackageName(stoppackageName);
        if (pusher!=null){
            pusher.disconnect();
        }

    }

    private void sendCheckActionBack(String content) {
        Log.d(TAG, "sendCheckActionBack: 服务器已经启动，返回广播：： content = "+content);
        Intent intent = new Intent(LianluoComunicatorMessages.SERVICE_SERVICE_CHECK_BACK_ACTION);
        intent.putExtra("packagename",content);
        sendBroadcast(intent);
    }

    //获取标签列表
    private void getTagsByPackageNam(String packageName){
        Log.d(TAG, "getTagsByPackageNam: ");
        String tagsString = null;
        for (LianluoComunicator pusher:pushers){
            if (pusher.pusherBean.packageName.equals(packageName)){
                tagsString = pusher.getthisTags();
                break;
            }
        }

        Log.d(TAG, "getTagsByPackageNam: tagsString = "+tagsString);

        if (tagsString !=null){

            Intent intent = new Intent(LianluoComunicatorMessages.GETTAGS_BACK_ACTION);
            intent.putExtra("packagename",packageName);
            intent.putExtra("tags",tagsString);
            getApplicationContext().sendBroadcast(intent);

        }

    }

    /**
     * 获取设备列表，以String发送。
     * @param packagename
     */

    private void getDevListStringByPackageName(String packagename){
        Log.d(TAG, "getDevListStringByPackageName: ");
        String devlist = null;
        for (LianluoComunicator pusher:pushers){
            if (pusher.pusherBean.packageName.equals(packagename)){
                devlist = pusher.getDevicelist();
                break;
            }
        }

        if (devlist!=null){
            Intent intent = new Intent(LianluoComunicatorMessages.SERVICE_GET_DEVICELIST_BACK_ACTION);
            intent.putExtra("packagename",packagename);
            intent.putExtra("devices",devlist);
            getApplicationContext().sendBroadcast(intent);
        }
    }

//删除标签
    private void removeTagsByPkgName(String packageName, String tags){
        if (tags!=null && tags.length()>0) {
            for (LianluoComunicator pusher : pushers) {
                if (pusher.pusherBean.packageName.equals(packageName)) {
                    pusher.removeTags(tags);
                    break;
                }
            }
        }
    }
//标签
    private void setTagsByPkgName(String packageName, String tags) {
        for (LianluoComunicator pusher:pushers){
            Log.d(TAG, "setTagsByPkgName: pushername = "+pusher.pusherBean.packageName +" packageName = "+packageName);
            if (pusher.pusherBean.packageName.equals(packageName)){
                pusher.setTags(tags);
                Log.d(TAG, "setTagsByPkgName: 停止检测。。。");
                break;
            }
        }

    }

    ArrayList<LianluoComunicator> pushers = new ArrayList<LianluoComunicator>();


    LianluoPushSharePreferencesManager pushSPmng = null;

    private LianluoComunicator getPusherByPackageName(String pkgname){
        for (LianluoComunicator tpusher:pushers){
            if (tpusher.pusherBean.packageName.equals(pkgname)){
                return tpusher;
            }
        }

        return null;
    }
   private LianluoComunicator getPusherInList(String appkey, String appsecret, String clientID){
       for (LianluoComunicator tpusher:pushers){
           if (tpusher.pusherBean.clientID.equals(clientID) && tpusher.pusherBean.account.equals(appkey)){
               return tpusher;
           }
       }
       return null;
   }




    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: 创建服务》》》》");
        initData();
        super.onCreate();

    }

    private void initData() {

        //获取SP--------待测
        //ENDDEBUG
        Log.d(TAG, "onCreate----->initData: >>>>>>>>>>>开始注册广播");
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(LianluoComunicatorMessages.STARTPUSH_ACTION);
        iFilter.addAction(LianluoComunicatorMessages.SETTAGS_ACTION);
        iFilter.addAction(LianluoComunicatorMessages.REMOVETAGS_ACTION);
        iFilter.addAction(LianluoComunicatorMessages.GETTAGS_ACTION);
        iFilter.addAction(LianluoComunicatorMessages.SERVICE_SERVICE_CHECK_ACTION);
        iFilter.addAction(LianluoComunicatorMessages.SERVICE_SEND_MESSAGE_ACTION);
        iFilter.addAction(LianluoComunicatorMessages.SERVICE_GET_DEVICELIST_ACTION);
        iFilter.addAction(LianluoComunicatorMessages.SERVICE_SUBCRIBE_DEVICEID_ACTION);
        iFilter.addAction(LianluoComunicatorMessages.SERVICE_DESUBCRIBE_DEVICEID_ACTION);
        iFilter.addAction(LianluoComunicatorMessages.SERVICE_UPDATE_DEVICELIST_ACTION);
        iFilter.addAction(LianluoComunicatorMessages.SERVICE_SEND_MESSAGE_DEVICE_ONLINE_ACTION);
        iFilter.addAction(LianluoComunicatorMessages.SERVICE_SERVICE_PUSH_STOP_CTION);
        iFilter.addAction(LianluoComunicatorMessages.SERVICE_SERVICE_PUSH_STOP_BACK_CTION);
        iFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        iFilter.addAction(Intent.ACTION_SCREEN_OFF);
        iFilter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(pushStartReceiver,iFilter);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        pushSPmng = new LianluoPushSharePreferencesManager(getApplicationContext());
        startPrePushers();

        if (intent!=null) {
            Log.d(TAG, "onStartCommand: >>>启动服务服务...."+intent.getStringExtra("packagename"));
            Intent startIntent = new Intent(LianluoComunicatorMessages.SERVICE_STATED_ACTION);

            startIntent.putExtra("packagename", intent.getStringExtra("packagename"));
            sendBroadcast(startIntent);
        }

        Log.d(TAG, "onStartCommand: >>>启动服务服务....2222");
        return START_STICKY;
    }



    private void startPrePushers() {
        Log.d(TAG, "startPrePushers: 启动以前的客户端-----》》》》");
        List<LianluoPusherBean> pusherBeans = loadPushers();
        if (pusherBeans!=null) {
            for (int i = 0; i < pusherBeans.size(); i++) {
                LianluoPusherBean pusherBean = pusherBeans.get(i);
                startPushClient(pusherBean.account,pusherBean.key,pusherBean.packageName,pusherBean.clientID);
//                checkValuesValidByHead(pusherBean.account, pusherBean.key, pusherBean.devid, pusherBean.packageName, pusherBean.channelID);
            }
        }
    }


    private void startLianluoPushClient(String appkey, String appsecret, String packagename, String clientID){
        Log.d(TAG, "startLianluoPushClient: 启动推送客户端---------------");

        LianluoComunicator newpusher = new LianluoComunicator(getApplicationContext(),appkey,appsecret,packagename,clientID);
        pushers.add(newpusher);
        savePushers(pushers);

    }

    public void startPushClient(String appkey, String appsecret, String packagename, String clientID){

        Log.d(TAG, "startPushClient: appkey = "+appkey+" appsecret = "+appsecret+" clientID = "+clientID+" packagename = "+packagename);
        LianluoComunicator currentPush = getPusherInList(appkey,appsecret,clientID);

        if (currentPush != null){

            Log.d(TAG, "startPushClient: >>>>>>>>>>已经存在相同declientID,无需重新连接");
            Intent connectIntent = new Intent(LianluoComunicatorMessages.SERVICE_LIANLUO_IM_CONNECTED_ACTION);
            connectIntent.putExtra("packagename",currentPush.pusherBean.packageName);
            sendBroadcast(connectIntent);

        }else {
            startLianluoPushClient(appkey, appsecret, packagename, clientID);
        }
    }


    @Override
    public IBinder onBind(Intent intent) {

        // TODO: Return the communication channel to the service.
        return null;
    }

    private List<LianluoPusherBean> loadPushers(){
        List<LianluoPusherBean> pusherBeans = new ArrayList<LianluoPusherBean>();
        Gson gson = new Gson();
        String clientInfos = pushSPmng.getPushList();
        Log.d(TAG, "loadPushers:  clientInfos =  "+clientInfos);
        if (clientInfos!=null){
            pusherBeans = gson.fromJson(clientInfos,new TypeToken<List<LianluoPusherBean>>(){}.getType());

            if (pusherBeans!=null) {
                for (int i = 0; i < pusherBeans.size(); i++) {
                    LianluoPusherBean p = pusherBeans.get(i);
                    Log.d(TAG, "loadPushers:    " + p.toString());
                }
            }

        }
        Log.d(TAG, "loadPushers: pusherBeans = "+pusherBeans.size());
        return pusherBeans;

    }

    private void savePushers(ArrayList<LianluoComunicator> pushers) {
        Log.d(TAG, "savaPushers: >>>>>>>>>>>>>>>>>  pushers.size = "+pushers.size());
        List<LianluoPusherBean> pusherBeans = new ArrayList<LianluoPusherBean>();

        Gson gson = new Gson();
        for (LianluoComunicator pusher:pushers){
            pusherBeans.add(pusher.pusherBean);
        }
        String gsonString = gson.toJson(pusherBeans);
        Log.d(TAG, "savaPushers: gsonString = "+gsonString);

        pushSPmng.savePushList(gsonString);
    }


}
