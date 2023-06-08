package com.LiveDataRTE.IMLib;

import com.LiveDataRTE.LDUtils;
import com.LiveDataRTE.LiveDataStruct.*;
import com.LiveDataRTE.IMLib.IMStruct.*;
import com.LiveDataRTE.LDInterface.*;
import com.fpnn.sdk.FunctionalAnswerCallback;
import com.fpnn.sdk.proto.Answer;
import com.fpnn.sdk.proto.Quest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class IMGroup extends IMRoom {
    /**
     * 加入群组（如果群组允许任何人加入，直接加入成功；如果需要验证才能加入，需要群主和管理员进行处理)
     * @param extra    附言备注
     * @param attrs    用户自定义信息
     * @param callback IMCallback回调
     */
    public void joinGroup(long groupId, String extra, String attrs, IEmptyCallback callback) {
        Quest quest = new Quest("imclient_joingroup");
        quest.param("gid", groupId);
        quest.param("extra", extra);
        quest.param("attrs", attrs);
        engineClient.sendQuestEmptyCallback(callback, quest);
    }

    /**
     * 回应入群请求
     * @param groupId  群组id
     * @param agree    是否同意
     * @param fromUid  申请者uid
     * @param attrs    用户自定义信息
     * @param callback 回调
     */
    public void ackJoinGroup(long groupId, long fromUid, boolean agree, String attrs, final IEmptyCallback callback) {
        Quest quest = new Quest("imclient_ackjoingroup");
        quest.param("gid", groupId);
        quest.param("agree", agree);
        quest.param("from", fromUid);
        quest.param("attrs", attrs);
        engineClient.sendQuestEmptyCallback(callback, quest);
    }

    /**
     * 邀请入群(默认只有群主和管理员可以邀请 可以通过群权限设置允许普通成员邀请其他人加入群组)
     * @param groupId  群组id
     * @param userIds  用户id列表
     * @param extra    邀请留言
     * @param attrs    用户自定义信息
     * @param callback 回调
     */
    public void inviteIntoGroup(long groupId, List<Long> userIds, String extra, String attrs, IEmptyCallback callback) {
        Quest quest = new Quest("imclient_inviteintogroup");
        quest.param("gid", groupId);
        quest.param("target_uids", userIds);
        quest.param("extra", safeNull(extra));
        quest.param("attrs", attrs);
        engineClient.sendQuestEmptyCallback(callback, quest);
    }


    /**
     * 回应邀请入群(被邀请用户)
     * @param groupId  群组id
     * @param userId   邀请者用户Id
     * @param agree    是否同意
     * @param callback 回调
     */
    public void ackInvitedIntoGroup(long groupId, long userId, boolean agree,  String attrs, IEmptyCallback callback) {
        Quest quest = new Quest("imclient_ackinviteintogroup");
        quest.param("gid", groupId);
        quest.param("from", userId);
        quest.param("agree", agree);
        quest.param("attrs", attrs);
        engineClient.sendQuestEmptyCallback(callback, quest);
    }


    /**
     * 移除群组成员(只有群主或管理员有权移除成员)
     * @param groupId  群组id
     * @param userIds  用户id列表
     * @param callback 回调
     */
    public void removeGroupMembers(final long groupId, final List<Long> userIds, final IEmptyCallback callback) {
        Quest quest = new Quest("imclient_removegroupmembers");
        quest.param("gid", groupId);
        quest.param("uids", userIds);
        engineClient.sendQuestEmptyCallback(callback, quest);
    }


    /**
     * 群主转让(转让后原群主身份变更为普通群员)
     * @param groupId  群组id
     * @param userId   用户id
     * @param callback 回调
     */
    public void changeGroupLeader(final long groupId, final long userId, final IEmptyCallback callback) {
        Quest quest = new Quest("imclient_transfergroup");
        quest.param("gid", groupId);
        quest.param("to_uid", userId);
        engineClient.sendQuestEmptyCallback(callback, quest);
    }

    /**
     * 添加管理员(只有群主有权添加管理员)
     * @param groupId  群组id
     * @param userIds  用户id列表
     * @param callback 回调
     */
    public void addGroupManagers(final long groupId, final List<Long> userIds, final IEmptyCallback callback) {
        Quest quest = new Quest("imclient_addgroupmanagers");
        quest.param("gid", groupId);
        quest.param("uids", userIds);
        engineClient.sendQuestEmptyCallback(callback, quest);
    }

    /**
     * 移除管理员(只有群主有权移除管理员)
     * @param groupId  群组id
     * @param userIds  管理员用户id列表
     * @param callback 回调
     */
    public void removeGroupManagers(final long groupId, final List<Long> userIds, final IEmptyCallback callback) {
        Quest quest = new Quest("imclient_removegroupmanagers");
        quest.param("gid", groupId);
        quest.param("uids", userIds);
        engineClient.sendQuestEmptyCallback(callback, quest);
    }


    /**
     * 退出群组(群主不能退出，如果为群主，将返回错误，需要先将群主身份转让)
     * @param groupId  群组id
     * @param callback 回调
     */
    public void leaveGroup(final long groupId, final IEmptyCallback callback) {
        Quest quest = new Quest("imclient_leavegroup");
        quest.param("gid", groupId);
        engineClient.sendQuestEmptyCallback(callback, quest);
    }


    /**
     * 解散群组(仅群主可以操作)
     * @param groupId  群组id
     * @param callback 回调
     */
    public void dismissGroup(final long groupId, final IEmptyCallback callback) {
        Quest quest = new Quest("imclient_dismissgroup");
        quest.param("gid", groupId);
        engineClient.sendQuestEmptyCallback(callback, quest);
    }


    /**
     * 获取群组信息
     * @param groupIds  群组id
     * @param callback 回调
     */
    public void getGroupInfos(List<Long> groupIds, final ICallback<List<IMGroupInfo>> callback) {
        Quest quest = new Quest("imclient_getgroupinfos");
        quest.param("gids", groupIds);
        engineClient.sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    List<IMGroupInfo> imGroupInfos = new ArrayList<>();
                    List<Long> gids = LDUtils.wantLongList(answer, "xid");
                    List<String> names = LDUtils.getStringList(answer, "name");
                    List<String> portraitUrls = LDUtils.getStringList(answer, "portraitUrl");
                    List<String> profiles = LDUtils.getStringList(answer, "profile");
                    List<String> attrs = LDUtils.getStringList(answer, "attrs");
                    List<Integer> applyGrants = LDUtils.wantIntList(answer, "applyGrant");
                    List<Integer> inviteGrants = LDUtils.wantIntList(answer, "inviteGrant");
                    List<Long> ownerUids = LDUtils.wantLongList(answer, "ownerUid");
                    List<Object> managerUids = (List<Object>) answer.want("managerUids");

                    for (int i=0;i<gids.size();i++){
                        IMGroupInfo imGroupInfo = new IMGroupInfo(gids.get(i),ownerUids.get(i),LDUtils.wantLongList(managerUids.get(i)),names.get(i),portraitUrls.get(i),profiles.get(i),attrs.get(i),
                                AddPermission.intToEnum(applyGrants.get(i)), inviteGrants.get(i));
                        imGroupInfos.add(imGroupInfo);
                    }
                    callback.onSuccess(imGroupInfos);
                } else {
                    callback.onError(engineClient.genLDAnswer(answer, errorCode));
                }
            }
        });
    }


    /**获取群组成员
     * @param groupId  群组id
     * @param callback
     */
    public void getGroupMembers(final long groupId, final ICallback<List<IMGroupMemberInfo>> callback) {
        Quest questgroup = new Quest("imclient_getgroupmembers");
        questgroup.param("gid", groupId);
        engineClient.sendQuest(questgroup, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    List<IMGroupMemberInfo> groupMemberInfos = new ArrayList<>();
                    List<Map<String, String>> retdata = LDUtils.wantListHashMap(answer, "data");
                    for (Map<String, String> obj : retdata) {
                        long uid = LDUtils.wantLong(obj.get("uid"));
                        int roletype = LDUtils.wantInt(obj.get("role"));
                        int online = LDUtils.wantInt(obj.get("online"));
                        IMGroupMemberInfo tt = new IMGroupMemberInfo(uid, roletype, online);
                        groupMemberInfos.add(tt);
                    }
                    callback.onSuccess(groupMemberInfos);
                } else {
                    callback.onError(engineClient.genLDAnswer(answer, errorCode));
                }
            }
        });
    }

    /**
     * 获取群组成员数量 该方法不需要是群成员就可以获取的到
     * @param groupId 群组id
     */
    public void getGroupMembersCount(long groupId, ICallback<Integer> callback) {
        Quest quest = new Quest("imclient_getgroupmembercount");
        quest.param("gid", groupId);
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


//    /**
//     * 群内成员禁言，只有群主或管理员有权禁言成员，管理员不能禁言禁言群主和其他管理员，群主可禁言管理员
//     * @param groupId  群组id
//     * @param userId   用户id
//     * @param banTime  禁言时长（单位：秒）
//     * @param callback 回调
//     */
//    public void addGroupMemberBan(final long groupId, final long userId, long banTime, final IEmptyCallback callback) {
//        Quest quest = new Quest("imclient_addgroupmemberban");
//        quest.param("gid", groupId);
//        quest.param("to_uid", userId);
//        quest.param("ban_time", banTime);
//        engineClient.sendQuestEmptyCallback(callback, quest);
//    }
//
//    /**
//     * 解除群内成员禁言
//     * @param groupId  群组id
//     * @param userId   用户id
//     * @param callback 回调
//     */
//    public void removeGroupMemberBan(long groupId, long userId, final IEmptyCallback callback) {
//        Quest quest = new Quest("imclient_removegroupmemberban");
//        quest.param("gid", groupId);
//        quest.param("to_uid", userId);
//        engineClient.sendQuestEmptyCallback(callback, quest);
//    }


    /**
     * 获取用户所在的群组
     * @param callback 回调
     */
    public void getUserGroups(ICallback<List<Long>> callback) {
        Quest quest = new Quest("getusergroups");
        engineClient.sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    List<Long> gids = LDUtils.wantLongList(answer, "gids");
                    callback.onSuccess(gids);
                } else {
                    callback.onError(engineClient.genLDAnswer(answer, errorCode));
                }
            }
        });
    }


    /**
     * 获取入群邀请列表
     * @param callback
     */
    public void getInviteGroupList(ICallback<List<IMInviteInfo>> callback) {
        Quest quest = new Quest("imclient_getgroupinvitelist");
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

    /**
     * 获取群组申请列表(管理员和群主调用)
     */
    public void getGroupdApplyList(long groupId, ICallback<List<IMApplyInfo>> callback) {
        Quest quest = new Quest("imclient_getgroupapplylist");
        quest.param("gid", groupId);
        engineClient.sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    ArrayList<IMApplyInfo> lists = new ArrayList<>();
                    List<Long> fromUids = LDUtils.wantLongList(answer, "fromUid");
                    List<String> grantExtras = (List<String>) (answer.want("grantExtra"));
                    List<String> attrs = (List<String>) (answer.want("attrs"));
                    List<Long> createTimes = LDUtils.wantLongList(answer, "createTime");

                    for (int i = 0; i < fromUids.size(); i++) {
                        lists.add(new IMApplyInfo(fromUids.get(i), createTimes.get(i), grantExtras.get(i), attrs.get(i)));
                    }
                    callback.onSuccess(lists);
                } else {
                    callback.onError(engineClient.genLDAnswer(answer, errorCode));
                }
            }
        });
    }

    /**
     * 获取自己发出的入群申请列表
     */
    public void getGroupRequestList(ICallback<List<IMRquestInfo>> callback) {
        Quest quest = new Quest("imclient_getgrouprequestlist");
        engineClient.sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    ArrayList<IMRquestInfo> lists = new ArrayList<>();
                    List<Long> fromUids = LDUtils.wantLongList(answer, "targetXid");
                    List<String> attrs = (List<String>) (answer.want("attrs"));
                    List<Long> createTimes = LDUtils.wantLongList(answer, "createTime");

                    for (int i = 0; i < fromUids.size(); i++) {
                        lists.add(new IMRquestInfo(fromUids.get(i), createTimes.get(i), attrs.get(i)));
                    }
                    callback.onSuccess(lists);
                } else {
                    callback.onError(engineClient.genLDAnswer(answer, errorCode));
                }
            }
        });
    }
}

