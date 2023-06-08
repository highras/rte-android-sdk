package com.LiveDataRTE.InternalEngine;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceView;

import com.LiveDataRTE.IBasePushProcessor;
import com.LiveDataRTE.IMLib.IIMPushProcessor;
import com.LiveDataRTE.IMLib.TransStruct;
import com.LiveDataRTE.LiveDataConfig;
import com.LiveDataRTE.RTCLib.IRTCPushProcessor;
import com.LiveDataRTE.RTCLib.RTCStruct.*;
import com.LiveDataRTE.LiveDataStruct.*;
import com.LiveDataRTE.RTMLib.IRTMPushProcessor;
import com.LiveDataRTE.RTMLib.RTMErrorCode;
import com.LiveDataRTE.LDInterface.*;
import com.fpnn.sdk.ClientEngine;
import com.fpnn.sdk.ConnectionWillCloseCallback;
import com.fpnn.sdk.ErrorCode;
import com.fpnn.sdk.ErrorRecorder;
import com.fpnn.sdk.FunctionalAnswerCallback;
import com.fpnn.sdk.TCPClient;
import com.fpnn.sdk.proto.Answer;
import com.fpnn.sdk.proto.MessagePayloadUnpacker;
import com.fpnn.sdk.proto.Quest;
import com.livedata.rtc.RTCEngine;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import com.LiveDataRTE.*;
import com.LiveDataRTE.RTMLib.RTMStruct.*;

class EngineCore extends BroadcastReceiver implements Application.ActivityLifecycleCallbacks{

    AudioManager.OnAudioFocusChangeListener afChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                public void onAudioFocusChange(int focusChange) {
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
//                        printLog("AudioManager.AUDIOFOCUS_LOSS_TRANSIENT");
                        RTCEngine.setVoiceStat(false);

                    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                        errorRecorder.recordError("AudioManager.AUDIOFOCUS_LOSS");
                        RTCEngine.setVoiceStat(false);

                        mAudioManager.abandonAudioFocus(afChangeListener);
                    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {

                    } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
//                        printLog("AudioManager.AUDIOFOCUS_GAIN");
                        String  ret = RTCEngine.resumeAudioFocus();
//                        printLog("AudioManager.AUDIOFOCUS_GAIN resumeAudioFocus ret " + ret);
                    }
                }
            };

    public enum ClientStatus {
        Closed,
        Connecting,
        Connected
    }

    public enum CloseType {
        ByUser,
        ByServer,
        Timeout,
        None
    }

    boolean isMicAvaliable(){
        Boolean available = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if( mAudioManager.getActiveRecordingConfigurations().size()>0)
                available = false;
        }
        else {
            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_DEFAULT, 44100);
            try {
                if (recorder.getRecordingState() != AudioRecord.RECORDSTATE_STOPPED) {
                    available = false;

                }
                recorder.startRecording();
                if (recorder.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                    recorder.stop();
                    available = false;

                }
                recorder.stop();
            } finally {
                recorder.release();
                recorder = null;
            }
        }
        if (!available)
            printLog("microphone is be used in another app");
        return available;
    }

    //for network change
    private int LAST_TYPE = NetUtils.NETWORK_NOTINIT;


    @Override
    public void onReceive(Context context, Intent intent) {
        String b= ConnectivityManager.CONNECTIVITY_ACTION;
        String a= intent.getAction();
        if ("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED".equals(a)) {
            if (!isInitRTC)
                return;
            int intExtra = intent.getIntExtra("android.bluetooth.profile.extra.STATE", Integer.MIN_VALUE);
            if (intExtra == 2 || intExtra == 0) {//2-连接 0-断开
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                        mAudioManager.setMode(AudioManager.MODE_NORMAL);
                        RTCEngine.headsetStat(intExtra);
                    }
                }, 1000L);
            }
        }
        else if (a == b || (a != null && a.equals(b))) {
            int netWorkState = NetUtils.getNetWorkState(context);
            if (LAST_TYPE != netWorkState) {
                LAST_TYPE = netWorkState;
                onNetChange(netWorkState);
            }
        }
        else if (a.equals(Intent.ACTION_HEADSET_PLUG)) {
//            if (!isInitRTC)
//                return;
            if (intent.hasExtra("state")){
                int ret = intent.getIntExtra("state", 0);
                Log.e("sdktest", "ACTION_HEADSET_PLUG " + ret);
                if (ret ==0 || ret == 1){//0-拔出 1-插入
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
//                        mAudioManager.setMode(AudioManager.MODE_NORMAL);
                            RTCEngine.headsetStat(ret);
                        }
                    }, 500L);
                }
            }
        }
    }
    //for network change

    //-------------[ Fields ]--------------------------//
    private Object interLocker =  new Object();
    private long pid;
    private long uid;
    private String lang;
    private String token;
    private long logints;
    private String rtmEndpoint;
    private Context context;
    private Application application;
    private WeakReference<Activity> currentActivity;
    private boolean background = false;
    private boolean encrptyFlag = false;
    private String rtcEndpoint = "";
    private String curve = "secp256k1";
    private byte[] encrptyData = new byte[]{48,86,48,16,6,0,42,-122,72,-50,61,2,1,
            6,5,43,-127,4,0,10,3,66,0,4,-5,43,-54,-28,37,-40,-49,25,-128,2,-58,115,51,
            -71,64,8,63,101,33,-71,-102,-27,45,-53,68,125,-20,62,50,-73,38,-94,3,118,
            -28,46,-70,-96,-60,-80,26,-38,124,-41,121,126,-23,-91,35,38,-127,-109,42,
            -52,70,-48,-115,-95,-46,63,-21,41,50,-1};
    private String endpoint;
    boolean cameraStatus = false;
    private boolean isInitRTC = false;
    public OrientationEventListener mOrEventListener;
    int currVideoLevel = CaptureLevle.MIddle.value();

    long lastCallId = 0; //p2pRTC 用
    int lastP2Ptype = 0; //1-音频 2-视频
    long peerUid = 0; //p2p对方uid

    private Map<String, String> loginAttrs;
    private ClientStatus rtmGateStatus = ClientStatus.Closed;
    private CloseType closedCase = CloseType.None;
    private int lastNetType = NetUtils.NETWORK_NOTINIT;
    private AtomicBoolean isRelogin = new AtomicBoolean(false);
    private AtomicBoolean running = new AtomicBoolean(true);
    private AtomicBoolean initCheckThread = new AtomicBoolean(false);
    private Thread checkThread;
    private EngineQuestProcessor processor;
    ErrorRecorder errorRecorder = new ErrorRecorder();
    private TCPClient rtmGate;
    private Map<String, Map<TCPClient, Long>> fileGates;
    private AtomicLong connectionId = new AtomicLong(0);
    private AtomicBoolean noNetWorkNotify = new AtomicBoolean(false);
    private LDAnswer lastReloginAnswer = new LDAnswer();
    private IBasePushProcessor basePushProcessor;
    private IRTMPushProcessor rtmPushProcessor = null;
    private IIMPushProcessor imPushProcessor = null;
    private IRTCPushProcessor rtcPushProcessor = null;
    LiveDataConfig rtmConfig;
    EngineUtils LDUtils = new EngineUtils();

    int okRet = RTMErrorCode.RTM_EC_OK.value();
    int unknownErr = RTMErrorCode.RTM_EC_UNKNOWN_ERROR.value();
    int videoError = RTMErrorCode.RTM_EC_VIDEO_ERROR.value();
    int voiceError = RTMErrorCode.RTM_EC_VOICE_ERROR.value();

    //voice
    //video
    public enum RTMModel{
        Normal,
        VOICE,
        VIDEO
    }


    private void setBackground(boolean flag){
        new Thread(new Runnable() {
            @Override
            public void run() {
                RTCEngine.setBackground(flag);
            }
        }).start();
    }

    private ArrayList<Integer> finishCodeList = new ArrayList<Integer>(){{
        add(RTMErrorCode.RTM_EC_INVALID_AUTH_TOEKN.value());
        add(RTMErrorCode.RTM_EC_PROJECT_BLACKUSER.value()); }};

    //voice
//    private int mFinalCount = 0;
    @Override
    public void onActivityCreated( Activity activity,  Bundle bundle) {
//        printLog("onActivityCreated");
    }

    @Override
    public void onActivityStarted( Activity activity) {
//        Log.i("sdktest","onActivityStarted " + activity.getLocalClassName() );
        this.currentActivity = new WeakReference<Activity>(activity);
        if (this.background && !activity.isChangingConfigurations()) {
            this.background = false;
            if (rtmGateStatus == ClientStatus.Closed){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        reloginEvent(1);
                    }
                }).start();
            }
            if (isInitRTC)
                setBackground(false);
        }
    }

    @Override
    public void onActivityResumed( Activity activity) {
    }

    @Override
    public void onActivityPaused( Activity activity) {
    }

    @Override
    public void onActivityStopped( Activity activity) {
//        Log.i("sdktest","onActivityStopped " + activity.getLocalClassName() );
        if (!this.background && (this.currentActivity == null || activity == this.currentActivity.get()) && !activity.isChangingConfigurations()) {
            this.background = true;
            if (isInitRTC)
                setBackground(true);
        }
    }



    @Override
    public void onActivitySaveInstanceState( Activity activity,  Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed( Activity activity) {
    }

    public  static AudioManager mAudioManager;
    //    AtomicBoolean pause = new AtomicBoolean(false);
    Object videoLocker =  new Object();
    private int voiceConnectionId = 0;

    public void enterRTCRoom(ICallback<RTCRoomInfo> callback, long roomId, String lang) {
        LDAnswer answer = initRTC();
        if (answer.errorCode != okRet){
            callback.onError(answer);
            return;
        }

        if (lastCallId > 0){
            callback.onError(genLDAnswer(voiceError, "in p2pRTC type"));
            return;
        }

        Quest quest = new Quest("enterRTCRoom");
        quest.param("rid", roomId);
        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                RTCRoomInfo rtcRoomInfo = new RTCRoomInfo();
                if (errorCode == okRet) {
                    String roomtoken = LDUtils.wantString(answer,"token");
                    rtcRoomInfo.roomType = RTCRoomType.intToEnum(LDUtils.wantInt(answer,"type"));
                    if (rtcRoomInfo.roomType == RTCRoomType.VIDEO || rtcRoomInfo.roomType == RTCRoomType.TRANSLATE){ //视频房间或者带翻译的房间
                        if (RTCEngine.isInRTCRoom() > 0){
                            callback.onError(genLDAnswer(voiceError, "enter video Room error you are in rtcroom-" + RTCEngine.isInRTCRoom() ));
                            return;
                        }
                    }
                    enterRTCRoomReal(callback, roomId, roomtoken, rtcRoomInfo, rtcRoomInfo.roomType, lang);
                } else {
                    callback.onError(genLDAnswer(answer));
                }
            }
        });
    }

    public void createRTCRoom(long roomId, RTCRoomType roomType, int enableRecord, String language, IEmptyCallback callback) {
        LDAnswer tanswer = initRTC();
        if (tanswer.errorCode != okRet){
            callback.onError(tanswer);
            return;
        }

        if (lastCallId > 0){
            callback.onError(genLDAnswer(voiceError, "createRTCRoom error , already in p2pRTC type"));
            return;
        }

        if ((roomType == RTCRoomType.VIDEO || roomType == RTCRoomType.TRANSLATE) && RTCEngine.isInRTCRoom() > 0){
            callback.onError(genLDAnswer(voiceError, "createRTCRoom error, you are in rtcroom-" + RTCEngine.isInRTCRoom()));
            return;
        }

        Quest quest = new Quest("createRTCRoom");
        quest.param("rid", roomId);
        quest.param("type", roomType.value());
        quest.param("enableRecord", enableRecord);
        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    String bringlang = language;
                    if (bringlang == null)
                        bringlang = "";
                    String roomToken = LDUtils.wantString(answer,"token");
                    enterRTCRoomReal(new ICallback() {
                        @Override
                        public void onSuccess(Object o) {
                            callback.onSuccess();
                        }

                        @Override
                        public void onError(LDAnswer answer) {
                            callback.onError(answer);
                        }
                    }, roomId, roomToken, new RTCRoomInfo(), roomType, bringlang);
                }
                else {
                    callback.onError(genLDAnswer(answer, errorCode));
                }
            }
        });
    }

     RTMHistoryMessage parseRTMHistoryMessage(long targetId, ConversationType type, List<Object> value){
        RTMHistoryMessage tmp = new RTMHistoryMessage();
        tmp.cursorId = LDUtils.wantLong(value.get(0));
        tmp.fromUid = LDUtils.wantLong(value.get(1));

        if (type == ConversationType.P2P) {
            if (tmp.fromUid == 1) {
                tmp.fromUid = getUid();
                tmp.targetId = targetId;
            } else {
                tmp.fromUid = targetId;
                tmp.targetId = getUid();
            }
        }
        tmp.messageType = (byte) LDUtils.wantInt(value.get(2));
        tmp.messageId = LDUtils.wantLong(value.get(3));
        Object obj = value.get(5);
        tmp.attrs = String.valueOf(value.get(6));
        tmp.modifiedTime = LDUtils.wantLong(value.get(7));
        try {
            if (tmp.messageType >= MessageType.IMAGEFILE && tmp.messageType <= MessageType.NORMALFILE) {
                String msg = String.valueOf(obj);
                tmp.fileInfo = new FileStruct();
                tmp.attrs = parseFileInfo(msg,tmp.attrs,tmp.messageType,tmp.fileInfo);
            } else {
                if (obj instanceof byte[])
                    tmp.binaryMessage = (byte[]) obj;
                else
                    tmp.stringMessage = String.valueOf(obj);
            }
        } catch (Exception ex) {
            errorRecorder.recordError("parseRTMHistoryMessage parse json failed " + ex.getMessage());
        }
        return tmp;
    }

    String parseFileInfo(String msg, String attrs, int fileType, FileStruct fileInfo){
        String realattrs = "";
        try {
            JSONObject attrsJson = new JSONObject(attrs);
            JSONObject filemsgJson = new JSONObject(msg);

            fileInfo.url = filemsgJson.optString("url");
            fileInfo.fileMessageType = FileMessageType.intToEnum(fileType);
            fileInfo.fileSize = filemsgJson.getLong("size");
            if (filemsgJson.has("surl"))
                fileInfo.surl = filemsgJson.optString("surl");

            if (attrsJson.has("rtm")) {
                JSONObject rtmjson = attrsJson.getJSONObject("rtm");
                fileInfo.fileName = rtmjson.optString("filename");
                if (rtmjson.has("type") && rtmjson.getString("type").equals("audiomsg")) {//rtm语音消息
                    fileInfo.lang = rtmjson.optString("lang");
                    fileInfo.duration = rtmjson.optInt("duration");
                }
            }
            if (attrsJson.has("custom")) {
                JSONObject custtomObject = attrsJson.getJSONObject("custom");
                realattrs = custtomObject.toString();
            }
        }
        catch (Exception e){
            errorRecorder.recordError("parseFileInfo json failed " + e.getMessage());
        }
        return realattrs;
    }


    void enterRTCRoomReal(ICallback callback, long roomId, String token, RTCRoomInfo ret, RTCRoomType roomType, String lang) {
        byte[] enterRet = RTCEngine.enterRTCRoom(token, roomId, roomType.value(), "", 0, lang);
        MessagePayloadUnpacker kk = new MessagePayloadUnpacker(enterRet);
        HashMap retmap;
        LDAnswer rst = new LDAnswer(0,"");
        try {
            retmap= new HashMap(kk.unpack());
            if (retmap.containsKey("ex")){
                rst.errorCode = LDUtils.wantInt(retmap.get("code"));
                rst.errorMsg = String.valueOf(retmap.get("ex"));
                callback.onError(rst);
            }
            else
            {
                if (roomType == RTCRoomType.VIDEO && mOrEventListener!=null){ //视频房间
                    mOrEventListener.enable();
                }
                ret.uids = LDUtils.longList(retmap.get("uids"));
                ret.owner = LDUtils.wantLong(retmap.get("owner"));
                ret.managers = LDUtils.longList(retmap.get("administrators"));
                callback.onSuccess(ret);
            }
        }
        catch (Exception e) {
            errorRecorder.recordError("Decoding enterRTCRoomReal package exception");
        }
    }

    private void getRTCgate(){
        Quest quest = new Quest("getRTCGateEndpoint");
        rtmGate.sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == 0){
                    rtcEndpoint = LDUtils.wantString(answer, "endpoint");
                }else{
                    Log.e("livedataRTE","getRTCGateEndpoint error: " + errorCode);
                }
            }
        });
    }

    class EngineQuestProcessor{
        private DuplicatedMessageFilter duplicatedFilter;
        private AtomicLong lastPingTime;

        public EngineQuestProcessor() {
            duplicatedFilter = new DuplicatedMessageFilter();
            lastPingTime = new AtomicLong();
        }

        synchronized void setLastPingTime(long time){
            lastPingTime.set(time);
        }

        synchronized long getLastPingTime(){
            return lastPingTime.get();
        }

        boolean ConnectionIsAlive() {
            long lastPingSec = lastPingTime.get();
            boolean ret = true;

            if (Genid.getCurrentSeconds() - lastPingSec > LiveDataConfig.lostConnectionAfterLastPingInSeconds) {
                ret = false;
            }
            return ret;
        }


        void rtmConnectClose() {
            basePushProcessor.rtmConnectClose(uid);
        }

        class MessageInfo {
            public boolean isBinary;
            public byte[] binaryData;
            public String message;

            MessageInfo() {
                isBinary = false;
                message = "";
                binaryData = null;
            }
        }

        private boolean checkRTCPushProcessor(){
            if (rtcPushProcessor == null){
                printLog("rtcpushprocessor is null");
                return false;
            }
            return true;
        }
        //----------------------[ RTM Messagess Utilities ]-------------------//
        private TranslatedInfo processChatMessage(Quest quest) {
            Object ret = quest.want("msg");
            Map<String, String> msg = new HashMap<>((Map<String, String>) ret);
            TranslatedInfo tm = new TranslatedInfo();
            tm.source = msg.get("source");
            tm.target = msg.get("target");
            tm.sourceText = msg.get("sourceText");
            tm.targetText = msg.get("targetText");
            return tm;
        }

        private MessageInfo BuildMessageInfo(Quest quest) {
            MessageInfo info = new MessageInfo();

            Object obj = quest.want("msg");
            if (obj instanceof byte[]) {
                info.isBinary = true;
                info.binaryData = (byte[]) obj;
            } else
                info.message = (String) obj;

            return info;
        }

        private void processNotification(String attrs, String msg){
            try {
                JSONObject jsonObject = new JSONObject(attrs);
                String custom = jsonObject.optString("custom");
                int type = jsonObject.getInt("type");
                long fromUid = jsonObject.optLong("from");
                long userId = jsonObject.optLong("userId");
                int changeType = jsonObject.optInt("changeType");
                long groupId = jsonObject.optLong("groupId");
                long roomId = jsonObject.optLong("roomId");
                switch (type){
                    case SystemNotificationType.AddFriend:
                        imPushProcessor.pushAddFriend(fromUid, msg, custom);
                        break;
                    case SystemNotificationType.FriendChanged:
                        imPushProcessor.pushEstablishFriend(userId, custom);
                        break;
                    case SystemNotificationType.AgreeAddFriend:
                        imPushProcessor.pushAgreeApplyFriend(userId, custom);
                        break;
                    case SystemNotificationType.DenyAddFriend:
                        imPushProcessor.pushRefuseApplyFriend(userId, custom);
                        break;
                    case SystemNotificationType.InviteIntoGroup:
                        imPushProcessor.pushInviteGroup(fromUid, groupId,msg,  custom);
                        break;
                    case SystemNotificationType.AgreeInviteGroup:
                        imPushProcessor.pushAgreeInviteGroup(fromUid, groupId, custom);
                        break;
                    case SystemNotificationType.RefuseInviteGroup:
                        imPushProcessor.pushRefuseInviteGroup(fromUid, groupId, custom);
                        break;
                    case SystemNotificationType.AddGroup:
                        imPushProcessor.pushApplyGroup(fromUid, groupId, msg, custom);
                        break;
                    case SystemNotificationType.AgreeApplyGroupResult:
                        imPushProcessor.pushAgreeApplyGroup(fromUid, groupId, custom);
                        break;
                    case SystemNotificationType.RefuseApplyGroupResult:
                        imPushProcessor.pushRefuseApplyGroup(fromUid, groupId, custom);
                        break;
                    case SystemNotificationType.GroupChanged:
                        imPushProcessor.pushGroupChange(groupId, custom,changeType);
                        break;
                    case SystemNotificationType.GroupMemberChanged:
                        imPushProcessor.pushGroupMemberChange(groupId, fromUid, changeType);
                        break;
                    case SystemNotificationType.GroupAddMangager:
                        List<Long> adds = new ArrayList<>();
                        try {
                            JSONArray array = jsonObject.getJSONArray("managers");
                            for (int i = 0; i < array.length(); i++) {//循环json数组
                                long ob = array.getLong(i);//得到json对象
                                adds.add(ob);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        imPushProcessor.pushGroupManagerChange(groupId, adds,0);
                        break;
                    case SystemNotificationType.GroupRemoveMangager:
                        adds = new ArrayList<>();
                        try {
                            JSONArray array = jsonObject.getJSONArray("managers");

                            for (int i = 0; i < array.length(); i++) {//循环json数组
                                long ob = array.getLong(i);//得到json对象
                                adds.add(ob);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        imPushProcessor.pushGroupManagerChange(groupId, adds,1);
                        break;
                    case SystemNotificationType.GroupOwnerrChanged:
                        long oldOwner = jsonObject.optLong("oldOwner");
                        long newOwner = jsonObject.optLong("newOwner");
                        imPushProcessor.pushGroupLeaderChange(groupId, oldOwner, newOwner);
                        break;



                    case SystemNotificationType.InviteIntoRoom:
                        imPushProcessor.pushInviteRoom(fromUid, roomId,msg,  custom);
                        break;
                    case SystemNotificationType.AgreeInviteRoom:
                        imPushProcessor.pushAgreeInviteRoom(fromUid, roomId, custom);
                        break;
                    case SystemNotificationType.RefuseInviteRoom:
                        imPushProcessor.pushRefuseInviteRoom(fromUid, roomId, custom);
                        break;
                    case SystemNotificationType.RoomChanged:
                        imPushProcessor.pushRoomChange(roomId, custom,changeType);
                        break;
                    case SystemNotificationType.RoomMemberChanged:
                        imPushProcessor.pushRoomMemberChange(roomId, fromUid, changeType);
                        break;
                    case SystemNotificationType.RoomAddMangager:
                        adds = new ArrayList<>();
                        try {
                            JSONArray array = jsonObject.getJSONArray("managers");

                            for (int i = 0; i < array.length(); i++) {//循环json数组
                                long ob = array.getLong(i);//得到json对象
                                adds.add(ob);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        imPushProcessor.pushRoomManagerChange(roomId, adds,0);
                        break;
                    case SystemNotificationType.RoomRemoveMangager:
                        adds = new ArrayList<>();
                        try {
                            JSONArray array = jsonObject.getJSONArray("managers");

                            for (int i = 0; i < array.length(); i++) {//循环json数组
                                long ob = array.getLong(i);//得到json对象
                                adds.add(ob);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        imPushProcessor.pushRoomManagerChange(roomId, adds,1);
                        break;
                    case SystemNotificationType.RoomOwnerrChanged:
                        oldOwner = jsonObject.optLong("oldOwner");
                        newOwner = jsonObject.optLong("newOwner");
                        imPushProcessor.pushRoomLeaderChange(roomId, oldOwner, newOwner);
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
        }

        private void processPushMsg(Quest quest, ConversationType conversationType){
            String toidKey = "";

            if (conversationType == ConversationType.P2P)
                toidKey = "to";
            else if (conversationType == ConversationType.GROUP)
                toidKey = "gid";
            else if (conversationType == ConversationType.ROOM)
                toidKey = "rid";

            long from = LDUtils.wantLong(quest,"from");
            long toid = LDUtils.wantLong(quest,toidKey);
            long mid = LDUtils.wantLong(quest,"mid");
            if (!duplicatedFilter.CheckMessage(conversationType, from, mid))
                return ;

            int mtype =  LDUtils.wantInt(quest,"mtype");
            String attrs = LDUtils.wantString(quest,"attrs");
            long mtime = LDUtils.wantLong(quest,"mtime");

            RTMMessage userMsg = new RTMMessage();
            userMsg.attrs = attrs;
            userMsg.fromUid = from;
            userMsg.modifiedTime = mtime;
            userMsg.messageType = mtype;
            userMsg.messageId = mid;
            userMsg.targetId = toid;

            if (mtype == MessageType.CHAT) {
                userMsg.translatedInfo = processChatMessage(quest);
                if (userMsg.translatedInfo.source.isEmpty())
                    userMsg.stringMessage = userMsg.translatedInfo.sourceText;
                else {
                    userMsg.stringMessage = userMsg.translatedInfo.targetText;
                }
                if (rtmPushProcessor != null)
                    rtmPushProcessor.pushChat(userMsg, conversationType);
                if (imPushProcessor != null){
                    imPushProcessor.pushChat(TransStruct.transIMMessage(userMsg), conversationType);
                }
                return ;
            }

            MessageInfo messageInfo = BuildMessageInfo(quest);
            if (mtype == MessageType.NOTIFICATION){
                if (imPushProcessor == null) {
                    printLog("recieve NOTIFICATION but imPushProcessor is null");
                    return;
                }
                processNotification(attrs, messageInfo.message);
            }
            else if (mtype == MessageType.CMD) {
                userMsg.stringMessage = messageInfo.message;
                if (rtmPushProcessor != null)
                    rtmPushProcessor.pushCmd(userMsg, conversationType );
                else if (imPushProcessor != null){
                    processNotification(attrs, messageInfo.message);
                }
            } else if (mtype >= MessageType.IMAGEFILE && mtype <= MessageType.NORMALFILE) {
                String fileRecieve = quest.getString("msg");
                userMsg.fileInfo = new FileStruct();
                userMsg.attrs = parseFileInfo(fileRecieve, attrs, mtype, userMsg.fileInfo);

                if (imPushProcessor != null)
                    imPushProcessor.pushFile(TransStruct.transIMMessage(userMsg), conversationType);
                if (rtmPushProcessor != null)
                    rtmPushProcessor.pushFile(userMsg, conversationType);
            }
            else {
                if (messageInfo.isBinary) {
                    userMsg.binaryMessage = messageInfo.binaryData;
                }
                else {
                    userMsg.stringMessage = messageInfo.message;
                }
                if (rtmPushProcessor != null)
                    rtmPushProcessor.pushMessage(userMsg, conversationType);
            }
            return;
        }

        //----------------------[ RTM Messagess ]-------------------//
        Answer ping(Quest quest, InetSocketAddress peer) {
//            Log.i("sdktest"," receive rtm ping");

            long now = Genid.getCurrentSeconds();
            lastPingTime.set(now);
            return new Answer(quest);
        }

        Answer kickout(Quest quest, InetSocketAddress peer) {
            setCloseType(CloseType.ByServer);
            close();
            if (mOrEventListener!=null){
                mOrEventListener.disable();
            }
            basePushProcessor.kickout();
            return null;
        }

        Answer kickoutRoom(Quest quest, InetSocketAddress peer) {
            long roomId = (long) quest.get("rid");
            rtmPushProcessor.kickoutRoom(roomId);
            return null;
        }
        
        Answer pushmsg(Quest quest, InetSocketAddress peer){
            rtmGate.sendAnswer(new Answer(quest));
            processPushMsg(quest, ConversationType.P2P);
            return null;
        }

        Answer pushgroupmsg(Quest quest, InetSocketAddress peer) {
            rtmGate.sendAnswer(new Answer(quest));
            processPushMsg(quest, ConversationType.GROUP);
            return null;
        }

        Answer pushroommsg(Quest quest, InetSocketAddress peer) {
            rtmGate.sendAnswer(new Answer(quest));
            processPushMsg(quest, ConversationType.ROOM);
            return null;
        }

        Answer pushbroadcastmsg(Quest quest, InetSocketAddress peer) {
            rtmGate.sendAnswer(new Answer(quest));
            processPushMsg(quest, ConversationType.BROADCAST);
            return null;
        }


        //-------------RTC message--------------//
        Answer pushEnterRTCRoom(Quest quest, InetSocketAddress peer) {
            rtmGate.sendAnswer(new Answer(quest));
            if (!checkRTCPushProcessor())
                return null;

            long roomId = LDUtils.wantLong(quest,"rid");
            long userId = LDUtils.wantLong(quest,"uid");
            long time = LDUtils.wantLong(quest,"mtime");
            rtcPushProcessor.pushEnterRTCRoom(roomId,  userId, time);
            return null;
        }

        Answer pushAdminCommand(Quest quest, InetSocketAddress peer) {
            rtmGate.sendAnswer(new Answer(quest));
            if (!checkRTCPushProcessor())
                return null;

            int command = LDUtils.wantInt(quest,"command");
            List<Long> uids = LDUtils.wantLongList(quest,"uids");
            rtcPushProcessor.pushAdminCommand(command, uids);
            return null;
        }

        Answer pushP2PRTCRequest(Quest quest, InetSocketAddress peer){
            rtmGate.sendAnswer(new Answer(quest));
            if (!checkRTCPushProcessor())
                return null;

//            int pid = LDUtils.wantInt(quest,"pid");
            lastCallId = LDUtils.wantLong(quest,"callId");
            peerUid = LDUtils.wantLong(quest,"peerUid");
            lastP2Ptype = LDUtils.wantInt(quest,"type");
            rtcPushProcessor.pushRequestP2PRTC(peerUid, P2PRTCType.intToEnum(lastP2Ptype));
            return null;
        }

        Answer pushP2PRTCEvent(Quest quest, InetSocketAddress peer){
            rtmGate.sendAnswer(new Answer(quest));
            if (!checkRTCPushProcessor())
                return null;

//            int pid = LDUtils.wantInt(quest,"pid");
            long callId = LDUtils.wantLong(quest,"callId");
            long calluid = LDUtils.wantLong(quest,"peerUid");
            P2PRTCType type = P2PRTCType.intToEnum(LDUtils.wantInt(quest,"type"));
            int ievent = LDUtils.wantInt(quest,"event");
//            Log.i("sdktest","receive pushP2PRTCEvent callId " + callId + " peerUid "+ peerUid +  " type " + type + " ievent " + ievent);
            P2PRTCEvent event = P2PRTCEvent.intToEnum(ievent);

            if (event == P2PRTCEvent.Accept){
                String ret = RTCEngine.startP2P(type.value(), calluid, callId);
                if (ret.isEmpty()){
//                    Log.i("sdktest", "user " + calluid + "accept P2P " + type +" startP2P ok");
                    if (type == P2PRTCType.VIDEO) {
                        Handler mainhandle = new Handler(Looper.getMainLooper());
                        mainhandle.post(new Runnable() {
                            @Override
                            public void run() {
                                SurfaceView view = rtcPushProcessor.pushP2PRTCEvent(calluid, type, event);
                                if (view !=null) {
                                    RTCEngine.bindDecodeSurface(calluid, view.getHolder().getSurface());
                                }
                            }
                        });
                    }
                    else {
                        rtcPushProcessor.pushP2PRTCEvent(calluid, type, event);
                    }
                }
                else{
//                    Log.i("sdktest", "Accept connect P2P rtc failed " + ret);
                    errorRecorder.recordError("pushP2PRTCEvent startP2P error " + ret);
                }
                return null;
            }
            else {
                lastCallId = 0;
                peerUid = 0;
                lastP2Ptype = 0;
                RTCEngine.closeP2P();
            }

            rtcPushProcessor.pushP2PRTCEvent(calluid, type, event);
            return null;
        }

        Answer pushExitRTCRoom(Quest quest, InetSocketAddress peer) {
            rtmGate.sendAnswer(new Answer(quest));
            if (!checkRTCPushProcessor())
                return null;

            long roomId = LDUtils.wantLong(quest,"rid");
            long userId = LDUtils.wantLong(quest,"uid");
            long time = LDUtils.wantLong(quest,"mtime");
            RTCEngine.userLeave(userId);
            if (mOrEventListener!=null){
                mOrEventListener.disable();
            }
            rtcPushProcessor.pushExitRTCRoom(roomId,  userId, time);
            return null;
        }

        Answer pushRTCRoomClosed(Quest quest, InetSocketAddress peer) {
            rtmGate.sendAnswer(new Answer(quest));
            if (!checkRTCPushProcessor())
                return null;

            long roomId = LDUtils.wantLong(quest,"rid");
            RTCEngine.leaveRTCRoom(roomId);
            if (mOrEventListener!=null){
                mOrEventListener.disable();
            }
            rtcPushProcessor.pushRTCRoomClosed(roomId);
            return null;
        }

        Answer pushInviteIntoRTCRoom(Quest quest, InetSocketAddress peer) {
            rtmGate.sendAnswer(new Answer(quest));
            if (!checkRTCPushProcessor())
                return null;

            long roomId = LDUtils.wantLong(quest,"rid");
            long userId = LDUtils.wantLong(quest,"fromUid");
            rtcPushProcessor.pushInviteIntoRTCRoom(roomId, userId);

            return null;
        }

        Answer pushPullIntoRTCRoom(Quest quest, InetSocketAddress peer) {
            rtmGate.sendAnswer(new Answer(quest));
            if (!checkRTCPushProcessor())
                return null;
            long roomId = LDUtils.wantLong(quest,"rid");
            enterRTCRoom(new ICallback<RTCRoomInfo>() {
                @Override
                public void onSuccess(RTCRoomInfo roomInfo) {
                    rtcPushProcessor.pushPullRoom(roomId,roomInfo);
                }

                @Override
                public void onError(LDAnswer answer) {

                }
            },roomId, lang);
            return null;
        }

        Answer pushKickOutRTCRoom(Quest quest, InetSocketAddress peer) {
            rtmGate.sendAnswer(new Answer(quest));
            if (!checkRTCPushProcessor())
                return null;
            long roomId = LDUtils.wantLong(quest,"rid");
            RTCEngine.leaveRTCRoom(roomId);
            if (mOrEventListener!=null){
                mOrEventListener.disable();
            }
            rtcPushProcessor.pushKickoutRTCRoom(roomId);
            return null;
        }
    }

    void  internalReloginCompleted(long uid, boolean successful, int reloginCount){
        if (!successful){
            RTCEngine.RTCClear();
            RTCEngine.closeP2P();
            lastCallId = 0;
            lastP2Ptype = 0;
            peerUid = 0;
        }
        basePushProcessor.reloginCompleted(uid, successful, lastReloginAnswer, reloginCount);
    }

    void reloginEvent(int count){
        if (noNetWorkNotify.get()) {
            isRelogin.set(false);
            internalReloginCompleted(uid, false, count);
            return;
        }
//        isRelogin.set(true);
        int num = count;
        Map<String, String> kk = loginAttrs;
        if (basePushProcessor.reloginWillStart(uid, num)) {
            lastReloginAnswer = loginRTM(token, lang, kk, logints);
            if(lastReloginAnswer.errorCode == okRet || lastReloginAnswer.errorCode == RTMErrorCode.RTM_EC_DUPLCATED_AUTH.value()) {
                isRelogin.set(false);
                internalReloginCompleted(uid, true, num);
                return;
            }
            else {
                if (finishCodeList.contains(lastReloginAnswer.errorCode)){
                    isRelogin.set(false);
                    internalReloginCompleted(uid, false, num);
                    return;
                }
                else {
                    if (num >= basePushProcessor.internalReloginMaxTimes){
                        isRelogin.set(false);
                        internalReloginCompleted(uid, false, num);
                        return;
                    }
                    if (!isRelogin.get()) {
                        internalReloginCompleted(uid, false, num);
                        return;
                    }
                    try {
                        Thread.sleep(2 * 1000);
                    } catch (InterruptedException e) {
                        isRelogin.set(false);
                        internalReloginCompleted(uid, false, num);
                        return;
                    }
                    reloginEvent(++num);
                }
            }
        }
        else {
            isRelogin.set(false);
            internalReloginCompleted(uid, false, --num);
        }
    }

    public void onNetChange(int netWorkState){
        if (lastNetType != NetUtils.NETWORK_NOTINIT) {
            switch (netWorkState) {
                case NetUtils.NETWORK_NONE:
                    noNetWorkNotify.set(true);
                    break;
                case NetUtils.NETWORK_MOBILE:
                case NetUtils.NETWORK_WIFI:
//                    Log.e("sdktest","have network");

                    if (rtmGate == null)
                        return;
                    noNetWorkNotify.set(false);
                    if (lastNetType != netWorkState) {
                        if (isRelogin.get())
                            return;
                        close();
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        isRelogin.set(true);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                reloginEvent(1);
//                                if (getClientStatus() == ClientStatus.Connected){
//                                    Quest quest = new Quest("bye");
//                                    sendQuest(quest, new FunctionalAnswerCallback() {
//                                        @Override
//                                        public void onAnswer(Answer answer, int errorCode) {
//                                            close();
//                                            try {
//                                                Thread.sleep(200);
//                                            } catch (InterruptedException e) {
//                                                e.printStackTrace();
//                                            }
//                                            reloginEvent(1);
//                                        }
//                                    }, 5);
//                                }
//                                else {
////                                    voiceClose();
//                                    reloginEvent(1);
//                                }
                            }
                        }).start();
                    }
                    break;
            }
        }
        lastNetType = netWorkState;
    }

    public static boolean isH265DecoderSupport(){
        int count = MediaCodecList.getCodecCount();
        for(int i=0;i<count;i++){
            MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
            String name = info.getName();
            if(name.contains("decoder") && name.contains("hevc")){
                return true;
            }
        }
        return false;
    }


     void initEngine(String rtmendpoint, long pid, long uid, IBasePushProcessor serverPushProcessor, Activity currentActivity, LiveDataConfig config) {
        if (config == null)
            rtmConfig = new LiveDataConfig();
        else
            rtmConfig = config;

        errorRecorder = rtmConfig.defaultErrorRecorder;
        LDUtils.errorRecorder = errorRecorder;
        this.rtmEndpoint = rtmendpoint;

        this.pid = pid;
        this.uid = uid;
        isRelogin.set(false);
        fileGates = new HashMap<>();
        processor = new EngineQuestProcessor();
        this.basePushProcessor = serverPushProcessor;
        this.currentActivity = new WeakReference<Activity>(currentActivity);

        application = currentActivity.getApplication();
         ClientEngine.setMaxThreadInTaskPool(LiveDataConfig.globalMaxThread);

         application.registerActivityLifecycleCallbacks(this);

        if (currentActivity == null){
            printLog("currentActivity is null ");
            return;
        }
        context = currentActivity.getApplicationContext();

        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        try {
            //网络监听
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            intentFilter.addAction("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED");
            intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
            context.registerReceiver(this, intentFilter);

        }
        catch (Exception ex){
            ex.printStackTrace();
            errorRecorder.recordError("registerReceiver exception:" + ex.getMessage());
        }
    }

    public void setErrorRecoder(ErrorRecorder value){
        if (value == null)
            return;
        errorRecorder = value;
        LDUtils.errorRecorder = errorRecorder;
    }

    public void enableEncryptor() {
        encrptyFlag = true;
    }


    public long getPid() {
        return pid;
    }

    public long getUid() {
        return uid;
    }

    public void setBasePushProcessor(IBasePushProcessor basePushProcessor) {
        this.basePushProcessor = basePushProcessor;
    }

    public String setRTMPushProcessor(IRTMPushProcessor rtmPushProcessor){
        if (imPushProcessor != null){
            return "IMPushProcessor和RTMPushProcessor不能同时设置";
        }
        this.rtmPushProcessor = rtmPushProcessor;
        return "";
    }

    public String setIMPushProcessor(IIMPushProcessor imPushProcessor){
        if (rtmPushProcessor != null){
            return "IMPushProcessor和RTMPushProcessor不能同时设置";
        }
        this.imPushProcessor = imPushProcessor;
        return "";
    }

    public void setRTCPushProcessor(IRTCPushProcessor rtcPushProcessor){
        this.rtcPushProcessor = rtcPushProcessor;
    }

    public LDAnswer initRTC() {
        int errCode = unknownErr;

        if (isInitRTC)
            return genLDAnswer(okRet);

        mOrEventListener = new OrientationEventListener(currentActivity.get()) {
            @Override
            public void onOrientationChanged(int rotation) {
                if (((rotation >= 0) && (rotation <= 45)) || (rotation > 315)) {
                    rotation = 0;
                } else if ((rotation > 70) && (rotation <= 110)) {
                    rotation = 90;
                } else if ((rotation > 160) && (rotation <= 200)) {
                    rotation = 180;
                } else if ((rotation > 250) && (rotation <= 290)) {
                    rotation = 270;
                } else {
                    rotation = 0;
                }
                RTCEngine.setRotation(rotation);
            }
        };
//        mOrEventListener.enable();
        if (rtmGateStatus != ClientStatus.Connected) {
            return genLDAnswer(errCode, "you must RTMlogin sucessfully at first");
        }

        if (rtcEndpoint.isEmpty()){
            Quest quest = new Quest("getRTCGateEndpoint");
            try {
                Answer answer  = rtmGate.sendQuest(quest);
                if (answer.getErrorCode() != okRet){
                    return genLDAnswer(errCode, "get getRTCGateEndpoint error " + answer.getErrorCode() + answer.getErrorMessage());
                }
                rtcEndpoint = LDUtils.wantString(answer, "endpoint");
            } catch (InterruptedException e) {
                e.printStackTrace();
                return genLDAnswer(errCode, "get getRTCGateEndpoint error " + e.getMessage());
            }

            if (rtcEndpoint.isEmpty()){
                return genLDAnswer(errCode, "get getRTCGateEndpoint error rtcEndpoint is empty");
            }
        }

        String ret = RTCEngine.create(this, rtcEndpoint, currVideoLevel, pid, uid, application, afChangeListener);
        if (!ret.isEmpty()) {
            return genLDAnswer(errCode,"initRTC create error " + ret);
        }

        isInitRTC = true;
        return genLDAnswer(okRet);
    }


    synchronized protected ClientStatus getClientStatus() {
        synchronized (interLocker) {
            return rtmGateStatus;
        }
    }

    private boolean connectionIsAlive() {
        return processor.ConnectionIsAlive();
    }

    public LDAnswer genLDAnswer(int errCode){
        return genLDAnswer(errCode,"");
    }

    public LDAnswer genLDAnswer(int errCode, String msg)
    {
        LDAnswer tt = new LDAnswer();
        tt.errorCode = errCode;
        if (msg == null || msg.isEmpty())
            tt.errorMsg = RTMErrorCode.getMsg(errCode);
        else
            tt.errorMsg = msg;
        return tt;
    }

    private TCPClient getCoreClient() {
        synchronized (interLocker) {
            if (rtmGateStatus == ClientStatus.Connected)
                return rtmGate;
            else
                return null;
        }
    }


    public boolean isOnline() {
        return this.getClientStatus() == ClientStatus.Connected;
    }


    public LDAnswer genLDAnswer(Answer answer) {
        if (answer == null)
            return new LDAnswer(ErrorCode.FPNN_EC_CORE_INVALID_CONNECTION.value(), "invalid connection");
        return new LDAnswer(answer.getErrorCode(),answer.getErrorMessage());
    }



    public LDAnswer genLDAnswer(Answer answer, String msg) {
        if (answer == null)
            return new LDAnswer(ErrorCode.FPNN_EC_CORE_INVALID_CONNECTION.value(), "invalid connection");
        return new LDAnswer(answer.getErrorCode(),answer.getErrorMessage() + " " + msg);
    }


    public LDAnswer genLDAnswer(Answer answer, int errcode) {
        if (answer == null && errcode !=0) {
            if (errcode == ErrorCode.FPNN_EC_CORE_TIMEOUT.value())
                return new LDAnswer(errcode, "FPNN_EC_CORE_TIMEOUT");
            else
                return new LDAnswer(errcode,"fpnn  error");
        }
        else
            return new LDAnswer(answer.getErrorCode(),answer.getErrorMessage());
    }

    void setCloseType(CloseType type)
    {
        closedCase = type;
    }

    void sayBye(IEmptyCallback callback) {
        closedCase = CloseType.ByUser;
        TCPClient client = getCoreClient();
        if (client == null) {
            close();
            return;
        }
        Quest quest = new Quest("bye");
        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                close();
            }
        }, 5);
    }

    public void realClose(){
        closedCase = CloseType.ByUser;
        encrptyFlag = false;
        imPushProcessor = null;
        rtmPushProcessor = null;
        try {
            if (context != null)
                context.unregisterReceiver(this);
            if (application != null)
                application.unregisterActivityLifecycleCallbacks(this);

        } catch (IllegalArgumentException e){
        }

        isInitRTC = false;
        if (mOrEventListener != null)
            mOrEventListener.disable();
        close(true);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void sayBye(boolean async) {
        closedCase = CloseType.ByUser;
        TCPClient client = getCoreClient();
        if (client == null) {
            close();
            return;
        }
        Quest quest = new Quest("bye");
        if (async) {
            sendQuest(quest, new FunctionalAnswerCallback() {
                @Override
                public void onAnswer(Answer answer, int errorCode) {
                    close();
                }
            }, 5);
        } else {
            try {
                client.sendQuest(quest,5);
                close();
            } catch (InterruptedException e) {
                close();
            }
        }
    }

     void sendFileQuest(Quest quest, FunctionalAnswerCallback callback) {
        sendQuest(quest, callback, rtmConfig.globalFileQuestTimeoutSeconds);
    }

    public void sendQuest(Quest quest, FunctionalAnswerCallback callback) {
        sendQuest(quest, callback, rtmConfig.globalQuestTimeoutSeconds);
    }

    Answer sendFileQuest(Quest quest) {
        return sendQuest(quest,rtmConfig.globalFileQuestTimeoutSeconds);
    }

    public Answer sendQuest(Quest quest) {
        return sendQuest(quest,rtmConfig.globalQuestTimeoutSeconds);
    }

    Answer sendQuest(Quest quest, int timeout) {
        Answer answer = new Answer(new Quest(""));
        TCPClient client = getCoreClient();
        if (client == null) {
            answer.fillErrorInfo(ErrorCode.FPNN_EC_CORE_INVALID_CONNECTION.value(), "invalid connection");
        }else {
            try {
                answer = client.sendQuest(quest, timeout);
            } catch (Exception e) {
                if (errorRecorder != null)
                    errorRecorder.recordError(e);
                answer = new Answer(quest);
                answer.fillErrorInfo(unknownErr, e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        return answer;
    }

    void sendQuest(Quest quest, FunctionalAnswerCallback callback, int timeout) {
        TCPClient client = getCoreClient();
        Answer answer = new Answer(quest);
        if (client == null) {
            answer.fillErrorInfo(ErrorCode.FPNN_EC_CORE_INVALID_CONNECTION.value(),"invalid connection");
            callback.onAnswer(answer,answer.getErrorCode());//当前线程
            return;
        }
        if (timeout <= 0)
            timeout = rtmConfig.globalQuestTimeoutSeconds;
        try {
            client.sendQuest(quest, callback, timeout);
        }
        catch (Exception e){
            answer.fillErrorInfo(unknownErr,e.getMessage());
            callback.onAnswer(answer, answer.getErrorCode());
        }
    }

    public void sendQuestEmptyCallback(IEmptyCallback callback, Quest quest) {
        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet)
                    callback.onSuccess();
                else
                    callback.onError(genLDAnswer(answer,errorCode));
            }
        }, rtmConfig.globalQuestTimeoutSeconds);
    }

    LDAnswer sendQuestEmptyResult(Quest quest){
        Answer ret =  sendQuest(quest);
        if (ret == null)
            return genLDAnswer(ErrorCode.FPNN_EC_CORE_INVALID_CONNECTION.value(),"invalid connection");
        return genLDAnswer(ret);
    }

    void activeFileGateClient(String endpoint, TCPClient client) {
        synchronized (interLocker) {
            if (fileGates.containsKey(endpoint)) {
                if (fileGates.get(endpoint) != null)
                    fileGates.get(endpoint).put(client, Genid.getCurrentSeconds());
            }
            else
                fileGates.put(endpoint, new HashMap<TCPClient, Long>() {{
                    put(client, Genid.getCurrentSeconds());
                }});
        }
    }

    TCPClient fecthFileGateClient(String endpoint) {
        synchronized (interLocker) {
            if (fileGates.containsKey(endpoint)) {
                if(fileGates.get(endpoint) != null)
                    for (TCPClient client : fileGates.get(endpoint).keySet())
                        return client;
            }
        }
        return null;
    }

    private void checkRoutineInit() {
        if (initCheckThread.get() || !running.get())
            return;

        checkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (running.get()) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        synchronized (interLocker) {
                            rtmGateStatus = ClientStatus.Closed;
                        }
                        return;
                    }

                    if (rtmGateStatus != ClientStatus.Closed && !connectionIsAlive()) {
                        closedCase = CloseType.Timeout;
                        close();
                    }
                }
            }
        });
        checkThread.setName("RTM.ThreadCheck");
        checkThread.setDaemon(true);
        checkThread.start();

        initCheckThread.set(true);
        running.set(true);
    }

    public static byte[] getBytes(int data)
    {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (data & 0xff);
        bytes[1] = (byte) ((data & 0xff00) >> 8);
        bytes[2] = (byte) ((data & 0xff0000) >> 16);
        bytes[3] = (byte) ((data & 0xff000000) >> 24);
        return bytes;
    }

    public static byte[] getBytes(long data)
    {
        byte[] bytes = new byte[8];
        bytes[0] = (byte) (data & 0xff);
        bytes[1] = (byte) ((data & 0xff00) >> 8);
        bytes[2] = (byte) ((data & 0xff0000) >> 16);
        bytes[3] = (byte) ((data & 0xff000000) >> 24);
        bytes[4] = 0;
        bytes[5] = 0;
        bytes[6] = 0;
        bytes[7] = 0;
        return bytes;
    }

    public void whoSpeak(long uid){
        if (rtcPushProcessor == null){
            Log.e("sdktest","rtcPushProcessor is null");
            return;
        }
        rtcPushProcessor.voiceSpeak(uid);
    }

    public void pushVoiceTranslate(String text, String slang, long uid){
//        Log.e("sdktest",msg);
//        Log.i("sdktest","pushText " + text);

        rtcPushProcessor.pushVoiceTranslate(text, slang, uid);
    }

    public void printLog(String msg){
        Log.e("sdktest",msg);
        errorRecorder.recordError(msg);
    }

    boolean isAirplaneModeOn() {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON,0) != 0;
    }

    boolean isNetWorkConnected() {
        boolean isConnected = false;
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeInfo = cm.getActiveNetworkInfo();
            if (activeInfo != null && activeInfo.isAvailable() && activeInfo.isConnected())
                isConnected = true;
        }
        return isConnected;
    }

    //-------------[ Auth(Login) utilies functions ]--------------------------//
    private void ConfigRtmGateClient(TCPClient client) {
        client.setQuestTimeout(rtmConfig.globalQuestTimeoutSeconds);

        if (encrptyFlag)
            client.enableEncryptorByDerData(curve, encrptyData);

        if (errorRecorder != null)
            client.setErrorRecorder(errorRecorder);

        client.setQuestProcessor(processor, "com.LiveDataRTE.InternalEngine.EngineCore$EngineQuestProcessor");

        client.setWillCloseCallback(new ConnectionWillCloseCallback() {
            @Override
            public void connectionWillClose(InetSocketAddress peerAddress, int _connectionId,boolean causedByError) {
//                printLog("closedCase " + closedCase + " getClientStatus() " + getClientStatus());
                if (connectionId.get() != 0 && connectionId.get() == _connectionId && closedCase != CloseType.ByUser && closedCase != CloseType.ByServer && getClientStatus() != ClientStatus.Connecting) {
                    close();

                    processor.rtmConnectClose();

                    if (closedCase == CloseType.ByServer || isRelogin.get()) {
                        return;
                    }

                    if (isAirplaneModeOn()) {
                        return;
                    }

                    if(getClientStatus() == ClientStatus.Closed){
                        try {
                            Thread.sleep(2* 1000);//处理一些特殊情况
                            if (noNetWorkNotify.get()) {
                                return;
                            }
                            if (isRelogin.get() || getClientStatus() == ClientStatus.Connected) {
                                return;
                            }
                            isRelogin.set(true);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    reloginEvent(1);
                                }
                            }).start();
                        }
                        catch (Exception e){
                            printLog(" relogin error " + e.getMessage());
                        }
                    }
                }
            }
        });
    }


    //------------voice add---------------//
    private LDAnswer auth(String token, Map<String, String> attr, boolean retry, long ts) {
        String deviceid = Build.BRAND + "-" + Build.MODEL;

        Quest qt = new Quest("auth");
        qt.param("pid", pid);
        qt.param("uid", uid);
        qt.param("token", token);
        qt.param("lang", lang);
        qt.param("device", deviceid);
        qt.param("version", "Android-livedataLib-" + LiveDataConfig.SDKVersion);
        if (ts == 0){
            qt.param("authv", 1);
        }
        else{
            qt.param("authv", 2);
        }
        qt.param("ts", ts);

        if (attr != null)
            qt.param("attrs", attr);
        try {
            Answer answer = rtmGate.sendQuest(qt, rtmConfig.globalQuestTimeoutSeconds);

            if (answer  == null || answer.getErrorCode() != okRet) {
                closeStatus();
                return genLDAnswer(answer,"when send sync auth ");
            }
            else if (!LDUtils.wantBoolean(answer,"ok")) {
                closeStatus();
                return genLDAnswer(RTMErrorCode.RTM_EC_INVALID_AUTH_TOEKN.value(),"sync auth failed token maybe expired");
            }
            synchronized (interLocker) {
                rtmGateStatus = ClientStatus.Connected;
            }
            processor.setLastPingTime(Genid.getCurrentSeconds());
            checkRoutineInit();
            getRTCgate();
            connectionId.set(rtmGate.getConnectionId());
            return genLDAnswer(answer);
        }
        catch (Exception  ex){
            closeStatus();
            return genLDAnswer(unknownErr, ex.getMessage());
        }
    }

    private void auth(IEmptyCallback callback, String token, Map<String, String> attr, boolean retry, long ts) {
        String deviceid = Build.BRAND + "-" + Build.MODEL;
        Quest qt = new Quest("auth");
        qt.param("pid", pid);
        qt.param("uid", uid);
        qt.param("token", token);
        qt.param("lang", lang);
        qt.param("device", deviceid);
        if (ts == 0){
            qt.param("authv", 1);
        }
        else{
            qt.param("authv", 2);
        }
        qt.param("ts", ts);
        qt.param("version", "Android-livedataLib-" + rtmConfig.SDKVersion);
        if (attr != null)
            qt.param("attrs", attr);

        rtmGate.sendQuest(qt, new FunctionalAnswerCallback() {
            @SuppressLint("NewApi")
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                try {
                    if (answer == null || errorCode != okRet) {
                        closeStatus();
                        callback.onError(genLDAnswer( answer, "when send async auth " + answer.getErrorMessage()));
                        return;
                    } else if (!LDUtils.wantBoolean(answer,"ok")) {
                        closeStatus();
                        callback.onError(genLDAnswer(RTMErrorCode.RTM_EC_INVALID_AUTH_TOEKN.value(), "async auth failed token maybe expired"));
                    } else {
                        synchronized (interLocker) {
                            rtmGateStatus = ClientStatus.Connected;
                        }

                        processor.setLastPingTime(Genid.getCurrentSeconds());
                        checkRoutineInit();
                        connectionId.set(rtmGate.getConnectionId());
                        getRTCgate();
                        callback.onSuccess();
                    }
                }
                catch (Exception e){
                    callback.onError(genLDAnswer(unknownErr,"when async auth " + e.getMessage()));
                }
            }
        }, rtmConfig.globalQuestTimeoutSeconds);
    }


    public void loginRTM(IEmptyCallback callback, String token, String lang, Map<String, String> attr, long ts) {
        if (token ==null || token.isEmpty()){
            callback.onError(genLDAnswer(unknownErr," token  is null or empty"));
            return;
        }

        String errDesc = "";
        if (rtmEndpoint == null || rtmEndpoint.isEmpty() || rtmEndpoint.lastIndexOf(':') == -1)
            errDesc = "invalid rtmEndpoint:" + rtmEndpoint;
        if (pid <= 0)
            errDesc += " pid is invalid:" + pid;
        if (uid <= 0)
            errDesc += " uid is invalid:" + uid;

        if (!errDesc.equals("")) {
            errorRecorder.recordError("rtmclient init error." + errDesc);
            callback.onError(genLDAnswer(unknownErr, errDesc));
            return;
        }

        if (rtmGateStatus == ClientStatus.Connected || rtmGateStatus == ClientStatus.Connecting) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    callback.onSuccess();
                }
            }).start();
            return;
        }
        synchronized (interLocker) {
            rtmGateStatus = ClientStatus.Connecting;
        }

        if (rtmGate != null) {
            rtmGate.close();
            auth(callback, token, attr,false, ts);
        } else {
            try {
                rtmGate = TCPClient.create(rtmEndpoint);
                rtmGate.setErrorRecorder(errorRecorder);
            }
            catch (IllegalArgumentException ex){
                callback.onError(genLDAnswer(unknownErr,"create rtmgate error endpoint Illegal:" +ex.getMessage() + " :" +  rtmEndpoint ));
                return;
            }
            catch (Exception e){
                String msg = "create rtmgate error orginal error:" + e.getMessage() + " endpoint: " + rtmEndpoint;
                if (rtmGate != null)
                    msg = msg + " parse endpoint " + rtmGate.endpoint();
                callback.onError(genLDAnswer(unknownErr,msg));
                return;
            }
            this.token = token;
            this.logints = ts;
            if (lang == null)
                this.lang = "";
            else
                this.lang = lang;
            this.loginAttrs = attr;
            closedCase = CloseType.None;
            ConfigRtmGateClient(rtmGate);
            auth(callback, token, attr,false, ts);
        }
    }

    private  void closeStatus()
    {
        synchronized (interLocker) {
            rtmGateStatus = ClientStatus.Closed;
        }
    }

    public LDAnswer loginRTM(String token, String lang, Map<String, String> attr, long ts) {
        if (token == null || token.isEmpty())
            return genLDAnswer(RTMErrorCode.RTM_EC_INVALID_AUTH_TOEKN.value(), "login failed token  is null or empty");

        String errDesc = "";
        if (rtmEndpoint == null || rtmEndpoint.isEmpty() || rtmEndpoint.lastIndexOf(':') == -1)
            errDesc = "invalid rtmEndpoint:" + rtmEndpoint;
        if (pid <= 0)
            errDesc += " pid is invalid:" + pid;
        if (uid <= 0)
            errDesc += " uid is invalid:" + uid;

        if (!errDesc.equals("")) {
            errorRecorder.recordError("login init error." + errDesc);
            return genLDAnswer(unknownErr, errDesc);
        }

        synchronized (interLocker) {
            if (rtmGateStatus == ClientStatus.Connected || rtmGateStatus == ClientStatus.Connecting)
                return genLDAnswer(okRet);

            rtmGateStatus = ClientStatus.Connecting;
        }

        if (rtmGate != null) {
            rtmGate.close();
            return auth(token, attr,false, ts);
        } else {
            try {
                rtmGate = TCPClient.create(rtmEndpoint);
                rtmGate.setErrorRecorder(errorRecorder);
            }
            catch (IllegalArgumentException ex){
                return genLDAnswer(unknownErr,"create rtmgate error endpoint Illegal:" +ex.getMessage() + " :" +  rtmEndpoint );
            }
            catch (Exception e){
                String msg = "create rtmgate error orginal error:" + e.getMessage() + " endpoint: " + rtmEndpoint;
                if (rtmGate != null)
                    msg = msg + " parse endpoint " + rtmGate.endpoint();
                return genLDAnswer(unknownErr,msg );
            }
            if (lang == null)
                this.lang = "";
            else
                this.lang = lang;
            this.token =  token;
            this.logints = ts;
            this.loginAttrs = attr;
            closedCase = CloseType.None;
            ConfigRtmGateClient(rtmGate);
            return auth(token, attr, false,ts);
        }
    }

    void rtcclear(final boolean delete){
        if (isInitRTC){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    RTCEngine.RTCClear();
                    if (delete)
                        RTCEngine.delete();
                }
            }).start();
        }
        isInitRTC = false;
    }

    void close(){
        close(false);
    }

    public void close(boolean deleteRTC) {
        if (isRelogin.get()) {
            return;
        }
        synchronized (interLocker) {
            initCheckThread.set(false);
            running.set(false);
            fileGates.clear();
            if (rtmGateStatus == ClientStatus.Closed) {
                return;
            }
            rtmGateStatus = ClientStatus.Closed;
        }
        if (rtmGate !=null)
            rtmGate.close();
        rtcclear(deleteRTC);
    }
}
