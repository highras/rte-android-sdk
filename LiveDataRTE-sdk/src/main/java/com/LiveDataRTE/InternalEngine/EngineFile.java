package com.LiveDataRTE.InternalEngine;

import com.LiveDataRTE.LDRecordAudio;
import com.LiveDataRTE.RTMLib.RTMErrorCode;
import com.fpnn.sdk.ErrorCode;
import com.fpnn.sdk.FunctionalAnswerCallback;
import com.fpnn.sdk.TCPClient;
import com.fpnn.sdk.proto.Answer;
import com.fpnn.sdk.proto.Quest;
import com.LiveDataRTE.LiveDataStruct.*;
import com.LiveDataRTE.LDInterface.*;

import org.json.JSONObject;
import java.security.MessageDigest;

class EngineFile extends EngineRTC {
    interface DoubleStringCallback{
        void onResult(String str1, String str2, LDAnswer answer);
    }


    private static class SendFileInfo {
        public ConversationType actionType;

        public long xid;
        public int mtype;
        public byte[] fileContent;
        public String filename;
        public JSONObject attrs;

        public String token;
        public String endpoint;
        public String fileExt;
        public long lastActionTimestamp;
        public ISendFileCallback callback;
        public RecordAudioStruct audioAttrs;//给语音用 语言+时长
    }

    //===========================[ File Token ]=========================//
    private void fileToken(final DoubleStringCallback callback, ConversationType tokenType, long xid) {
        Quest quest = new Quest("filetoken");
        switch (tokenType) {
            case P2P:
                quest.param("cmd", "sendfile");
                quest.param("to", xid);
                break;
            case GROUP:
                quest.param("cmd", "sendgroupfile");
                quest.param("gid", xid);
                break;
            case ROOM:
                quest.param("cmd", "sendroomfile");
                quest.param("rid", xid);
                break;
            case BROADCAST:
                quest.param("cmd", "uploadfile");
                break;
        }

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                String token = "" ,endpoint = "";
                if (errorCode == okRet) {
                    token = LDUtils.wantString(answer,"token");
                    endpoint = LDUtils.wantString(answer,"endpoint");
                }
                callback.onResult(token, endpoint, genLDAnswer(answer,errorCode));
            }
        });
    }


    //===========================[ File Utilies ]=========================//

    private String extraFileExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx == -1)
            return "";

        return filename.substring(idx + 1);
    }

    private String buildFileAttrs(SendFileInfo info) {
        String fileAttrs = "";
        try {
            MessageDigest  md5 = MessageDigest.getInstance("MD5");
            md5.update(info.fileContent);
            byte[] md5Binary = md5.digest();
            String md5Hex = LDUtils.bytesToHexString(md5Binary, true) + ":" + info.token;

            md5 = MessageDigest.getInstance("MD5");
            md5.update(md5Hex.getBytes("UTF-8"));
            md5Binary = md5.digest();
            md5Hex = LDUtils.bytesToHexString(md5Binary, true);
            JSONObject allatrrs = new JSONObject();

            if (info.attrs != null)
                allatrrs.put("custom",info.attrs);
            JSONObject rtmAttrs = new JSONObject();

            rtmAttrs.put("sign",md5Hex);

            if (info.filename != null && info.filename.length() > 0){
                rtmAttrs.put("filename",info.filename);
                info.fileExt = extraFileExtension(info.filename);
                rtmAttrs.put("ext", info.fileExt);
            }
            if (info.audioAttrs != null) {
                rtmAttrs.put("lang",info.audioAttrs.lang);
                rtmAttrs.put("duration",info.audioAttrs.duration);
                rtmAttrs.put("ext","amr");
                rtmAttrs.put("type","audiomsg");
                rtmAttrs.put("codec","AMR_WB");
                rtmAttrs.put("srate",16000);
            }
            allatrrs.put("rtm",rtmAttrs);
            fileAttrs = allatrrs.toString();
        } catch (Exception e) {
            errorRecorder.recordError("buildFileAttrs error " + e.getMessage());
        }
        return fileAttrs;
    }

    private Quest buildSendFileQuest(SendFileInfo info) {
        Quest quest = null;
        switch (info.actionType) {
            case P2P:
                quest = new Quest("sendfile");
                quest.param("to", info.xid);
                break;

            case GROUP:
                quest = new Quest("sendgroupfile");
                quest.param("gid", info.xid);
                break;

            case ROOM:
                quest = new Quest("sendroomfile");
                quest.param("rid", info.xid);
                break;
        }

        quest.param("pid", getPid());
        quest.param("from", getUid());
        quest.param("token", info.token);
        quest.param("mtype", info.mtype);
        quest.param("mid", Genid.genMid());

        quest.param("file", info.fileContent);
        quest.param("attrs", buildFileAttrs(info));

        return quest;
    }

    private int sendFileWithClient(final SendFileInfo info, final TCPClient client) {
        final Quest quest = buildSendFileQuest(info);
        client.sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    try {
                        long mtime = LDUtils.wantLong(answer,"mtime");
                        activeFileGateClient(info.endpoint, client);
                        info.callback.onSuccess(mtime, LDUtils.wantLong(quest,"mid"));
                        return;
                    } catch (Exception e) {
                        errorCode = ErrorCode.FPNN_EC_CORE_INVALID_PACKAGE.value();
                    }
                }
                else
                    info.callback.onError(genLDAnswer(answer,errorCode));
            }
        });

        return okRet;
    }

    private int sendFileWithoutClient(final SendFileInfo info){
        String fileGateEndpoint;
        fileGateEndpoint = info.endpoint;

        final TCPClient client = TCPClient.create(fileGateEndpoint, true);
        client.setQuestTimeout(rtmConfig.globalFileQuestTimeoutSeconds);
        client.setErrorRecorder(errorRecorder);

        sendFileWithClient(info, client);

        return okRet;
    }

    private void getFileTokenCallback(SendFileInfo info, String token, String endpoint, LDAnswer answer) throws InterruptedException {
        if (answer.errorCode == okRet) {
            info.token = token;
            info.endpoint = endpoint;
            int  err;

            TCPClient fileClient = fecthFileGateClient(info.endpoint);
            if (fileClient != null)
                err = sendFileWithClient(info, fileClient);
            else
                err = sendFileWithoutClient(info);

            if (err == okRet)
                return;
            else
                errorRecorder.recordError("send file error");
        } else
            info.callback.onError(answer);
    }



    //===========================[ Real Send File ]=========================//
     public void realSendFile(final ISendFileCallback callback, ConversationType tokenType, long targetId, int mtype, byte[] fileContent, String filename, JSONObject attrs, RecordAudioStruct audioAttrs, SendSource sendSourceType) {
        if (mtype < MessageType.IMAGEFILE || mtype > MessageType.NORMALFILE) {
            callback.onError(genLDAnswer(RTMErrorCode.RTM_EC_INVALID_MTYPE.value()));
            return ;
        }

        byte[] realData = fileContent;
        String realName = filename;
        if (audioAttrs!=null && audioAttrs.audioData != null) {
            if (!LDRecordAudio.getInstance().checkAudio(audioAttrs.audioData)) {
                LDAnswer tt = new LDAnswer();
                tt.errorCode = RTMErrorCode.RTM_EC_INVALID_FILE_OR_SIGN_OR_TOKEN.value();
                tt.errorMsg = "invalid audio type";
                callback.onError(tt);
                return;
            }
            realData = audioAttrs.audioData;
            realName = "";
        }

        SendFileInfo info = new SendFileInfo();
        info.actionType = tokenType;
        info.xid = targetId;
        info.mtype = mtype;
        info.fileContent = realData;
        info.filename = realName;
        info.attrs = attrs;
        info.lastActionTimestamp = LDUtils.getCurrentMilliseconds();
        info.callback = callback;
        info.audioAttrs = audioAttrs;
        final SendFileInfo inFile = info;
        fileToken(new DoubleStringCallback() {
            @Override
            public void onResult(String token, String endpoint, LDAnswer answer) {
                try {
                    getFileTokenCallback(inFile, token, endpoint, answer);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, tokenType, info.xid);
    }


    public void internalUploadFile(final FileMessageType mtype, final String fileName, final byte[] fileContent, final RecordAudioStruct audioInfo, final IUploadFileCallback callback){
        fileToken(new DoubleStringCallback() {
            @Override
            public void onResult(String token, final String endpoint, LDAnswer tokenanswer) {
                    if (tokenanswer.errorCode == okRet){
                        TCPClient fileClient = fecthFileGateClient(endpoint);
                        if (fileClient != null){
                            Quest quest = new Quest("uploadfile");
                            quest.param("pid", getPid());
                            quest.param("uid",getUid());
                            quest.param("token",token);
                            quest.param("file",fileContent);
                            quest.param("mtype", mtype.value());

                            SendFileInfo sendFileInfo = new SendFileInfo();
                            sendFileInfo.fileContent = fileContent;
                            sendFileInfo.filename = fileName;
                            sendFileInfo.token = token;
                            sendFileInfo.audioAttrs = audioInfo;
                            String tmp = buildFileAttrs(sendFileInfo);

                            quest.param("attrs", tmp);
                            fileClient.sendQuest(quest, new FunctionalAnswerCallback() {
                                @Override
                                public void onAnswer(Answer answer, int errorCode) {
                                    String url = "";
                                    long size = 0;

                                    if (errorCode == okRet){
                                        url = LDUtils.wantString(answer,"url");
                                        size = LDUtils.wantLong(answer,"size");
                                        callback.onSuccess(url, size);
                                    }else
                                        callback.onError(genLDAnswer(answer,errorCode));
                                }
                            }, rtmConfig.globalFileQuestTimeoutSeconds);
                        }
                        else{
                            final TCPClient client = TCPClient.create(endpoint, true);
                            Quest quest = new Quest("uploadfile");
                            quest.param("pid", getPid());
                            quest.param("uid",getUid());
                            quest.param("token",token);
                            quest.param("file",fileContent);
                            quest.param("mtype", mtype.value());

                            SendFileInfo sendFileInfo = new SendFileInfo();
                            sendFileInfo.fileContent = fileContent;
                            sendFileInfo.filename = fileName;
                            sendFileInfo.token = token;
                            sendFileInfo.audioAttrs = audioInfo;
                            String tmp = buildFileAttrs(sendFileInfo);
                            quest.param("attrs", tmp);

                            client.sendQuest(quest, new FunctionalAnswerCallback() {
                                @Override
                                public void onAnswer(Answer answer, int errorCode) {
                                    String url = "";
                                    long size = 0;

                                    if (errorCode == okRet){
                                        url = LDUtils.wantString(answer,"url");
                                        size = LDUtils.wantLong(answer,"size");
                                        activeFileGateClient(endpoint, client);
                                        callback.onSuccess(url, size);
                                    }else
                                        callback.onError(genLDAnswer(answer,errorCode));
                                }
                            }, rtmConfig.globalFileQuestTimeoutSeconds);
                        }
                    }
                    else {
                        callback.onError(tokenanswer);
                    }
                }
        }, ConversationType.BROADCAST, 0);

    }
}
