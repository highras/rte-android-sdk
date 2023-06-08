package com.LiveDataRTE.ValueAdd;

import com.LiveDataRTE.InternalEngine.EngineClient;
import com.LiveDataRTE.LiveDataStruct.*;
import com.LiveDataRTE.LDInterface.*;
import com.LiveDataRTE.ValueAdd.AddValueStruct.*;
import com.fpnn.sdk.proto.Quest;

//增值服务类
public class ValueAddedClient {
    EngineClient engineClient;

    public static final String defaultCodec = "AMR_WB";
    public static final int sample_rate = 16000;
    public void setEngineClient(EngineClient engineClient_){
        this.engineClient = engineClient_;
    }


    /**
     *设置目标翻译语言
     * @param callback  IRTMEmptyCallback回调
     * @param targetLanguage    目标语言(详见TranslateLang.java语言列表)
     */
    public void setTranslatedLanguage(String targetLanguage, IEmptyCallback callback) {
        String slang ="";
        if (targetLanguage!=null)
            slang = targetLanguage;
        Quest quest = new Quest("setlang");
        quest.param("lang", slang);
        engineClient.sendQuestEmptyCallback(callback,quest);
    }


    /**
     *文本翻译 (调用此接口需在管理系统启用翻译系统）
     * @param callback      IRTMCallback<TranslatedInfo>回调
     * @param text          需要翻译的内容
     * @param destinationLanguage   目标语言
     * @param sourceLanguage        源文本语言
     * @param type                  可选值为chat或mail。如未指定，则默认使用'chat'
     * @param profanity             对翻译结果进行敏感语过滤。设置为以下2项之一: off, censor，默认：off
     */
    public void translate( String text, String destinationLanguage, String sourceLanguage,
                           TranslateType type, ProfanityType profanity,final ICallback<TranslatedInfo> callback){
        engineClient.translate(text, destinationLanguage,sourceLanguage,type,profanity,callback);
    }


    /**
     *文本检测 (调用此接口需在管理系统启用文本审核系统）
     * @param callback      IRTMCallback<CheckResult>回调
     * @param text          需要检测的文本
     */
    public void textCheck(String text,final ICallback<CheckResult> callback){
        engineClient.textCheck(callback,text);
    }

    /**图片检测 (调用此接口需在管理系统启用图片审核系统)
     * @param callback  ICallback<CheckResult>回调
     * @param url       url地址
     * @param strategyId 对应文本审核的策略编号(https://docs.ilivedata.com/textcheck/technologydocument/introduction/ 可空)
     */
    public void imageCheck( String url, String strategyId, ICallback<CheckResult> callback) {
        engineClient.checkContentAsync(callback, url, CheckSourceType.URL, CheckType.PIC, "", null,"",0, strategyId);
    }


    /**图片检测 
     * @param callback  ICallback<CheckResult>回调
     * @param content   图片内容
     * @param strategyId 对应文本审核的策略编号(https://docs.ilivedata.com/textcheck/technologydocument/introduction/)
     */
    public void imageCheck( byte[] content, String strategyId,  ICallback<CheckResult> callback) {
        engineClient.checkContentAsync(callback, content, CheckSourceType.CONTENT, CheckType.PIC, "", null,"",0, strategyId);
    }


    /**语音检测 (调用此接口需在管理系统启用语音审核系统)
     * @param callback  ICallback<CheckResult>回调
     * @param url       语音url地址
     * @param lang      语言(详见TranscribeLang.java枚举列表)
     * @param codec     音频格式(传空默认"AMR_WB")
     * @param srate     采样率(传0默认16000)
     * @param strategyId 对应文本审核的策略编号(https://docs.ilivedata.com/textcheck/technologydocument/introduction/ 可空)
     */
    public void audioCheckURL(String url, String lang, String codec, int srate, String strategyId,ICallback<CheckResult> callback) {
        engineClient.checkContentAsync(callback, url, CheckSourceType.URL, CheckType.AUDIO, "", lang,codec, srate, strategyId);
    }

    /**语音检测
     * @param callback  ICallback<CheckResult>回调
     * @param content   语音内容
     * @param lang      语言(详见TranscribeLang.java枚举列表)
     * @param codec     音频格式(传空默认"AMR_WB")
     * @param srate     采样率(传0默认16000)
     * @param strategyId 对应文本审核的策略编号(https://docs.ilivedata.com/textcheck/technologydocument/introduction/ 可空)
     */
    public void audioCheck(byte[] content, String lang, String codec, int srate, String strategyId,ICallback<CheckResult> callback) {
        engineClient.checkContentAsync(callback, content, CheckSourceType.CONTENT, CheckType.AUDIO, "", lang, codec, srate, strategyId);
    }


    /**视频检测 (调用此接口需在管理系统启用视频审核系统)
     * @param callback  ICallback<CheckResult>回调
     * @param url       视频url地址
     * @param strategyId 对应文本审核的策略编号(https://docs.ilivedata.com/textcheck/technologydocument/introduction/ 可空)
     */
    public void videoCheckURL(String url,String strategyId, ICallback<CheckResult> callback) {
        engineClient.checkContentAsync(callback, url, CheckSourceType.URL, CheckType.VIDEO, "", null,"",0, strategyId);
    }

    /**视频检测
     * @param callback  ICallback<CheckResult>回调
     * @param content   视频内容
     * @param videoName   视频名称
     * @param strategyId 对应文本审核的策略编号(https://docs.ilivedata.com/textcheck/technologydocument/introduction/ 可空)
     */
    public void videoCheck(byte[] content, String videoName, String strategyId,ICallback<CheckResult> callback) {
        engineClient.checkContentAsync(callback, content, CheckSourceType.CONTENT, CheckType.VIDEO, videoName, null, "",0, strategyId);
    }

    /**语音转文字 (调用此接口需在管理系统启用语音识别系统) codec为空则默认为AMR_WB,srate为0或者空则默认为16000
     * @param callback  ICallback<AudioTextStruct>回调
     * @param url       语音url地址
     * @param lang      语言(详见TranscribeLang.java枚举值)
     * @param codec     音频格式(传空默认"AMR_WB")
     * @param srate     采样率(传0默认16000)
     */
    public void audioToTextURL(String url, String lang, String codec, int srate, ICallback<AudioTextStruct> callback) {
        engineClient.audioToTextAsync(callback, url, CheckSourceType.URL, lang, codec, srate);
    }


    /**语音转文字
     * @param callback  ICallback<AudioTextStruct>
     * @param content   语音内容
     * @param lang      语言(详见TranscribeLang.java枚举值)
     * @param codec     音频格式(传空默认"AMR_WB")
     * @param srate     采样率(传0默认16000)
     */
    public void audioToText(byte[] content, String lang, String codec, int srate, ICallback<AudioTextStruct> callback) {
        engineClient.audioToTextAsync(callback, content, CheckSourceType.CONTENT, lang, codec, srate);
    }

}
