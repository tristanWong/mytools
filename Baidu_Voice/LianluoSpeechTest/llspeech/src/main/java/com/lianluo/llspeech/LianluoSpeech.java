package com.lianluo.llspeech;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.SynthesizerTool;
import com.baidu.tts.client.TtsMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Tristan on 2017/4/14.
 */

public class LianluoSpeech implements SpeechSynthesizerListener {
    private static final String TAG = "LianluoSpeech";

    private String mSampleDirPath = null; //本地文件目录
    private SpeechSynthesizer mSpeechSynthesizer;
    private static final String SAMPLE_DIR_NAME = "lianluospeechdir";
    private static final String SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female.dat";
    private static final String SPEECH_MALE_MODEL_NAME = "bd_etts_speech_male.dat";
    private static final String TEXT_MODEL_NAME = "bd_etts_text.dat";
    //使用临时授权文件
//    private static final String LICENSE_FILE_NAME = "temp_license_2017-04-14";
    private static final String ENGLISH_SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female_en.dat";
    private static final String ENGLISH_SPEECH_MALE_MODEL_NAME = "bd_etts_speech_male_en.dat";
    private static final String ENGLISH_TEXT_MODEL_NAME = "bd_etts_text_en.dat";

    static String mAppid;
    static String mAppkey;
    static String mAppsecret;
    static Context mContext;
    static LianluoSpeech llSpeech = null;

    /**
     * 获取Speech对象
     * @param context
     * @param appid
     * @param appkey
     * @param appsecret
     * @return
     */
    public static LianluoSpeech getInstance(Context context,String appid,String appkey,String appsecret){
        if (llSpeech == null){
            llSpeech = new LianluoSpeech();

        }
        mContext = context;
        mAppid = appid;
        mAppkey = appkey;
        mAppsecret = appsecret;
        return llSpeech;
    }


    public void LianluoSpeechInit(){
        initialEnv();
        initialTts();

    }

    private void initialEnv() {
        if (mSampleDirPath == null) {
            String sdcardPath = Environment.getExternalStorageDirectory().toString();
            mSampleDirPath = sdcardPath + "/" + SAMPLE_DIR_NAME;
        }
        Log.d(TAG, "initialEnv: mSampleDirPath =【 "+mSampleDirPath+"】");
        makeDir(mSampleDirPath);
        copyFromAssetsToSdcard(false, SPEECH_FEMALE_MODEL_NAME, mSampleDirPath + "/" + SPEECH_FEMALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, SPEECH_MALE_MODEL_NAME, mSampleDirPath + "/" + SPEECH_MALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, TEXT_MODEL_NAME, mSampleDirPath + "/" + TEXT_MODEL_NAME);
//        copyFromAssetsToSdcard(false, LICENSE_FILE_NAME, mSampleDirPath + "/" + LICENSE_FILE_NAME);

        copyFromAssetsToSdcard(false, "english/" + ENGLISH_SPEECH_FEMALE_MODEL_NAME, mSampleDirPath + "/"
                + ENGLISH_SPEECH_FEMALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, "english/" + ENGLISH_SPEECH_MALE_MODEL_NAME, mSampleDirPath + "/"
                + ENGLISH_SPEECH_MALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, "english/" + ENGLISH_TEXT_MODEL_NAME, mSampleDirPath + "/"
                + ENGLISH_TEXT_MODEL_NAME);
    }






    private void makeDir(String dirPath) {
        Log.d(TAG, "makeDir: ==========================》》》》");


        File file = new File(dirPath);
        if (!file.exists()) {
            Log.d(TAG, "makeDir: 》》》》》》》》》》》》》》》》》》》 Create  >>> "+file.mkdirs());

        }
    }

    /**
     * 将sample工程需要的资源文件拷贝到SD卡中使用（授权文件为临时授权文件，请注册正式授权）
     *
     * @param isCover 是否覆盖已存在的目标文件
     * @param source
     * @param dest
     */
    private void copyFromAssetsToSdcard(boolean isCover, String source, String dest) {
//        if (source.equals(LICENSE_FILE_NAME)){
//            Log.d(TAG, "copyFromAssetsToSdcard: start copyFromAssetsToSdcard>>>>source = "+source);
//        }
        File file = new File(dest);
        if (isCover || (!isCover && !file.exists())) {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = mContext.getResources().getAssets().open(source);
                String path = dest;
                fos = new FileOutputStream(path);
//                if (source.equals(LICENSE_FILE_NAME)){
//                    Log.d(TAG, "copyFromAssetsToSdcard: start copyFromAssetsToSdcard>>>> "+fos);
//                }

                byte[] buffer = new byte[1024];
                int size = 0;
                while ((size = is.read(buffer, 0, 1024)) >= 0) {
//                    if (source.equals(LICENSE_FILE_NAME)) {
//                        Log.d(TAG, "copyFromAssetsToSdcard: size = " + size);
//                    }
                    fos.write(buffer, 0, size);
                }
            } catch (FileNotFoundException e) {
//                if (source.equals(LICENSE_FILE_NAME)){
//                    Log.d(TAG, "copyFromAssetsToSdcard: start copyFromAssetsToSdcard>>>>FileNotFoundException:: "+e.toString());
//                }
                e.printStackTrace();
            } catch (IOException e) {
//                if (source.equals(LICENSE_FILE_NAME)){
//                    Log.d(TAG, "copyFromAssetsToSdcard: start copyFromAssetsToSdcard>>>>IOException::  "+e.toString());
//                }
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void initialTts() {
        this.mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        this.mSpeechSynthesizer.setContext(mContext);
        this.mSpeechSynthesizer.setSpeechSynthesizerListener(this);
        // 文本模型文件路径 (离线引擎使用)
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, mSampleDirPath + "/"
                + TEXT_MODEL_NAME);
        // 声学模型文件路径 (离线引擎使用)
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, mSampleDirPath + "/"
                + SPEECH_FEMALE_MODEL_NAME);
        // 本地授权文件路径,如未设置将使用默认路径.设置临时授权文件路径，LICENCE_FILE_NAME请替换成临时授权文件的实际路径，仅在使用临时license文件时需要进行设置，如果在[应用管理]中开通了正式离线授权，不需要设置该参数，建议将该行代码删除（离线引擎）
        // 如果合成结果出现临时授权文件将要到期的提示，说明使用了临时授权文件，请删除临时授权即可。
//        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_LICENCE_FILE, mSampleDirPath + "/"
//                + LICENSE_FILE_NAME);
        // 请替换为语音开发者平台上注册应用得到的App ID (离线授权)
        this.mSpeechSynthesizer.setAppId("9513277"/*这里只是为了让Demo运行使用的APPID,请替换成自己的id。*/);
        // 请替换为语音开发者平台注册应用得到的apikey和secretkey (在线授权)
        this.mSpeechSynthesizer.setApiKey("N1st6GhRgU3vGaTNhNKPodXS",
                "915329dfa57f2cb4e8c10c3307658cf8"/*这里只是为了让Demo正常运行使用APIKey,请替换成自己的APIKey*/);
        // 发音人（在线引擎），可用参数为0,1,2,3。。。（服务器端会动态增加，各值含义参考文档，以文档说明为准。0--普通女声，1--普通男声，2--特别男声，3--情感男声。。。）
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置Mix模式的合成策略
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        // 授权检测接口(只是通过AuthInfo进行检验授权是否成功。)
        // AuthInfo接口用于测试开发者是否成功申请了在线或者离线授权，如果测试授权成功了，可以删除AuthInfo部分的代码（该接口首次验证时比较耗时），不会影响正常使用（合成使用时SDK内部会自动验证授权）
        AuthInfo authInfo = this.mSpeechSynthesizer.auth(TtsMode.MIX);

        if (authInfo.isSuccess()) {
            Log.d(TAG, "initialTts: auth success");
        } else {
            String errorMsg = authInfo.getTtsError().getDetailMessage();
            Log.d(TAG, "initialTts: auth failed errorMsg=" + errorMsg);
        }

        // 初始化tts
        mSpeechSynthesizer.initTts(TtsMode.MIX);
        // 加载离线英文资源（提供离线英文合成功能）
        int result =
                mSpeechSynthesizer.loadEnglishModel(mSampleDirPath + "/" + ENGLISH_TEXT_MODEL_NAME, mSampleDirPath
                        + "/" + ENGLISH_SPEECH_FEMALE_MODEL_NAME);
        Log.d(TAG, "initialTts: result = "+result);

        //打印引擎信息和model基本信息
        printEngineInfo();
    }


    /**
     * 打印引擎so库版本号及基本信息和model文件的基本信息
     */
    private void printEngineInfo() {
        Log.d(TAG, "printEngineInfo: EngineVersioin=" + SynthesizerTool.getEngineVersion());
        Log.d(TAG, "printEngineInfo: EngineInfo=" + SynthesizerTool.getEngineInfo());
        String textModelInfo = SynthesizerTool.getModelInfo(mSampleDirPath + "/" + TEXT_MODEL_NAME);
        Log.d(TAG, "printEngineInfo: textModelInfo=" + textModelInfo);
        String speechModelInfo = SynthesizerTool.getModelInfo(mSampleDirPath + "/" + SPEECH_FEMALE_MODEL_NAME);
        Log.d(TAG, "printEngineInfo: speechModelInfo=" + speechModelInfo);
    }

    public void speekText(String text){
        int result = this.mSpeechSynthesizer.speak(text);
        if (result < 0) {
            Log.d(TAG, "speekText: error,please look up error code in doc or URL:http://yuyin.baidu.com/docs/tts/122 ");
        }
    }

    public void pauseSpeek(){
        if (mSpeechSynthesizer!=null) {
            mSpeechSynthesizer.pause();
        }

    }
    public void resumeSpeek(){
        if (mSpeechSynthesizer!=null) {
            mSpeechSynthesizer.resume();
        }
    }

    public void stopSpeek(){
        if (mSpeechSynthesizer!=null) {
            mSpeechSynthesizer.stop();
        }
    }
    public void exitSpeek(){
        if (mSpeechSynthesizer!=null) {
            mSpeechSynthesizer.release();
        }
    }

    @Override
    public void onSynthesizeStart(String s) {

    }

    @Override
    public void onSynthesizeDataArrived(String s, byte[] bytes, int i) {

    }

    @Override
    public void onSynthesizeFinish(String s) {

    }

    @Override
    public void onSpeechStart(String s) {

    }

    @Override
    public void onSpeechProgressChanged(String s, int i) {

    }

    @Override
    public void onSpeechFinish(String s) {

    }

    @Override
    public void onError(String s, SpeechError speechError) {
        Log.d(TAG, "onError: >>>>>> s = "+s);
    }
}
