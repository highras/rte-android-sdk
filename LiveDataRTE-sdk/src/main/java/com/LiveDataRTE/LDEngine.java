package com.LiveDataRTE;

import android.app.Activity;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import com.LiveDataRTE.IMLib.IIMPushProcessor;
import com.LiveDataRTE.IMLib.IMClient;
import com.LiveDataRTE.InternalEngine.EngineClient;
import com.LiveDataRTE.LiveDataStruct.*;
import com.LiveDataRTE.RTCLib.IRTCPushProcessor;
import com.LiveDataRTE.RTCLib.RTCClient;
import com.LiveDataRTE.RTMLib.IRTMPushProcessor;
import com.LiveDataRTE.RTMLib.RTMClient;
import com.LiveDataRTE.LDInterface.*;
import com.LiveDataRTE.RTMLib.RTMErrorCode;
import com.LiveDataRTE.ValueAdd.ValueAddedClient;
import com.fpnn.sdk.ErrorRecorder;

public class LDEngine {
    static HashMap<String, LDEngine> LDEngineClients = new HashMap<>();

    public RTMClient RTM = new RTMClient();
    public IMClient IM = new IMClient();
    public RTCClient RTC = new RTCClient();
    public ValueAddedClient ValueAdded = new ValueAddedClient();
    private EngineClient engineClient;


    public static LDEngine CreateEngine(String endpoint, long pid, long uid, IBasePushProcessor serverPushProcessor, Activity currentActivity){
        return CreateEngine(endpoint, pid, uid, serverPushProcessor, currentActivity, null);
    }


    public static LDEngine CreateEngine(String endpoint, long pid, long uid, IBasePushProcessor serverPushProcessor, Activity currentActivity, LiveDataConfig liveDataConfig)  {
        synchronized (LDEngineClients){
            String findkey = pid + ":" + uid;
            if (LDEngineClients.containsKey(findkey)){
                return LDEngineClients.get(findkey);
            }
            else
            {
                LDEngine ldEngine = new LDEngine(endpoint, pid,uid, serverPushProcessor, currentActivity, liveDataConfig);
                LDEngineClients.put(findkey, ldEngine);
                return ldEngine;
            }
        }
    }


    LDEngine(String endpoint, long pid, long uid, IBasePushProcessor serverPushProcessor, Activity currentActivity, LiveDataConfig liveDataConfig)  {
        try {
            Class<EngineClient> tt = EngineClient.class;
            Constructor constructor = tt.getDeclaredConstructor(String.class, long.class, long.class, IBasePushProcessor.class,Activity.class, LiveDataConfig.class);
            constructor.setAccessible(true);
            engineClient = (EngineClient) constructor.newInstance(endpoint,pid, uid, serverPushProcessor, currentActivity,liveDataConfig);
        }
        catch (Exception ex){
            ex.printStackTrace();
            return;
        }

        RTM.setEngineClient(this.engineClient);
        IM.setEngineClient(this.engineClient);
        RTC.setEngineClient(this.engineClient);
        ValueAdded.setEngineClient(this.engineClient);
    }


    /**
     *登陆
     * @param token     用户token
     * @param lang      用户语言(详见TranslateLang.java枚举列表)(当项目启用了自动翻译  如果客户端设置了语言会收到翻译后的结果 不设置语言或者为空则不会自动翻译,后续可以通过setTranslatedLanguage设置)
     * @param attr      登陆附加信息
     */
    public LDAnswer login(String token, String lang, Map<String, String> attr) {
        if (engineClient == null)
            return new LDAnswer(RTMErrorCode.RTM_EC_UNKNOWN_ERROR.value(),"engineClient is null");
        return engineClient.loginRTM(token, lang, attr, 0);
    }

    /**
     *登陆(新的验签方式)
     * @param token     用户token
     * @param lang      用户语言(详见TranslateLang.java枚举列表)(当项目启用了自动翻译  如果客户端设置了语言会收到翻译后的结果 不设置语言或者为空则不会自动翻译,后续可以通过setTranslatedLanguage设置)
     * @param attr      登陆附加信息
     */
    public LDAnswer login(String token, String lang, Map<String, String> attr, long ts) {
        if (engineClient == null)
            return new LDAnswer(RTMErrorCode.RTM_EC_UNKNOWN_ERROR.value(),"engineClient is null");
        return engineClient.loginRTM(token, lang, attr, ts);
    }


    /**
     *登陆
     * @param callback  登陆结果回调
     * @param token     用户token
     * @param lang      用户语言(详见TranslateLang.java枚举列表)(当项目启用了自动翻译  如果客户端设置了语言会收到翻译后的结果 不设置语言或者为空则不会自动翻译,后续可以通过setTranslatedLanguage设置)
     * @param attr      登陆附加信息
     */
    public void login(String token, String lang, Map<String, String> attr, IEmptyCallback callback) {
        if (engineClient == null) {
            callback.onError( new LDAnswer(RTMErrorCode.RTM_EC_UNKNOWN_ERROR.value(), "engineClient is null"));
            return;
        }
        engineClient.loginRTM(callback, token, lang, attr, 0);
    }


    /**
     *登陆(新的验签方式)
     * @param callback  登陆结果回调
     * @param token     用户token
     * @param lang      用户语言(详见TranslateLang.java枚举列表)(当项目启用了自动翻译  如果客户端设置了语言会收到翻译后的结果 不设置语言或者为空则不会自动翻译,后续可以通过setTranslatedLanguage设置)
     * @param attr      登陆附加信息
     */
    public void login(String token, String lang, Map<String, String> attr, long ts, IEmptyCallback callback) {
        if (engineClient == null) {
            callback.onError( new LDAnswer(RTMErrorCode.RTM_EC_UNKNOWN_ERROR.value(), "engineClient is null"));
            return;
        }
        engineClient.loginRTM(callback, token, lang, attr, ts);
    }

    public boolean isOnline() {
        if (engineClient == null)
            return false;
        return engineClient.isOnline();
    }


    /**释放ldengine(切换账号或者完全退出时候调用(释放资源,网络广播监听会持有LDEngine对象 如果不调用对象会一直持有不释放))
     */
    public void closeEngine(){
        engineClient.realClose();
        synchronized (LDEngineClients){
            String findkey = engineClient.getPid() + ":" + engineClient.getUid();
            LDEngineClients.remove(findkey);
        }
    }

    /**
     * 设置错误日志类
     * @param value
     */
    public void setErrorRecoder(ErrorRecorder value){
        if (engineClient == null)
            return;
        engineClient.setErrorRecoder(value);
    }

    public long getUid(){
        if (engineClient == null)
            return 0;
        return engineClient.getUid();
    }

    public long getPid(){
        if (engineClient == null)
            return 0;
        return engineClient.getPid();
    }

    /**
     * 设置RTM接收消息处理类(和IIMPushProcessor不能同时设置)
     * @param rtmPushProcessor
     */
    public String setRTMPushProcessor(IRTMPushProcessor rtmPushProcessor){
        if (engineClient == null)
            return "engineClient is null";
        return engineClient.setRTMPushProcessor(rtmPushProcessor);
    }

    /**
     * 设置IM接收消息处理类(和IRTMPushProcessor不能同时设置)
     * @param imPushProcessor
     */
    public String setIMPushProcessor(IIMPushProcessor imPushProcessor){
        if (engineClient == null)
            return "engineClient is null";
        return engineClient.setIMPushProcessor(imPushProcessor);
    }

    /**
     * 设置RTC接收消息处理类
     * @param rtcPushProcessor
     */
    public void setRTCPushProcessor(IRTCPushProcessor rtcPushProcessor){
        if (engineClient == null)
            return;
        engineClient.setRTCPushProcessor(rtcPushProcessor);
    }

    public void setBasePushProcessor(IBasePushProcessor basePushProcessor) {
        if (engineClient == null)
            return;
        engineClient.setBasePushProcessor(basePushProcessor);
    }

}
