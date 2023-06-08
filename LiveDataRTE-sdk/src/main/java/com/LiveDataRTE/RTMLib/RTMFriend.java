package com.LiveDataRTE.RTMLib;

import com.LiveDataRTE.LDInterface.*;

import java.util.List;

public class RTMFriend extends RTMGroup {

    /**
     * 添加好友 (每次最多添加100人)
     * @param userIds   用户id集合
     * @param callback 回调
     */
    public void addFriends(List<Long> userIds, IEmptyCallback callback) {
        engineClient.addFriends(userIds, callback);
    }

    /**
     * 查询自己好友
     * @param callback 回调
     */
    public void getFriendList( final ICallback<List<Long>> callback) {
        engineClient.getFriends(callback);
    }


    /**
     * 删除好友 (每次最多删除100人)
     * @param userIds   用户id集合
     * @param callback 回调
     */
    public void deleteFriends(List<Long> userIds,  IEmptyCallback callback) {
        engineClient.deleteFriends(userIds, callback);
    }


    /**
     * 添加黑名单 (拉黑用户，每次最多添加100人，拉黑后对方不能给自己发消息，自己可以给对方发，双方能正常获取session)
     * @param userIds   用户id集合
     * @param callback 回调
     */
    public void addBlacklist( List<Long> userIds,  IEmptyCallback callback) {
        engineClient.addBlacklist(userIds, callback);
    }


    /**
     * 删除黑名单用户 (每次最多添加100人)
     * @param userIds   用户id集合
     * @param callback 回调
     */
    public void deleteBlacklist( List<Long> userIds,  IEmptyCallback callback) {
        engineClient.deleteBlacklist(userIds, callback);
    }


    /**
     * 查询黑名单
     * @param callback 回调
     */
    public void getBlacklist(final ICallback<List<Long>> callback) {
        engineClient.getBlacklist(callback);
    }

}
