package com.LiveDataRTE.RTMLib;

import com.LiveDataRTE.LiveDataStruct.*;
import com.LiveDataRTE.RTMLib.RTMStruct.*;

public interface IRTMPushProcessor {

    //接收普通聊天消息
    default void pushChat(RTMMessage rtmMessage, ConversationType conversationType){};

    default void kickoutRoom(long roomId){};

    //接收命令消息
    default void pushCmd(RTMMessage rtmMessage,ConversationType conversationType){};

    //接收用户自定义类型消息(type为51-127)
    default void pushMessage(RTMMessage rtmMessage, ConversationType conversationType){};

    //接收文件
    default void pushFile(RTMMessage rtmMessage, ConversationType conversationType){};
}
