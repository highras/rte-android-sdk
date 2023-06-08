package com.LiveDataRTE.RTMLib;

import com.LiveDataRTE.LDInterface.*;

import java.util.List;
import java.util.Map;

class RTMGroup extends RTMRoom {

    /**
     * 获取群组人数
     * @param groupId   群组id
     * @param isOnline 是否获取在线人数
     * @param callback  <总人数，在线人数>回调
     * */
    public void getGroupMemberCount(long groupId, boolean isOnline, final IDoubleCallBack<Integer, Integer> callback) {
        engineClient.getGroupMemberCount(groupId, isOnline, callback);
    }

    /**
     * 获取群组用户列表
     * @param groupId   群组id
     * @param isOnline 是否获取在线人数列表
     * @param callback  <用户列表,用户在线列表>回调
     * */
    public void getGroupMembers(long groupId, boolean isOnline, final ICallback<RTMStruct.RTMGroupMembers> callback) {
        engineClient.getGroupMembers(groupId,isOnline, callback);
    }

    /**
     * 添加群组用户 (注意 调用接口的用户必须在群组里)
     * @param groupId   群组id
     * @param userIds   用户id集合
     * @param callback  回调
     * */
    public void addGroupMembers(long groupId, List<Long> userIds, final IEmptyCallback callback) {
        engineClient.addGroupMembers(groupId, userIds, callback);
    }


    /**
     * 删除群组用户
     * @param groupId   群组id
     * @param userIds   用户id集合
     * @param callback  回调
     * */
    public void removeGroupMembers(long groupId,List<Long> userIds,  final IEmptyCallback callback) {
        engineClient.deleteGroupMembers(groupId, userIds, callback);
    }


    /**
     * 获取用户所在的群组
     * @param callback  IRTMCallback回调
     * */
    public void getUserGroups( final ICallback<List<Long>> callback) {
        engineClient.getUserGroups(callback);
    }


    /**
     * 获取群组的公开信息和私有信息 (需要调用者是群组成员)
     * @param groupId   群组id
     * @param callback  <公开信息,私有信息>回调
     */
    public void getGroupInfo(final long groupId,final IGetinfoCallback callback) {
        engineClient.getGroupInfo(groupId, callback);
    }

    /**
     * 设置群组的公开信息或者私有信息
     * @param groupId   群组id
     * @param publicInfo    群组公开信息
     * @param privateInfo   群组 私有信息
     * @param callback  回调
     */
    public void setGroupInfo(long groupId, String publicInfo, String privateInfo, IEmptyCallback callback) {
        engineClient.setGroupInfo(groupId, publicInfo, privateInfo, callback);
    }


    /**
     * 获取群组的公开信息，每次最多获取100个群组
     * @param groupIds        群组id集合
     *@param callback     <群组id，群组公开信息>结构
     */
    public void getGroupsPublicInfo(List<Long> groupIds, final ICallback<Map<Long, String>> callback) {
        engineClient.getGroupsPublicInfo(groupIds, callback);
    }
}
