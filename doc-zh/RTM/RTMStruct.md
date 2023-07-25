~~~c++
    public static class RTMMessage
    {
        public long fromUid;    //发送者id 若等于自己uid 说明发送者是自己
        public long targetId;       //目标id 根据会话类型userId/groupId/roomId
        public int messageType;  //消息类型
        public long messageId;  //消息id
        public String stringMessage; //字符串消息
        public byte[] binaryMessage; //二进制消息
        public String attrs;        //客户端发送消息自定义的附加信息
        public long modifiedTime;   //服务器处理返回时间
        public FileStruct fileInfo; //文件结构信息(语音的信息也存在这里)
        public TranslatedInfo translatedInfo = new TranslatedInfo(); //聊天信息结构(push)
    }

    public static class RTMHistoryMessage extends RTMMessage //历史消息结构
    {
        public long cursorId;       //历史消息的索引id
    }

    //历史消息结果 需要循环调用
    public static class RTMHistoryMessageResult{
        public int count;   //实际返回消息条数
        public long lastId; //最后一条消息的索引id
        public long beginMsec; //下一次查询用的开始时间戳(毫秒)
        public long endMsec;    //下一次查询用的结束时间戳(毫秒)
        public List<RTMHistoryMessage> messages; //历史消息详细信息结构集合
    }

    public  static class RTMConversationInfo{
        public long targetId;       //目标id userId/groupId
        public int unreadNum; //未读数量
        public RTMHistoryMessage lastHistortMessage = new RTMHistoryMessage();//最后一条消息
    }


    public  static class RTMUnreadConversationInfo{
        public ArrayList<RTMConversationInfo> groupUnread = new ArrayList<>(); //群组未读消息
        public ArrayList<RTMConversationInfo> p2pUnread = new ArrayList<>();   //p2p未读消息
    }

    //getmsg单条消息结构
    public static class RTMSingleMessage{
        public long cusorId; //消息的索引id(数据库中存储的id)
        public byte messageType;  ////消息类型 常规聊天类型见 RTMcore enum MessageType 用户可以自定义messagetype 51-127
        public String stringMessage; //文本数据
        public byte[] binaryMessage;//二进制数据
        public String attrs;    //消息的附加属性信息(客户端自定义)
        public long modifiedTime;   //服务器应答时间
        public FileStruct fileInfo; //文件类型结构 messageType为FileMessageType的都存在这里
    }
~~~