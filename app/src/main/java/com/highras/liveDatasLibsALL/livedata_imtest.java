package com.highras.liveDatasLibsALL;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.LiveDataRTE.IMLib.IIMPushProcessor;
import com.LiveDataRTE.IMLib.IMStruct.*;
import com.LiveDataRTE.LDEngine;
import com.LiveDataRTE.LDInterface.*;
import com.LiveDataRTE.LiveDataStruct.*;
import com.LiveDataRTE.*;
import com.fpnn.sdk.FunctionalAnswerCallback;
import com.fpnn.sdk.proto.Answer;
import com.fpnn.sdk.proto.Quest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class livedata_imtest extends AppCompatActivity implements View.OnClickListener{
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
    List<Long> roomids = new ArrayList<Long>(){{add(roomId);}};
    String userattrs = "我的消息";
    String extra = "123456";
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
    IIMPushProcessor iimPushProcessor = new IIMPushProcessor() {
        @Override
        public void pushChat(IMMessage imMessage, ConversationType conversationType) {
            addLog("recieve msg " + transConversationType(conversationType) + imMessage.getInfo());
        }

        @Override
        public void pushFile(IMMessage imMessage, ConversationType conversationType) {
            addLog("recieve file " + transConversationType(conversationType) + imMessage.getInfo());
        }

        @Override
        public void pushAddFriend(long fromUid, String extraMessage, String attrs) {
            agreeOrRefuse(fromUid + "请求添加你为好友 留言:" + extraMessage + "attrs:" + attrs, new Agreeorrefuse() {
                @Override
                public void onAgree() {
                    ldEngine.IM.ackAddFriend(fromUid, true, userattrs, new IEmptyCallback() {
                        @Override
                        public void onSuccess() {
                            outputMsg("ackAddFriend true from:" + fromUid);
                        }

                        @Override
                        public void onError(LDAnswer answer) {
                            outputMsg("ackAddFriend true from:" + fromUid , answer);
                        }
                    });

                }

                @Override
                public void onRefuse() {
                    ldEngine.IM.ackAddFriend(fromUid, false, userattrs, new IEmptyCallback() {
                        @Override
                        public void onSuccess() {
                            outputMsg("ackAddFriend false from:" + fromUid);
                        }

                        @Override
                        public void onError(LDAnswer answer) {
                            outputMsg("ackAddFriend false from:" + fromUid , answer);
                        }
                    });
                }
            });
        }

        @Override
        public void pushAgreeApplyFriend(long userId, String attrs) {
            addLog(userId  +"同意了你的好友请求 attrs:" + attrs );
        }

        @Override
        public void pushRefuseApplyFriend(long userId, String attrs) {
            addLog(userId +" 拒绝了你的好友请求 attrs:" + attrs );
        }

        @Override
        public void pushEstablishFriend(long userId, String attrs) {
            addLog("你和" + userId +"成为了好友 attrs:" + attrs );
        }

        @Override
        public void pushApplyGroup(long fromUid, long groupId, String extraMessage, String attrs) {
            agreeOrRefuse(fromUid + " 申请加入群组" + groupId + " 留言:"+ extraMessage + " attrs:" + attrs, new Agreeorrefuse() {
                @Override
                public void onAgree() {
                    ldEngine.IM.ackJoinGroup(groupId, fromUid, true, attrs, new IEmptyCallback() {
                        @Override
                        public void onSuccess() {
                            outputMsg("ackJoinGroup true :" + groupId );
                        }

                        @Override
                        public void onError(LDAnswer answer) {
                            outputMsg("ackJoinGroup true :" + groupId,answer);
                        }
                    });
                }

                @Override
                public void onRefuse() {
                    ldEngine.IM.ackJoinGroup(groupId, fromUid, false, attrs, new IEmptyCallback() {
                        @Override
                        public void onSuccess() {
                            outputMsg("ackJoinGroup false :" + groupId );
                        }

                        @Override
                        public void onError(LDAnswer answer) {
                            outputMsg("ackJoinGroup false :" + groupId,answer);
                        }
                    });                }
            });
        }

        @Override
        public void pushAgreeApplyGroup(long fromUid, long groupId, String attrs) {
            addLog(fromUid  +"同意了你的入群请求,gid:" + groupId + " attrs:" +attrs);
        }

        @Override
        public void pushRefuseApplyGroup(long fromUid, long groupId, String attrs) {
            addLog(fromUid  +"拒绝了你的入群请求,gid:" + groupId + " attrs:" +attrs);
        }

        @Override
        public void pushInviteGroup(long fromUid, long groupId, String extraMessage, String attrs) {
            agreeOrRefuse(fromUid + " 邀请你加入群组 " +groupId +  " 留言:"+ extraMessage + " attrs:" + attrs, new Agreeorrefuse() {
                @Override
                public void onAgree() {
                    ldEngine.IM.ackInvitedIntoGroup(groupId, fromUid, true,attrs, new IEmptyCallback() {
                        @Override
                        public void onSuccess() {
                            outputMsg("ackInvitedIntoGroup" + groupId);
                        }

                        @Override
                        public void onError(LDAnswer answer) {
                            outputMsg("ackInvitedIntoGroup",answer);
                        }
                    });
                }

                @Override
                public void onRefuse() {
                    ldEngine.IM.ackInvitedIntoGroup(groupId, fromUid, false, attrs, new IEmptyCallback() {
                        @Override
                        public void onSuccess() {
                            outputMsg("拒绝群组邀请 群组id:" + groupId);
                        }

                        @Override
                        public void onError(LDAnswer answer) {
                            outputMsg("拒绝群组邀请",answer);
                        }
                    });
                }
            });
        }

        @Override
        public void pushAgreeInviteGroup(long fromUid, long groupId, String attrs) {
            addLog(fromUid  +"同意了你的入群邀请 gid:" + groupId+ " attrs:" +attrs);
        }

        @Override
        public void pushRefuseInviteGroup(long fromUid, long groupId, String attrs) {
            addLog(fromUid  +"拒绝了你的入群邀请 gid:" + groupId+ " attrs:" +attrs);
        }

        @Override
        public void pushGroupChange(long groupId, String attrs, int type) {
            switch (type){
                case 0:
                    addLog(  "你加入了群组 gid:" + groupId+ " attrs:" +attrs);
                    break;
                case 1:
                    addLog(  "你离开了群组 gid:" + groupId+ " attrs:" +attrs);
                    break;
                case 2:
                    addLog(   "群组" + groupId + "已解散");
                    break;
                case 3:
                    addLog(   "你被踢出了群组 gid:" + groupId);
                    break;
            }
        }

        @Override
        public void pushGroupMemberChange(long groupId, long userId,int type) {
            if (type == 0)
                addLog(  userId + "加入了群组" + groupId);
            else if (type == 1)
                addLog(  userId + "离开了群组" + groupId);
        }

        @Override
        public void pushGroupManagerChange(long gid, List<Long> uids, int type) {
            if (type == 0)
                addLog(   "群组" + gid + "增加了管理员" + uids.toString());
            else if (type == 1)
                addLog(   "gid:" + gid + "删除了管理员" + uids.toString());
        }

        @Override
        public void pushGroupLeaderChange(long gid, long oldLeader, long newLeader) {
            addLog(   "gid:" + gid + "群主变更old:" + oldLeader + " new:" + newLeader);
        }



        @Override
        public void pushInviteRoom(long fromUid, long roomId, String extraMessage, String attrs) {
            agreeOrRefuse(fromUid + " 邀请你加入房间 " +roomId +  " 留言:"+ extraMessage + " attrs:" + attrs, new Agreeorrefuse() {
                @Override
                public void onAgree() {
                    ldEngine.IM.ackInvitedIntoRoom(roomId, fromUid, true,attrs, new IEmptyCallback() {
                        @Override
                        public void onSuccess() {
                            outputMsg("ackInvitedIntoRoom" + roomId);
                        }

                        @Override
                        public void onError(LDAnswer answer) {
                            outputMsg("ackInvitedIntoRoom",answer);
                        }
                    });
                }

                @Override
                public void onRefuse() {
                    ldEngine.IM.ackInvitedIntoRoom(roomId, fromUid, false,attrs, new IEmptyCallback() {
                        @Override
                        public void onSuccess() {
                            outputMsg("拒绝房间邀请 房间id:" + roomId);
                        }

                        @Override
                        public void onError(LDAnswer answer) {
                            outputMsg("拒绝房间邀请",answer);
                        }
                    });
                }
            });
        }

        @Override
        public void pushAgreeInviteRoom(long fromUid, long roomId, String attrs) {
            addLog(fromUid  +"同意了你的加入房间邀请 rid:" + roomId+ " attrs:" +attrs);
        }

        @Override
        public void pushRefuseInviteRoom(long fromUid, long roomId, String attrs) {
            addLog(fromUid  +"拒绝了你的加入房间邀请 rid:" + roomId+ " attrs:" +attrs);
        }

        @Override
        public void pushRoomChange(long roomId, String attrs, int type) {
            switch (type){
                case 0:
                    addLog(  "你加入了房间 rid:" + roomId+ " attrs:" +attrs);
                    break;
                case 1:
                    addLog(  "你离开了房间 rid:" + roomId+ " attrs:" +attrs);
                    break;
                case 2:
                    addLog(   "房间" + roomId + "已解散");
                    break;
                case 3:
                    addLog(   "你被踢出了房间 rid:" + roomId);
                    break;
            }
        }

        @Override
        public void pushRoomMemberChange(long roomId, long userId,int type) {
            if (type == 0)
                addLog(  userId + "加入了房间" + roomId);
            else if (type == 1)
                addLog(  userId + "离开了房间" + roomId);
        }

        @Override
        public void pushRoomManagerChange(long rid, List<Long> uids, int type) {
            if (type == 0)
                addLog(   "房间" + rid + "增加了管理员" + uids.toString());
            else if (type == 1)
                addLog(   "rid:" + rid + "删除了管理员" + uids.toString());
        }

        @Override
        public void pushRoomLeaderChange(long rid, long oldLeader, long newLeader) {
            addLog(   "rid:" + rid + "房主变更old:" + oldLeader + " new:" + newLeader);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livedataimtest);
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
        findViewById(R.id.creategroup).setOnClickListener(this);
        findViewById(R.id.setgroup).setOnClickListener(this);
        findViewById(R.id.setuserinfos).setOnClickListener(this);
        findViewById(R.id.setgroupinfos).setOnClickListener(this);

        findViewById(R.id.createroom).setOnClickListener(this);
        findViewById(R.id.setroominfos).setOnClickListener(this);

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
                    switch (childPosition){
                        case 0:
                            ldEngine.IM.sendChatMessage(toUid, ConversationType.P2P, sendMsg, userattrs, new ISendMsgCallback() {
                                @Override
                                public void onSuccess(long messageTime, long messageId, String msg) {
                                    outputMsg("sendChatMessage P2P");
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
                            ldEngine.IM.sendFile(toUid,  ConversationType.P2P,FileMessageType.IMAGEFILE, imageData, imageName,jsonattrs, new ISendFileCallback() {
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
                            ldEngine.IM.sendFile(toUid, ConversationType.P2P,FileMessageType.AUDIOFILE, audioData, audioName,jsonattrs, new ISendFileCallback() {
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
                            ldEngine.IM.sendFile(toUid, ConversationType.P2P,FileMessageType.VIDEOFILE, videoData, videoName,jsonattrs,new ISendFileCallback() {
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
                            ldEngine.IM.sendFile(toUid, ConversationType.P2P,FileMessageType.NORMALFILE, normalFileData, normalFileName,jsonattrs, new ISendFileCallback() {
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
                    }
                }
                else if (groupPosition == 1){
                    switch (childPosition){
                        case 0:
                            ldEngine.IM.sendChatMessage(groupId, ConversationType.GROUP,sendMsg, userattrs,  new ISendMsgCallback() {
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
                            ldEngine.IM.sendFile(groupId, ConversationType.GROUP, FileMessageType.IMAGEFILE, imageData, imageName,jsonattrs,new ISendFileCallback() {
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
                            ldEngine.IM.sendFile(groupId,ConversationType.GROUP,FileMessageType.AUDIOFILE, audioData, audioName,jsonattrs,  new ISendFileCallback() {
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
                            ldEngine.IM.sendFile(groupId,ConversationType.GROUP,FileMessageType.VIDEOFILE, videoData, videoName,jsonattrs,  new ISendFileCallback() {
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
                            ldEngine.IM.sendFile(groupId,ConversationType.GROUP, FileMessageType.NORMALFILE, normalFileData, normalFileName,jsonattrs, new ISendFileCallback() {
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
                            jsonattrs  = new JSONObject();
                            try {
                                jsonattrs.put("群组名字","呵呵");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            ldEngine.IM.joinGroup(groupId, extra, userattrs, new IEmptyCallback() {
                                @Override
                                public void onSuccess() {
                                    outputMsg("joinGroup gid:" + groupId);
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("joinGroup gid:" +groupId,answer);
                                }
                            });
                            break;
                        case 7:
                            jsonattrs  = new JSONObject();
                            try {
                                jsonattrs.put("群组名字","呵呵");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            ldEngine.IM.inviteIntoGroup(groupId, testUids, "请加入", userattrs, new IEmptyCallback() {
                                @Override
                                public void onSuccess() {
                                    outputMsg("inviteIntoGroup");
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("inviteIntoGroup",answer);
                                }
                            });
                            break;
                        case 8:
//                            testUids = new ArrayList<Long>(){{add(200L);}};
//                            testUids = new ArrayList<Long>(){{add(98769L);}};
//                            testUids = new ArrayList<Long>(){{add(200L);add(9876L);}};
//                            testUids = new ArrayList<Long>(){{add(2001L);add(556677L);}};

                            ldEngine.IM.removeGroupMembers(groupId, testUids, new IEmptyCallback() {
                                @Override
                                public void onSuccess() {
                                    outputMsg("removeGroupMembers");
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("removeGroupMembers",answer);
                                }
                            });
                            break;
                        case 9:
                            ldEngine.IM.addGroupManagers(groupId, testUids, new IEmptyCallback() {
                                @Override
                                public void onSuccess() {
                                    outputMsg("addGroupManagers");
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("addGroupManagers",answer);
                                }
                            });
                            break;
                        case 10:
                            ldEngine.IM.removeGroupManagers(groupId, testUids, new IEmptyCallback() {
                                @Override
                                public void onSuccess() {
                                    outputMsg("removeGroupManagers");
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("removeGroupManagers",answer);
                                }
                            });
                            break;
                        case 11:
                            ldEngine.IM.changeGroupLeader(groupId, toUid, new IEmptyCallback() {
                                @Override
                                public void onSuccess() {
                                    outputMsg("changeGroupLeader");
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("changeGroupLeader",answer);
                                }
                            });
                            break;
                        case 12:
                            ldEngine.IM.leaveGroup(groupId, new IEmptyCallback() {
                                @Override
                                public void onSuccess() {
                                    outputMsg("leaveGroup");
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("leaveGroup",answer);
                                }
                            });
                            break;
                        case 13:
                            ldEngine.IM.dismissGroup(groupId, new IEmptyCallback() {
                                @Override
                                public void onSuccess() {
                                    outputMsg("dismissGroup");
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("dismissGroup",answer);
                                }
                            });
                            break;
                        case 14:
                            final  String[] values = {"允许任何人添加", "需要同意", "禁止任何人"};
                            checkboxSelect("设置群组加入权限", values, new CheckboxSelect() {
                                @Override
                                public void onConfirm(int index) {
                                    Quest quest = new Quest("imclient_setgroupinfos");
                                    quest.param("applyGrant",index);
                                    quest.param("gid",groupId);
                                    ldEngine.IM.getEngineClient().sendQuest(quest, new FunctionalAnswerCallback() {
                                        @Override
                                        public void onAnswer(Answer answer, int errorCode) {
                                            if (errorCode == 0){
                                                outputMsg("设置群组加入权限" + values[index]);
                                            }else{
                                                outputMsg("设置群组加入权限" + values[index], ldEngine.IM.getEngineClient().genLDAnswer(answer,errorCode));
                                            }
                                        }
                                    });
                                }

                                @Override
                                public void onCancel() {

                                }
                            });
                            break;
                        case 15:
                            final  String[] valuesGroup = {"允许普通成员邀请", "不允许普通成员邀请"};
                            checkboxSelect("设置群组邀请权限", valuesGroup, new CheckboxSelect() {
                                @Override
                                public void onConfirm(int index) {
                                    Quest quest = new Quest("imclient_setgroupinfos");
                                    quest.param("name","天下会");
                                    quest.param("gid",groupId);
                                    quest.param("inviteGrant",index);
                                    ldEngine.IM.getEngineClient().sendQuest(quest, new FunctionalAnswerCallback() {
                                        @Override
                                        public void onAnswer(Answer answer, int errorCode) {
                                            if (errorCode == 0){
                                                outputMsg("imclient_setgroupinfos" + valuesGroup[index]);
                                            }else{
                                                outputMsg("imclient_setgroupinfos" + valuesGroup[index], ldEngine.IM.getEngineClient().genLDAnswer(answer,errorCode));
                                            }
                                        }
                                    });
                                }

                                @Override
                                public void onCancel() {

                                }
                            });
                            break;
                        case 16:
                            ldEngine.IM.getGroupInfos(groupids, new ICallback<List<IMGroupInfo>>() {
                                @Override
                                public void onSuccess(List<IMGroupInfo> imGroupInfos) {
                                    for (IMGroupInfo imGroupInfo: imGroupInfos){
                                        outputMsg("getGroupInfos-" +imGroupInfo.getinfo());
                                    }
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("getGroupInfos",answer);
                                }
                            });
                            break;
                        case 17:

                            break;
                        case 18:

                            break;
                        case 19:
                            ldEngine.IM.getGroupMembersCount(groupId, new ICallback<Integer>() {
                                @Override
                                public void onSuccess(Integer count) {
                                    outputMsg("getGroupMembersCount" + groupId + " count:"+count);
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("getGroupMembersCount" + groupId,answer);
                                }
                            });
                            break;
                        case 20:
                            ldEngine.IM.getGroupMembers(groupId, new ICallback<List<IMGroupMemberInfo>>() {
                                @Override
                                public void onSuccess(List<IMGroupMemberInfo> imGroupMemberInfos) {
                                    String msg = "";
                                    for(IMGroupMemberInfo imGroupMemberInfo: imGroupMemberInfos){
                                        String tt = "uid:"+ imGroupMemberInfo.userId + " role:" +imGroupMemberInfo.roleType + " isonline:"+imGroupMemberInfo.isOnLine+"\n";
                                        msg += tt;
                                    }
                                    outputMsg("getGroupMembers gid:"+ groupId + " " + msg);
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("getGroupMembers gid:"+groupId, answer);
                                }
                            });
                            break;
                        case 21:
                            ldEngine.IM.getGroupdApplyList(groupId, new ICallback<List<IMApplyInfo>>() {
                                @Override
                                public void onSuccess(List<IMApplyInfo> imApplyInfos) {
                                    String showmsg = "";
                                    for (IMApplyInfo imApplyInfo:imApplyInfos){
                                        String msg = "applyUid:" +imApplyInfo.fromId + " apply时间:"+imApplyInfo.requestTime +
                                                " 留言:" + imApplyInfo.grantExtra + " 附加信息:" + imApplyInfo.attrs + "\n";
                                        showmsg += msg;
                                    }
                                    outputMsg("getGroupdApplyList"+ groupId + " "+ showmsg);
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("getGroupdApplyList",answer);
                                }
                            });
                            break;
                        case 22:
                            ldEngine.IM.getGroupRequestList(new ICallback<List<IMRquestInfo>>() {
                                @Override
                                public void onSuccess(List<IMRquestInfo> imRquestInfos) {
                                    String showmsg = "";
                                    for (IMRquestInfo imRquestInfo:imRquestInfos){
                                        String msg = "targetId:" +imRquestInfo.targetId + " request时间:"+imRquestInfo.applyTime +
                                                " 附加信息:" + imRquestInfo.attrs + "\n";
                                        showmsg += msg;
                                    }
                                    outputMsg("getGroupRequestList" + groupId + " " + showmsg);
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("getGroupRequestList", answer);
                                }
                            });
                            break;
                        case 23:
                            ldEngine.IM.getInviteGroupList(new ICallback<List<IMInviteInfo>>() {
                                @Override
                                public void onSuccess(List<IMInviteInfo> imInviteInfos) {
                                    String showmsg = "";
                                    for (IMInviteInfo imInviteInfo:imInviteInfos){
                                        String msg = "targetGroupId:" +imInviteInfo.targetId + " inviteTime:"+imInviteInfo.inviteTime +
                                                " fromId:" + imInviteInfo.fromId + " attrs:" +imInviteInfo.attrs +  "\n";
                                        showmsg += msg;
                                    }
                                    outputMsg("getInviteGroupList" + groupId + " " + showmsg);
                                }

                                @Override
                                public void onError(LDAnswer answer) {

                                }
                            });
                            break;
                        case 24:
                            ldEngine.IM.getUserGroups(new ICallback<List<Long>>() {
                                @Override
                                public void onSuccess(List<Long> longs) {
                                    outputMsg("getUserGroups"  + longs.toString());
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("getUserGroups" ,answer);
                                }
                            });
                            break;
                    }
                }

                else if (groupPosition == 2){
                    switch (childPosition){
                        case 0:
                            final  String[] values = {"允许任何人添加", "需要同意", "禁止任何人"};
                            checkboxSelect("设置好友被加权限", values, new CheckboxSelect() {
                                @Override
                                public void onConfirm(int index) {
                                    Quest quest = new Quest("imclient_setuserinfos");
                                    quest.param("applyGrant",index);
                                    ldEngine.IM.getEngineClient().sendQuest(quest, new FunctionalAnswerCallback() {
                                        @Override
                                        public void onAnswer(Answer answer, int errorCode) {
                                            if (errorCode == 0){
                                                outputMsg("setFriendApplyGrant" + values[index]);
                                            }else{
                                                outputMsg("setFriendApplyGrant" + values[index], ldEngine.IM.getEngineClient().genLDAnswer(answer,errorCode));
                                            }
                                        }
                                    });
                                }

                                @Override
                                public void onCancel() {

                                }
                            });
                            break;
                        case 1:
                            ldEngine.IM.addFriend(toUid, "你好啊", jsonattrs.toString(), new IEmptyCallback() {
                                @Override
                                public void onSuccess() {
                                    outputMsg("addFriend");
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("addFriend", answer);
                                }
                            });
                            break;
                        case 2:
                            ldEngine.IM.deleteFriend(testUids, new IEmptyCallback() {
                                @Override
                                public void onSuccess() {
                                    outputMsg("deleteFriend " + toUid);
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("deleteFriend", answer);
                                }
                            });
                            break;
                        case 3:
                            ldEngine.IM.getFriendList(new ICallback<List<Long>>() {
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
                        case 4:
                            ldEngine.IM.addBlacklist(testUids, new IEmptyCallback() {
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
                        case 5:
                            ldEngine.IM.delBlacklist(testUids, new IEmptyCallback() {
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
                        case 6:
                            ldEngine.IM.getBlacklist(new ICallback<List<Long>>() {
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
                        case 7:
                            ldEngine.IM.getFriendApplyList(new ICallback<List<IMApplyInfo>>() {
                                @Override
                                public void onSuccess(List<IMApplyInfo> imApplyInfos) {
                                    String showmsg = "";
                                    for (IMApplyInfo imApplyInfo:imApplyInfos){
                                        String msg = "applyUid:" +imApplyInfo.fromId + " apply时间:"+imApplyInfo.requestTime +
                                                " 留言:" + imApplyInfo.grantExtra + " 附加信息:" + imApplyInfo.attrs + "\n";
                                        showmsg += msg;
                                    }
                                    outputMsg("getFriendApplyList" + showmsg);
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("getFriendApplyList", answer);
                                }
                            });
                            break;
                        case 8:
                            ldEngine.IM.getFriendRequestList(new ICallback<List<IMRquestInfo>>() {
                                @Override
                                public void onSuccess(List<IMRquestInfo> imRquestInfos) {
                                    String showmsg = "";
                                    for (IMRquestInfo imRquestInfo:imRquestInfos){
                                        String msg = "targetId:" +imRquestInfo.targetId + " request时间:"+imRquestInfo.applyTime +
                                                " 附加信息:" + imRquestInfo.attrs + "\n";
                                        showmsg += msg;
                                    }
                                    outputMsg("getFriendRequestList" + showmsg);
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("getFriendRequestList", answer);
                                }
                            });
                            break;
                        case 9:
                            ldEngine.IM.getUserInfos(testUids, new ICallback<List<IMUserInfo>>() {
                                @Override
                                public void onSuccess(List<IMUserInfo> imUserInfos) {
                                    String showmsg = "";
                                    for (IMUserInfo imUserInfo:imUserInfos){
                                        String msg = "userId:" +imUserInfo.userId + " name:"+imUserInfo.name +
                                                " portraitUrl:" + imUserInfo.portraitUrl + " profile:"+imUserInfo.profile+
                                                " customData:"+ imUserInfo.customData + "\n";
                                        showmsg += msg;
                                    }
                                    outputMsg("getUserInfos" + showmsg);
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("getUserInfos",answer);
                                }
                            });
                            break;
                    }
                }
                else if (groupPosition == 3){
                    switch (childPosition){
                        case 0:
                            ldEngine.IM.getAllConversation(0, ConversationType.P2P, new ICallback<List<IMConversationInfo>>() {
                                @Override
                                public void onSuccess(List<IMConversationInfo> imConversationInfos) {
                                    String showmsg = "";
                                    for (IMConversationInfo imConversationInfo: imConversationInfos){
                                        String msg = "uid:" +imConversationInfo.targetId + " 未读条数:"+imConversationInfo.unreadNum +
                                                " lastmsg:" +imConversationInfo.lastHistortMessage.getInfo() + "\n";
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
                            ldEngine.IM.getAllConversation(0, ConversationType.GROUP, new ICallback<List<IMConversationInfo>>() {
                                @Override
                                public void onSuccess(List<IMConversationInfo> imConversationInfos) {
                                    String showmsg = "";
                                    for (IMConversationInfo imConversationInfo: imConversationInfos){
                                        String msg = "uid:" +imConversationInfo.targetId + " 未读条数:"+imConversationInfo.unreadNum +
                                                " lastmsg:" +imConversationInfo.lastHistortMessage.getInfo() + "\n";
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
                            ldEngine.IM.getAllUnreadConversation(true, 0, new ICallback<IMUnreadConversationInfo>() {
                                @Override
                                public void onSuccess(IMUnreadConversationInfo imUnreadConversationInfos) {
                                    String showmsg = "";
                                    for (IMConversationInfo imConversationInfo: imUnreadConversationInfos.groupUnread){
                                        String msg = "groupId:" +imConversationInfo.targetId + " 未读条数:"+imConversationInfo.unreadNum +
                                                " lastmsg:" +imConversationInfo.lastHistortMessage.getInfo() + "\n";
                                        showmsg += msg;
                                    }
                                    for (IMConversationInfo imConversationInfo: imUnreadConversationInfos.p2pUnread){
                                        String msg = "uid:" +imConversationInfo.targetId + " 未读条数:"+imConversationInfo.unreadNum +
                                                " lastmsg:" +imConversationInfo.lastHistortMessage.getInfo() + "\n";
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
                            ldEngine.IM.getMessage( ldEngine.getUid(), toUid, ConversationType.P2P, 12345678, new ICallback<IMSingleMessage>() {
                                @Override
                                public void onSuccess(IMSingleMessage imSingleMessage) {
                                    outputMsg("getMessage" + imSingleMessage.getInfo());
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("getMessage" ,answer);
                                }
                            });
                            break;
                        case 4:
                            ldEngine.IM.deleteMessage(ldEngine.getUid(), toUid,  ConversationType.P2P,12345678, new IEmptyCallback() {
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
                            ldEngine.IM.getHistoryChat(toUid, ConversationType.P2P, true, 10, 0, 0, 0, new ICallback<IMHistoryMessageResult>() {
                                @Override
                                public void onSuccess(IMHistoryMessageResult imHistoryMessageResult) {
                                    outputMsg("getHistoryChatMessage count:" + imHistoryMessageResult.count + " beginMsec:" + imHistoryMessageResult.beginMsec
                                    + " endMsec:"+ imHistoryMessageResult.endMsec + " lastcurid:" + imHistoryMessageResult.lastId);

                                    if (imHistoryMessageResult.count>0 && imHistoryMessageResult.messages!=null) {
                                        for (IMHistoryMessage hm : imHistoryMessageResult.messages) {
                                            addLog(hm.getInfo());
                                        }
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

                else if (groupPosition == 4){
                    switch (childPosition){
                        case 0:
                            ldEngine.IM.addDevice("123456789", new IEmptyCallback() {
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
                            ldEngine.IM.removeDevice("123456789", new IEmptyCallback() {
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
                            ldEngine.IM.addDevicePushOption(0, toUid, null, new IEmptyCallback() {
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
                            ldEngine.IM.removeDevicePushOption(0, toUid, null, new IEmptyCallback() {
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
                            ldEngine.IM.getDevicePushOption(new ICallback<DevicePushOption>() {
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
                    }
                }
                else if (groupPosition == 5){
                    switch (childPosition){
                        case 0:
                            ldEngine.IM.sendChatMessage(roomId, ConversationType.ROOM,sendMsg, userattrs,  new ISendMsgCallback() {
                                @Override
                                public void onSuccess(long messageTime, long messageId,String msg) {
                                    outputMsg("sendChatMessage Room");
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("sendChatMessage Room",answer);
                                }
                            });
                            break;
                        case 1:
                            break;
                        case 2:
                            ldEngine.IM.sendFile(roomId, ConversationType.ROOM, FileMessageType.IMAGEFILE, imageData, imageName,jsonattrs,new ISendFileCallback() {
                                @Override
                                public void onSuccess(long aLong, long aLong2) {
                                    outputMsg("sendFile Room image");
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("sendFile Room image",answer);
                                }
                            });
                            break;
                        case 3:
                            ldEngine.IM.sendFile(roomId,ConversationType.ROOM,FileMessageType.AUDIOFILE, audioData, audioName,jsonattrs,  new ISendFileCallback() {
                                @Override
                                public void onSuccess(long aLong, long aLong2) {
                                    outputMsg("sendFile Room audio");
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("sendFile Room audio",answer);
                                }
                            });
                            break;
                        case 4:
                            ldEngine.IM.sendFile(roomId,ConversationType.ROOM,FileMessageType.VIDEOFILE, videoData, videoName,jsonattrs,  new ISendFileCallback() {
                                @Override
                                public void onSuccess(long aLong, long aLong2) {
                                    outputMsg("sendFile Room video");
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("sendFile Room video",answer);
                                }
                            });
                            break;
                        case 5:
                            ldEngine.IM.sendFile(roomId,ConversationType.ROOM, FileMessageType.NORMALFILE, normalFileData, normalFileName,jsonattrs, new ISendFileCallback() {
                                @Override
                                public void onSuccess(long aLong, long aLong2) {
                                    outputMsg("sendFile Room normal");
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("sendFile Room normal",answer);
                                }
                            });
                            break;

                        case 6:
                            jsonattrs  = new JSONObject();
                            try {
                                jsonattrs.put("房间名字","呵呵");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            ldEngine.IM.enterRoom(roomId,extra, userattrs, new IEmptyCallback() {
                                @Override
                                public void onSuccess() {
                                    outputMsg("enterRoom rid:" + roomId);
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("enterRoom rid:" +roomId,answer);
                                }
                            });
                            break;
                        case 7:
                            jsonattrs  = new JSONObject();
                            try {
                                jsonattrs.put("房间名字","呵呵");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            ldEngine.IM.inviteIntoRoom(roomId, testUids, "请加入", userattrs, new IEmptyCallback() {
                                @Override
                                public void onSuccess() {
                                    outputMsg("inviteIntoRoom");
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("inviteIntoRoom",answer);
                                }
                            });
                            break;
                        case 8:
                            ldEngine.IM.removeRoomMembers(roomId, testUids, new IEmptyCallback() {
                                @Override
                                public void onSuccess() {
                                    outputMsg("removeRoomMembers");
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("removeRoomMembers",answer);
                                }
                            });
                            break;
                        case 9:
                            ldEngine.IM.addRoomManagers(roomId, testUids, new IEmptyCallback() {
                                @Override
                                public void onSuccess() {
                                    outputMsg("addRoomManagers rid:"+ roomId);
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("addRoomManagers rid:"+ roomId,answer);
                                }
                            });
                            break;
                        case 10:
                            ldEngine.IM.removeRoomManagers(roomId, testUids, new IEmptyCallback() {
                                @Override
                                public void onSuccess() {
                                    outputMsg("removeRoomManagers rid:"+ roomId);
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("removeRoomManagers rid:"+ roomId,answer);
                                }
                            });
                            break;
                        case 11:
                            ldEngine.IM.changeRoomLeader(roomId, toUid, new IEmptyCallback() {
                                @Override
                                public void onSuccess() {
                                    outputMsg("changeRoomLeader rid:"+ roomId);
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("changeRoomLeader rid:"+ roomId,answer);
                                }
                            });
                            break;
                        case 12:
                            ldEngine.IM.leaveRoom(roomId, new IEmptyCallback() {
                                @Override
                                public void onSuccess() {
                                    outputMsg("leaveRoom rid:"+ roomId);
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("leaveRoom rid:"+ roomId,answer);
                                }
                            });
                            break;
                        case 13:
                            ldEngine.IM.dismissRoom(roomId, new IEmptyCallback() {
                                @Override
                                public void onSuccess() {
                                    outputMsg("dismissRoom rid:"+ roomId);
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("dismissRoom rid:"+ roomId,answer);
                                }
                            });
                            break;
                        case 14:
                            final  String[] valuesRoom = {"允许普通成员邀请", "不允许普通成员邀请"};
                            checkboxSelect("设置房间邀请权限", valuesRoom, new CheckboxSelect() {
                                @Override
                                public void onConfirm(int index) {
                                    Quest quest = new Quest("imclient_setroominfos");
                                    quest.param("rid",roomId);
                                    quest.param("inviteGrant",index);
                                    ldEngine.IM.getEngineClient().sendQuest(quest, new FunctionalAnswerCallback() {
                                        @Override
                                        public void onAnswer(Answer answer, int errorCode) {
                                            if (errorCode == 0){
                                                outputMsg("imclient_setroominfos" + valuesRoom[index]);
                                            }else{
                                                outputMsg("imclient_setroominfos" + valuesRoom[index], ldEngine.IM.getEngineClient().genLDAnswer(answer,errorCode));
                                            }
                                        }
                                    });
                                }

                                @Override
                                public void onCancel() {

                                }
                            });
                            break;
                        case 15:
                            ldEngine.IM.getRoomInfos(roomids, new ICallback<List<IMRoomInfo>>() {
                                @Override
                                public void onSuccess(List<IMRoomInfo> imRoomInfos) {
                                    for (IMRoomInfo imRoomInfo: imRoomInfos){
                                        outputMsg("getRoomInfos-" +imRoomInfo.getinfo());
                                        try {
                                            JSONObject jj = new JSONObject(imRoomInfo.customData);
                                            Log.i("sdktest",jj.toString());
                                        }
                                        catch (Exception e){

                                        }

                                    }
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("getRoomInfos",answer);
                                }
                            });
                            break;
/*                        case 16:
                                ldEngine.IM.addRoommemberBan(roomId, toUid, 60, new IEmptyCallback() {
                                    @Override
                                    public void onSuccess() {
                                        outputMsg("addRoommemberBan rid:"+ roomId);
                                    }

                                    @Override
                                    public void onError(LDAnswer answer) {
                                        outputMsg("addRoommemberBan rid:"+ roomId,answer);
                                    }
                                });
                            break;
                        case 17:
                            ldEngine.IM.removeRoomMemberBan(roomId, toUid, new IEmptyCallback() {
                                @Override
                                public void onSuccess() {
                                    outputMsg("removeRoomMemberBan rid:"+ roomId);
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("removeRoomMemberBan rid:"+ roomId,answer);
                                }
                            });
                            break;*/
                        case 18:
                            ldEngine.IM.getRoomMembersCount(roomId, new ICallback<Integer>() {
                                @Override
                                public void onSuccess(Integer count) {
                                    outputMsg("getRoomMembersCount" + roomId + " count:"+count);
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("getRoomMembersCount" + roomId,answer);
                                }
                            });
                            break;
                        case 19:
                            ldEngine.IM.getRoomMembers(roomId, new ICallback<List<IMRoomMemberInfo>>() {
                                @Override
                                public void onSuccess(List<IMRoomMemberInfo> imRoomMemberInfos) {
                                    String msg = "";
                                    for(IMRoomMemberInfo imRoomMemberInfo: imRoomMemberInfos){
                                        String tt = "uid:"+ imRoomMemberInfo.userId + " role:" +imRoomMemberInfo.roleType +"\n";
                                        msg += tt;
                                    }
                                    outputMsg("getRoomMembers rid:"+ roomId + " " + msg);
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("getRoomMembers rid:"+roomId, answer);
                                }
                            });
                            break;
                        case 20:
                            ldEngine.IM.getRoomInviteList(new ICallback<List<IMInviteInfo>>() {
                                @Override
                                public void onSuccess(List<IMInviteInfo> imInviteInfos) {
                                    String showmsg = "";
                                    for (IMInviteInfo imInviteInfo:imInviteInfos){
                                        String msg = "targetRoomId:" +imInviteInfo.targetId + " inviteTime:"+imInviteInfo.inviteTime +
                                                " fromId:" + imInviteInfo.fromId + " attrs:" +imInviteInfo.attrs +  "\n";
                                        showmsg += msg;
                                    }
                                    outputMsg("getRoomInviteList" + roomId + " " + showmsg);
                                }

                                @Override
                                public void onError(LDAnswer answer) {

                                }
                            });
                            break;
                        case 21:
                            ldEngine.IM.getUserRooms(new ICallback<List<Long>>() {
                                @Override
                                public void onSuccess(List<Long> longs) {
                                    outputMsg("getUserRooms"  + longs.toString());
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    outputMsg("getUserRooms" ,answer);
                                }
                            });
                            break;
                    }
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
            }};
        mItemSet.add(messageType);
        ArrayList<String> messageType1 = new ArrayList<String>(){{
            add("发送群组聊天");
            add("发送群组离线语音");
            add("发送群组图片");
            add("发送群组语音文件");
            add("发送群组视频文件");
            add("发送群组普通文件");
            add("申请入群");
            add("邀请成员");
            add("删除群组成员");
            add("添加群组管理员");
            add("删除群组管理员");
            add("群主转让");
            add("退出群组");
            add("解散群组");
            add("设置群组加入方式");
            add("设置群组邀请权限");
            add("获取群组信息");
            add("添加群组禁言");
            add("解除群组禁言");
            add("获取群组成员数");
            add("获取群组成员列表");
            add("获取入群申请列表");
            add("获取自己发出的申请入群列表");
            add("获取邀请入群列表");
            add("获取自己的群组");
        }};
        mItemSet.add(messageType1);

        ArrayList<String> messageType2 = new ArrayList<String>(){{
            add("设置自己被加好友权限");
            add("添加好友");
            add("删除好友");
            add("获取自己好友列表");
            add("添加黑名单");
            add("删除黑名单用户");
            add("查询黑名单");
            add("获取好友申请列表");
            add("获取自己发出的添加好友列表");
            add("获取用户信息");
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
        }};
        mItemSet.add(messageType4);


        ArrayList<String> messageType5 = new ArrayList<String>(){{
            add("发送房间聊天");
            add("发送房间离线语音");
            add("发送房间图片");
            add("发送房间语音文件");
            add("发送房间视频文件");
            add("发送房间普通文件");
            add("进入房间");
            add("邀请成员加入房间");
            add("删除房间成员");
            add("添加房间管理员");
            add("删除房间管理员");
            add("房主转让");
            add("退出房间");
            add("解散房间");
            add("设置房间邀请权限");
            add("获取房间信息");
            add("添加房间禁言");
            add("解除房间禁言");
            add("获取房间成员数");
            add("获取房间成员列表");
            add("获取邀请入群列表");
            add("获取自己的房间");
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
//            agreeOrRefuse("lala", new Agreeorrefuse() {
//                @Override
//                public void onAgree() {
//
//                }
//
//                @Override
//                public void onRefuse() {
//
//                }
//            });
//            if(true)
//                return;
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
                        ldEngine.setIMPushProcessor(iimPushProcessor);
                        utils.toast(livedata_imtest.this, utils.currentUserid + " 登录成功");
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        utils.alertDialog(livedata_imtest.this, utils.currentUserid + " 登录失败 " + answer.getErrInfo());
                    }
                });

            }
            catch (Exception e){
                e.printStackTrace();
                utils.alertDialog(livedata_imtest.this, "发生异常 " + e.getMessage());
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
        else if (v.getId() == R.id.creategroup){
            Quest quest = new Quest("imclient_creategroup");
            quest.param("owner_uid",utils.currentUserid);
            quest.param("gid",groupId);
            quest.param("apply_grant",AddPermission.PermitAll.value());
            quest.param("invite_grant", 0);
            ldEngine.IM.getEngineClient().sendQuest(quest, new FunctionalAnswerCallback() {
                @Override
                public void onAnswer(Answer answer, int errorCode) {
                    if (errorCode == 0){
                        addLog("创建群组" + groupId + "成功");
                    }else{
                        addLog("创建群组失败:" + errorCode);
                    }
                }
            });

         /*   AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("创建群组").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONArray array = new JSONArray();
                                array.put(utils.currentUserid);

                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("owner_uid", utils.currentUserid);
                                jsonObject.put("member_uids", array);
                                utils.doPost(utils.CREATE_GROUP_URL, jsonObject, new IHttpCallback() {
                                    @Override
                                    public void onSuccess(String data) throws JSONException {
                                        JSONObject ret = new JSONObject(data);
                                        long gid = ret.getLong("gid");
                                        if (gid >0){
                                            groupId = gid;
                                        }
                                        addLog("创建群组成功 " + data);
                                    }

                                    @Override
                                    public void onError(int code, String msg) {
                                        addLog("创建群组失败 " + code + " msg " + msg);
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();

                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.show();*/
        }
        else if (v.getId() == R.id.createroom){
            Quest quest = new Quest("imclient_createroom");
            quest.param("owner_uid",utils.currentUserid);
            quest.param("rid",roomId);
            quest.param("extra","");
            quest.param("invite_grant", 0);
            ldEngine.IM.getEngineClient().sendQuest(quest, new FunctionalAnswerCallback() {
                @Override
                public void onAnswer(Answer answer, int errorCode) {
                    if (errorCode == 0){
                        addLog("创建房间" + groupId + "成功");
                    }else{
                        addLog("创建房间失败:" + errorCode);
                    }
                }
            });
        }
        else if (v.getId() == R.id.setroominfos){
            Quest quest = new Quest("imclient_setroominfos");
            quest.param("name","语聊房");
            quest.param("rid",roomId);
            quest.param("portraitUrl","www.baidu.com");
            quest.param("profile","武林第一帮派");
            quest.param("inviteGrant",0);
            quest.param("applyGrant",3);
            quest.param("extra","123456");
            JSONObject tt = new JSONObject();
            try {
                tt.put("food",1000);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            quest.param("attrs", tt.toString());
            ldEngine.IM.getEngineClient().sendQuest(quest, new FunctionalAnswerCallback() {
                @Override
                public void onAnswer(Answer answer, int errorCode) {
                    if (errorCode ==0){
                        outputMsg("设置房间信息");
                    }else{
                        outputMsg("设置房间信息",ldEngine.IM.getEngineClient().genLDAnswer(answer,errorCode));
                    }
                }
            });
        }

        else if (v.getId() == R.id.setgroup){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            EditText editText = new EditText(this);
            builder.setView(editText);
            builder.setMessage("设置群组房间id").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        groupId = Long.parseLong(editText.getText().toString());
                        roomId = groupId;
                    } catch (NumberFormatException ex) {
                        addLog("设置群组房间id错误:" + ex.getMessage());
                    }
                    catch (Exception e){
                        addLog("设置群组房间id错误:" + e.getMessage());
                    }
                    groupids = new ArrayList<Long>(){{
                        add(groupId);
                    }};
                    roomids = new ArrayList<Long>(){{
                        add(roomId);
                    }};
                    addLog("设置群组房间id成功 " + groupId);
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.show();
        }
        else if (v.getId() == R.id.setuserinfos){
            Quest quest = new Quest("imclient_setuserinfos");
            quest.param("name","鸣人");
            quest.param("portraitUrl","www.baidu.com");
            quest.param("profile","忍村大哥");
            quest.param("applyGrant",0);
            JSONObject tt = new JSONObject();
            try {
                tt.put("level",100);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            quest.param("attrs", tt.toString());
            ldEngine.IM.getEngineClient().sendQuest(quest, new FunctionalAnswerCallback() {
                @Override
                public void onAnswer(Answer answer, int errorCode) {
                    if (errorCode == 0){
                        outputMsg("setuserinfos");
                    }else {
                        outputMsg("setuserinfos",ldEngine.IM.getEngineClient().genLDAnswer(answer,errorCode));
                    }
                }
            });
        }
        else if (v.getId() == R.id.setgroupinfos){
            Quest quest = new Quest("imclient_setgroupinfos");
            quest.param("name","天下会");
            quest.param("gid",groupId);
            quest.param("portraitUrl","www.baidu.com");
            quest.param("profile","武林第一帮派");
            quest.param("applyGrant",0);
            quest.param("inviteGrant",1);
            JSONObject tt = new JSONObject();
            try {
                tt.put("food",1000);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            quest.param("attrs", tt.toString());
            ldEngine.IM.getEngineClient().sendQuest(quest, new FunctionalAnswerCallback() {
                @Override
                public void onAnswer(Answer answer, int errorCode) {
                    if (errorCode ==0){
                        outputMsg("设置群组信息");
                    }else{
                        outputMsg("设置群组信息",ldEngine.IM.getEngineClient().genLDAnswer(answer,errorCode));
                    }
                }
            });
        }
    }
}