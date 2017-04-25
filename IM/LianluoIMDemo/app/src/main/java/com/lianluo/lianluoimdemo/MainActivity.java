package com.lianluo.lianluoimdemo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.lianluo.lianluoIM.LianluoIM;
import com.lianluo.lianluoIM.OnDeviceListOptionListener;
import com.lianluo.lianluoIM.OnMessageListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,OnMessageListener,OnDeviceListOptionListener{
    public static final String TAG = "LianluoIM";
    public static final String APP_KEY = "6eb3c19e-5b16-3d8f-bd7f-e1107a001110";//"b4499688-010e-3be6-a294-a7bd3e3ef74e";
    public static final String APP_SECRET ="GiX_oEFhKDYbUmydiOvZnITsOs3BG5Gg";// "W0shko4wA_OcK7FHR8cCohA8LwaoMOkR";
    public static final String APP_PACKAGENAME ="com.smarthome.lilos.lilossmarthome"; //"com.lianluo.lianluoimsourcedemo";

    Button btn_devcotrol = null;
    Button btn_tagcontrol = null;
    Button btn_onoffline = null;
    EditText et_clientId = null;
    TextView tv_main_text = null;
    IMDemoDatas myDatas = null;

    private static final int UPDATE_CLIENTID_MESSAGE = 1;
    private static final int MESSAGE_DEVICE_ONLINE = 7;


    String mCurentClientID = null;
    boolean deviceOnlinestat = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initData();
    }

    private void initData() {
        Log.d(TAG, "initData: 初始化数据");

        LianluoIM.startIMWork(getApplicationContext(),APP_KEY,APP_SECRET,APP_PACKAGENAME,this);

        LianluoIM.setDevOptionLister(this);
        myDatas = IMDemoDatas.getInstance();


    }
    private void disableView(Button  view){
        view.setEnabled(false);
        view.setTextColor(Color.rgb(128, 128, 128));

    }


    private void initViews() {

        et_clientId = (EditText) findViewById(R.id.et_clientid);
        btn_onoffline = (Button) findViewById(R.id.btn_onoffline);
        disableView(btn_onoffline);
        btn_onoffline.setOnClickListener(this);

        btn_devcotrol = (Button) findViewById(R.id.btn_devicemanager);
        disableView(btn_devcotrol);
        btn_devcotrol.setOnClickListener(this);

        btn_tagcontrol = (Button) findViewById(R.id.btn_tagmanager);
        disableView(btn_tagcontrol);
        btn_tagcontrol.setOnClickListener(this);

        tv_main_text = (TextView) findViewById(R.id.main_textshow);
        tv_main_text.setText("");

        tv_main_text.setMovementMethod(ScrollingMovementMethod.getInstance());
    }
    public void updateText(String text){
        String showMessage = tv_main_text.getText().toString().trim();
        if (text.length()>0){
            showMessage = showMessage+"\n"+text;
        }
        tv_main_text.setText(showMessage);

    }

    private void startDeviceManager(){
        // TODO: 2016/11/22 跳转到设备管理界面。
        Intent intent = new Intent();
        intent.putExtra("clientid",mCurentClientID);
        intent.setClass(getApplicationContext(),DeviceControlActivity.class);
        startActivity(intent);
    }

    private void startTagManager(){
        // TODO: 2016/11/22 tag管理
        Intent intent = new Intent();
        intent.putExtra("clientid",mCurentClientID);
        intent.setClass(getApplicationContext(),TagsManagerActivity.class);
        startActivity(intent);
    }




    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_devicemanager:
//                startAddDevices();
                startDeviceManager();
                break;
            case R.id.btn_tagmanager:
                startTagManager();
                break;
            case R.id.btn_onoffline:
//                startDelDevices();
                
                deviceOnOffline();
                break;
        }
    }

    private void deviceOnOffline() {
        if (deviceOnlinestat == false){
            //设备离线，上线设备
            LianluoIM.startIMWork(getApplicationContext(),APP_KEY,APP_SECRET,APP_PACKAGENAME,this);

        }else{
            LianluoIM.stopIMWork();
            deviceOnlinestat = false;
            btn_onoffline.setText("上线");
        }

    }



    @Override
    public void onConnected() {
        Log.d(TAG, "onConnected: 服务器连接上了---获取ClientID");

        LianluoIM.getClientID();
        //仅在连接上服务器的时候去获取一次。
        Log.d(TAG, "onConnected: >>>>>>>>>>>>>获取设备列表〉〉〉");
        LianluoIM.getDeviceList();
        mHandler.sendEmptyMessage(MESSAGE_DEVICE_ONLINE);

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onMessageGot(String s) {
        Log.d(TAG, "onMessageGot: 收到消息了>>>"+s);
        processMessage(s);
    }

    private void processMessage(String s) {

        try {
            JSONObject messageObj = new JSONObject(s);
            if (messageObj.has("custom_content")){
                JSONObject custom_obj = messageObj.getJSONObject("custom_content");
                if (custom_obj.has("onlinestat")){
                    //设备上下线数据
                    sendoutOnOffLineBroadcast(s);
                }else {
                    sendoutCommonBroadCast(s);
                    //发广播出来。
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
            sendoutCommonBroadCast(s);

        }

    }

    private void sendoutCommonBroadCast(String s) {
        String outMessage = null;
        JSONObject messageObj = null;
        String deviceFrom = null;
        try {
            messageObj = new JSONObject(s);
            if (messageObj.getInt("notification_builder_id") == 1) {
                if (messageObj.has("from")) {
                    deviceFrom = messageObj.getString("from");
                }
                if (messageObj.has("custom_content")) {
                    JSONObject custom_obj = messageObj.getJSONObject("custom_content");
                    if (deviceFrom == null) {
                        if (custom_obj.has("devid")) {
                            deviceFrom = custom_obj.getString("devid");
                        } else {
                            deviceFrom = "系统";
                        }
                    }
                    outMessage = custom_obj.toString();
                }

            }else{
                deviceFrom = "系统";
                outMessage = s;
            }
            

            Message msg = mHandler.obtainMessage();
            msg.what = 2;
            msg.obj = deviceFrom + " 发来消息：" + outMessage;
            mHandler.sendMessage(msg);
            //sendMessage;
            Log.d(TAG, "sendoutCommonBroadCast: deviceFrom 顶顶顶顶付= " + deviceFrom);

            Intent sendIntent = new Intent(IMDemoActions.ACTION_MESSAGE_GOT);
            sendIntent.putExtra("content", deviceFrom + "发来消息：" + outMessage);
            sendBroadcast(sendIntent);
        } catch (JSONException e) {
            e.printStackTrace();
            sendCommonmessage_extra(s);
        }
    }

    private void sendCommonmessage_extra(String s) {
        Log.d(TAG, "sendCommonmessage_extra: 进入到这里，说明消息内容为透传消息，但是解析出错了。");
        String deviceFrom = null;
        String outMessage = null;
        try {
            JSONObject messageObj = new JSONObject(s);
            if (messageObj.has("from")) {
                deviceFrom = messageObj.getString("from");
            }

            if (deviceFrom == null){
                deviceFrom = "系统";
            }
            outMessage = s;


            Message msg = mHandler.obtainMessage();
            msg.what = 2;
            msg.obj = deviceFrom + " 发来消息：" + outMessage;
            mHandler.sendMessage(msg);
            //sendMessage;
            Log.d(TAG, "sendoutCommonBroadCast: deviceFrom 222222>>>>顶顶顶顶付= " + deviceFrom);

            Intent sendIntent = new Intent(IMDemoActions.ACTION_MESSAGE_GOT);
            sendIntent.putExtra("content", deviceFrom + "发来消息：" + outMessage);
            sendBroadcast(sendIntent);


        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    private void sendoutOnOffLineBroadcast(String s) {

        String outMessage = null;

        JSONObject messageObj = null;
        String deviceStr = null;

        try {
            messageObj = new JSONObject(s);
            if (messageObj.has("from")){
                deviceStr =messageObj.getString("from");
            }
            Log.d(TAG, "sendoutOnOffLineBroadcast: deviceStr  = "+deviceStr);

            if (messageObj.has("custom_content")) {
                JSONObject custom_obj = messageObj.getJSONObject("custom_content");
                if (deviceStr==null){
                    if(custom_obj.has("devid")){
                        deviceStr =custom_obj.getString("devid");
                    }else{
                        deviceStr = "未知设备";
                    }
                }
                String onlineStates = custom_obj.getString("onlinestat");
                if (onlineStates.equalsIgnoreCase("on")){
                    //设备上线
                    outMessage= deviceStr+"设备上线";
                }else if (onlineStates.equalsIgnoreCase("off")){
                    outMessage= deviceStr+"设备下线";
                }
                Message msg = mHandler.obtainMessage();
                msg.what = 3;
                msg.obj = outMessage;
                mHandler.sendMessage(msg);

                //发广播 >>>.
                Intent intent = new Intent(IMDemoActions.ACTION_DEVICE_ON_OFF_LINE);
                intent.putExtra("content",outMessage);
                sendBroadcast(intent);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTagsGot(String s) {
        sendoutMessages(IMDemoActions.ACTION_TAGLIST_GOT,s);
    }

    @Override
    public void onGetClientId(String s) {
        mCurentClientID = s;
        mHandler.sendEmptyMessage(UPDATE_CLIENTID_MESSAGE);
    }

    @Override
    public void onDevListGot(String s) {
        Log.d(TAG, "onDevListGot: 获取到设备列表 "+s);
        refreshDevList(s);
    }

    private void refreshDevList(String s) {
        myDatas.clearDevices();
        List<String> tempList = new ArrayList<>();
        if (s.length()>0) {
            String[] deviceArray = s.split(",");
            for (int i = 0; i < deviceArray.length; i++) {
                tempList.add(deviceArray[i]);
            }
            if (tempList.size()>0) {
                myDatas.addDevices(tempList);
                sendoutMessages(IMDemoActions.ACTION_DEVICELIST_GOT,"");
            }
        }
    }

    @Override
    public void onDeviceListChanged(String s) {
        Log.d(TAG, "onDeviceListChanged: 添加设备成功　　");
        String tag = s;
        sendoutMessages(IMDemoActions.ACTION_DEVICE_ADD_CHANGE,tag);
    }

    @Override
    public void onDeviceOptionFailed(String s) {
        String tag = s;
    }




    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_CLIENTID_MESSAGE:
                    et_clientId.setText(mCurentClientID);
                    break;
                case MESSAGE_DEVICE_ONLINE:
                    enableAllButtons();
                    deviceOnlinestat = true;
                    btn_onoffline.setText("离线");
                    break;
                case 2:
                    String updateText = (String) msg.obj;
                    updateText(updateText);

                    break;
                case 3:
                    String onlineText = (String) msg.obj;
                    updateText(onlineText);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void enableAllButtons() {
        btn_onoffline.setEnabled(true);
        btn_onoffline.setTextColor(Color.rgb(0, 0, 0));

        btn_devcotrol.setEnabled(true);
        btn_devcotrol.setTextColor(Color.rgb(0, 0, 0));

        btn_tagcontrol.setEnabled(true);
        btn_tagcontrol.setTextColor(Color.rgb(0, 0, 0));


    }


    private void sendoutMessages(String action,String extratag){
        Intent intent = new Intent(action);
        intent.putExtra("extral",extratag);
        sendBroadcast(intent);
    }

}
