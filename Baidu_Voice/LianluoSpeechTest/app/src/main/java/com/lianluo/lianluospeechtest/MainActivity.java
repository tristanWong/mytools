package com.lianluo.lianluospeechtest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.lianluo.llspeech.LianluoSpeech;

public class MainActivity extends AppCompatActivity {
    EditText et_readcontent = null;
    Button btn_read=null;
    LianluoSpeech myspeech = null;

    public static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 8334;

    public static final String APP_ID = "9513277";
    public static final String APP_KEY = "N1st6GhRgU3vGaTNhNKPodXS";
    public static final String APP_SECRET = "915329dfa57f2cb4e8c10c3307658cf8";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();

    }

    private void permissoncheck(){

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                myspeech.LianluoSpeechInit();

            } else {

                Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
            }
        }

    }

    private void initData() {
        permissoncheck();

        if (myspeech == null) {
            myspeech = LianluoSpeech.getInstance(getApplicationContext(), APP_ID, APP_KEY, APP_SECRET);
        }
        myspeech.LianluoSpeechInit();
    }

    private void initView() {
        et_readcontent = (EditText) findViewById(R.id.readText);

        btn_read = (Button) findViewById(R.id.read);
        btn_read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSpeek(et_readcontent.getText().toString().trim());
            }
        });

    }

    private void startSpeek(String trim) {

        if (myspeech!=null){
            myspeech.speekText(trim);
        }

    }
}
