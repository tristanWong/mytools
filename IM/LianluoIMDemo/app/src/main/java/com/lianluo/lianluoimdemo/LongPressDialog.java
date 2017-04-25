package com.lianluo.lianluoimdemo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import com.lianluo.lianluoIM.LianluoIM;

/**
 * Created by Kevin on 2016/8/2.
 */
public class LongPressDialog extends Dialog implements View.OnClickListener{
    Context mContext;
    Fragment mFragment;
    String mDeviceID;
    private TextView mTvRename;
    private TextView mTvCheckstatus;
    private TextView mTvSendMessage;

    public LongPressDialog(Activity fragment, int theme,String deviceID) {
        super(fragment, theme);

        this.mContext = fragment;
        mDeviceID = deviceID;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_long_press);
        mTvRename = (TextView) findViewById(R.id.tv_rename);
        mTvRename.setOnClickListener(this);

        mTvCheckstatus = (TextView) findViewById(R.id.tv_checkstatus);
        mTvCheckstatus.setOnClickListener(this);

        mTvSendMessage = (TextView) findViewById(R.id.tv_sendmessage);
        mTvSendMessage.setOnClickListener(this);

    }

    // 直接在这里面写的话就必须要将那些参数传进来，增加这个自定义控件的负担
    @Override
    public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_rename:

                  LianluoIM.delDevice(mDeviceID);

                  dismiss();
                  break;
                case R.id.tv_checkstatus:
                    LianluoIM.isDeviceOnline(mDeviceID);
                    dismiss();
                    break;
                case R.id.tv_sendmessage:
                    DeviceControlActivity.startSendMessage(mDeviceID,mContext);
                    dismiss();
                    break;
              default:
                      break;
          }

        }

}
