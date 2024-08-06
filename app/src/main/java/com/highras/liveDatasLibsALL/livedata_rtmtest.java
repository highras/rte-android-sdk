package com.highras.liveDatasLibsALL;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.LiveDataRTE.RTMLib.RTMStruct.*;
import com.LiveDataRTE.LDEngine;
import com.LiveDataRTE.LDInterface.*;
import com.LiveDataRTE.LiveDataStruct.*;
import com.LiveDataRTE.*;
import com.LiveDataRTE.RTMLib.IRTMPushProcessor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class livedata_rtmtest extends AppCompatActivity implements View.OnClickListener{
    ExpandableListView faqList;
    MyAdapter adapter;
    Utils utils = Utils.INSTANCE;
    LDEngine ldEngine;
    EditText logintext;
    EditText touidtext;
    TextView logView;
    byte[] recordAudioData;
    byte[] audioData;
    byte[] videoData;
    byte[] imageData;
    byte[] normalFileData;
    int baseType = 88;
    String sendMsg = "hello 你好";

    String imageName = "ct4.jpeg";
    String audioName = "testaudio.mp3";
    String videoName = "testvideo.mp4";
    String normalFileName = "normal.txt";
    Context mycontext = this;
    long toUid = 999;
    long groupId = 200L;
    long roomId = 200L;
    List<Long> groupids = new ArrayList<Long>(){{add(groupId);}};
    List<Long> roomIds = new ArrayList<Long>(){{add(roomId);}};
    String userattrs = "我的消息";
    String extra = "申请加入";
    JSONObject jsonattrs = new JSONObject();
    ArrayList<Long> testUids = new ArrayList<Long>(){{
        add(toUid);
    }};

    void outputMsg(String method){
        outputMsg(method, null);
    }
    private void outputMsg(String method, LDAnswer answer){
        if (answer == null) {
            if (method.substring(method.length()-1) == "\n")
                method = method.substring(0,method.length()-2);
            addLog(method + " 成功");
        }
        else
            addLog(method + " 失败 " + answer.getErrInfo());
    }



    void addLog(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                String realmsg = "[" + (new SimpleDateFormat("MM-dd HH:mm:ss.SSS").format(new Date())) + "] " + msg + "\n";
                String realmsg = "[" + (new SimpleDateFormat("HH:mm:ss.SSS").format(new Date())) + "] " + msg + "\n";
                logView.append(realmsg);
            }
        });
    }

    void p2ptest(int childPosition){
        if (ldEngine == null){
            Utils.alertDialog(this,"请先登录");
            return;
        }
        switch (childPosition){
            case 0:
                ldEngine.RTM.sendChatMessage(toUid,  ConversationType.P2P, sendMsg, userattrs, new ISendMsgCallback() {
                    @Override
                    public void onSuccess(long messageTime, long messageId, String msg) {
                        outputMsg("sendChatMessage P2P msg:" + msg);
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("sendChatMessage P2P",answer);
                    }
                });
                break;
            case 1:
                break;
            case 2:
                ldEngine.RTM.sendFile(toUid,ConversationType.P2P,imageData, imageName,jsonattrs, FileMessageType.IMAGEFILE, new ISendFileCallback() {
                    @Override
                    public void onSuccess(long aLong, long aLong2) {
                        outputMsg("sendFile P2P image");
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("sendFile P2P image",answer);
                    }
                });
                break;
            case 3:
                ldEngine.RTM.sendFile(toUid,ConversationType.P2P, audioData, audioName,jsonattrs,  FileMessageType.AUDIOFILE,new ISendFileCallback() {
                    @Override
                    public void onSuccess(long aLong, long aLong2) {
                        outputMsg("sendFile P2P audio");
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("sendFile P2P audio",answer);
                    }
                });
                break;
            case 4:
                ldEngine.RTM.sendFile(toUid,ConversationType.P2P,videoData, videoName,jsonattrs,  FileMessageType.VIDEOFILE, new ISendFileCallback() {
                    @Override
                    public void onSuccess(long aLong, long aLong2) {
                        outputMsg("sendFile P2P video");
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("sendFile P2P video",answer);
                    }
                });
                break;
            case 5:
                ldEngine.RTM.sendFile(toUid,ConversationType.P2P, normalFileData, normalFileName,jsonattrs,  FileMessageType.NORMALFILE,new ISendFileCallback() {
                    @Override
                    public void onSuccess(long aLong, long aLong2) {
                        outputMsg("sendFile P2P normal");
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("sendFile P2P normal",answer);
                    }
                });
                break;
            case 6:
                ldEngine.RTM.sendBasicMessage(toUid,ConversationType.P2P, baseType, sendMsg, userattrs, new ISendMsgCallback() {
                    @Override
                    public void onSuccess(long messageTime, long messageId, String msg) {
                        outputMsg("sendBasicMessage P2P");
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("sendBasicMessage P2P",answer);
                    }
                });
                break;
            case 7:
                ldEngine.RTM.sendCMDMessage(toUid,ConversationType.P2P, sendMsg, userattrs, new ISendMsgCallback() {
                    @Override
                    public void onSuccess(long messageTime, long messageId, String msg) {
                        outputMsg("sendCMDMessage P2P");
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("sendCMDMessage P2P",answer);
                    }
                });
                break;
        }

    }
    void grouptest(int childPosition){
        if (ldEngine == null){
            Utils.alertDialog(this,"请先登录");
            return;
        }
        switch (childPosition){
            case 0:
                ldEngine.RTM.sendChatMessage(groupId, ConversationType.GROUP,sendMsg, userattrs,  new ISendMsgCallback() {
                    @Override
                    public void onSuccess(long messageTime, long messageId,String msg) {
                        outputMsg("sendChatMessage Group");
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("sendChatMessage Group",answer);
                    }
                });
                break;
            case 1:
                break;
            case 2:
                ldEngine.RTM.sendFile(groupId,ConversationType.GROUP,  imageData, imageName,jsonattrs, FileMessageType.IMAGEFILE,new ISendFileCallback() {
                    @Override
                    public void onSuccess(long aLong, long aLong2) {
                        outputMsg("sendFile Group image");
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("sendFile Group image",answer);
                    }
                });
                break;
            case 3:
                ldEngine.RTM.sendFile(groupId,ConversationType.GROUP, audioData, audioName,jsonattrs,  FileMessageType.AUDIOFILE,new ISendFileCallback() {
                    @Override
                    public void onSuccess(long aLong, long aLong2) {
                        outputMsg("sendFile Group audio");
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("sendFile Group audio",answer);
                    }
                });
                break;
            case 4:
                ldEngine.RTM.sendFile(groupId,ConversationType.GROUP, videoData, videoName,jsonattrs,  FileMessageType.VIDEOFILE,new ISendFileCallback() {
                    @Override
                    public void onSuccess(long aLong, long aLong2) {
                        outputMsg("sendFile Group video");
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("sendFile Group video",answer);
                    }
                });
                break;
            case 5:
                ldEngine.RTM.sendFile(groupId,ConversationType.GROUP, normalFileData, normalFileName,jsonattrs, FileMessageType.NORMALFILE, new ISendFileCallback() {
                    @Override
                    public void onSuccess(long aLong, long aLong2) {
                        outputMsg("sendFile Group normal");
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("sendFile Group normal",answer);
                    }
                });
                break;
            case 6:
                ldEngine.RTM.sendBasicMessage(groupId,ConversationType.GROUP, baseType, sendMsg, userattrs, new ISendMsgCallback() {
                    @Override
                    public void onSuccess(long messageTime, long messageId, String msg) {
                        outputMsg("sendBasicMessage GROUP");
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("sendBasicMessage GROUP",answer);
                    }
                });
                break;
            case 7:
                ldEngine.RTM.sendCMDMessage(groupId,ConversationType.GROUP, sendMsg, userattrs, new ISendMsgCallback() {
                    @Override
                    public void onSuccess(long messageTime, long messageId, String msg) {
                        outputMsg("sendCMDMessage GROUP");
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("sendCMDMessage GROUP",answer);
                    }
                });
                break;
            case 8:
                ldEngine.RTM.addGroupMembers(groupId, testUids, new IEmptyCallback() {
                    @Override
                    public void onSuccess() {
                        outputMsg("addGroupMembers gid:"+ groupId);
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("addGroupMembers",answer);
                    }
                });
                break;
            case 9:
                ldEngine.RTM.removeGroupMembers(groupId, testUids, new IEmptyCallback() {
                    @Override
                    public void onSuccess() {
                        outputMsg("removeGroupMembers gid:" + groupId);
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("removeGroupMembers",answer);
                    }
                });
                break;
            case 10:
                ldEngine.RTM.getGroupMemberCount(groupId, true, new IDoubleCallBack<Integer, Integer>() {
                    @Override
                    public void onSuccess(Integer integer, Integer integer2) {
                        outputMsg("getGroupMemberCount gid:" + groupId + " count:" + integer + " onlinecount:"+ integer2);
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("getGroupMemberCount",answer);
                    }
                });
                break;
            case 11:
                ldEngine.RTM.getGroupMembers(groupId, true, new ICallback<RTMGroupMembers>() {
                    @Override
                    public void onSuccess(RTMGroupMembers rtmGroupMembers) {
                        outputMsg("getGroupMembers gid:" + groupId + " members:" + rtmGroupMembers.userids.toString() + " onlines:"+ rtmGroupMembers.onlineUserids.toString());
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("getGroupMembers",answer);
                    }
                });
                break;
            case 12:
                ldEngine.RTM.getGroupsPublicInfo(groupids, new ICallback<Map<Long, String>>() {
                    @Override
                    public void onSuccess(Map<Long, String> stringStringMap) {
                        String msg = "";
                        for(Long gid: stringStringMap.keySet()){
                            String tt = "gid:"+ gid+ " publicinfo:" +stringStringMap.get(gid)+"\n";
                            msg += tt;
                        }
                        outputMsg("getGroupsPublicInfo " + msg);
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("getGroupsPublicInfo ",answer);
                    }
                });
                break;
            case 13:
                ldEngine.RTM.getUserGroups(new ICallback<List<Long>>() {
                    @Override
                    public void onSuccess(List<Long> longs) {
                        outputMsg("getUserGroups " + longs.toString());
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("getUserGroups ",answer);
                    }
                });
                break;
            case 14:
                ldEngine.RTM.setGroupInfo(groupId, "群组公开信息", "群组私有信息", new IEmptyCallback() {
                    @Override
                    public void onSuccess() {
                        outputMsg("setGroupInfo gid:" + groupId);
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("setGroupInfo gid:" + groupId, answer);
                    }
                });
                break;
            case 15:
                ldEngine.RTM.getGroupInfo(groupId, new IGetinfoCallback() {
                    @Override
                    public void onSuccess(String publicInfo, String privateInfo) {
                        outputMsg("getGroupInfo gid:" + groupId + " publicInfo:" + publicInfo + " privateInfo:"+privateInfo);
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("getGroupInfo gid:" + groupId, answer);

                    }
                });
                break;
        }

    }
    void friendtest(int childPosition){
        if (ldEngine == null){
            Utils.alertDialog(this,"请先登录");
            return;
        }
        switch (childPosition){
            case 0:
                ldEngine.RTM.addFriends(testUids, new IEmptyCallback() {
                    @Override
                    public void onSuccess() {
                        outputMsg("addFriends");
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("addFriends" +  answer.getErrInfo());
                    }
                });
                break;
            case 1:
                ldEngine.RTM.deleteFriends(testUids, new IEmptyCallback() {
                    @Override
                    public void onSuccess() {
                        outputMsg("deleteFriends");

                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("deleteFriends" +  answer);

                    }
                });
                break;
            case 2:
                ldEngine.RTM.getFriendList(new ICallback<List<Long>>() {
                    @Override
                    public void onSuccess(List<Long> longs) {
                        outputMsg("getFriendList " + longs.toString());
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("getFriendList", answer);
                    }
                });
                break;
            case 3:
                ldEngine.RTM.addBlacklist(testUids, new IEmptyCallback() {
                    @Override
                    public void onSuccess() {
                        outputMsg("addBlacklist " + testUids.toString());
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("addBlacklist", answer);
                    }
                });
                break;
            case 4:
                ldEngine.RTM.deleteBlacklist(testUids, new IEmptyCallback() {
                    @Override
                    public void onSuccess() {
                        outputMsg("delBlacklist " + testUids.toString());
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("delBlacklist", answer);
                    }
                });
                break;
            case 5:
                ldEngine.RTM.getBlacklist(new ICallback<List<Long>>() {
                    @Override
                    public void onSuccess(List<Long> longs) {
                        outputMsg("getBlacklist " + longs.toString());
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("getBlacklist", answer);
                    }
                });
                break;
        }

    }
    void conversitationtest(int childPosition){
        if (ldEngine == null){
            Utils.alertDialog(this,"请先登录");
            return;
        }
        List<Integer> searchTypes = new ArrayList<>();
        searchTypes.add(30);
        searchTypes.add(31);
        searchTypes.add(32);

        switch (childPosition){
            case 0:
                ldEngine.RTM.getAllConversation(0, null,ConversationType.P2P, new ICallback<List<RTMConversationInfo>>() {
                    @Override
                    public void onSuccess(List<RTMConversationInfo> rtmConversationInfos) {
                        String showmsg = "";
                        for (RTMConversationInfo rtmConversationInfo: rtmConversationInfos){
                            String msg = "uid:" +rtmConversationInfo.targetId + " 未读条数:"+rtmConversationInfo.unreadNum +
                                    " lastmsg:" +rtmConversationInfo.lastHistortMessage.getInfo() + "\n";
                            showmsg += msg;
                        }
                        outputMsg("getConversation P2P" + showmsg);
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("getConversation P2P" ,answer);
                    }
                });
                break;
            case 1:
                ldEngine.RTM.getAllConversation(0, null, ConversationType.GROUP,new ICallback<List<RTMConversationInfo>>() {
                    @Override
                    public void onSuccess(List<RTMConversationInfo> rtmConversationInfos) {
                        String showmsg = "";
                        for (RTMConversationInfo rtmConversationInfo: rtmConversationInfos){
                            String msg = "uid:" +rtmConversationInfo.targetId + " 未读条数:"+rtmConversationInfo.unreadNum +
                                    " lastmsg:" +rtmConversationInfo.lastHistortMessage.getInfo() + "\n";
                            showmsg += msg;
                        }
                        outputMsg("getConversation GROUP" + showmsg);
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("getConversation GROUP" ,answer);
                    }
                });
                break;
            case 2:
                ldEngine.RTM.getAllUnreadConversation(true, 0, null,new ICallback<RTMUnreadConversationInfo>() {
                    @Override
                    public void onSuccess(RTMUnreadConversationInfo rtmUnreadConversationInfo) {
                        String showmsg = "";
                        for (RTMConversationInfo rtmConversationInfo: rtmUnreadConversationInfo.groupUnread){
                            String msg = "groupId:" +rtmConversationInfo.targetId + " 未读条数:"+rtmConversationInfo.unreadNum +
                                    " lastmsg:" +rtmConversationInfo.lastHistortMessage.getInfo() + "\n";
                            showmsg += msg;
                        }
                        for (RTMConversationInfo rtmConversationInfo: rtmUnreadConversationInfo.p2pUnread){
                            String msg = "uid:" +rtmConversationInfo.targetId + " 未读条数:"+rtmConversationInfo.unreadNum +
                                    " lastmsg:" +rtmConversationInfo.lastHistortMessage.getInfo() + "\n";
                            showmsg += msg;
                        }
                        outputMsg("getAllUnreadConversation" + showmsg);
                    }

                    @Override
                    public void onError(LDAnswer answer) {

                    }
                });
                break;
            case 3:
                ldEngine.RTM.getMessage( ldEngine.getUid(), toUid, ConversationType.P2P, 12345678, new ICallback<RTMSingleMessage>() {
                    @Override
                    public void onSuccess(RTMSingleMessage rtmSingleMessage) {
                        outputMsg("getMessage" + rtmSingleMessage.getInfo());
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("getMessage" ,answer);
                    }
                });
                break;
            case 4:
                ldEngine.RTM.deleteMessage(ldEngine.getUid(), toUid, ConversationType.P2P, 12345678, new IEmptyCallback() {
                    @Override
                    public void onSuccess() {
                        outputMsg("deleteMessage" );
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("deleteMessage" ,answer);

                    }
                });
                break;
            case 5:
                ldEngine.RTM.getHistoryChat(toUid, ConversationType.P2P,true, 10, 0, 0, 0,  new ICallback<RTMHistoryMessageResult>() {
                    @Override
                    public void onSuccess(RTMHistoryMessageResult rtmHistoryMessageResult) {
                        String msg;
                        outputMsg("getHistoryChatMessage count:" + rtmHistoryMessageResult.count + " beginMsec:" + rtmHistoryMessageResult.beginMsec
                                + " endMsec:"+ rtmHistoryMessageResult.endMsec + " lastcurid:" + rtmHistoryMessageResult.lastId);

                        for (RTMHistoryMessage hm : rtmHistoryMessageResult.messages) {
                            addLog(hm.getInfo());
                        }
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("getHistoryChatMessage", answer);
                    }
                });
                break;
        }


    }
    void usertest(int childPosition){
        if (ldEngine == null){
            Utils.alertDialog(this,"请先登录");
            return;
        }
        switch (childPosition){
            case 0:
                ldEngine.RTM.addDevice("123456789","tag",new IEmptyCallback() {
                    @Override
                    public void onSuccess() {
                        outputMsg("addDevice");
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("addDevice",answer);
                    }
                });
                break;
            case 1:
                ldEngine.RTM.removeDevice("123456789","tag",new IEmptyCallback() {
                    @Override
                    public void onSuccess() {
                        outputMsg("removeDevice");
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("removeDevice",answer);
                    }
                });
                break;
            case 2:
                ldEngine.RTM.addDevicePushOption(0, toUid, null, new IEmptyCallback() {
                    @Override
                    public void onSuccess() {
                        outputMsg("addDevicePushOption");
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("addDevicePushOption",answer);
                    }
                });
                break;
            case 3:
                ldEngine.RTM.removeDevicePushOption(0, toUid, null, new IEmptyCallback() {
                    @Override
                    public void onSuccess() {
                        outputMsg("removeDevicePushOption");
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("removeDevicePushOption",answer);
                    }
                });
                break;
            case 4:
                ldEngine.RTM.getDevicePushOption(new ICallback<DevicePushOption>() {
                    @Override
                    public void onSuccess(DevicePushOption devicePushOption) {
                        outputMsg("getDevicePushOption");
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("getDevicePushOption",answer);
                    }
                });
                break;
            case 5:
                ldEngine.RTM.setUserInfo("我的公开信息", "我的私有信息", new IEmptyCallback() {
                    @Override
                    public void onSuccess() {
                        outputMsg("setUserInfo");
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("setUserInfo",answer);
                    }
                });
                break;
            case 6:
                ldEngine.RTM.getUserInfo(new IGetinfoCallback() {
                    @Override
                    public void onSuccess(String publicInfo, String privateInfo) {
                        outputMsg("getUserInfo publicInfo:"+ publicInfo + " privateInfo:"+privateInfo);
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("getUserInfo",answer);
                    }
                });
                break;
            case 7:
                ldEngine.RTM.getUserPublicInfo(testUids, new ICallback<Map<Long, String>>() {
                    @Override
                    public void onSuccess(Map<Long, String> stringStringMap) {
                        String msg = "";
                        for(Long uid: stringStringMap.keySet()){
                            String tt = "uid:"+ uid+ " publicinfo:" +stringStringMap.get(uid)+"\n";
                            msg += tt;
                        }
                        outputMsg("getUserPublicInfo " + msg);
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("getUserPublicInfo ",answer);

                    }
                });
                break;
            case 8:
                ldEngine.RTM.dataSet("setkey", "123", new IEmptyCallback() {
                    @Override
                    public void onSuccess() {
                        outputMsg("dataSet");

                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("dataSet ",answer);

                    }
                });
                break;
            case 9:
                ldEngine.RTM.dataGet("setkey", new ICallback<String>() {
                    @Override
                    public void onSuccess(String s) {
                        outputMsg("dataGet value:"+s);

                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("dataGet ",answer);

                    }
                });
                break;
            case 10:
                ldEngine.RTM.dataDelete("setkey", new IEmptyCallback() {
                    @Override
                    public void onSuccess() {
                        outputMsg("dataDelete ");
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("dataDelete ",answer);
                    }
                });
                break;
        }

    }
    void roomtest(int childPosition){
        if (ldEngine == null){
            Utils.alertDialog(this,"请先登录");
            return;
        }
        switch (childPosition){
            case 0:
                ldEngine.RTM.sendChatMessage(roomId,  ConversationType.ROOM, sendMsg, userattrs, new ISendMsgCallback() {
                    @Override
                    public void onSuccess(long messageTime, long messageId, String msg) {
                        outputMsg("sendChatMessage ROOM");
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("sendChatMessage ROOM",answer);
                    }
                });
                break;
            case 1:
                ldEngine.RTM.sendBasicMessage(roomId,  ConversationType.ROOM, baseType,sendMsg, userattrs, new ISendMsgCallback() {
                    @Override
                    public void onSuccess(long messageTime, long messageId, String msg) {
                        outputMsg("sendBasicMessage ROOM");
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("sendBasicMessage ROOM",answer);
                    }
                });
                break;
            case 2:
                ldEngine.RTM.enterRoom(roomId, new IEmptyCallback() {
                    @Override
                    public void onSuccess() {
                        outputMsg("enterRoom ");
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("enterRoom ", answer);
                    }
                });
                break;
            case 3:
                ldEngine.RTM.leaveRoom(roomId, new IEmptyCallback() {
                    @Override
                    public void onSuccess() {
                        outputMsg("leaveRoom ");
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("leaveRoom ", answer);
                    }
                });
                break;
            case 4:
                ldEngine.RTM.getRoomMembers(roomId, new ICallback<List<Long>>() {
                    @Override
                    public void onSuccess(List<Long> longs) {
                        outputMsg("getRoomMembers " + longs.toString());

                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("getRoomMembers ",answer);
                    }
                });
                break;
            case 5:
                ldEngine.RTM.getRoomMemberCount(roomIds, new ICallback<Map<Long, Integer>>() {
                    @Override
                    public void onSuccess(Map<Long, Integer> longIntegerMap) {
                        String msg = "";
                        for(Long rid: longIntegerMap.keySet()){
                            String tt = "roomid:"+ rid+ " publicinfo:" +longIntegerMap.get(rid)+"\n";
                            msg += tt;
                        }
                        outputMsg("getRoomMemberCount " + msg);
                    }

                    @Override
                    public void onError(LDAnswer answer) {

                    }
                });
                break;
            case 6:
                ldEngine.RTM.getUserRooms(new ICallback<List<Long>>() {
                    @Override
                    public void onSuccess(List<Long> longs) {
                        outputMsg("getUserRooms rooms:" + longs);
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("getUserRooms " ,answer);
                    }
                });
                break;
            case 7:
                ldEngine.RTM.setRoomInfo(roomId,"房间公开信息", "房间私有信息", new IEmptyCallback() {
                    @Override
                    public void onSuccess() {
                        outputMsg("setRoomInfo");
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("setRoomInfo",answer);
                    }
                });
                break;
            case 8:
                ldEngine.RTM.getRoomInfo(roomId,new IGetinfoCallback() {
                    @Override
                    public void onSuccess(String publicInfo, String privateInfo) {
                        outputMsg("getRoomInfo publicInfo:"+ publicInfo + " privateInfo:"+privateInfo);
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("getRoomInfo",answer);
                    }
                });
                break;
            case 9:
                ldEngine.RTM.getRoomsPublicInfo(roomIds, new ICallback<Map<Long, String>>() {
                    @Override
                    public void onSuccess(Map<Long, String> stringStringMap) {
                        String msg = "";
                        for(long rid: stringStringMap.keySet()){
                            String tt = "roomid:"+ rid+ " publicinfo:" +stringStringMap.get(rid)+"\n";
                            msg += tt;
                        }
                        outputMsg("getRoomsPublicInfo " + msg);
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        outputMsg("getRoomsPublicInfo",answer);

                    }
                });
                break;
        }

    }

    String transConversationType(ConversationType conversationType){
        if (conversationType == ConversationType.P2P)
            return "P2P";
        else if (conversationType == ConversationType.GROUP)
            return "GROUP";
        else if (conversationType == ConversationType.ROOM)
            return "ROOM";
        return "";
    }

    interface Agreeorrefuse{
        void onAgree();
        void onRefuse();
    }

    interface CheckboxSelect{
        void onConfirm(int index);
        void onCancel();
    }

    void agreeOrRefuse(String titile, Agreeorrefuse agreeorrefuse){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(mycontext);
                builder.setMessage(titile).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        agreeorrefuse.onAgree();
                    }
                }).setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        agreeorrefuse.onRefuse();
                    }
                }).setNeutralButton("再说", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
                builder.show();
            }
        });
    }

    void dialogText(String titile, Agreeorrefuse agreeorrefuse){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        EditText editText = new EditText(this);
        builder.setView(editText);
        builder.setMessage(titile).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                agreeorrefuse.onAgree();
            }
        }).setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                agreeorrefuse.onRefuse();
            }
        });
        builder.show();
    }



    void checkboxSelect(String title, String[] items, CheckboxSelect checkboxSelect){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mycontext);
                alertBuilder.setTitle(title);
                final int[] index = {0};
                alertBuilder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        index[0] = i;
                    }
                });
                alertBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        checkboxSelect.onConfirm(index[0]);
                    }
                });
                alertBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        checkboxSelect.onCancel();
                    }
                });
                alertBuilder.show();
            }
        });
    }


    IBasePushProcessor imBasePushProcessor = new IBasePushProcessor() {
        @Override
        public void rtmConnectClose(long uid) {
            addLog("链接已断开");
        }

        @Override
        public boolean reloginWillStart(long uid, int reloginCount) {
            addLog("开始重连第"+reloginCount+"次");
            return true;
        }

        @Override
        public void reloginCompleted(long uid, boolean successful, LDAnswer answer, int reloginCount) {
            addLog("重连结束 结果:" + answer.getErrInfo()+ "  重连次数"+reloginCount);
        }

        @Override
        public void kickout() {
            addLog("被服务器踢下线");
        }
    };
    IRTMPushProcessor irtmPushProcessor = new IRTMPushProcessor() {
        @Override
        public void pushChat(RTMMessage rtmMessage, ConversationType conversationType) {
            addLog("recieve msg " + transConversationType(conversationType) + rtmMessage.getInfo());

        }

        @Override
        public void kickoutRoom(long roomId) {
            addLog("recieve kickoutRoom roomid:" +roomId);
        }

        @Override
        public void pushCmd(RTMMessage rtmMessage, ConversationType conversationType) {
            addLog("recieve cmd " + transConversationType(conversationType) + rtmMessage.getInfo());
        }

        @Override
        public void pushMessage(RTMMessage rtmMessage, ConversationType conversationType) {
            addLog("recieve cmd " + transConversationType(conversationType) + rtmMessage.getInfo());
        }

        @Override
        public void pushFile(RTMMessage rtmMessage, ConversationType conversationType) {
            addLog("recieve file " + transConversationType(conversationType) + rtmMessage.getInfo());
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livedatartmtest);
        faqList = findViewById(R.id.faqList);
        logView = findViewById(R.id.logview);
        logView.setMovementMethod(ScrollingMovementMethod.getInstance());

        findViewById(R.id.clearlog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logView.setText("");
            }
        });
        try {
            jsonattrs.put("testattrs","hehe");
        } catch (JSONException e) {
            e.printStackTrace();
            e.printStackTrace();
        }
        logintext = findViewById(R.id.loginid);
        touidtext = findViewById(R.id.touid);
        try {
            InputStream inputStream = getAssets().open("audioDemo.amr");
            recordAudioData = utils.toByteArray(inputStream);

            inputStream = getAssets().open(audioName);
            audioData = utils.toByteArray(inputStream);

            inputStream = getAssets().open(videoName);
            videoData = utils.toByteArray(inputStream);

            inputStream=getAssets().open(imageName);
            imageData = utils.toByteArray(inputStream);

            inputStream=getAssets().open(normalFileName);
            normalFileData = utils.toByteArray(inputStream);
        }
        catch (Exception e){
            e.printStackTrace();
            return;
        }

        findViewById(R.id.login).setOnClickListener(this);
        findViewById(R.id.settouid).setOnClickListener(this);
        findViewById(R.id.setgroup).setOnClickListener(this);

        faqList.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                for(int i = 0;i < adapter.getGroupCount();i++){
                    if (i!=groupPosition){
                        faqList.collapseGroup(i);
                    }
                }
            }
        });

        faqList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (groupPosition == 0){
                    p2ptest(childPosition);
                }
                else if (groupPosition == 1){
                    grouptest(childPosition);
                }

                else if (groupPosition == 2){
                    friendtest(childPosition);
                }
                else if (groupPosition == 3){
                    conversitationtest(childPosition);
                }

                else if (groupPosition == 4){
                    usertest(childPosition);
                }
                else if (groupPosition == 5){
                    roomtest(childPosition);
                }
                return true;
            }
        });


        ArrayList<String> mGroupList = new ArrayList<String>(){{
            add("P2P接口");
            add("群组接口");
            add("好友接口");
            add("会话和历史接口");
            add("用户相关接口");
            add("房间相关接口");
        }};

        ArrayList<ArrayList<String>> mItemSet = new ArrayList<ArrayList<String>>();
        ArrayList<String> messageType = new ArrayList<String>(){{
            add("发送P2P聊天");
            add("发送P2P离线语音");
            add("发送P2P图片");
            add("发送P2P语音文件");
            add("发送P2P视频文件");
            add("发送P2P普通文件");
            add("发送P2P自定义消息");
            add("发送P2P命令消息");
        }};
        mItemSet.add(messageType);
        ArrayList<String> messageType1 = new ArrayList<String>(){{
            add("发送群组聊天");
            add("发送群组离线语音");
            add("发送群组图片");
            add("发送群组语音文件");
            add("发送群组视频文件");
            add("发送群组普通文件");
            add("发送群组自定义消息");
            add("发送群组命令消息");
            add("添加群组成员");
            add("删除群组成员");
            add("获取群组人数");
            add("获取群组成员列表");
            add("获取群组公开信息");
            add("获取自己的群组");
            add("设置群组信息");
            add("获取群组的公开和私有信息");
        }};
        mItemSet.add(messageType1);

        ArrayList<String> messageType2 = new ArrayList<String>(){{
            add("添加好友");
            add("删除好友");
            add("获取自己好友列表");
            add("添加黑名单");
            add("删除黑名单用户");
            add("查询黑名单");
        }};
        mItemSet.add(messageType2);

        ArrayList<String> messageType3 = new ArrayList<String>(){{
            add("获取所有P2P会话");
            add("获取所有Group会话");
            add("获取未读会话");
            add("获取单条消息");
            add("删除单条消息");
            add("获取历史消息");
        }};
        mItemSet.add(messageType3);

        ArrayList<String> messageType4 = new ArrayList<String>(){{
            add("添加android推送设备");
            add("删除推送设备");
            add("设置设备推送属性");
            add("取消设备推送属性");
            add("获取设备推送属性");
            add("设置自己的公开信息和私有信息");
            add("获取自己的公开信息和私有信息");
            add("获取多个用户的公开信息");
            add("设置存储信息");
            add("获取存储信息");
            add("删除存储信息");
        }};
        mItemSet.add(messageType4);

        ArrayList<String> messageType5 = new ArrayList<String>(){{
            add("发送房间消息");
            add("发送房间自定义消息");
            add("进入房间");
            add("离开房间");
            add("获取房间成员列表");
            add("获取房间人数");
            add("获取用户加入的房间");
            add("设置房间的公开和私有信息");
            add("获取房间的公开和私有信息");
            add("获取多个房间的公开信息");
        }};
        mItemSet.add(messageType5);

        adapter = new MyAdapter(getApplicationContext(), mGroupList, mItemSet);
        faqList.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (utils.ldEngine==null)
            return;
        utils.ldEngine.closeEngine();
        utils.ldEngine = null;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.login){
            try {
                String loginstring = logintext.getText().toString();
                if (!loginstring.isEmpty()){
                    long uid = Long.parseLong(logintext.getText().toString());
                    if (uid > 0){
                        utils.currentUserid = uid;
                    }
                }
                utils.login(this, new LDInterface.IEmptyCallback() {
                    @Override
                    public void onSuccess() {
                        ldEngine = utils.ldEngine;
                        ldEngine.setBasePushProcessor(imBasePushProcessor);
                        ldEngine.setRTMPushProcessor(irtmPushProcessor);
                        utils.toast((Activity) mycontext, utils.currentUserid + " 登录成功");
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        utils.alertDialog((Activity) mycontext, utils.currentUserid + " 登录失败 " + answer.getErrInfo());
                    }
                });

            }
            catch (Exception e){
                e.printStackTrace();
                utils.alertDialog((Activity) mycontext, "发生异常 " + e.getMessage());
            }
        }
        else if (v.getId() == R.id.settouid){
            String totuidstring = touidtext.getText().toString();
            if (!totuidstring.isEmpty()){
                long touid = Long.parseLong(touidtext.getText().toString());
                if (touid > 0) {
                    toUid = touid;
                    testUids = new ArrayList<Long>(){{
                        add(toUid);
                    }};
                }
                utils.toast(this,"设置uid" + touid + "成功");
            }
        }
        else if (v.getId() == R.id.setgroup){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            EditText editText = new EditText(this);
            builder.setView(editText);
            builder.setMessage("设置群组和房间id").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        groupId = Long.parseLong(editText.getText().toString());
                        roomId =  groupId;
                    } catch (NumberFormatException ex) {
                        addLog("设置群组id错误:" + ex.getMessage());
                    }
                    catch (Exception e){
                        addLog("设置群组id错误:" + e.getMessage());
                    }
                    groupids = new ArrayList<Long>(){{
                        add(groupId);
                    }};
                    roomIds = new ArrayList<Long>(){{
                        add(roomId);
                    }};
                    addLog("设置群组和房间id成功 " + groupId);
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.show();
        }
    }
}