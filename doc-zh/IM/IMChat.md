~~~c++
      /**
     *发送聊天消息
     * @param targetId  目标用户id
     * @param message   聊天消息
     * @param attrs     用户自定义附加信息(可空)
     * @param conversationType   会话类型
     */
    public void sendChatMessage(long targetId,ConversationType conversationType,String message, String attrs, ISendMsgCallback callback)

    /**
     *分页获得历史聊天消息
     * @param targetId   目标id(对于Broadcast类消息 可传任意值)
     * @param desc      是否按时间倒叙排列
     * @param count     获取条数(后台配置 最多建议20条)
     * @param beginTime 开始时间戳(毫秒)(如果循环拉取历史下一次查询传入上次结果的beginMsec)
     * @param endTime   结束时间戳(毫秒)(如果循环拉取历史下一次查询传入上次结果的endMsec)
     * @param lastId    索引id(可选默认0，第一次获取传入0 第二次查询传入上次结果的lastId)
     * @param conversationType   消息类别
     * 注意: 建议时间和id不要同时传入 通过时间查询是左右闭区间(beginMsec<=x<=endMsec)
     *       通过id查询是开区间lastId<x
     */
    public void getHistoryChat(long targetId,  ConversationType conversationType, boolean desc, int count, long beginTime, long endTime, long lastId,ICallback<IMHistoryMessageResult>  callback)


    /**获取单条消息
     * @param fromUserId   发送者id
     * @param targetId     接收者id(uid/groupid/roomid)
     * @param messageId   消息id
     * @param conversationType   会话类型
     */
    public void getMessage(long fromUserId, long targetId, ConversationType conversationType, long messageId,  ICallback<IMSingleMessage> callback)


    /**
     *删除单条消息
     * @param messageId     消息id
     * @param fromUserId   发送者id
     * @param targetId     接收者id(uid/groupid/roomid)
     * @param conversationType       消息类别
     */
    public void deleteMessage(long fromUserId, long targetId, ConversationType conversationType, long messageId,IEmptyCallback callback)
    

    /**
     * 获取所有P2p或group会话列表
     * @param time 毫秒级时间戳，大于该时间戳的消息被计为未读消息，传0则默认取上次离线时间。
     * @param conversationType 会话类型(P2P或者Group)
     */
    public void getAllConversation(long time, ConversationType conversationType, ICallback<List<IMConversationInfo>> callback)


    /**
     * 获取所有P2p和group未读会话列表
     * @param clear 是否清除会话未读状态
     * @param time 毫秒级时间戳，大于该时间戳的消息被计为未读消息，传0则默认取上次离线时间。
     */
    public void getAllUnreadConversation(boolean clear, long time, ICallback<IMUnreadConversationInfo> callback)


    /**
     *清除服务器未读
     */
    public void clearUnread(IEmptyCallback callback)
    

    /**
     * 删除p2p会话
     * @param userId 对方id
     * @param oneway true-单向删除 false-双向删除
     */
    public void removeP2PConversation(long userId, boolean oneway, IEmptyCallback callback)
    

    /**
     * 发送文件
     * @param targetId      目标id
     * @param messageType   文件类型
     * @param fileContent   文件内容
     * @param filename      文件名字
     * @param attrs         客户端自定义信息(可空)
     * @param conversationType  会话类型
     */
    public void sendFile(long targetId, ConversationType conversationType, FileMessageType messageType, byte[] fileContent, String filename, JSONObject attrs, ISendFileCallback callback )


    /**
     * 发送RTM录音消息
     * @param targetId      目标id
     * @param audioInfo     RTM录音结构
     * @param fileName      文件名字
     * @param conversationType  会话类型
     */
    public void sendAudioMessage(long targetId,ConversationType conversationType, RecordAudioStruct audioInfo, String fileName, JSONObject attrs, ISendFileCallback callback )
~~~