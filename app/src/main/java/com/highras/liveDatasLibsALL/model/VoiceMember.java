package com.highras.liveDatasLibsALL.model;

/**
 * @author fengzi
 * @date 2022/5/17 17:25
 */
public class VoiceMember {
    private long uid;
    private String nickName;
    // 上次说话时间
    private long previousVoiceTime;

    public VoiceMember(long uid, String nickName) {
        this.uid = uid;
        this.nickName = nickName;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public long getPreviousVoiceTime() {
        return previousVoiceTime;
    }

    public void setPreviousVoiceTime(long previousVoiceTime) {
        this.previousVoiceTime = previousVoiceTime;
    }
}
