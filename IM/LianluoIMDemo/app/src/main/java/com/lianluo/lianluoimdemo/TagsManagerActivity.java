package com.lianluo.lianluoimdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lianluo.lianluoIM.LianluoIM;

/**
 * Created by Tristan on 2016/11/22.
 */

public class TagsManagerActivity extends Activity implements View.OnClickListener {
    Button btn_addtag = null;
    Button btn_deltag = null;
    Button btn_gettag = null;
    TextView tv_showMessage = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tags_manager);
        registerBReciever();
        initViews();

    }



    private void initViews() {

        btn_addtag = (Button) findViewById(R.id.btn_addtags);
        btn_addtag.setOnClickListener(this);
        btn_deltag = (Button) findViewById(R.id.btn_deltags);
        btn_deltag.setOnClickListener(this);
        btn_gettag = (Button) findViewById(R.id.btn_gettags);
        btn_gettag.setOnClickListener(this);
        tv_showMessage = (TextView) findViewById(R.id.tags_showmessage);
        tv_showMessage.setText("");
        tv_showMessage.setMovementMethod(ScrollingMovementMethod.getInstance());


    }
    private void updateText(String text){
        String finalString = tv_showMessage.getText().toString();
        if (text.length()>0){
            finalString = finalString+"\n"+text;
        }
        tv_showMessage.setText(finalString);
    }

    // 设置标签,以英文逗号隔开ＩＭ
    private void setTags() {
        LinearLayout layout = new LinearLayout(TagsManagerActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText textviewGid = new EditText(TagsManagerActivity.this);
        textviewGid.setHint("请输入多个标签，以英文逗号隔开                                                       ");
        layout.addView(textviewGid);

        AlertDialog.Builder builder = new AlertDialog.Builder(
                TagsManagerActivity.this);
        builder.setView(layout);
        builder.setPositiveButton("设置标签",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Push: 设置tag调用方式
                        String tags = textviewGid.getText().toString();
                        LianluoIM.setTags(tags);
                    }

                });
        builder.show();
    }

    // 删除tag操作
    private void deleteTags() {
        LinearLayout layout = new LinearLayout(TagsManagerActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText textviewGid = new EditText(TagsManagerActivity.this);
        textviewGid.setHint("请输入多个标签，以英文逗号隔开");
        layout.addView(textviewGid);

        AlertDialog.Builder builder = new AlertDialog.Builder(
                TagsManagerActivity.this);
        builder.setView(layout);
        builder.setPositiveButton("删除标签",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Push: 删除tag调用方式
                        String tags = textviewGid.getText().toString();
                        LianluoIM.removeTags(tags);
                    }
                });
        builder.show();
    }


    private void registerBReciever() {
        IntentFilter ifilter = new IntentFilter();
        ifilter.addAction(IMDemoActions.ACTION_MESSAGE_GOT);
        ifilter.addAction(IMDemoActions.ACTION_DEVICE_ON_OFF_LINE);
        ifilter.addAction(IMDemoActions.ACTION_TAGLIST_GOT);
        registerReceiver(broadcastReciever,ifilter);
    }
    BroadcastReceiver broadcastReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (IMDemoActions.ACTION_MESSAGE_GOT.equals(action)){
                String messageString  =intent.getStringExtra("content");
                updateText(messageString);
            }else if (IMDemoActions.ACTION_DEVICE_ON_OFF_LINE.equals(action)){

                String showMessage = intent.getStringExtra("content");
                updateText(showMessage);

            }else if (IMDemoActions.ACTION_TAGLIST_GOT.equals(action)){
                String tagString = intent.getStringExtra("extral");
                tagString = "TAG列表："+tagString;
                updateText(tagString);

            }
        }
    };




    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_addtags:
                setTags();
                break;
            case R.id.btn_deltags:
                deleteTags();
                break;
            case R.id.btn_gettags:
                LianluoIM.getTags();
                break;


        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReciever);
        super.onDestroy();
    }
}
