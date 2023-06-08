package com.LiveDataRTE.IMLib;

import com.LiveDataRTE.LiveDataStruct.*;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class IMStruct {
    public static class IMMessage {
        public IMMessageType imMessageType; //消息类型
        public long fromUid;    //发送者id 若等于自己uid 说明发送者是自己
        public long targetId;    //目标id 根据会话类型 userId/groupId/roomId
        public long messageId;  //消息id
        public String stringMessage;  //消息内容
        public String attrs;        //客户端发送消息自定义的附加信息
        public long modifiedTime;   //服务器处理返回时间
        public FileStruct fileInfo; //文件结构信息(语音的信息也存在这里)
        public TranslatedInfo translatedInfo = new TranslatedInfo(); //聊天信息结构(push)

        public String getInfo() {
            String info ="";
            StringBuilder kk = new StringBuilder();
            JSONObject all = new JSONObject();
            JSONObject file = new JSONObject();
            JSONObject trans = new JSONObject();
            try {

                all.put("fromUid", fromUid);
                all.put("imMessageType", imMessageType.value());
                all.put("targetId", targetId);
                all.put("messageId", messageId);
                all.put("stringMessage", stringMessage);
                all.put("attrs", attrs);
                all.put("modifiedTime", modifiedTime);

                if (fileInfo!=null) {
                    file.put("url", fileInfo.url);
                    file.put("filename", fileInfo.fileName);
                    file.put("filetype", fileInfo.fileMessageType.value());
                    file.put("fileSize", fileInfo.fileSize);
                    file.put("surl", fileInfo.surl);
                    file.put("lang", fileInfo.lang);
                    file.put("duration", fileInfo.duration);
                    all.put("fileInfo", file);
                }
                if (translatedInfo != null) {
                    trans.put("source", translatedInfo.source);
                    trans.put("target", translatedInfo.target);
                    trans.put("sourceText", translatedInfo.sourceText);
                    trans.put("targetText", translatedInfo.targetText);
                    all.put("transInfo", trans);
                }

                info = all.toString(10);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return info;
        }
    }


    public enum IMMessageType {
        Text(1), //文本类消息
        AudioMessage(2), //离线语音消息
        File(3); //文件类消息
        private int value;

        IMMessageType (int value) {
            this.value = value;
        }
        public int value() {
            return value;
        }
        public static IMMessageType intToEnum(int value) {    //将数值转换成枚举值
            switch (value) {
                case 1:
                    return Text;
                case 2:
                    return AudioMessage;
                case 3:
                    return File;
                default:
                    return Text;
            }
        }
    }

    public static class IMHistoryMessage extends IMMessage //历史消息结构
    {
        public long cursorId;       //历史消息的索引id
    }

    //历史消息结果
    public static class IMHistoryMessageResult{
        public int count;   //实际返回消息条数
        public long lastId; //最后一条消息的索引id
        public long beginMsec; //下一次查询用的开始时间戳(毫秒)
        public long endMsec;    //下一次查询用的结束时间戳(毫秒)
        public List<IMHistoryMessage> messages; //历史消息详细信息结构集合
    }

    //getmsg单条消息结构
    public static class
    IMSingleMessage{
        public long cusorId; //消息的索引id(数据库中存储的id)
        public String stringMessage; //文本数据
        public String attrs;    //消息的附加属性信息(客户端自定义)
        public long modifiedTime;   //服务器应答时间
        public FileStruct fileInfo; //文件类型结构 messageType为FileMessageType的都存在这里

        public String getInfo()
        {
            String info ="";
            JSONObject all = new JSONObject();
            JSONObject file = new JSONObject();
            try {

                all.put("cusorId", cusorId);
                all.put("stringMessage", stringMessage);
                all.put("attrs", attrs);
                all.put("modifiedTime", modifiedTime);

                if (fileInfo!=null) {
                    file.put("url", fileInfo.url);
                    file.put("duration", fileInfo.duration);
                    file.put("fileSize", fileInfo.fileSize);
                    file.put("lang", fileInfo.lang);
                    file.put("surl", fileInfo.surl);
                    all.put("fileInfo", file);
                }
                info = all.toString(10);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return info;
        }
    }


    public  static class IMConversationInfo{
        public long targetId;  //目标id userId/groupid
        public int unreadNum; //未读数量
        public IMHistoryMessage lastHistortMessage = new IMHistoryMessage();//最后一条消息
    }


    public static List<Integer> IMChatMTypes = new ArrayList<Integer>() {{
        add(MessageType.CHAT);
        add(MessageType.IMAGEFILE);
        add(MessageType.AUDIOFILE);
        add(MessageType.VIDEOFILE);
        add(MessageType.NORMALFILE);
        add(MessageType.AUDIOMESSAGE);
        }
    };


    public  static class IMUnreadConversationInfo{
        public ArrayList<IMConversationInfo> groupUnread = new ArrayList<>(); //群组未读消息
        public ArrayList<IMConversationInfo> p2pUnread = new ArrayList<>();   //p2p未读消息
    }


    //房间信息
    public static class IMRoomInfo {
        public long roomId; //房间id
        public long ownerId;//房主id
        public List<Long> managerUids;//管理员id;
        public String name;  //房间名称
        public String portraitUrl; //房间头像url
        public String profile;  //房间简介
        public String customData; //房间自定义数据
        public int inviteGrant; //房间邀请权限 0-不允许普通成员邀请 1-允许普通成员邀请

        public IMRoomInfo(long roomId, long ownerId, List<Long> managerUids, String name, String portraitUrl, String profile, String customData, int inviteGrant) {
            this.roomId = roomId;
            this.ownerId = ownerId;
            this.managerUids = managerUids;
            this.name = name;
            this.portraitUrl = portraitUrl;
            this.profile = profile;
            this.customData = customData;
            this.inviteGrant = inviteGrant;
        }

        public String getinfo(){
            JSONObject show = new JSONObject();
            try {
                show.put("roomId", roomId);
                show.put("ownerId", ownerId);
                show.put("managerUids", managerUids.toString());
                show.put("name", name);
                show.put("portraitUrl", portraitUrl);
                show.put("profile", profile);
                show.put("customData", customData);
                show.put("inviteGrant", inviteGrant);
            }
            catch (Exception ex){

            }
            return show.toString();
        }
    }

    //群组信息
    public static class IMGroupInfo {
        public long groupId; //群组id
        public long ownerId;//群主id
        public List<Long> managerUids;//管理员id;
        public String name;  //群组名称
        public String portraitUrl; //群组头像url
        public String profile;  //群组简介
        public String customData; //群组自定义数据
        public AddPermission addPermission; //群组加入权限
        public int inviteGrant; //群组邀请权限 0-不允许普通成员邀请 1-允许普通成员邀请

        public IMGroupInfo(long groupId, long ownerId, List<Long> managerUids, String name, String portraitUrl, String profile, String customData, AddPermission addPermission, int inviteGrant) {
            this.groupId = groupId;
            this.ownerId = ownerId;
            this.managerUids = managerUids;
            this.name = name;
            this.portraitUrl = portraitUrl;
            this.profile = profile;
            this.customData = customData;
            this.addPermission = addPermission;
            this.inviteGrant = inviteGrant;
        }

        public String getinfo(){
            JSONObject show = new JSONObject();
            try {
                show.put("groupId", groupId);
                show.put("ownerId", ownerId);
                show.put("managerUids", managerUids.toString());
                show.put("name", name);
                show.put("portraitUrl", portraitUrl);
                show.put("profile", profile);
                show.put("customData", customData);
                show.put("addPermission", addPermission.value());
                show.put("inviteGrant", inviteGrant);
            }
            catch (Exception ex){

            }
            return show.toString();
        }
    }

    //邀请加入信息
    public static class IMInviteInfo{
        public long targetId; //邀请加入的群组id或者房间id
        public long inviteTime; //邀请时间
        public long fromId; //邀请人id
        public String attrs; //用户自定义数据

        public IMInviteInfo(long targetId, long inviteTime, long fromId, String attrs) {
            this.targetId = targetId;
            this.inviteTime = inviteTime;
            this.fromId = fromId;
            this.attrs = attrs;
        }
    }

    //申请加入信息(群组或者好友)
    public static class IMApplyInfo {
        public long fromId; //用户id
        public long requestTime; //申请时间
        public String grantExtra; //留言
        public String attrs; //用户自定义数据

        public IMApplyInfo(long userId, long requestTime, String grantExtra, String attrs) {
            this.fromId = userId;
            this.requestTime = requestTime;
            this.grantExtra = grantExtra;
            this.attrs = attrs;
        }
    }

    //请求加入信息(自己添加好友或者群组)
    public static class IMRquestInfo {
        public long targetId; //用户id
        public long applyTime; //请求时间
        public String attrs; //用户自定义数据

        public IMRquestInfo(long userId, long applyTime, String attrs) {
            this.targetId = userId;
            this.applyTime = applyTime;
            this.attrs = attrs;
        }
    }

    public static class IMUserInfo {
        public long userId;  //用户id
        public String name;  //用户名称
        public String portraitUrl; //用户头像
        public String profile;  //用户简介
        public String customData; //用户自定义数据
        public AddPermission applyGrant; //用户被添加权限

        public IMUserInfo(long userId, String name, String portraitUrl, String profile, String customData, AddPermission applyGrant) {
            this.userId = userId;
            this.name = name;
            this.portraitUrl = portraitUrl;
            this.profile = profile;
            this.customData = customData;
            this.applyGrant = applyGrant;
        }
    }


    // 房间成员信息
    public static class IMRoomMemberInfo {
        public long userId;
        public int roleType; //0-房主、1-管理员、2-普通成员)

        public IMRoomMemberInfo(long uid, int roleType) {
            this.userId = uid;
            this.roleType = roleType;
        }
    }


    // 群成员信息
    public static class IMGroupMemberInfo {
        public long userId;
        public int roleType; //0-群主、1-管理员、2-普通成员)
        public int isOnLine; //0离线 1在线

        public IMGroupMemberInfo(long uid, int roleType, int isOnLine) {
            this.userId = uid;
            this.roleType = roleType;
            this.isOnLine = isOnLine;
        }
    }

}
