~~~c++
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
    }


    public enum IMMessageType {
        Text(1), //文本类消息
        AudioMessage(2), //离线语音消息
        File(3); //文件类消息
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
    }


    public  static class IMConversationInfo{
        public long targetId;  //目标id userId/groupid
        public int unreadNum; //未读数量
        public IMHistoryMessage lastHistortMessage = new IMHistoryMessage();//最后一条消息
    }

    public  static class IMUnreadConversationInfo{
        public ArrayList<IMConversationInfo> groupUnread = new ArrayList<>(); //群组未读消息
        public ArrayList<IMConversationInfo> p2pUnread = new ArrayList<>();   //p2p未读消息
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
        public GroupInviteGrant inviteGrant; //群组加入权限
    }

    //邀请加入信息
    public static class IMInviteInfo{
        public long targetGroupId; //邀请加入的群组id
        public long inviteTime; //邀请时间
        public long fromId; //邀请人id
        public String attrs; //用户自定义数据
    }

    //申请加入信息(群组或者好友)
    public static class IMApplyInfo {
        public long fromId; //用户id
        public long requestTime; //申请时间
        public String grantExtra; //留言
        public String attrs; //用户自定义数据
    }

    //请求加入信息(自己添加好友或者群组)
    public static class IMRquestInfo {
        public long targetId; //用户id
        public long applyTime; //请求时间
        public String attrs; //用户自定义数据

    }

    public static class IMUserInfo {
        public long userId;  //用户id
        public String name;  //用户名称
        public String portraitUrl; //用户头像
        public String profile;  //用户简介
        public String customData; //用户自定义数据
        public AddPermission applyGrant; //用户被添加权限
    }

    // 群成员信息
    public static class IMGroupMemberInfo {
        public long userId;
        public int roleType; //0-群主、1-管理员、2-普通成员)
        public int isOnLine; //0离线 1在线
    }
~~~