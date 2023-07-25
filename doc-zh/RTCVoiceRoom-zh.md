~~~ c++
    **
     * 设置rtc回调事件
     * @param rtcEventHandle
     */
    public void setRTCEventHandle(RTCEventHandle rtcEventHandle)
    

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
    public void setBGMVolume(int volume)
    


    /**
     * 设置音效音量(1-100)
     */
    public void setAudioEffectVolume(int volume)
    

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
     * @param loop 循环次数(-1 无限循环)
     * @return
     */
    public  int startAudioMixing(String filePath, int pos, int loop)
    
    

    /**
     * 设置音效
     * @param id 音效id
     * @param filePath  文件路径
     * @param loop 循环次数（-1为无限循环）
     * @return
     */
    public  void startAudioEffect(long id, String filePath, int loop)
    

    /**
     * 暂停bgm播放
     */
    public  void pauseBGM()


    /**
     * 恢复bgm播放
     */
    public  void resumeBGM()


    /**
     * 停止bgm播放
     */
    public  void stopBGM()


    /**
     * 设置bgm位置(秒)
     */
    public  void setBGMPos(int second)


    /**
     * 获取bgm播放(秒)
     */
    public  int getBGMCurrPos()


    /**
     * 暂停音效播放
     * @param id 音效id （0位全部音效）
     */
    public  void pauseAudioEffect(int id)
    

    /**
     * 暂停音效播放
     * @param id 音效id （0位全部音效）
     */
    public  void resumeAudioEffect(int id)


    /**
     * 停止音效播放
     * @param id 音效id （0位全部音效）
     */
    public  void stopAudioEffect(int id)


    /**
     * 创建语聊房
     * @roomId 房间id
     */
    public void createRTCRoom(long roomId, IEmptyCallback callback)

    /**
     * 进入语聊房
     * @param roomId   房间id
     */
    public void enterRTCVoiceRoom(long roomId, ICallback<List<Long>> callback)


    /**离开语聊房
     * @param roomId   房间id
     */
    public void leaveRTCRoom(long roomId, IEmptyCallback callback)



    /**
     * 获取语语聊房成员列表
     */
    public void getRTCRoomMembers(long roomId,  ICallback<List<Long>> callback)
    
    

    /**
     * 获取语聊房成员个数
     */
    public void getRTCRoomMemberCount(long roomId,  ICallback<Integer> callback)
~~~