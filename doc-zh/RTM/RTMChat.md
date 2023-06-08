~~~c++
     /**
     *发送聊天消息
     * @param targetId    目标id
     * @param message   聊天消息
     * @param attrs     用户自定义附加信息(可空)
     * @param conversationType   会话类型
     * @param callback
     */
    public void sendChatMessage(long targetId, ConversationType conversationType, String message, String attrs, ISendMsgCallback callback) 


    /**
     *发送指令消息(可以用作非消息类的通知)
     * @param targetId  目标id
     * @param message   指令内容
     * @param attrs     用户自定义附加信息(可空)
     * @param conversationType   会话类型
     * @param callback
     */
    public void sendCMDMessage(long targetId, ConversationType conversationType, String message, String attrs, ISendMsgCallback callback) 
    

    /**
     *分页获得历史聊天消息(async)
     * @param targetId   目标id(对于Broadcast类消息 可传任意值)
     * @param desc      是否按时间倒叙排列
     * @param count     获取条数(后台配置 最多建议20条)
     * @param beginTime 开始时间戳(毫秒)(可选默认0,第二次查询传入上次结果的HistoryMessageResult.beginMsec)
     * @param endTime   结束时间戳(毫秒)(可选默认0,第二次查询传入上次结果的HistoryMessageResult.endMsec)
     * @param lastId    索引id(可选默认0，第一次获取传入0 第二次查询传入上次结果HistoryMessageResult的lastId)
     * @param conversationType   消息类别
     * @param callback  HistoryMessageResult类型回调
     * 注意: 建议时间和id不要同时传入 通过时间查询是左右闭区间(beginMsec<=x<=endMsec)
     *       通过id查询是开区间lastId<x
     */
    public void getHistoryChat(long targetId, ConversationType conversationType, boolean desc, int count, long beginTime, long endTime, long lastId, ICallback<RTMHistoryMessageResult>  callback)
    

    /**
     *发送指定类型消息
     * @param targetId       目标用户id
     * @param messageType     消息类型(51-127)
     * @param message   消息内容
     * @param attrs     客户端自定义信息(可空)
     * @param conversationType   会话类型
     * @param callback
     */
    public void sendBasicMessage(long targetId, ConversationType conversationType, int messageType, String message, String attrs, ISendMsgCallback callback)


    /**
     *发送指定类型消息(二进制数据)
     * @param targetId       目标用户id
     * @param messageType   消息类型
     * @param message   消息内容
     * @param attrs     客户端自定义信息(可空)
     * @param conversationType   会话类型
     * @param callback
     */
    public void sendBinaryMessage(long targetId, ConversationType conversationType, int messageType, byte[] message, String attrs,  ISendMsgCallback callback)


    /**
     *分页获得指定类型历史消息
     * @param targetId   目标id(对于Broadcast类消息 可传任意值)
     * @param desc      是否按时间倒叙排列
     * @param count     获取条数(后台配置 最多建议20条)
     * @param beginMsec 开始时间戳(毫秒)(可选默认0,第二次查询传入上次结果的HistoryMessageResult.beginMsec)
     * @param endMsec   结束时间戳(毫秒)(可选默认0,第二次查询传入上次结果的HistoryMessageResult.endMsec)
     * @param lastCursorId    索引id(可选默认0，第一次获取传入0 第二次查询传入上次结果HistoryMessageResult的lastId)
     * @param conversationType   消息类别
     * @param callback  HistoryMessageResult类型回调
     * 注意: 建议时间和id不要同时传入 通过时间查询是左右闭区间(beginMsec<=x<=endMsec)
     *       通过id查询是开区间lastId<x
     */
    public void getHistoryBasicMessage(long targetId, ConversationType conversationType, boolean desc, int count, long beginMsec, long endMsec, long lastCursorId, List<Integer> mtypes,  ICallback<RTMHistoryMessageResult> callback)
    
    
    /**获取单条消息
     * @param fromUid   发送者id
     * @param targetId   接收者id(userId/groupid/roomid)
     * @param messageId   消息id
     * @param conversationType   会话类型
     * @param callback
     */
    public void getMessage(long fromUid, long targetId, ConversationType conversationType, long messageId, ICallback<RTMSingleMessage> callback)


    /**
     *删除单条消息
     * @param messageId   消息id
     * @param fromUid   发送者id
     * @param targetId     接收者id(userId/groupid/roomid)
     * @conversationType 会话类型
     */
    public void deleteMessage(long fromUid, long targetId, ConversationType conversationType, long messageId, IEmptyCallback callback)
    
    
    /**
     * 发送文件
     * @param targetId   roomId/groupId/userId
     * @param messageType     文件类型
     * @param fileContent   文件内容
     * @param filename      文件名字
     * @param attrs 客户端自定义信息(可空)
     * @param conversationType  会话类型
     * @param callback
     */
    public void sendFile(long targetId, ConversationType conversationType, byte[] fileContent, String filename, JSONObject attrs, FileMessageType messageType, ISendFileCallback callback ) 

    /**
     * 发送离线语音消息
     * @param targetId   roomId/groupId/userId
     * @param audioInfo 录音结构
     * @param fileName 文件名字
     * @param conversationType  会话类型
     * @param callback
     */
    public void sendAudioMessage(long targetId, ConversationType conversationType, RecordAudioStruct audioInfo, String fileName, ISendFileCallback callback ) 


    /**
     * 发送文件并返回url
     * @param messageType 文件类型
     * @param fileName 文件名字(需要带后缀)
     * @param fileContent  文件内容
     * @param audioInfo  RTM录音完成返回的结构
     * @param callback
     */
    void uploadFile(final FileMessageType messageType, final String fileName, final byte[] fileContent, final RecordAudioStruct audioInfo, final IUploadFileCallback callback)


    /**
     * 获取所有P2p或者group会话列表
     * @param time 毫秒级时间戳，大于该时间戳的消息被计为未读消息，传0则默认取上次离线时间。
     * mtypes   消息类型列表，传null 则默认查询mtype为30-50的消息
     * @param conversationType 会话类型(P2P或者Group)
     */
    public void getAllConversation(long time, List<Integer> mtypes, ConversationType conversationType, final ICallback<List<RTMConversationInfo>> callback)


    /**
     * 获取所有未读会话列表
     * @param clear 是否清除会话未读状态
     * @param time 毫秒级时间戳，大于该时间戳的消息被计为未读消息，传0则默认取上次离线时间。
     * messageTypes   消息类型列表，传null 则默认查询mtype为30-50的聊天类消息
     * @param callback
     */
    public void getAllUnreadConversation(boolean clear, long time, List<Integer> messageTypes, final ICallback<RTMUnreadConversationInfo> callback)


    /**
     *清除离线提醒
     * @param callback 回调
     */
    public void clearUnread(IEmptyCallback callback)


    /**
     * 删除p2p会话
     * @param userId 对方id
     * @param oneway true-单向删除 false-双向删除
     */
    public void removeP2PConversation(long userId, boolean oneway, IEmptyCallback callback)

~~~