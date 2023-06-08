package com.LiveDataRTE.RTMLib;

import com.LiveDataRTE.LiveDataStruct.*;
import com.LiveDataRTE.LDInterface.*;
import com.LiveDataRTE.RTMLib.RTMStruct.*;

import java.util.List;

public class RTMBaseMessage extends RTMUser {
    /**
     *发送指定类型消息
     * @param targetId       目标用户id
     * @param messageType     消息类型(51-127)
     * @param message   消息内容
     * @param attrs     客户端自定义信息(可空)
     * @param conversationType   会话类型
     * @param callback
     */
    public void sendBasicMessage(long targetId, ConversationType conversationType, int messageType, String message, String attrs, ISendMsgCallback callback) {
        engineClient.sendMsgAsync(callback, targetId, messageType, message, attrs, conversationType, sendType);
    }


    /**
     *发送指定类型消息(二进制数据)
     * @param targetId       目标用户id
     * @param messageType   消息类型
     * @param message   消息内容
     * @param attrs     客户端自定义信息(可空)
     * @param conversationType   会话类型
     * @param callback
     */
    public void sendBinaryMessage(long targetId, ConversationType conversationType, int messageType, byte[] message, String attrs,  ISendMsgCallback callback) {
        engineClient.sendMsgAsync(callback, targetId, messageType, message, attrs, conversationType, sendType);
    }


    /**
     *分页获得指定类型历史消息
     * @param targetId   目标id(对于Broadcast类消息 可传任意值)
     * @param desc      是否按时间倒叙排列
     * @param count     获取条数(后台配置 最多建议20条)
     * @param beginMsec 开始时间戳(毫秒)(可选默认0,第二次查询传入上次结果的HistoryMessageResult.beginMsec)
     * @param endMsec   结束时间戳(毫秒)(可选默认0,第二次查询传入上次结果的HistoryMessageResult.endMsec)
     * @param lastCursorId    索引id(可选默认0，第一次获取传入0 第二次查询传入上次结果HistoryMessageResult的lastId)
     * @param conversationType   消息类别
     * @param callback  HistoryMessageResult类型回调
     * 注意: 建议时间和id不要同时传入 通过时间查询是左右闭区间(beginMsec<=x<=endMsec)
     *       通过id查询是开区间lastId<x
     */
    public void getHistoryBasicMessage(long targetId, ConversationType conversationType, boolean desc, int count, long beginMsec, long endMsec, long lastCursorId, List<Integer> mtypes,  ICallback<RTMHistoryMessageResult> callback) {
        engineClient.getHistoryMessages(callback, targetId, desc, count, beginMsec, endMsec, lastCursorId, mtypes, conversationType, sendType);
    }

}
