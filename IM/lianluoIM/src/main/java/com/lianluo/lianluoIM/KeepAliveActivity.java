package com.lianluo.lianluoIM;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

public class KeepAliveActivity extends Activity {
    static KeepAliveActivity mInstance = null;

    public static KeepAliveActivity getInstance(){
        return mInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("LianluoIM", "onCreate: 》》》》》》》》》》》》》》》打开点点点Activity。");
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 0;
        params.y = 0;
        params.height = 1;
        params.width = 1;
        window.setAttributes(params);
        mInstance = this;

    }

    @Override
    protected void onDestroy() {
        Log.d("LianluoIM", "onDestroy: >>>>>>>>>>退出点点点Activity");
        super.onDestroy();
    }
}
