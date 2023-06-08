package com.LiveDataRTE.RTMLib;

import com.LiveDataRTE.LiveDataStruct;
import com.LiveDataRTE.LDInterface.*;

import java.util.List;
import java.util.Map;

public class RTMUser extends RTMFriend{

    /**
     * 设置用户自己的公开信息和者私有信息(publicInfo,privateInfo 最长 65535)
     * @param publicInfo  公开信息
     * @param privateInfo 私有信息
     * @param callback    回调
     */
    public void setUserInfo(String publicInfo, String privateInfo,  IEmptyCallback callback) {
        engineClient.setUserInfo(publicInfo, privateInfo, callback);
    }


    /**
     * 获取的用户公开信息和私有信息
     * @param callback ICallback<GroupInfoStruct>回调
     */
    public void getUserInfo( final IGetinfoCallback callback) {
        engineClient.getUserInfo(callback);
    }


    /**
     * 获取其他用户的公开信息，每次最多获取100人
     * @param callback UserAttrsCallback回调
     * @param userIds     用户uid集合
     */
    public void getUserPublicInfo(List<Long> userIds, final ICallback<Map<Long, String>> callback) {
        engineClient.getUserPublicInfo(userIds, callback);
    }


    /**
     * 查询用户在线状态
     * @param userIds   待查询的用户id集合
     * @param callback <在线用户列表>回调
     */
    public void getOnlineUsers(List<Long> userIds,  final ICallback<List<Long>> callback) {
        engineClient.getOnlineUsers(userIds, callback);
    }


    /**
     *添加key_value形式的变量（例如设置客户端信息，会保存在当前链接中）
     * @param callback IRTMEmptyCallback回调
     * @param attrs     客户端自定义属性值
     */
    void addAttributes(Map<String, String> attrs,  IEmptyCallback callback) {
        engineClient.addAttributes(attrs, callback);
    }


    /**
     * 获取用户属性
     * @param callback  用户属性回调 其中map的key
     *                  map中自动添加如下几个参数：
     *                  login：登录时间，utc时间戳
     *                  my：当前链接的attrs
     */
    void getAttributes( final ICallback<List<Map<String, String>>> callback) {
        engineClient.getAttributes(callback);
    }


    /**
     * 添加debug日志
     * @param callback  IRTMEmptyCallback回调(notnull)
     * @param message   消息内容
     * @param attrs     消息属性信息
     */
    void addDebugLog(String message, String attrs,  IEmptyCallback callback) {
        engineClient.addDebugLog(message, attrs, callback);
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
     * 删除设备
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

    /**
     * 获取存储的数据信息（仅能操作自己信息）(key:最长128字节，val：最长65535字节)
     * @param key      key值
     * @param callback  获取value回调
     */
    public void dataGet( String key, final ICallback<String> callback) {
        engineClient.dataGet(callback, key);
    }


    /**
     * 设置存储的数据信息（仅能操作自己信息）(key:最长128字节，val：最长65535字节)
     * @param key      key值
     * @param callback  IRTMEmptyCallback接口回调
     */
    public void dataSet(String key,  String value, final IEmptyCallback callback) {
        engineClient.dataSet(callback, key, value);
    }


    /**
     * 删除存储的数据信息
     * @param key      key值
     * @param callback  IRTMEmptyCallback接口回调
     */
    public void dataDelete( String key,  final IEmptyCallback callback) {
        engineClient.dataDelete(key, callback);
    }
}
