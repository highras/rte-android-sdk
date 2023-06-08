package com.LiveDataRTE.RTCLib;

import android.view.SurfaceView;

import com.LiveDataRTE.InternalEngine.EngineClient;
import com.fpnn.sdk.proto.Quest;
import com.livedata.rtc.RTCEngine;
import com.LiveDataRTE.LDInterface.*;
import com.LiveDataRTE.LiveDataStruct.*;
import java.util.HashMap;
import java.util.HashSet;


public class RTCClient {
    EngineClient engineClient;

    public void setEngineClient(EngineClient _engineClient){
        engineClient = _engineClient;
    }

    public EngineClient getEngineClient(){
        return engineClient;
    }
    
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
        engineClient.adminCommand(roomId, uids, command, callback);
    }

    /**
     * 开启摄像头
     */
    public LDAnswer openCamera(){
        return engineClient.openCamera();
    }

    /**
     * 关闭摄像头
     */
    public void closeCamera(){
        RTCEngine.setCameraFlag(false);
    }

    /**
     * 摄像头切换
     * @param front true-使用前置  false-使用后置
     */
    public void switchCamera(boolean front){
        RTCEngine.switchCamera(front);
    }

    /**
     * 打开麦克风(音频模式进入房间初始默认关闭  视频模式进入房间默认开启)
     */
    public void openMic(){
        RTCEngine.canSpeak(true);
    }

    /**
     * 关闭麦克风
     */
    public void closeMic(){
        RTCEngine.canSpeak(false);
    }


    /**
     * 打开音频输出
     */
    public void openAudioOutput(){
        RTCEngine.audioOutputFlag(true);
    }

    /**
     * 关闭音频输出(静音)
     */
    public void closeAudioOutput(){
        RTCEngine.audioOutputFlag(false);
    }


    /**
     * 设置麦克风增益等级(声音自动增益 取值 范围0-10)
     */
    public void setMicphoneLevel(int level){
        if (level <= 0 || level >= 10)
            return;
        RTCEngine.setMicphoneGain(level);
    }

    /**
     * 取消订阅视频流
     * @param roomId 房间id
     * @param uids 取消订阅的成员列表
     * @return
     */
    public void unsubscribeVideo(long roomId, HashSet<Long> uids, IEmptyCallback callback){
        engineClient.unsubscribeVideo(roomId, uids, callback);
    }

    /**
     * 创建RTC房间
     * @roomId 房间id
     * @roomType 1-voice 2-video 3-实时翻译语音房间(视频房间摄像头默认关闭 麦克风默认开启)
     * @lang 语言(当创建实时翻译房间 必传)
     * @param callback
     */
    public void createRTCRoom(long roomId, RTCStruct.RTCRoomType roomType, String lang, IEmptyCallback callback) {
        engineClient.createRTCRoom(roomId, roomType, 0,lang,callback);
    }


    /**
     * 进入RTC房间
     * @param callback 回调
     * @param roomId   房间id
     * @param lang 自己的语言(当为实时语音翻译房间必传)
     */
    public void enterRTCRoom(long roomId, String lang,  ICallback<RTCStruct.RTCRoomInfo> callback) {
        engineClient.enterRTCRoom(callback,roomId, lang);
    }


    /**
     * 订阅视频流
     * @param roomId 房间id
     * @param userViews  key-订阅的用户id value-显示用户的surfaceview(需要view创建完成可用)
     */
    public void subscribeVideos(long roomId, HashMap<Long, SurfaceView> userViews, IEmptyCallback callback){
       engineClient.subscribeVideos(roomId, userViews, callback);
    }

    /**
     * 邀请用户加入RTC房间(需要对端确认)
     * @param callback 回调
     * @param roomId   房间id
     * @param uids     需要邀请的用户列表
     */
    public void inviteUserIntoRTCRoom(long roomId, HashSet<Long> uids, IEmptyCallback callback){
        Quest quest = new Quest("inviteUserIntoRTCRoom");
        quest.param("rid",roomId);
        quest.param("uids",uids);
        engineClient.sendQuestEmptyCallback(callback, quest);
    }

    /**
     * 设置目前活跃的房间(仅对语音房间有效)
     * @param roomId
     */
    public LDAnswer setActivityRoom(long roomId){
        return engineClient.setActivityRoom(roomId);
    }

    /**
     * 切换扬声器听筒(耳机状态下不操作)(默认扬声器)
     * @param usespeaker true-使用扬声器 false-使用听筒
     */
    public void switchAudioOutput(boolean usespeaker){
        new Thread(new Runnable() {
            @Override
            public void run() {
                RTCEngine.switchVoiceOutput(usespeaker);
            }
        }).start();
    }

    /**离开RTC房间
     * @param roomId   房间id
     */
    public void leaveRTCRoom(long roomId, IEmptyCallback callback){
        engineClient.leaveRTCRoom(roomId, callback);
    }

    /**
     * 屏蔽房间某些人的语音
     * @param callback 回调
     * @param roomId   房间id
     * @param uids     屏蔽语音的用户列表
     */
    public void blockUserInVoiceRoom(long roomId, HashSet<Long> uids,IEmptyCallback callback){
        engineClient.blockUserInVoiceRoom(roomId, uids, callback);
    }

    /**
     * 解除屏蔽房间某些人的语音
     * @param callback 回调
     * @param roomId   房间id
     * @param uids     解除屏蔽语音的用户列表
     */
    public void unblockUserInVoiceRoom(long roomId, HashSet<Long> uids, IEmptyCallback callback){
        engineClient.unblockUserInVoiceRoom(roomId, uids, callback);
    }


    /**
     * 获取语RTC房间成员列表
     * @param callback 回调<RoomInfo>
     */
    public void getRTCRoomMembers(long roomId,  ICallback<RTCStruct.RTCRoomInfo> callback) {
        engineClient.getRTCRoomMembers(roomId, callback);
    }

    /**
     * 获取RTC房间成员个数
     * @param callback 回调
     */
    public void getRTCRoomMemberCount(long roomId,  ICallback<Integer> callback) {
        engineClient.getRTCRoomMemberCount(roomId, callback);
    }

    /**
     * 切换视频质量
     * @level 视频质量详见RTMStruct.CaptureLevle
     * @return
     */
    public LDAnswer switchVideoQuality(int level){
        return engineClient.switchVideoQuality(level);
    }


    /**
     * 设置预览view(需要传入的view 真正建立完成)
     * @return
     */
    public void setPreview(SurfaceView view){
        engineClient.setPreview(view);
    }


    /****************P2P*****************/
    /**
     *发起p2p音视频请求(对方是否回应通过 pushP2PRTCEvent接口回调)
     * @param type 通话类型
     * @SurfaceView view(如果为视频 自己预览的view 需要view创建完成并可用)
     * @param toUid 对方id
     */
    public void requestP2PRTC(RTCStruct.P2PRTCType type , long toUid, SurfaceView view, IEmptyCallback callback){
        engineClient.requestP2PRTC(type, toUid, view, callback);
    }

    /**
     * 取消p2p RTC请求
     * @param callback
     */
    public void cancelP2PRTC(IEmptyCallback callback){
        engineClient.cancelP2PRTC(callback);
    }

    /**
     * 关闭p2p 会话
     * @param callback
     */
    public void closeP2PRTC(IEmptyCallback callback){
        engineClient.closeP2PRTC(callback);
    }

    /**
     * 接受p2p 会话
     * @param callback
     * @param preview 自己预览的view(仅视频)
     * @param bindview 对方的view(仅视频)
     */
    public void acceptP2PRTC(SurfaceView preview, SurfaceView bindview, IEmptyCallback callback){
        engineClient.acceptP2PRTC(preview, bindview, callback);
    }

    /**
     * 拒绝p2p请求
     * @param callback
     */
    public void refuseP2PRTC(IEmptyCallback callback) {
        engineClient.refuseP2PRTC(callback);
    }
}
