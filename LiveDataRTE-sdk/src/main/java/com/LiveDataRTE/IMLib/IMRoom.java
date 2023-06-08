package com.LiveDataRTE.IMLib;

import com.LiveDataRTE.InternalEngine.EngineClient;
import com.LiveDataRTE.LDUtils;
import com.LiveDataRTE.LiveDataStruct;
import com.LiveDataRTE.LDInterface.*;
import com.LiveDataRTE.IMLib.IMStruct.*;
import com.fpnn.sdk.FunctionalAnswerCallback;
import com.fpnn.sdk.proto.Answer;
import com.fpnn.sdk.proto.Quest;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class IMRoom {
    int okRet = 0;
    EngineClient engineClient;
    LiveDataStruct.SendSource sendType = LiveDataStruct.SendSource.IM;

    public void setEngineClient(EngineClient _engineClient){
        engineClient = _engineClient;
    }

    public EngineClient getEngineClient(){
        return engineClient;
    }

    String safeNull(String msg) {
        return msg == null ? "" : msg;
    }

    String paramAttrs(JSONObject jsonObject){
        if (jsonObject == null)
            return "";
        return jsonObject.toString();
    }


    /**
     * 进入房间（如果需要密码 extra需要填入）
     * @param roomId   房间id
     * @param callback 回调
     * @param extra 密码
     * @param attrs 自定义内容
     */
    public void enterRoom(long roomId,   String extra, String attrs, IEmptyCallback callback) {
        Quest quest = new Quest("imclient_joinroom");
        quest.param("rid",roomId);
        if (extra!=null)
            quest.param("extra", extra);
        if (attrs!=null)
            quest.param("attrs",attrs);
        engineClient.sendQuestEmptyCallback(callback, quest);
    }


    /**
     * 邀请进入房间(只有房主或者管理员可以)
     * @param roomId  房间id
     * @param userIds  用户id列表
     * @param extra    邀请留言
     * @param attrs    用户自定义信息
     * @param callback 回调
     */
    public void inviteIntoRoom(long roomId, List<Long> userIds, String extra, String attrs, IEmptyCallback callback) {
        Quest quest = new Quest("imclient_inviteintoroom");
        quest.param("rid", roomId);
        quest.param("target_uids", userIds);
        quest.param("extra", safeNull(extra));
        quest.param("attrs", attrs);
        engineClient.sendQuestEmptyCallback(callback, quest);
    }

    /**
     * 回应邀请加入房间请求(被邀请用户)
     * @param roomId  房间id
     * @param from   邀请者Id
     * @param agree    是否同意
     * @param attrs    用户自定义信息
     * @param callback 回调
     */
    public void ackInvitedIntoRoom(long roomId, long from, boolean agree, String attrs, IEmptyCallback callback) {
        Quest quest = new Quest("imclient_ackinviteintoroom");
        quest.param("rid", roomId);
        quest.param("from", from);
        quest.param("agree", agree);
        quest.param("attrs", attrs);
        engineClient.sendQuestEmptyCallback(callback, quest);
    }


    /**
     * 移除房间成员(只有房主或管理员有权移除成员)
     * @param roomId 房间id
     * @param userIds 用户id列表
     * @param callback
     */
    public void removeRoomMembers(long roomId, List<Long> userIds, IEmptyCallback callback) {
        Quest quest = new Quest("imclient_removeroommembers");
        quest.param("rid", roomId);
        quest.param("uids", userIds);
        engineClient.sendQuestEmptyCallback(callback, quest);
    }

    /**
     * 退出房间
     * @param roomId    房间id
     * @param callback
     */
    public void leaveRoom(long roomId, IEmptyCallback callback) {
        Quest quest = new Quest("imclient_leaveroom");
        quest.param("rid", roomId);
        engineClient.sendQuestEmptyCallback(callback, quest);
    }

    /**
     * 解散房间(仅房主可以操作)
     * @param roomId    房间id
     * @param callback 回调
     */
    public void dismissRoom(final long roomId, final IEmptyCallback callback) {
        Quest quest = new Quest("imclient_dismissroom");
        quest.param("rid", roomId);
        engineClient.sendQuestEmptyCallback(callback, quest);
    }


    /**
     * 房主转让(转让后原房主身份变更为普通群员)
     * @param roomId    房间id
     * @param userId   用户id
     * @param callback 回调
     */
    public void changeRoomLeader(final long roomId, final long userId, final IEmptyCallback callback) {
        Quest quest = new Quest("imclient_transferroom");
        quest.param("rid", roomId);
        quest.param("to_uid", userId);
        engineClient.sendQuestEmptyCallback(callback, quest);
    }

    /**
     * 添加管理员(只有房主有权添加管理员)
     * @param roomId    房间id
     * @param userIds  用户id列表
     * @param callback 回调
     */
    public void addRoomManagers(final long roomId, final List<Long> userIds, final IEmptyCallback callback) {
        Quest quest = new Quest("imclient_addroommanagers");
        quest.param("rid", roomId);
        quest.param("uids", userIds);
        engineClient.sendQuestEmptyCallback(callback, quest);
    }

    /**
     * 移除管理员(只有房主有权移除管理员)
     * @param roomId  房间id
     * @param userIds  管理员用户id列表
     * @param callback 回调
     */
    public void removeRoomManagers(final long roomId, final List<Long> userIds, final IEmptyCallback callback) {
        Quest quest = new Quest("imclient_removeroommanagers");
        quest.param("rid", roomId);
        quest.param("uids", userIds);
        engineClient.sendQuestEmptyCallback(callback, quest);
    }

    
    /**获取房间成员
     * @param roomId    房间id
     * @param callback
     */
    public void getRoomMembers(final long roomId, final ICallback<List<IMRoomMemberInfo>> callback) {
        Quest quest = new Quest("imclient_getroommembers");
        quest.param("rid", roomId);
        engineClient.sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    List<IMRoomMemberInfo> roomMemberInfos = new ArrayList<>();
                    List<Map<String, String>> retdata = LDUtils.wantListHashMap(answer, "data");
                    for (Map<String, String> obj : retdata) {
                        long uid = LDUtils.wantLong(obj.get("uid"));
                        int roletype = LDUtils.wantInt(obj.get("role"));
                        IMRoomMemberInfo tt = new IMRoomMemberInfo(uid, roletype);
                        roomMemberInfos.add(tt);
                    }
                    callback.onSuccess(roomMemberInfos);
                } else {
                    callback.onError(engineClient.genLDAnswer(answer, errorCode));
                }
            }
        });
    }

    /**
     * 获取房间成员数量 该方法不需要是房间成员就可以获取的到
     * @param roomId    房间id
     */
    public void getRoomMembersCount(long roomId, final ICallback<Integer> callback) {
        Quest quest = new Quest("imclient_getroommembercount");
        quest.param("rid", roomId);
        engineClient.sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    int totalCount = LDUtils.wantInt(answer, "cn");
                    callback.onSuccess(totalCount);
                } else {
                    callback.onError(engineClient.genLDAnswer(answer, errorCode));
                }
            }
        });
    }
    

    /**
     * 获取用户所在的房间
     * @param callback 回调
     */
    public void getUserRooms( final ICallback<List<Long>> callback) {
        engineClient.getUserRooms(callback);
    }




    /**
     * 获取房间信息
     * @param roomIds  房间id
     * @param callback 回调
     */
    public void getRoomInfos(List<Long> roomIds, final ICallback<List<IMRoomInfo>> callback) {
        Quest quest = new Quest("imclient_getroominfos");
        quest.param("rids", roomIds);
        engineClient.sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    List<IMRoomInfo> imRoomInfos = new ArrayList<>();
                    List<Long> gids = LDUtils.wantLongList(answer, "xid");
                    List<String> names = LDUtils.getStringList(answer, "name");
                    List<String> portraitUrls = LDUtils.getStringList(answer, "portraitUrl");
                    List<String> profiles = LDUtils.getStringList(answer, "profile");
                    List<String> attrs = LDUtils.getStringList(answer, "attrs");
                    List<Integer> inviteGrants = LDUtils.wantIntList(answer, "inviteGrant");
                    List<Long> ownerUids = LDUtils.wantLongList(answer, "ownerUid");
                    List<Object> managerUids = (List<Object>) answer.want("managerUids");

                    for (int i=0;i<gids.size();i++){
                        IMRoomInfo imRoomInfo = new IMRoomInfo(gids.get(i),ownerUids.get(i),LDUtils.wantLongList(managerUids.get(i)),names.get(i),portraitUrls.get(i),profiles.get(i),attrs.get(i),
                                inviteGrants.get(i));
                        imRoomInfos.add(imRoomInfo);
                    }
                    callback.onSuccess(imRoomInfos);
                } else {
                    callback.onError(engineClient.genLDAnswer(answer, errorCode));
                }
            }
        });
    }

    /**
     * 获取入房间邀请列表
     * @param callback
     */
    public void getRoomInviteList(final ICallback<List<IMInviteInfo>> callback) {
        Quest quest = new Quest("imclient_getroominvitelist");
        engineClient.sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    ArrayList<IMInviteInfo> lists = new ArrayList<>();
                    List<Long> gids = LDUtils.wantLongList(answer, "targetXid");
                    List<Long> fromUids = LDUtils.wantLongList(answer, "fromXid");
                    List<String> attrs = (List<String>) (answer.want("attrs"));
                    List<Long> createTimes = LDUtils.wantLongList(answer, "createTime");

                    for (int i = 0; i < fromUids.size(); i++) {
                        lists.add(new IMInviteInfo(gids.get(i), createTimes.get(i), fromUids.get(i), attrs.get(i)));
                    }
                    callback.onSuccess(lists);
                } else {

                    callback.onError(engineClient.genLDAnswer(answer, errorCode));
                }
            }
        });
    }

}
