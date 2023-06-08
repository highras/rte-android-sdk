package com.LiveDataRTE.IMLib;

import com.LiveDataRTE.IMLib.IMStruct.*;
import com.LiveDataRTE.LiveDataStruct.*;
import com.LiveDataRTE.RTMLib.RTMStruct.*;

import java.util.ArrayList;

public class TransStruct {

    public static IMMessageType transRTMMessageType(int type){
        IMMessageType ret = IMMessageType.Text;
        if (type == MessageType.CHAT){
            ret = IMMessageType.Text;
        }else if (type == MessageType.AUDIOMESSAGE){
            ret = IMMessageType.AudioMessage;
        }else if (type>= MessageType.IMAGEFILE && type<=MessageType.NORMALFILE){
            ret = IMMessageType.File;
        }
        return ret;
    }

    public static IMMessage transIMMessage(RTMMessage rtmMessage){
        IMMessage imMessage = new IMMessage();
        imMessage.stringMessage = rtmMessage.stringMessage;
        imMessage.imMessageType = transRTMMessageType(rtmMessage.messageType);
        imMessage.messageId = rtmMessage.messageId;
        imMessage.attrs = rtmMessage.attrs;
        if (rtmMessage.fileInfo != null) {
            imMessage.fileInfo = new FileStruct();
            rtmMessage.fileInfo.copy(imMessage.fileInfo);
        }
        imMessage.fromUid = rtmMessage.fromUid;
        imMessage.modifiedTime = rtmMessage.modifiedTime;
        imMessage.targetId = rtmMessage.targetId;
        rtmMessage.translatedInfo.copy(imMessage.translatedInfo);

        return imMessage;
    }


    public static IMHistoryMessage transIMHistoryMessage(RTMHistoryMessage rtmHistoryMessage){
        IMHistoryMessage imHistoryMessage = new IMHistoryMessage();
        imHistoryMessage.stringMessage = rtmHistoryMessage.stringMessage;
        imHistoryMessage.imMessageType = transRTMMessageType(rtmHistoryMessage.messageType);
        imHistoryMessage.messageId = rtmHistoryMessage.messageId;
        imHistoryMessage.attrs = rtmHistoryMessage.attrs;
        if (rtmHistoryMessage.fileInfo != null) {
            imHistoryMessage.fileInfo = new FileStruct();
            rtmHistoryMessage.fileInfo.copy(imHistoryMessage.fileInfo);
        }
        imHistoryMessage.fromUid = rtmHistoryMessage.fromUid;
        imHistoryMessage.modifiedTime = rtmHistoryMessage.modifiedTime;
        imHistoryMessage.targetId = rtmHistoryMessage.targetId;
        rtmHistoryMessage.translatedInfo.copy(imHistoryMessage.translatedInfo);
        rtmHistoryMessage.cursorId = rtmHistoryMessage.cursorId;
        return imHistoryMessage;
    }

    public static IMSingleMessage transIMSingleMessage(RTMSingleMessage rtmSingleMessage){
        IMSingleMessage imSingleMessage = new IMSingleMessage();
        imSingleMessage.stringMessage = rtmSingleMessage.stringMessage;
        imSingleMessage.attrs = rtmSingleMessage.attrs;
        if (rtmSingleMessage.fileInfo != null) {
            imSingleMessage.fileInfo = new FileStruct();
            rtmSingleMessage.fileInfo.copy(imSingleMessage.fileInfo);
        }
        imSingleMessage.modifiedTime = rtmSingleMessage.modifiedTime;
        imSingleMessage.cusorId = rtmSingleMessage.cusorId;
        return imSingleMessage;
    }


    public static IMHistoryMessageResult transIMHistoryResult( RTMHistoryMessageResult rtmHistoryMessageResult){
        IMHistoryMessageResult imHistoryMessageResult = new IMHistoryMessageResult();
        if (rtmHistoryMessageResult.count >0) {
            imHistoryMessageResult.count = rtmHistoryMessageResult.count;
            imHistoryMessageResult.beginMsec = rtmHistoryMessageResult.beginMsec;
            imHistoryMessageResult.endMsec = rtmHistoryMessageResult.endMsec;
            imHistoryMessageResult.lastId = rtmHistoryMessageResult.lastId;
            imHistoryMessageResult.messages = new ArrayList<>();
            for (RTMHistoryMessage historyMessage : rtmHistoryMessageResult.messages) {
                IMHistoryMessage imMessage = transIMHistoryMessage(historyMessage);
                imHistoryMessageResult.messages.add(imMessage);
            }
        }
        return imHistoryMessageResult;
    }

    public static IMConversationInfo transIMConversationInfo(RTMConversationInfo rtmConversationInfo){
        IMConversationInfo imConversationInfo = new IMConversationInfo();
        imConversationInfo.targetId = rtmConversationInfo.targetId;
        imConversationInfo.unreadNum = rtmConversationInfo.unreadNum;
        if (rtmConversationInfo.lastHistortMessage!= null)
            imConversationInfo.lastHistortMessage = TransStruct.transIMHistoryMessage(rtmConversationInfo.lastHistortMessage);
        return imConversationInfo;
    }

    public static IMUnreadConversationInfo transIMUnreadConversationInfo(RTMUnreadConversationInfo rtmUnreadConversationInfo){
        IMUnreadConversationInfo imUnreadConversationInfo = new IMUnreadConversationInfo();
        for (RTMConversationInfo rtmConversationInfo: rtmUnreadConversationInfo.p2pUnread){
            IMConversationInfo imConversationInfo = transIMConversationInfo(rtmConversationInfo);
            imUnreadConversationInfo.p2pUnread.add(imConversationInfo);
        }

        for (RTMConversationInfo rtmConversationInfo: rtmUnreadConversationInfo.groupUnread){
            IMConversationInfo imConversationInfo = transIMConversationInfo(rtmConversationInfo);
            imUnreadConversationInfo.groupUnread.add(imConversationInfo);
        }
        return imUnreadConversationInfo;
    }
}
