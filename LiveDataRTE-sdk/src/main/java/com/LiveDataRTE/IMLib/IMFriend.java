package com.LiveDataRTE.IMLib;

import com.LiveDataRTE.LDInterface.*;
import com.LiveDataRTE.LDUtils;
import com.LiveDataRTE.IMLib.IMStruct.*;
import com.fpnn.sdk.FunctionalAnswerCallback;
import com.fpnn.sdk.proto.Answer;
import com.fpnn.sdk.proto.Quest;

import java.util.ArrayList;
import java.util.List;

public class IMFriend extends IMGroup {
    /**
     * 添加好友
     * @param callback 回调
     * @param userId   用户id
     * @param extra    验证消息
     * @param attrs    用户自定义消息
     */
    public void addFriend(final long userId, String extra, String attrs, final IEmptyCallback callback) {
        Quest quest = new Quest("imclient_addfriend");
        quest.param("ouid", userId);
        quest.param("extra", safeNull(extra));
        quest.param("attrs", attrs);
        engineClient.sendQuestEmptyCallback(callback, quest);
    }

    /**
     * 回应添加好友请求
     * @param callback   回调
     * @param userId     对方uid
     * @param agree      是否同意
     * @param attrs    用户自定义消息
     */
    public void ackAddFriend(long userId, final boolean agree, String attrs, final IEmptyCallback callback) {
        Quest quest = new Quest("imclient_ackaddfriend");
        quest.param("ouid", userId);
        quest.param("agree", agree);
        quest.param("attrs", attrs);
        engineClient.sendQuestEmptyCallback(callback, quest);
    }

    /**
     * 删除好友
     * @param userIds 用户id列表
     */
    public void deleteFriend(final List<Long> userIds, IEmptyCallback callback) {
        Quest quest = new Quest("delfriends");
        quest.param("friends", userIds);
        engineClient.sendQuestEmptyCallback(callback, quest);
    }


    /**
     * 获取自己好友列表
     * @param callback 回调
     */
    public void getFriendList(ICallback<List<Long>> callback) {
        Quest quest = new Quest("getfriends");
        engineClient.sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    List<Long> uids = LDUtils.wantLongList(answer, "uids");
                    callback.onSuccess(uids);
                }
                else {
                    callback.onError(engineClient.genLDAnswer(answer,errorCode));
                }
            }
        });
    }

    /**
     * 添加黑名单
     * @param callback 回调
     * @param userIds  用户id集合
     */
    public void addBlacklist(final List<Long> userIds, final IEmptyCallback callback) {
        Quest quest = new Quest("addblacks");
        quest.param("blacks", userIds);
        engineClient.sendQuestEmptyCallback(callback, quest);
    }

    /**
     * 删除黑名单用户
     * @param callback 回调
     * @param uids     用户id集合
     */
    public void delBlacklist(final List<Long> uids, final IEmptyCallback callback) {
        Quest quest = new Quest("delblacks");
        quest.param("blacks", uids);
        engineClient.sendQuestEmptyCallback(callback, quest);
    }


    /**
     * 查询黑名单
     * @param callback 回调
     */
    public void getBlacklist(final ICallback<List<Long>> callback) {
        Quest quest = new Quest("getblacks");
        engineClient.sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet){
                    List<Long> uids = LDUtils.wantLongList(answer, "uids");
                    callback.onSuccess(uids);
                }else {
                    callback.onError(engineClient.genLDAnswer(answer,errorCode));
                }
            }
        });
    }

    /**
     * 获取好友申请列表
     */
    public void getFriendApplyList(ICallback<List<IMApplyInfo>> callback){
        Quest quest = new Quest("imclient_getfriendapplylist");
        engineClient.sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet){
                    ArrayList<IMApplyInfo> lists = new ArrayList<>();
                    List<Long> fromUids = LDUtils.wantLongList(answer,"fromUid");
                    List<String> grantExtras = (List<String>)(answer.want("grantExtra"));
                    List<String> attrs = (List<String>)(answer.want("attrs"));
                    List<Long> createTimes = LDUtils.wantLongList(answer,"createTime");

                    for (int i =0;i< fromUids.size();i++){
                        lists.add(new IMApplyInfo(fromUids.get(i),createTimes.get(i),grantExtras.get(i),attrs.get(i)));
                    }
                    callback.onSuccess(lists);
                }else{
                    callback.onError(engineClient.genLDAnswer(answer,errorCode));
                }
            }
        });
    }


    /**
     * 获取自己发出的添加好友列表
     */
    public void getFriendRequestList(ICallback<List<IMRquestInfo>> callback){
        Quest quest = new Quest("imclient_getfriendrequestlist");
        engineClient.sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet){
                    ArrayList<IMRquestInfo> lists = new ArrayList<>();
                    List<Long> fromUids = LDUtils.wantLongList(answer,"targetXid");
                    List<String> attrs = (List<String>)(answer.want("attrs"));
                    List<Long> createTimes = LDUtils.wantLongList(answer,"createTime");

                    for (int i =0;i< fromUids.size();i++){
                        lists.add(new IMRquestInfo(fromUids.get(i),createTimes.get(i),attrs.get(i)));
                    }
                    callback.onSuccess(lists);
                }else{
                    callback.onError(engineClient.genLDAnswer(answer,errorCode));
                }
            }
        });
    }
}
