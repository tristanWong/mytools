package com.lianluo.lianluoimdemo;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.lianluo.lianluoIM.LianluoIM;
import com.lianluo.lianluoIM.OnSendMessageListener;

import java.util.ArrayList;
import java.util.List;

public class DeviceControlActivity extends AppCompatActivity implements View.OnClickListener,AdapterView.OnItemLongClickListener{
    Button btn_addDevices = null;
    TextView tv_message   = null;
    ListView lv_devList = null;


    private static final int HANDLER_MESSAGE_GOT = 10001;
    String addDeviceString = null;
    String delDeviceString = null;

    DevicelistAdapter mAdapter = null;
    IMDemoDatas myData = null;


    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case HANDLER_MESSAGE_GOT:

                    Log.d("LianluoIM", "handleMessage:  HANDLER_MESSAGE_GOT  "+(String) msg.obj);

                    String str = (String) msg.obj;
                    updateTexViewFeild(str);
                    break;

            }
            super.handleMessage(msg);
        }
    };

    private void updateTexViewFeild(String str) {
        String settext = tv_message.getText().toString();
        settext = settext+"\n"+str;
        tv_message.setText(settext);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);

        registBroadcast();
        initData();
        initViews();
    }

    private void initData() {
        myData = IMDemoDatas.getInstance();
        mAdapter = new DevicelistAdapter(DeviceControlActivity.this,myData.getDevices());
        LianluoIM.getClientID();

    }

    BroadcastReceiver broadcastReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (IMDemoActions.ACTION_MESSAGE_GOT.equals(action)){

                String str = intent.getStringExtra("content");
                Message msg = mHandler.obtainMessage();
                msg.what = HANDLER_MESSAGE_GOT;
                msg.obj = str;
                mHandler.sendMessage(msg);

            }else if (IMDemoActions.ACTION_DEVICE_ADD_CHANGE.equals(action)){
                String external = intent.getStringExtra("extral");
                Log.d("DeviceControl", "onReceive: external = "+external);
                if ("del".equals(external)){
                    delinDeviceList(delDeviceString);
                }else if ("add".equals(external)) {
                    addDeviceList(addDeviceString);
                }


            }else if (IMDemoActions.ACTION_DEVICE_ON_OFF_LINE.equals(action)){
//                updateOnOffLineDatas();
                String showMessage = intent.getStringExtra("content");
                updateTexViewFeild(showMessage);

            }else if (IMDemoActions.ACTION_DEVICELIST_GOT.equals(action)){
                mAdapter.notifyDataSetChanged();
            }
        }
    };




    @Override
    protected void onDestroy() {
        unRegistBrodcast();
        super.onDestroy();
    }

    private void registBroadcast() {
        IntentFilter filter =  new IntentFilter();
        filter.addAction(IMDemoActions.ACTION_DEVICE_ADD_CHANGE);
        filter.addAction(IMDemoActions.ACTION_DEVICELIST_GOT);
        filter.addAction(IMDemoActions.ACTION_DEVICE_ON_OFF_LINE);
        filter.addAction(IMDemoActions.ACTION_MESSAGE_GOT);
        registerReceiver(broadcastReciever,filter);
    }

    private void unRegistBrodcast(){
        if (broadcastReciever!=null) {
            unregisterReceiver(broadcastReciever);
        }
    }

    private void initViews() {

        tv_message = (TextView) findViewById(R.id.tv_showmessage);
        tv_message.setText("");
        tv_message.setMovementMethod(ScrollingMovementMethod.getInstance());

        btn_addDevices = (Button) findViewById(R.id.btn_sendmessage);
        btn_addDevices.setOnClickListener(this);

        lv_devList = (ListView) findViewById(R.id.lv_devicelist);
        lv_devList.setOnItemLongClickListener(this);
        lv_devList.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_sendmessage:
                startAddDevices();
                break;
        }
    }
   //添加设备入口
    private void startAddDevices() {
        LinearLayout layout = new LinearLayout(DeviceControlActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText textviewGid = new EditText(DeviceControlActivity.this);
        textviewGid.setHint("请输入设备ID，多个设备ID以英文逗号隔开");
        layout.addView(textviewGid);

        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceControlActivity.this);
        builder.setView(layout);
        builder.setPositiveButton("添加设备",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Push: 设置tag调用方式
                        // TODO: 2016/11/11 添加设备
                        String devices = textviewGid.getText().toString();
                        LianluoIM.addDevice(devices);
                        addDeviceString = devices;
                    }

                });
        builder.show();
    }
    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        delDeviceString = myData.getDevices().get(i);
        showLongPressDialog(i,view,delDeviceString);
        return true;
    }


    private LongPressDialog mLongPressDialog;

    private void showLongPressDialog(int postion, final View view,String deviceID) {
        mLongPressDialog = new LongPressDialog(this, R.style.LongPressDialogStyle,deviceID);
        int[] location = new int[2];
        view.setBackgroundColor(getResources().getColor(
                android.R.color.darker_gray));
        view.getLocationOnScreen(location);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Display display = getWindowManager().getDefaultDisplay();
        display.getMetrics(displayMetrics);
        WindowManager.LayoutParams params = mLongPressDialog.getWindow().getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.y = display.getHeight() - location[1] - (int)(view.getHeight() * 1.5);
        mLongPressDialog.getWindow().setAttributes(params);
        mLongPressDialog.setCanceledOnTouchOutside(true);
        mLongPressDialog.setOnCancelListener(new LongPressDialog.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                view.setBackgroundColor(getResources().getColor(
                        android.R.color.white));
            }
        });
        mLongPressDialog.show();

    }
    //增加设备成功
    private void addDeviceList(String tags){
        if (tags.length()>0) {
            List<String> mDeviceList = new ArrayList<String>();
            mDeviceList.clear();
            Log.d("DeviceControl", "addDeviceList: tags = " + tags);
            String[] devArray = tags.split(",");
            Log.d("DeviceControl", "addDeviceList: devArray = " + devArray.length);
            for (int index = 0; index < devArray.length; index++) {
                if (mDeviceList.contains(devArray[index]) == false) {
                    mDeviceList.add(devArray[index]);
                }
            }
            myData.addDevices(mDeviceList);
            mAdapter.notifyDataSetChanged();
        }
    }
    //删除设备
    private void delinDeviceList(String tags) {
        if (tags.length()>0) {
            List<String> mDeviceList = new ArrayList<String>();
            mDeviceList.clear();
            String[] devArray = tags.split(",");
            for (int index = 0; index < devArray.length; index++) {
                if (mDeviceList.contains(devArray[index]) == false) {
                    mDeviceList.add(devArray[index]);
                }
            }
            myData.delDevices(mDeviceList);
            mAdapter.notifyDataSetChanged();
        }
    }

    public static void startSendMessage(final String mDeviceID, final Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText textviewGid = new EditText(context);
        textviewGid.setHint("请输入要发送的消息");
        layout.addView(textviewGid);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(layout);
        builder.setPositiveButton("发送",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Push: 设置tag调用方式
                        // TODO: 2016/11/11 添加设备
                        String devices = textviewGid.getText().toString();
                        LianluoIM.sendMessage(null, mDeviceID, devices, new OnSendMessageListener() {
                            @Override
                            public void onMessageSendSuccess() {
                                Log.d("DeviceControl", "【DeviceControlActivity】----》》onMessageSendSuccess:发送消息成功  ");
                            }

                            @Override
                            public void onMessageSendFailed() {
                                Log.d("DeviceControl", "【DeviceControlActivity】----》》onMessageSendSuccess:发送消息失败 <_>!!! ");
                            }

                            @Override
                            public void onDeviceMessageGot(String s) {

                            }
                        });

                    }

                });
        builder.show();

    }
}
