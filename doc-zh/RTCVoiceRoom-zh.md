~~~ c++
     
    /**
     * 设置rtc回调事件
     * @param rtcEventHandle
     */
    public void setEventHandle(RTCEventHandle rtcEventHandle)


    /**
     * 打开麦克风
     */
    public void openMic()


    /**
     * 关闭麦克风
     */
    public void closeMic()


    /**
     * 打开音频输出
     */
    public void openAudioOutput()
    
    
    /**
     * 关闭音频输出(静音)
     */
    public void closeAudioOutput()


    /**
     * 设置背景音音量(1-100)
     */
    public void setBGNVolume(int volume)
    
    

    /**
     * 推流
     */
    public void pushStream()
    

    /**
     * 关闭推流
     */
    public void closeStream()
    


    /**
     * 设置BGM文件
     * @param filePath  文件路径
     * @param pos 播放位置(秒)
     * @param loop 是否单曲循环
     * @return
     */
    public  void startAudioMixing(String filePath, int pos, boolean loop)
    

    /**
     * 设置音效
     * @param filePath  文件路径
     * @return
     */
    public  void startAudioEffect(String filePath)
    

    /**
     * 暂停bgm播放
     */
    public  void pauseBGM()


    /**
     * 恢复bgm播放
     */
    public  void resumeBGM()


    /**
     * 设置bgm位置(秒)
     */
    public  void setBGMPos(int second)


    /**
     * 获取bgm播放(秒)
     */
    public  void getBGMCurrPos()



    /**
     * 创建语聊房
     * @roomId 房间id
     * @param callback
     */
    public void createRTCRoom(long roomId, IEmptyCallback callback) 


    /**
     * 进入语聊房
     * @param callback 回调
     * @param roomId   房间id
     */
    public void enterRTCVoiceRoom(long roomId, ICallback<List<Long>> callback)


    /**离开语聊房
     * @param roomId   房间id
     */
    public void leaveRTCRoom(long roomId, IEmptyCallback callback)



    /**
     * 获取语语聊房成员列表
     * @param callback 回调<RoomInfo>
     */
    public void getRTCRoomMembers(long roomId,  ICallback<List<Long>> callback)
    

    /**
     * 获取语聊房成员个数
     * @param callback 回调
     */
    public void getRTCRoomMemberCount(long roomId,  ICallback<Integer> callback)
~~~