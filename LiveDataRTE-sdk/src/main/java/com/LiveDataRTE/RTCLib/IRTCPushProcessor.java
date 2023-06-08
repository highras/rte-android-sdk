package com.LiveDataRTE.RTCLib;

import android.view.SurfaceView;

import com.LiveDataRTE.LiveDataStruct;
import com.LiveDataRTE.RTCLib.RTCStruct.*;

import java.util.HashSet;
import java.util.List;

//音视频功能推送类
public interface IRTCPushProcessor {
    default void pushEnterRTCRoom(long roomId, long userId, long time){} //某人进入语音房间
    default void pushExitRTCRoom(long roomId, long userId, long time){} //某人离开语音房间(如果有订阅关系会自动解除)
    default void pushRTCRoomClosed(long roomId){}//语音房间被关闭
    default void pushInviteIntoRTCRoom(long roomId, long userId){} //被邀请加入房间(需要再次调用进入房间接口真正进入语音房间)
    default void pushAdminCommand(int command, List<Long> uids){} //接收服务端推送的控制指令(自己发起 自己也可以收到)
    //     * 0 赋予管理员权
    //     * 1 剥夺管理员权限
    //     * 2 禁止发送音频数据
    //     * 3 允许发送音频数据
    //     * 4 禁止发送视频数据
    //     * 5 允许发送视频数据
    //     * 6 关闭他人麦克风
    //     * 7 关闭他人摄像头
    //     */
    default void pushKickoutRTCRoom(long roomId){} //某人被踢出RTC房间
    default void pushPullRoom(long roomId, RTCRoomInfo info){} //被服务器拉入房间
    default void voiceSpeak(long uid){} //谁正在说话

    //推送p2p rtc请求
    default void pushRequestP2PRTC(long uid, P2PRTCType type){}

    //推送p2p rtc event 如果是接受对方视频请求 需要返回显示对方的surfaceview(如果事件为P2PRTCEvent.Accept 自动切换为主线程)
    default SurfaceView pushP2PRTCEvent(long uid, P2PRTCType type, P2PRTCEvent event){return null;}

    default void pushVoiceTranslate(String text, String slang, long uid){};
}
