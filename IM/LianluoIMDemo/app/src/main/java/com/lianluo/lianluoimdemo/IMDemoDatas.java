package com.lianluo.lianluoimdemo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tristan on 2016/11/22.
 */

public class IMDemoDatas {
    static IMDemoDatas myImDemoDatas = null;
    List<String> datas_devlist = new ArrayList<String>();
//    List<String> datas_tagList = new ArrayList<String>();
    public static IMDemoDatas getInstance(){
        if (myImDemoDatas==null){
            myImDemoDatas = new IMDemoDatas();
        }
        return myImDemoDatas;
    }
//tag 的操作：：：
//    public void addTags(ArrayList<String> tags){
//        for (String tag:tags){
//            if (datas_tagList.contains(tag)==false){
//                datas_tagList.add(tag);
//            }
//        }
//    }
//    public void deltags(ArrayList<String> tags){
//        for (String tag:tags){
//            if (datas_tagList.contains(tag)==true){
//                datas_tagList.remove(tag);
//            }
//        }
//
//    }
//    public void addTag(String tag){
//        if (datas_tagList.contains(tag)==false){
//            datas_tagList.add(tag);
//        }
//    }
//    public void delTag(String tag){
//        if (datas_tagList.contains(tag)==true){
//            datas_tagList.remove(tag);
//        }
//    }
//    public void clearTags(){
//        datas_tagList.clear();
//    }
//    public List<String> getTags(){
//        return datas_tagList;
//    }

//设备的操作

    public void addDevices(List<String> devices){
        for (String device:devices){
            if (datas_devlist.contains(device)==false){
                datas_devlist.add(device);
            }
        }
    }
    public void delDevices(List<String> devices){
        for (String device:devices){
            if (datas_devlist.contains(device)==true){
                datas_devlist.remove(device);
            }
        }
    }
    public void addDevice(String device){
        if (datas_devlist.contains(device)==false){
            datas_devlist.add(device);
        }
    }
    public void delDevice(String device){
        if (datas_devlist.contains(device)==true){
            datas_devlist.remove(device);
        }
    }
    public void clearDevices(){
        datas_devlist.clear();
    }
    public List<String> getDevices(){
        return datas_devlist;
    }




}
