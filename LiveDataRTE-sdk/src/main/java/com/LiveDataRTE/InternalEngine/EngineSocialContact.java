package com.LiveDataRTE.InternalEngine;

import com.LiveDataRTE.RTMLib.RTMErrorCode;
import com.LiveDataRTE.LDInterface.*;
import com.LiveDataRTE.RTMLib.RTMStruct.*;
import com.fpnn.sdk.FunctionalAnswerCallback;
import com.fpnn.sdk.proto.Answer;
import com.fpnn.sdk.proto.Quest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class EngineSocialContact extends EngineFile{

    public void getGroupMemberCount(long groupId, boolean isOnline, final IDoubleCallBack<Integer, Integer> callback) {
        Quest quest = new Quest("getgroupcount");
        quest.param("gid", groupId);
        quest.param("online", isOnline);

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    callback.onSuccess(LDUtils.wantInt(answer, "cn"), LDUtils.wantInt(answer, "online"));
                }else
                    callback.onError(genLDAnswer(answer,errorCode));
            }
        });
    }


    public void getGroupMembers(long groupId, boolean isOnline, final ICallback<RTMGroupMembers> callback) {
        Quest quest = new Quest("getgroupmembers");
        quest.param("gid", groupId);
        quest.param("online", isOnline);

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == RTMErrorCode.RTM_EC_OK.value()) {
                    RTMGroupMembers rtmGroupMembers = new RTMGroupMembers();
                    rtmGroupMembers.onlineUserids = LDUtils.wantLongList(answer, "onlines");
                    rtmGroupMembers.userids = LDUtils.wantLongList(answer, "uids");
                    callback.onSuccess(rtmGroupMembers);
                }else
                    callback.onError(genLDAnswer(answer,errorCode));
            }
        });
    }


    public void addGroupMembers(long groupId, List<Long> uids, final IEmptyCallback callback) {
        Quest quest = new Quest("addgroupmembers");
        quest.param("gid", groupId);
        quest.param("uids", uids);

        sendQuestEmptyCallback(callback, quest);
    }



    public void deleteGroupMembers(long groupId,List<Long> uids,  final IEmptyCallback callback) {
        Quest quest = new Quest("delgroupmembers");
        quest.param("gid", groupId);
        quest.param("uids", uids);
        sendQuestEmptyCallback(callback, quest);
    }



    public void getUserGroups( final ICallback<List<Long>> callback) {
        Quest quest = new Quest("getusergroups");

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    callback.onSuccess(LDUtils.wantLongList(answer, "gids"));
                }else
                    callback.onError(genLDAnswer(answer,errorCode));
            }
        });
    }



    public void getGroupInfo(final long groupId,final IGetinfoCallback callback) {
        Quest quest = new Quest("getgroupinfo");
        quest.param("gid", groupId);

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    callback.onSuccess(LDUtils.wantString(answer,"oinfo"), LDUtils.wantString(answer,"pinfo"));
                }else
                    callback.onError(genLDAnswer(answer,errorCode));
            }
        });
    }


    public void getGroupsPublicInfo(List<Long> gids,  final ICallback<Map<Long, String>> callback) {
        Quest quest = new Quest("getgroupsopeninfo");
        quest.param("gids",gids);

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    Map<String, String> attributes = LDUtils.wantStringMap(answer, "info");
                    Map<Long, String> ret = new HashMap<>();
                    for(String id:attributes.keySet()){
                        ret.put(LDUtils.wantLong(id),attributes.get(id));
                    }
                    callback.onSuccess(ret);
                }else
                    callback.onError(genLDAnswer(answer,errorCode));
            }
        });
    }



    public void setGroupInfo(long groupId, String publicInfo, String privateInfo, IEmptyCallback callback) {
        Quest quest = new Quest("setgroupinfo");
        quest.param("gid", groupId);
        if (publicInfo != null)
            quest.param("oinfo", publicInfo);
        if (privateInfo != null)
            quest.param("pinfo", privateInfo);

        sendQuestEmptyCallback(callback, quest);
    }



    public void enterRoom(long roomId,  IEmptyCallback callback) {
        Quest quest = new Quest("enterroom");
        quest.param("rid", roomId);

        sendQuestEmptyCallback(callback, quest);
    }



    public void leaveRoom(long roomId,  IEmptyCallback callback) {
        Quest quest = new Quest("leaveroom");
        quest.param("rid", roomId);
        sendQuestEmptyCallback(callback, quest);
    }



    public void getRoomMembers(long roomId,  final ICallback<List<Long>> callback) {
        Quest quest = new Quest("getroommembers");
        quest.param("rid",roomId);

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    List<Long> uids = LDUtils.wantLongList(answer, "uids");
                    callback.onSuccess(uids);
                }else
                    callback.onError(genLDAnswer(answer,errorCode));
            }
        });
    }


    public void getRoomMemberCount(List<Long> rids, final ICallback<Map<Long,Integer>> callback) {
        Quest quest = new Quest("getroomcount");
        quest.param("rids",rids);

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    Map<Long,Integer> members = new HashMap<>();
                    Map oo = (Map) answer.want("cn");
                    for (Object kk: oo.keySet())
                        members.put(LDUtils.wantLong(kk),LDUtils.wantInt(oo.get(kk)));
                    callback.onSuccess(members);
                }else
                    callback.onError(genLDAnswer(answer,errorCode));
            }
        });
    }


    public void getUserRooms( final ICallback<List<Long>> callback) {
        Quest quest = new Quest("getuserrooms");

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    callback.onSuccess(LDUtils.wantLongList(answer, "rooms"));
                }else
                    callback.onError(genLDAnswer(answer,errorCode));
            }
        });
    }


    public void setRoomInfo(long roomId, String publicInfo, String privateInfo,  IEmptyCallback callback) {
        Quest quest = new Quest("setroominfo");
        quest.param("rid", roomId);
        if (publicInfo != null)
            quest.param("oinfo", publicInfo);
        if (privateInfo != null)
            quest.param("pinfo", privateInfo);

        sendQuestEmptyCallback(callback, quest);
    }



    public void getRoomInfo(final long roomId,  final IGetinfoCallback callback) {
        Quest quest = new Quest("getroominfo");
        quest.param("rid", roomId);

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    callback.onSuccess(LDUtils.wantString(answer,"oinfo"), LDUtils.wantString(answer,"pinfo"));
                }else
                    callback.onError(genLDAnswer(answer,errorCode));
            }
        });
    }


    public void getRoomsPublicInfo(List<Long> rids,  final ICallback<Map<Long, String>> callback) {
        Quest quest = new Quest("getroomsopeninfo");
        quest.param("rids",rids);

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    Map<String, String> attributes = LDUtils.wantStringMap(answer, "info");
                    Map<Long, String> ret = new HashMap<>();
                    for(String id:attributes.keySet()){
                        ret.put(LDUtils.wantLong(id),attributes.get(id));
                    }
                    callback.onSuccess(ret);
                }else
                    callback.onError(genLDAnswer(answer,errorCode));
            }
        });
    }


    public void addFriends(List<Long> uids,  IEmptyCallback callback) {
        Quest quest = new Quest("addfriends");
        quest.param("friends", uids);

        sendQuestEmptyCallback(callback, quest);
    }


    public void getFriends( final ICallback<List<Long>> callback) {
        Quest quest = new Quest("getfriends");

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    callback.onSuccess(LDUtils.wantLongList(answer, "uids"));
                }else
                    callback.onError(genLDAnswer(answer,errorCode));
            }
        });
    }


    public void deleteFriends( List<Long> uids,  IEmptyCallback callback) {
        Quest quest = new Quest("delfriends");
        quest.param("friends", uids);

        sendQuestEmptyCallback(callback, quest);
    }



    public void addBlacklist( List<Long> uids,  IEmptyCallback callback) {
        Quest quest = new Quest("addblacks");
        quest.param("blacks", uids);

        sendQuestEmptyCallback(callback, quest);
    }



    public void deleteBlacklist( List<Long> uids,  IEmptyCallback callback) {
        Quest quest = new Quest("delblacks");
        quest.param("blacks", uids);

        sendQuestEmptyCallback(callback, quest);
    }


    public void getBlacklist( final ICallback<List<Long>> callback) {
        Quest quest = new Quest("getblacks");

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    callback.onSuccess(LDUtils.wantLongList(answer, "uids"));
                }else
                    callback.onError(genLDAnswer(answer,errorCode));
            }
        });
    }


    public void setUserInfo(String publicInfo, String privateInfo,  IEmptyCallback callback) {
        Quest quest = new Quest("setuserinfo");
        if (publicInfo != null)
            quest.param("oinfo", publicInfo);
        if (privateInfo != null)
            quest.param("pinfo", privateInfo);

        sendQuestEmptyCallback(callback,quest);
    }


    public void getUserInfo( final IGetinfoCallback callback) {
        Quest quest = new Quest("getuserinfo");

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    callback.onSuccess(LDUtils.wantString(answer,"oinfo"), LDUtils.wantString(answer,"pinfo"));
                }else
                    callback.onError(genLDAnswer(answer,errorCode));
            }
        });
    }


    public void getUserPublicInfo(List<Long> uids, final ICallback<Map<Long, String>> callback) {
        Quest quest = new Quest("getuseropeninfo");
        quest.param("uids",uids);

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    Map<String, String> attributes = LDUtils.wantStringMap(answer, "info");
                    Map<Long, String> ret = new HashMap<>();
                    for(String id:attributes.keySet()){
                        ret.put(LDUtils.wantLong(id),attributes.get(id));
                    }
                    callback.onSuccess(ret);
                }else
                    callback.onError(genLDAnswer(answer,errorCode));
            }
        });
    }



    public void getOnlineUsers(List<Long> uids,  final ICallback<List<Long>> callback) {
        Quest quest = new Quest("getonlineusers");
        quest.param("uids", uids);

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    List<Long> onlineUids = LDUtils.wantLongList(answer, "uids");
                    callback.onSuccess(onlineUids);
                }else
                    callback.onError(genLDAnswer(answer,errorCode));
            }
        });
    }
}
