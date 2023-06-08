package com.LiveDataRTE.IMLib;

import com.LiveDataRTE.LDUtils;
import com.LiveDataRTE.LiveDataStruct;
import com.LiveDataRTE.LDInterface.*;
import com.fpnn.sdk.FunctionalAnswerCallback;
import com.fpnn.sdk.proto.Answer;
import com.fpnn.sdk.proto.Quest;

import java.util.ArrayList;
import java.util.List;

public class IMUser extends IMFriend {
    /**
     * 获取用户信息
     * @param userIds
     * @param callback
     */
    public void getUserInfos(List<Long> userIds, ICallback<List<IMStruct.IMUserInfo>> callback){
        Quest quest = new Quest("imclient_getuserinfos");
        quest.param("uids", userIds);
        engineClient.sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    List<IMStruct.IMUserInfo> IMUserInfos = new ArrayList<>();
                    List<Long> uids = LDUtils.wantLongList(answer, "uid");
                    List<String> names = LDUtils.getStringList(answer, "name");
                    List<String> portraitUrls = LDUtils.getStringList(answer, "portraitUrl");
                    List<String> profiles = LDUtils.getStringList(answer, "profile");
                    List<String> attrs = LDUtils.getStringList(answer, "attrs");
                    List<Integer> applyGrants = LDUtils.wantIntList(answer, "applyGrant");

                    for (int i=0;i<uids.size();i++){
                        IMStruct.IMUserInfo imUserInfo = new IMStruct.IMUserInfo(uids.get(i),names.get(i),portraitUrls.get(i),profiles.get(i),attrs.get(i),
                                LiveDataStruct.AddPermission.intToEnum(applyGrants.get(i)));
                        IMUserInfos.add(imUserInfo);
                    }
                    callback.onSuccess(IMUserInfos);
                } else {
                    callback.onError(engineClient.genLDAnswer(answer, errorCode));
                }
            }
        });
    }

    /**
     * 添加android推送设备token信息(Google FCM)
     * @param  callback
     * @param deviceToken   设备推送token
     */
    public void addDevice(String deviceToken,  IEmptyCallback callback) {
        engineClient.addDevice("fcm", deviceToken, callback);
    }

    /**
     * 删除设备，
     * @param  callback
     * @param deviceToken   设备推送token
     */
    public void removeDevice( String deviceToken,  IEmptyCallback callback) {
        engineClient.removeDevice(deviceToken, callback);
    }



    /**设置设备推送属性(注意此接口是设置个人或群组某个类型的type不推送的设置)
     * @param callback
     * @param type  type=0, 设置某个p2p 不推送；type=1, 设置某个group不推送
     * @param xid   当type =0 时 表示userid；当type =1时 表示groupId
     * @param messageTypes (为空，则所有mtype均不推送;否则表示指定mtype不推送)
     */
    public void addDevicePushOption( int type,  long xid, List<Integer> messageTypes,  IEmptyCallback callback){
        engineClient.addDevicePushOption(type, xid, messageTypes, callback);
    }


    /**取消设备推送属性(和addDevicePushOption对应)
     * @param callback
     * @param type  type=0, 取消p2p推送属性；type=1, 取消group推送属性
     * @param xid   当type =0 时 表示userid；当type =1时 表示groupId
     * @param messageTypes  需要取消设置的messagetype集合(如果为空表示什么都不做)
     */
    public void removeDevicePushOption(int type,  long xid, List<Integer> messageTypes, IEmptyCallback callback){
        engineClient.removeDevicePushOption(callback, type, xid, messageTypes);
    }


    /**获取设备推送属性(addDevicePushOption的结果)
     * @param callback 回调
     */
    public void getDevicePushOption(final ICallback<LiveDataStruct.DevicePushOption> callback) {
        engineClient.getDevicePushOption(callback);
    }
}
