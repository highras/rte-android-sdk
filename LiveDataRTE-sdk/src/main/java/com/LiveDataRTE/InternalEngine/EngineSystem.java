package com.LiveDataRTE.InternalEngine;

import com.LiveDataRTE.LDInterface.*;
import com.fpnn.sdk.FunctionalAnswerCallback;
import com.fpnn.sdk.proto.Answer;
import com.fpnn.sdk.proto.Quest;
import com.LiveDataRTE.LiveDataStruct.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class EngineSystem extends EngineSocialContact {

    public void addAttributes(Map<String, String> attrs,  IEmptyCallback callback) {
        Quest quest = new Quest("addattrs");
        quest.param("attrs", attrs);
        sendQuestEmptyCallback(callback,quest);
    }


    public void getAttributes( final ICallback<List<Map<String, String>>> callback) {
        Quest quest = new Quest("getattrs");

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                List<Map<String, String>> attributes = new ArrayList<>();
                if (errorCode == okRet) {
                    attributes = LDUtils.wantListHashMap(answer, "attrs");
                    callback.onSuccess(attributes);
                }
                else
                    callback.onError(genLDAnswer(answer, errorCode));
            }
        });
    }


    public void addDebugLog(String message, String attrs,  IEmptyCallback callback) {
        Quest quest = new Quest("adddebuglog");
        quest.param("msg", message);
        quest.param("attrs", attrs);

        sendQuestEmptyCallback(callback, quest);
    }



    public void addDevice( String appType,  String deviceToken,  IEmptyCallback callback) {
        Quest quest = new Quest("adddevice");
        quest.param("apptype", appType);
        quest.param("devicetoken", deviceToken);

        sendQuestEmptyCallback(callback, quest);
    }


    public void removeDevice( String deviceToken,  IEmptyCallback callback) {
        Quest quest = new Quest("removedevice");
        quest.param("devicetoken", deviceToken);

        sendQuestEmptyCallback(callback, quest);
    }



    public void addDevicePushOption( int type,  long xid, List<Integer> messageTypes,  IEmptyCallback callback){
        Quest quest = new Quest("addoption");
        quest.param("type", type);
        quest.param("xid", xid);
        if (messageTypes != null)
            quest.param("mtypes", messageTypes);
        sendQuestEmptyCallback(callback, quest);
    }


    public void removeDevicePushOption(IEmptyCallback callback,  int type,  long xid, List<Integer> messageTypes){
        Quest quest = new Quest("removeoption");
        quest.param("type", type);
        quest.param("xid", xid);
        if (messageTypes != null)
            quest.param("mtypes", messageTypes);
        sendQuestEmptyCallback(callback, quest);
    }


    public void getDevicePushOption(final ICallback<DevicePushOption> callback) {
        Quest quest = new Quest("getoption");

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    DevicePushOption ret = new DevicePushOption();
                    ret.p2pPushOptions = LDUtils.wantDeviceOption(answer,"p2p");
                    ret.groupPushOptions = LDUtils.wantDeviceOption(answer,"group");
                    callback.onSuccess(ret);
                }
                else
                    callback.onError(genLDAnswer(answer,errorCode));
            }
        });
    }


    public void dataGet( final ICallback<String> callback,  String key) {
        Quest quest = new Quest("dataget");
        quest.param("key", key);

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                String value = "";
                if (errorCode == okRet) {
                    value = answer.getString("val");
                    callback.onSuccess(value);
                }
                else
                    callback.onError(genLDAnswer(answer,errorCode));
            }
        });
    }


    public void dataSet( final IEmptyCallback callback,  String key,  String value) {
        Quest quest = new Quest("dataset");
        quest.param("key", key);
        quest.param("val", value);
        sendQuestEmptyCallback(callback, quest);
    }


    public void dataDelete( String key,  final IEmptyCallback callback) {
        Quest quest = new Quest("datadel");
        quest.param("key", key);
        sendQuestEmptyCallback(callback, quest);
    }

}
