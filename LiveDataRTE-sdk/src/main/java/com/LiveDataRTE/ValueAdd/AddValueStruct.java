package com.LiveDataRTE.ValueAdd;

import java.util.List;

public class AddValueStruct {
    public static class AudioTextStruct{
        public String  text = ""; //语音转文字的结果
        public String  lang = ""; //语音转文字的语言
    }


    //图片/音频/视频检测类型
    public enum CheckSourceType {
        URL, //url地址
        CONTENT //二进制内容
    }


    public static class CheckResult{ //文本/语音/图片/视频检测结构
        public int result = -1;      //检测结果 0-通过 2-不通过
        public String text;     //(只对文本检测接口)返回text，文本内容,含有的敏感词会被替换为*，如果检测通过,则无此字段
        public List<String> wlist; //(只对文本检测接口)敏感词列表
        public List<Integer> tags; //触发的分类，比如涉黄涉政等等，(只有审核结果不通过才有此值)
    }

    public enum CheckType {
        PIC,
        AUDIO,
        VIDEO
    }

    public enum TranslateType {
        Chat,
        Mail
    }

    //敏感词过滤类型
    public enum ProfanityType {
        Off, //不进行敏感词过滤
        Censor //如果有敏感词 结果用*号代替
    }

}
