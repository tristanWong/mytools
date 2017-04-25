package com.lianluo.lianluoIM;

import com.google.gson.Gson;

import java.util.Map;

/**
 * Created by Tristan on 2016/8/12.
 */
public class MQTTMessage {
    public String title;                    //标题,保留
    public String desc;                     //描述，保留
    public int notification_builder_id;     //消息类型，默认为0  - 普通推送消息， 1 - 控制等推送消息，具体参看附带信息。
    public int notification_basic_style;    //只有notification_builder_id为0时有效，可以设置通知的基本样式包括(响铃：0x04;振动：0x02;可清除：0x01;),这是一个flag整形，每一位代表一种样式,如果想选择任意两种或三种通知样式，notification_basic_style的值即为对应样式数值相加后的值。
    public int open_type;                   //点击通知后的行为(1：打开Url; 2：自定义行为；);
    public  String url;                     //需要打开的Url地址，open_type为1时才有效;
    public String from;                   //发送的源设备
    public Map custom_content;              //附件数据，可扩展，以key-value键值对出现，“devid”为必要的键值对。

    private class CustomContent {
        String key;
        String value;
    }


    public static Map<?, ?> jsonToMap(String jsonStr)
    {
         Gson gson = new Gson();
        Map<?, ?> objMap = null;
        if (gson != null)
        {
            java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<Map<?, ?>>()
            {
            }.getType();
            objMap = gson.fromJson(jsonStr, type);
        }
        return objMap;
    }

}
