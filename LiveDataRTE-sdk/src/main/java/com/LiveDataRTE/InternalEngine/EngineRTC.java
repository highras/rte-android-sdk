package com.LiveDataRTE.InternalEngine;

import android.view.SurfaceView;

import com.LiveDataRTE.LDInterface.*;
import com.LiveDataRTE.LDUtils;
import com.LiveDataRTE.LiveDataStruct.*;
import com.LiveDataRTE.RTCLib.RTCStruct.*;
import com.fpnn.sdk.ErrorCode;
import com.fpnn.sdk.FunctionalAnswerCallback;
import com.fpnn.sdk.proto.Answer;
import com.fpnn.sdk.proto.Quest;
import com.livedata.rtc.RTCEngine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

class EngineRTC extends EngineCore{

    /**
     *管理员权限操作
     * @param roomId 房间id
     * @param uids
     * @param command
     * 0 赋予管理员权
     * 1 剥夺管理员权限
     * 2 禁止发送音频数据
     * 3 允许发送音频数据
     * 4 禁止发送视频数据
     * 5 允许发送视频数据
     * 6 关闭他人麦克风
     * 7 关闭他人摄像头
     * @return
     */
    public void adminCommand (long roomId, HashSet<Long> uids, int command, IEmptyCallback callback){
        Quest quest = new Quest("adminCommand");
        quest.param("rid", roomId);
        quest.param("uids", uids);
        quest.param("command", command);
        sendQuestEmptyCallback(callback, quest);
    }

    /**
     * 开启摄像头
     */
    public LDAnswer openCamera(){
        cameraStatus =  true;
        String ret = RTCEngine.setCameraFlag(true);
        if (!ret.isEmpty())
            return genLDAnswer(videoError,"ret");
        return genLDAnswer(okRet);
    }


    /**
     * 取消订阅视频流
     * @param roomId 房间id
     * @param uids 取消订阅的成员列表
     * @return
     */
    public void unsubscribeVideo(long roomId, HashSet<Long> uids, IEmptyCallback callback){
        Quest quest = new Quest("unsubscribeVideo");
        quest.param("rid", roomId);
        quest.param("uids", uids);
        sendQuestEmptyCallback(new IEmptyCallback() {
            @Override
            public void onSuccess() {
                for (Long id : uids)
                    RTCEngine.unsubscribeUser(id);
                callback.onSuccess();
            }

            @Override
            public void onError(LDAnswer answer) {
                callback.onError(answer);
            }
        },quest);
    }

    /**
     * 创建RTC房间
     * @roomId 房间id
     * @roomType 1-voice 2-video 3-实时翻译语音房间(视频房间摄像头默认关闭 麦克风默认开启)
     * @lang 语言(如果为实时翻译房间必传)
     * @param callback 回调
     */
    public void createRTCRoom(long roomId, RTCRoomType roomType, String lang, IEmptyCallback callback) {
        createRTCRoom(roomId, roomType, 0,lang,callback);
    }


    /**
     * 进入RTC房间
     * @param callback 回调
     * @param roomId   房间id
     * @param lang 自己的语言(当为实时语音翻译房间必传)
     */
    public void enterRTCRoom(long roomId, String lang,  ICallback<RTCRoomInfo> callback) {
        super.enterRTCRoom(callback,roomId, lang);
    }


    /**
     * 订阅视频流
     * @param roomId 房间id
     * @param userViews  key-订阅的用户id value-显示用户的surfaceview(需要view创建完成可用)
     */
    public void subscribeVideos(long roomId, HashMap<Long, SurfaceView> userViews, IEmptyCallback callback){
        LDAnswer initanswer = initRTC();
        if (initanswer.errorCode != okRet) {
            callback.onError(initanswer);
            return;
        }
/*
        for (Map.Entry<Long, SurfaceView> it:userViews.entrySet()) {
            SurfaceView tmp = it.getValue();
            float ratio =  (float) tmp.getWidth() / tmp.getHeight();
            if (ratio < 0.749 ||  ratio> 0.759){
                return genLDAnswer(videoError,"user " + it.getKey() + " unfitable aspect ratio");
            }
            long uid = it.getKey();
        }
*/

        Quest quest = new Quest("subscribeVideo");
        quest.param("rid", roomId);
        quest.param("uids", userViews.keySet());
        Answer ret = sendQuest(quest);
        LDAnswer answer = genLDAnswer(ret);
        if (answer.errorCode == okRet){
            for (Map.Entry<Long, SurfaceView> it:userViews.entrySet()) {
                RTCEngine.bindDecodeSurface(it.getKey(), it.getValue().getHolder().getSurface());
            }
            callback.onSuccess();
        }else {
            callback.onError(answer);
        }
    }

    /**
     * 邀请用户加入RTC房间(非强制，需要对端确认)(发送成功仅代表收到该请求，至于用户最终是否进入房间结果未知)
     * @param callback 回调
     * @param roomId   房间id
     * @param uids     需要邀请的用户列表
     */
    public void inviteUserIntoRTCRoom(long roomId, HashSet<Long> uids, IEmptyCallback callback){
        Quest quest = new Quest("inviteUserIntoRTCRoom");
        quest.param("rid",roomId);
        quest.param("uids",uids);

        sendQuestEmptyCallback(callback, quest);
    }

    /**
     * 设置目前活跃的房间(仅对语音房间有效)
     * @param roomId
     */
    public LDAnswer setActivityRoom(long roomId){
        String msg = RTCEngine.setActivityRoom(roomId);
        if (msg.isEmpty()){
            return genLDAnswer(okRet);
        }
        else
            return genLDAnswer(voiceError,msg);
    }


    /**离开RTC房间
     * @param roomId   房间id
     */
    public void leaveRTCRoom(long roomId, IEmptyCallback callback){
        Quest quest = new Quest("exitRTCRoom");
        quest.param("rid",roomId);
        sendQuestEmptyCallback(new IEmptyCallback() {
            @Override
            public void onSuccess() {
                callback.onSuccess();
            }

            @Override
            public void onError(LDAnswer answer) {
                callback.onError(answer);

            }
        }, quest);
        if (mOrEventListener!=null){
            mOrEventListener.disable();
        }
        RTCEngine.leaveRTCRoom(roomId);
    }

    /**
     * 屏蔽房间某些人的语音
     * @param callback 回调
     * @param roomId   房间id
     * @param uids     屏蔽语音的用户列表
     */
    public void blockUserInVoiceRoom(long roomId, HashSet<Long> uids,IEmptyCallback callback){
        Quest quest = new Quest("blockUserVoiceInRTCRoom");
        quest.param("rid",roomId);
        quest.param("uids",uids);
        sendQuestEmptyCallback(callback, quest);
    }

    /**
     * 解除屏蔽房间某些人的语音
     * @param callback 回调
     * @param roomId   房间id
     * @param uids     解除屏蔽语音的用户列表
     */
    public void unblockUserInVoiceRoom(long roomId, HashSet<Long> uids, IEmptyCallback callback){
        Quest quest = new Quest("unblockUserVoiceInRTCRoom");
        quest.param("rid",roomId);
        quest.param("uids",uids);
        sendQuestEmptyCallback(callback, quest);
    }


    /**
     * 获取语RTC房间成员列表
     * @param callback 回调<RoomInfo>
     */
    public void getRTCRoomMembers(long roomId,  ICallback<RTCRoomInfo> callback) {
        Quest quest = new Quest("getRTCRoomMembers");
        quest.param("rid", roomId);

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    RTCRoomInfo tt = new RTCRoomInfo();
                    tt.uids = LDUtils.wantLongList(answer, "uids");
                    tt.managers = LDUtils.wantLongList(answer, "administrators");
                    tt.owner = LDUtils.wantInt(answer,"owner");
                    callback.onSuccess(tt);
                }else {
                    callback.onError(genLDAnswer(answer, errorCode));
                }
            }
        });
    }

    /**
     * 获取RTC房间成员个数
     * @param callback 回调
     */
    public void getRTCRoomMemberCount(long roomId,  ICallback<Integer> callback) {
        Quest quest = new Quest("getRTCRoomMemberCount");
        quest.param("rid", roomId);

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    int count = LDUtils.wantInt(answer, "count");
                    callback.onSuccess(count);
                }else {
                    callback.onError(genLDAnswer(answer, errorCode));
                }
            }
        });
    }


    /**
     * 切换视频质量
     * @level 视频质量详见RTMStruct.CaptureLevle
     * @return
     */
    public LDAnswer switchVideoQuality(int level){
        if (currVideoLevel == level)
            return genLDAnswer(okRet);
        currVideoLevel = level;
        String msg = RTCEngine.switchVideoCapture(level);
        if (msg.isEmpty())
            return genLDAnswer(okRet);
        return genLDAnswer(videoError,msg);
    }


    /**
     * 设置预览view(需要传入的view 真正建立完成)
     * @return
     */
    public void setPreview(SurfaceView view){
        initRTC();
        RTCEngine.setpreview(view.getHolder().getSurface());
    }


    /****************P2P*****************/
    /**
     *发起p2p音视频请求(对方是否回应通过 pushP2PRTCEvent接口回调)
     * @param type 通话类型
     * @SurfaceView view(如果为视频 自己预览的view 需要view创建完成并可用)
     * @param toUid 对方id
     */
    public void requestP2PRTC(P2PRTCType type , long toUid, SurfaceView view, IEmptyCallback callback){
        LDAnswer tanswer = initRTC();
        if (tanswer.errorCode != okRet){
            tanswer.errorMsg += " requestP2PRTC";
            callback.onError(tanswer);
            return;
        }

        if (RTCEngine.isInRTCRoom() > 0){
            callback.onError(genLDAnswer(voiceError, "requestP2PRTC error you are in rtcroom-" + RTCEngine.isInRTCRoom()));
            return;
        }

        if (lastCallId > 0){
            callback.onError(genLDAnswer(voiceError, "already in p2p type"));
            return;
        }

        Quest quest = new Quest("requestP2PRTC");
        quest.param("type", type.value());
        quest.param("peerUid", toUid);
        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    lastCallId = LDUtils.wantLong(answer, "callId");
                    peerUid = toUid;
                    lastP2Ptype = type.value();
                    if (type == P2PRTCType.VIDEO) {
                        String msg = RTCEngine.requestP2PVideo(view.getHolder().getSurface());
                        if (!msg.isEmpty()){
                            callback.onError(genLDAnswer(videoError, msg));
                            return;
                        }
//                        RTCEngine.setpreview(view.getHolder().getSurface());
//                        RTCEngine.setCameraFlag(true);
//                        RTCEngine.setVoiceStat(true);
//                        RTCEngine.canSpeak(true);
                    }
                    callback.onSuccess();
                }else {
                    callback.onError(genLDAnswer(answer, errorCode));
                }
            }
        });
    }

    /**
     * 取消p2p RTC请求
     * @param callback
     */
    public void cancelP2PRTC(IEmptyCallback callback){
        if (lastCallId <0) {
            callback.onError(genLDAnswer(0));
            return;
        }
        Quest quest = new Quest("cancelP2PRTC");
        quest.param("callId", lastCallId);
        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override

            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet){
                    callback.onSuccess();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            closeP2P();
                        }
                    }).start();

                }else {
                    callback.onError(genLDAnswer(answer, errorCode));
                }
            }
        });
    }

    void closeP2P(){
        RTCEngine.closeP2P();
        lastCallId = 0;
        peerUid = 0;
        lastP2Ptype = 0;
    }


    /**
     * 关闭p2p 会话
     * @param callback
     */
    public void closeP2PRTC(IEmptyCallback callback){
        if (lastCallId <0) {
            callback.onError(genLDAnswer(0));
            return;
        }
        Quest quest = new Quest("closeP2PRTC");
        quest.param("callId", lastCallId);
        sendQuestEmptyCallback(new IEmptyCallback() {
            @Override
            public void onSuccess() {
                callback.onSuccess();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        closeP2P();
                    }
                }).start();
            }

            @Override
            public void onError(LDAnswer answer) {
                callback.onError(answer);
            }
        },quest);
    }

    /**
     * 接受p2p 会话
     * @param callback
     * @param preview 自己预览的view(仅视频)
     * @param bindview 对方的view(仅视频)
     */
    public void acceptP2PRTC(SurfaceView preview, SurfaceView bindview, IEmptyCallback callback){
        if (lastCallId <= 0) {
            callback.onError(genLDAnswer(0));
            return;
        }
        LDAnswer tanswer = initRTC();
        if (tanswer.errorCode != okRet){
            tanswer.errorMsg += " acceptP2PRTC";
            callback.onError(tanswer);
            return;
        }

        Quest quest = new Quest("acceptP2PRTC");
        quest.param("callId", lastCallId);
        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == 0){
                    String ret = RTCEngine.startP2P(lastP2Ptype, peerUid, lastCallId);
                    if (!ret.isEmpty()){
                        callback.onError(genLDAnswer(ErrorCode.FPNN_EC_CORE_UNKNOWN_ERROR.value(),"acceptP2PRTC but startP2P error"));
                        return;
                    }

                    if (lastP2Ptype == 2 && preview != null && bindview != null) {
                        setPreview(preview);
                        openCamera();
                        RTCEngine.bindDecodeSurface(peerUid, bindview.getHolder().getSurface());
                    }
                    callback.onSuccess();
                }else {
                    callback.onError(genLDAnswer(answer, errorCode));
                }
            }
        });
    }

    /**
     * 拒绝p2p 会话
     * @param callback
     */
    public void refuseP2PRTC(IEmptyCallback callback){
        if (lastCallId <0) {
            callback.onError(genLDAnswer(0));
            return;
        }
        Quest quest = new Quest("refuseP2PRTC");
        quest.param("callId", lastCallId);
        sendQuestEmptyCallback(callback, quest);
    }
}


