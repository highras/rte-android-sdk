package com.LiveDataRTE;

import com.LiveDataRTE.RTCLib.RTCStruct;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class LiveDataStruct {

    public static class MessageType {
        public static final int NOTIFICATION = 6; //通知
        public static final int CHAT = 30; //聊天
        public static final int CMD = 32;  //命令
        public static final int IMAGEFILE = 40; //图片
        public static final int AUDIOMESSAGE = 41; //语音消息
        public static final int VIDEOFILE = 42; //视频文件
        public static final int AUDIOFILE = 43; //音频文件
        public static final int NORMALFILE = 50; //一般文件
    }

    public enum ConversationType {
        P2P    (1), //p2p消息
        GROUP  (2), //群组消息
        ROOM   (3), //房间消息
        BROADCAST   (4); //广播消息
        private int value;

        ConversationType (int value) {
            this.value = value;
        }
        public int value() {
            return value;
        }
        public static ConversationType intToEnum(int value) {    //将数值转换成枚举值
            switch (value) {
                case 1:
                    return P2P;
                case 2:
                    return GROUP;
                case 3:
                    return ROOM;
                case 4:
                    return BROADCAST;
                default :
                    return null;
            }
        }
    }


    public enum AddPermission {
        PermitAll  (0), //允许任何人
        NeedVerify (1), //需要验证
        ForbidAll  (2);//禁止任何人
        private int value;

        AddPermission (int value) {
            this.value = value;
        }
        public int value() {
            return value;
        }

        public static AddPermission intToEnum(int value) {    //将数值转换成枚举值
            switch (value) {
                case 0:
                    return PermitAll;
                case 1:
                    return NeedVerify;
                case 2:
                    return ForbidAll;
                default:
                    return PermitAll;
            }
        }
    }

    public enum FileMessageType {
        IMAGEFILE(40),  //图片
        VIDEOFILE(42),  //视频文件
        AUDIOFILE(43),  //音频文件
        NORMALFILE(50); //一般文件
        private int value;

        FileMessageType(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }


        public static FileMessageType intToEnum(int value) {    //将数值转换成枚举值
            switch (value) {
                case 40:
                    return IMAGEFILE;
                case 42:
                    return VIDEOFILE;
                case 43:
                    return AUDIOFILE;
                case 50:
                    return NORMALFILE;
                default:
                    return IMAGEFILE;
            }
        }
    }

    public static class SystemNotificationType{
        public static final int AddFriend = 1;
        public static final int DenyAddFriend = 2;
        public static final int AgreeAddFriend = 21;
        public static final int AgreeApplyGroupResult = 22; //同意申请入群的结果通知
        public static final int RefuseApplyGroupResult = 4; //拒绝申请入群的结果通知
        public static final int AgreeInviteGroup = 24; //同意邀请入群通知(自己发送的入群邀请被同意时会收到这个通知)
        public static final int RefuseInviteGroup = 7; //拒绝邀请入群通知(自己发送的入群邀请被拒绝时会收到这个通知)
        public static final int AddGroup = 3; //入群申请通知（有人申请入群时管理员会收到这个通知）
        public static final int InviteIntoGroup = 5;// 被邀请入群通知
        public static final int FriendChanged = 15;
        //        public static final int FriendOnlineOffline = 14;
        public static final int GroupChanged = 17;
        public static final int RoomChanged = 18;
        public static final int InviteIntoRoom = 11;
        public static final int AgreeInviteRoom = 25; //同意邀请房间通知(自己发送的加入房间邀请被同意时会收到这个通知)
        public static final int RefuseInviteRoom = 13; //拒绝邀请房间通知(自己发送的加入房间邀请被拒绝时会收到这个通知)
        public static final int RoomMemberChanged = 20; //有用户加入或者退出房间
        public static final int RoomAddMangager = 29; //添加房间管理员
        public static final int RoomRemoveMangager = 30; //删除房间管理员
        public static final int RoomOwnerrChanged = 31; //房主变更通知

        public static final int GroupMemberChanged = 19; //有用户入群或退群
        public static final int GroupAddMangager = 26; //添加群组管理员
        public static final int GroupRemoveMangager = 27; //删除群组管理员
        public static final int GroupOwnerrChanged = 28; //群主变更通知
        public static final int CustomNotification = 999;
    }

    public static class RecordAudioStruct {
        public  int duration; //语音时长
        public String lang; //语种
        public byte[] audioData;// 语音内容
        public File file;//语音文件路径
    }

    //errorCode==0为成功 非0错误 错误信息详见errorMsg字段
    public static class LDAnswer
    {
        public int errorCode = -1;
        public String errorMsg = "";
        public LDAnswer(){}
        public LDAnswer(int _code, String msg){
            errorCode = _code;
            errorMsg = msg;
        }
        public String getErrInfo(){
            return  " " + errorCode + "-" + errorMsg;
        }
    }

    public static class TranslatedInfo //翻译的消息结构
    {
        public String source = ""; //原语言
        public String target = ""; //翻译的目标语言
        public String sourceText = ""; //原文本
        public String targetText = ""; //设置自动翻译后的目标文本

        public void copy(TranslatedInfo translatedInfo){
            translatedInfo.source = this.source;
            translatedInfo.target = this.target;
            translatedInfo.sourceText = this.sourceText;
            translatedInfo.targetText = this.targetText;
        }
    }

    public static class Chat{
        public int roomTyppe; //房间类型 1-voice 2-video 3-语聊房
        public long roomId; //房间id
        public long owner;//房主
        public List<Long> uids; //房间的成员uid
        public List<Long> managers; //房间的管理员id
    }

    
    public static class DevicePushOption{
        public Map<Long, List<Integer>> p2pPushOptions; //p2p的不推送设置 key-uid value-messagetypes （如果集合里有0 表示所有type均不推送）
        public Map<Long, List<Integer>> groupPushOptions; //group的不推送设置 key-groupid value-messagetypes（如果集合里有0 表示所有type均不推送）
    }

    public static class FileStruct{ //文件结构
        public FileMessageType fileMessageType;
        public String fileName="";
        public String url = "";     //文件的url地址
        public long fileSize = 0;  //文件大小(字节)
        public String surl = "";    //缩略图的url地址

        //离线语音内容
        public String lang;      //语言
        public int duration = 0; //语音长度(毫秒)

        public void copy(FileStruct fileStruct){
            fileStruct.fileMessageType = this.fileMessageType;
            fileStruct.fileName = this.fileName;
            fileStruct.url = this.url;
            fileStruct.surl = this.surl;
            fileStruct.fileSize = this.fileSize;
            fileStruct.lang = this.lang;
            fileStruct.duration = this.duration;
        }
    }

    public enum SendSource {
        IM(1),
        RTM(2);
        private int value;

        SendSource (int value) {
            this.value = value;
        }
        public int value() {
            return value;
        }
    }
}
