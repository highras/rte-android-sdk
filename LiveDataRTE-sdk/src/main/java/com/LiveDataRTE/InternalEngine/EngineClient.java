package com.LiveDataRTE.InternalEngine;

import static com.LiveDataRTE.ValueAdd.ValueAddedClient.defaultCodec;
import static com.LiveDataRTE.ValueAdd.ValueAddedClient.sample_rate;

import android.app.Activity;

import com.LiveDataRTE.IBasePushProcessor;
import com.LiveDataRTE.LiveDataConfig;
import com.LiveDataRTE.RTMLib.RTMErrorCode;
import com.LiveDataRTE.LDInterface.*;
import com.LiveDataRTE.RTMLib.RTMStruct.*;
import com.LiveDataRTE.LiveDataStruct.*;
import com.LiveDataRTE.ValueAdd.AddValueStruct.*;
import com.fpnn.sdk.ErrorCode;
import com.fpnn.sdk.FunctionalAnswerCallback;
import com.fpnn.sdk.proto.Answer;
import com.fpnn.sdk.proto.Quest;

import java.util.ArrayList;
import java.util.List;

public class EngineClient extends EngineSystem {

    EngineClient(String rtmEndpoint, long pid, long uid, IBasePushProcessor serverPushProcessor, Activity currentActivity, LiveDataConfig liveDataConfig){
        initEngine(rtmEndpoint, pid, uid, serverPushProcessor, currentActivity, liveDataConfig);
    }


    public void audioToTextAsync(final ICallback<AudioTextStruct> callback, Object content, CheckSourceType type, String lang, String codec, int srate)
    {
        String sendcodec = codec==null?defaultCodec:codec;
        int  sendsrate = srate==0?sample_rate:srate;

        Quest quest = new Quest("speech2text");
        quest.param("audio", content);
        if (type == CheckSourceType.URL)
            quest.param("type", 1);
        else if (type == CheckSourceType.CONTENT)
            quest.param("type", 2);
        quest.param("lang", lang);
        quest.param("codec", sendcodec);
        quest.param("srate", sendsrate);

        sendFileQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                AudioTextStruct audioTextStruct = new AudioTextStruct();
                if (errorCode == okRet) {
                    audioTextStruct.text = answer.getString("text");
                    audioTextStruct.lang = answer.getString("lang");
                    callback.onSuccess(audioTextStruct);
                }else
                    callback.onError(genLDAnswer(answer,errorCode));
            }
        });
    }




    public void textCheck(final ICallback<CheckResult> callback, String text){
        Quest quest = new Quest("tcheck");
        quest.param("text", text);

        sendFileQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    CheckResult checkResult = new CheckResult();
                    List<Integer> tags = new ArrayList<>();
                    List<String> wlist = new ArrayList<>();
                    checkResult.text = answer.getString("text");
                    LDUtils.getIntList(answer,"tags",tags);
                    LDUtils.getStringList(answer,"wlist",wlist);
                    checkResult.tags = tags;
                    checkResult.wlist = wlist;
                    callback.onSuccess(checkResult);
                }else{
                    callback.onError(genLDAnswer(answer,errorCode));
                }
            }
        });
    }


    public void checkContentAsync(final ICallback<CheckResult> callback, Object content, CheckSourceType type, CheckType checkType, String videoName, String lang, String codec, int srate
            , String strategyId)
    {
        String method = "", rucankey = "";
        String sendcodec = codec==null?defaultCodec:codec;
        int  sendsrate = srate==0?sample_rate:srate;

        int sourfeType = 1;
        if (checkType == CheckType.PIC) {
            method = "icheck";
            rucankey = "image";
        }
        else if (checkType == CheckType.AUDIO) {
            method = "acheck";
            rucankey = "audio";
        }
        else if (checkType == CheckType.VIDEO) {
            method = "vcheck";
            rucankey = "video";
        }

        if (type == CheckSourceType.CONTENT)
            sourfeType = 2;

        Quest quest = new Quest(method);
        quest.param("type", sourfeType);
        if (strategyId!=null){
            quest.param("strategyId", strategyId);
        }

        quest.param(rucankey, content);
        if (checkType == CheckType.VIDEO) {
            quest.param("videoName", videoName);
        }
        else if (checkType == CheckType.AUDIO) {
            quest.param("lang", lang);
            quest.param("codec", sendcodec);
            quest.param("srate", sendsrate);
        }

        sendFileQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    CheckResult checkResult = new CheckResult();
                    checkResult.result = LDUtils.wantInt(answer,"result");
                    if (checkResult.result == 2){
                        checkResult.tags = LDUtils.wantIntList(answer,"tags");
                    }
                    callback.onSuccess(checkResult);
                }else
                    callback.onError(genLDAnswer(answer,errorCode));
            }
        });
    }
    
     public void sendMsgAsync(final ISendMsgCallback callback, long id, int mtype, Object message, String attrs, ConversationType type, SendSource sendSourceType) {
        String method = "", toWhere = "", att = "";
        if (attrs != null)
            att = attrs;
        switch (type) {
            case GROUP:
                method = "sendgroupmsg";
                toWhere = "gid";
                break;
            case ROOM:
                method = "sendroommsg";
                toWhere = "rid";
                break;
            case P2P:
                method = "sendmsg";
                toWhere = "to";
                break;
        }
        long mid = Genid.genMid();
        final Quest quest = new Quest(method);
        quest.param(toWhere, id);
        quest.param("mid", mid);
        quest.param("mtype", mtype);
        quest.param("msg", message);
        quest.param("attrs", att);

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {

                long mtime = 0;
                String msg = "";
                if (errorCode == okRet) {
                    mtime = LDUtils.wantLong(answer, "mtime");
                    if(mtype == MessageType.CHAT){
                        msg = answer.getString("msg");
                    }
                    callback.onSuccess(mtime, mid, msg);
                }else {
                    callback.onError(genLDAnswer(answer,errorCode));
                }
            }
        });
    }


    private RTMHistoryMessageResult buildHistoryMessageResult(Answer answer, ConversationType type, long toid) {
        RTMHistoryMessageResult result = new RTMHistoryMessageResult();

        if (answer != null && answer.getErrorCode() == okRet) {

            result.count = LDUtils.wantInt(answer, "num");
            result.lastId = LDUtils.wantLong(answer, "lastid");
            result.beginMsec = LDUtils.wantLong(answer, "begin");
            result.endMsec = LDUtils.wantLong(answer, "end");
            result.messages = new ArrayList<>();

            ArrayList<List<Object>> messages = (ArrayList<List<Object>>) answer.want("msgs");
            for (List<Object> value : messages) {
                boolean delete = (boolean) (value.get(4));
                if (delete)
                    continue;

                RTMHistoryMessage tmp = parseRTMHistoryMessage(toid, type, value);
                result.messages.add(tmp);
            }
        }
        result.count = result.messages.size();
        return result;
    }

    private Quest genGetMessageQuest(long id, boolean desc, int count, long beginMsec, long endMsec, long lastId, List<Integer> mtypes, ConversationType type)
    {
        String method = "", toWhere = "";
        switch (type) {
            case GROUP:
                method = "getgroupmsg";
                toWhere = "gid";
                break;
            case ROOM:
                method = "getroommsg";
                toWhere = "rid";
                break;
            case P2P:
                method = "getp2pmsg";
                toWhere = "ouid";
                break;
            case BROADCAST:
                method = "getbroadcastmsg";
                toWhere = "";
                break;
        }

        Quest quest = new Quest(method);
        if (!toWhere.equals(""))
            quest.param(toWhere, id);
        quest.param("desc", desc);
        quest.param("num", count);

        quest.param("begin", beginMsec);
        quest.param("end", endMsec);
        quest.param("lastid", lastId);

        if (mtypes != null && mtypes.size() > 0)
            quest.param("mtypes", mtypes);
        return quest;
    }

    public void getHistoryMessages(final ICallback<RTMHistoryMessageResult> callback, final long id, boolean desc, int count, long beginMsec, long endMsec, long lastId, List<Integer> mtypes, final ConversationType type
    , SendSource sendSourceType) {
        Quest quest = genGetMessageQuest(id, desc, count, beginMsec, endMsec, lastId, mtypes, type);
        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    RTMHistoryMessageResult result = buildHistoryMessageResult(answer,type, id);
                    callback.onSuccess(result);
                }else
                    callback.onError( genLDAnswer(answer,errorCode));
            }
        });
    }


    private RTMSingleMessage buildSingleMessage(Answer answer) {
        RTMSingleMessage message = new RTMSingleMessage();
        if (answer !=null && answer.getErrorCode() == okRet && answer.getPayload().keySet().size()>0) {
            message.cusorId = LDUtils.wantLong(answer,"id");
            message.messageType = (byte) LDUtils.wantInt(answer,"mtype");
            message.attrs = LDUtils.wantString(answer,"attrs");
            message.modifiedTime = LDUtils.wantLong(answer,"mtime");
            Object obj = answer.want("msg");
            if (message.messageType >= MessageType.IMAGEFILE && message.messageType <= MessageType.NORMALFILE) {
                message.fileInfo = new FileStruct();
                message.attrs = parseFileInfo(String.valueOf(obj), message.attrs,message.messageType,message.fileInfo);
            }
            else{
                if (obj instanceof byte[]){
                    byte[] data = (byte[]) obj;
                    message.binaryMessage = data;
                }
                else
                    message.stringMessage = String.valueOf(obj);
            }
        }
        return message;
    }

    public void getSingleMessage(final ICallback<RTMSingleMessage> callback, long fromUid, long xid, long messageId, int type) {
        Quest quest = new Quest("getmsg");
        quest.param("mid", messageId);
        quest.param("xid", xid);
        quest.param("from", fromUid);
        quest.param("type", type);

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                RTMSingleMessage RTMSingleMessage = new RTMSingleMessage();
                if (errorCode == okRet) {
                    RTMSingleMessage = buildSingleMessage(answer);
                    callback.onSuccess(RTMSingleMessage);
                }else
                    callback.onError(genLDAnswer(answer,errorCode));
            }
        });
    }


    public void delMessage(IEmptyCallback callback, long fromUid, long xid, long messageId, int type) {
        Quest quest = new Quest("delmsg");
        quest.param("mid", messageId);
        quest.param("xid", xid);
        quest.param("from", fromUid);
        quest.param("type", type);

        sendQuestEmptyCallback(callback,quest);
    }


    ArrayList<RTMConversationInfo> parseHis(ArrayList<List<Object>> msgs, ArrayList<Long> conversations, ArrayList<Integer> unreadNums, ConversationType type) {
        ArrayList<RTMConversationInfo> rets = new ArrayList<>();

        try {
            for (int i = 0; i < conversations.size(); i++) {
                RTMConversationInfo conversationInfo = new RTMConversationInfo();
                conversationInfo.targetId = conversations.get(i);
                conversationInfo.unreadNum = unreadNums.get(i);
                List<Object> value = msgs.get(i);

                conversationInfo.lastHistortMessage = parseRTMHistoryMessage(conversationInfo.targetId, type, value);
                rets.add(conversationInfo);
            }
        }
        catch (Exception ex){
            errorRecorder.recordError(ex);
        }
        return rets;
    }



    public void clearUnread(IEmptyCallback callback) {
        Quest quest = new Quest("cleanunread");
        sendQuestEmptyCallback(callback, quest);
    }

    public void getALLUnreadConversationList(boolean clear, long mtime, List<Integer> mtypes, final ICallback<RTMUnreadConversationInfo> callback) {
        Quest quest = new Quest("getunreadconversationlist");
        quest.param("clear", clear);
        if (mtime > 0)
            quest.param("mtime", mtime);
        if (mtypes != null)
            quest.param("mtypes", mtypes);

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == ErrorCode.FPNN_EC_OK.value()) {
                    RTMUnreadConversationInfo rtmUnreadConversationInfo = new RTMUnreadConversationInfo();
                    ArrayList<List<Object>> groupmsgs = (ArrayList<List<Object>>) answer.want("groupMsgs");
                    ArrayList<Long> groupconversations = LDUtils.wantLongList(answer, "groupConversations");
                    ArrayList<Integer> groupUnreads = LDUtils.wantIntList(answer, "groupUnreads");
                    rtmUnreadConversationInfo.groupUnread = parseHis(groupmsgs, groupconversations, groupUnreads, ConversationType.GROUP);

                    ArrayList<List<Object>> p2pMsgs = (ArrayList<List<Object>>) answer.want("p2pMsgs");
                    ArrayList<Long> p2pConversations = LDUtils.wantLongList(answer, "p2pConversations");
                    ArrayList<Integer> p2pUnreads = LDUtils.wantIntList(answer, "p2pUnreads");
                    rtmUnreadConversationInfo.p2pUnread = parseHis(p2pMsgs, p2pConversations, p2pUnreads, ConversationType.P2P);
                    callback.onSuccess(rtmUnreadConversationInfo);
                }else{
                    callback.onError(genLDAnswer(answer,errorCode));
                }
            }
        });
    }


    public void getConversationList(long mtime, List<Integer> mtypes, ConversationType conversationType, final ICallback<List<RTMConversationInfo>> callback, SendSource sendSourceType){
        Quest quest = null;

        if (conversationType == ConversationType.P2P)
            quest = new Quest("getp2pconversationlist");
        else if (conversationType == ConversationType.GROUP)
            quest = new Quest("getgroupconversationlist");
        else {
            callback.onError(genLDAnswer(RTMErrorCode.RTM_EC_UNKNOWN_ERROR.value(),"invalid getConversationList type " + conversationType));
            return;
        }

        if (mtime > 0)
            quest.param("mtime", mtime);
        if (mtypes != null)
            quest.param("mtypes", mtypes);

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    ArrayList<List<Object>> msgs = (ArrayList<List<Object>>) answer.want("msgs");
                    ArrayList<Long> conversations = LDUtils.wantLongList(answer, "conversations");
                    ArrayList<Integer> unreadNums = LDUtils.wantIntList(answer, "unreads");

                    ArrayList<RTMConversationInfo> Conversationinfos = parseHis(msgs, conversations, unreadNums, conversationType);
                    callback.onSuccess(Conversationinfos);
                }else
                    callback.onError(genLDAnswer(answer,errorCode));
            }
        });
    }


    public void removeSession(long id, boolean oneway, IEmptyCallback callback){
        Quest quest = new Quest("removesession");
        quest.param("toid", id);
        quest.param("oneway", oneway);
        sendQuestEmptyCallback(callback, quest);
    }


    public void translate( String text, String destinationLanguage, String sourceLanguage,
                           TranslateType type, ProfanityType profanity,final ICallback<TranslatedInfo> callback) {
        Quest quest = new Quest("translate");
        quest.param("text", text);
        quest.param("dst", destinationLanguage);

        if (sourceLanguage.length() > 0)
            quest.param("src", sourceLanguage);

        if (type == TranslateType.Mail)
            quest.param("type", "mail");
        else
            quest.param("type", "chat");

        if (profanity != null) {
            switch (profanity) {
                case Censor:
                    quest.param("profanity", "censor");
                    break;
                case Off:
                    quest.param("profanity", "off");
                    break;
            }
        }

        sendFileQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    TranslatedInfo tm = new TranslatedInfo();
                    tm.source = LDUtils.wantString(answer,"source");
                    tm.target = LDUtils.wantString(answer,"target");
                    tm.sourceText = LDUtils.wantString(answer,"sourceText");
                    tm.targetText = LDUtils.wantString(answer,"targetText");
                    callback.onSuccess(tm);
                }else{
                    callback.onError(genLDAnswer(answer, errorCode));
                }
            }
        });
    }
}
