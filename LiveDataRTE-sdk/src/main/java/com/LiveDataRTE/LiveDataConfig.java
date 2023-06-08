package com.LiveDataRTE;

import com.fpnn.sdk.ErrorRecorder;

public class LiveDataConfig {
    final public static int lostConnectionAfterLastPingInSeconds = 60;
    final public static int globalMaxThread = 8;

    public final static String SDKVersion = "1.0.0";
    public ErrorRecorder defaultErrorRecorder = new ErrorRecorder();
    public int globalQuestTimeoutSeconds = 30;   //请求超时时间
    public int globalFileQuestTimeoutSeconds = 120;  //传输文件/音频/翻译/语音识别/文本检测 最大超时时间
}
