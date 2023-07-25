~~~ c++
      /**
     *管理员权限操作
     * @param roomId 房间id
     * @param uids
     * @param command
     * 0 赋予管理员权
     * 1 剥夺管理员权限
     * 2 禁止发送音频数据
     * 3 允许发送音频数据
     * 4 禁止发送视频数据
     * 5 允许发送视频数据
     * 6 关闭他人麦克风
     * 7 关闭他人摄像头
     */
    public void adminCommand (long roomId, HashSet<Long> uids, int command, IEmptyCallback callback)
    

    /**
     * 开启摄像头
     */
    public LDAnswer openCamera()
    
    

    /**
     * 关闭摄像头
     */
    public void closeCamera()
    
    

    /**
     * 摄像头切换
     * @param front true-使用前置  false-使用后置
     */
    public void switchCamera(boolean front)
    
    

    /**
     * 打开麦克风(音频模式进入房间初始默认关闭  视频模式进入房间默认开启)
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
     * 设置麦克风增益等级(声音自动增益 取值 范围0-10)
     */
    public void setMicphoneLevel(int level)
    

    /**
     * 取消订阅视频流
     * @param roomId 房间id
     * @param uids 取消订阅的成员列表
     */
    public void unsubscribeVideo(long roomId, HashSet<Long> uids, IEmptyCallback callback)
    

    /**
     * 创建RTC房间
     * @roomId 房间id
     * @roomType 1-voice 2-video 3-实时翻译语音房间(视频房间摄像头默认关闭 麦克风默认开启)
     * @lang 语言(当创建实时翻译房间 必传)
     */
    public void createRTCRoom(long roomId, RTCStruct.RTCRoomType roomType, String lang, IEmptyCallback callback)


    /**
     * 进入RTC房间
     * @param roomId   房间id
     * @param lang 自己的语言(当为实时语音翻译房间必传)
     */
    public void enterRTCRoom(long roomId, String lang,  ICallback<RTCStruct.RTCRoomInfo> callback)
    


    /**
     * 订阅视频流
     * @param roomId 房间id
     * @param userViews  key-订阅的用户id value-显示用户的surfaceview(需要view创建完成可用)
     */
    public void subscribeVideos(long roomId, HashMap<Long, SurfaceView> userViews, IEmptyCallback callback)
    
    

    /**
     * 邀请用户加入RTC房间(需要对端确认)
     * @param roomId   房间id
     * @param uids     需要邀请的用户列表
     */
    public void inviteUserIntoRTCRoom(long roomId, HashSet<Long> uids, IEmptyCallback callback)
    
    

    /**
     * 设置目前活跃的房间(仅对语音房间有效)
     * @param roomId
     */
    public LDAnswer setActivityRoom(long roomId)
    
    

    /**
     * 切换扬声器听筒(耳机状态下不操作)(默认扬声器)
     * @param usespeaker true-使用扬声器 false-使用听筒
     */
    public void switchAudioOutput(boolean usespeaker)
    
    

    /**离开RTC房间
     * @param roomId   房间id
     */
    public void leaveRTCRoom(long roomId, IEmptyCallback callback)
    
    

    /**
     * 屏蔽房间某些人的语音
     * @param roomId   房间id
     * @param uids     屏蔽语音的用户列表
     */
    public void blockUserInVoiceRoom(long roomId, HashSet<Long> uids,IEmptyCallback callback)
    
    

    /**
     * 解除屏蔽房间某些人的语音
     * @param roomId   房间id
     * @param uids     解除屏蔽语音的用户列表
     */
    public void unblockUserInVoiceRoom(long roomId, HashSet<Long> uids, IEmptyCallback callback)
    


    /**
     * 获取语RTC房间成员列表
     */
    public void getRTCRoomMembers(long roomId,  ICallback<RTCStruct.RTCRoomInfo> callback) 
    

    /**
     * 获取RTC房间成员个数
     */
    public void getRTCRoomMemberCount(long roomId,  ICallback<Integer> callback)
    

    /**
     * 切换视频质量
     * @level 视频质量详见RTMStruct.CaptureLevle
     */
    public LDAnswer switchVideoQuality(int level)
    


    /**
     * 设置预览view(需要传入的view 真正建立完成)
     */
    public void setPreview(SurfaceView view)


    /****************P2P*****************/
    /**
     *发起p2p音视频请求(对方是否回应通过 pushP2PRTCEvent接口回调)
     * @param type 通话类型
     * @SurfaceView view(如果为视频 自己预览的view 需要view创建完成并可用)
     * @param toUid 对方id
     */
    public void requestP2PRTC(RTCStruct.P2PRTCType type , long toUid, SurfaceView view, IEmptyCallback callback)
    
    

    /**
     * 取消p2p RTC请求
     */
    public void cancelP2PRTC(IEmptyCallback callback)
    

    /**
     * 关闭p2p 会话
     */
    public void closeP2PRTC(IEmptyCallback callback)
    

    /**
     * 接受p2p 会话
     * @param preview 自己预览的view(仅视频)
     * @param bindview 对方的view(仅视频)
     */
    public void acceptP2PRTC(SurfaceView preview, SurfaceView bindview, IEmptyCallback callback)
    
    

    /**
     * 拒绝p2p请求
     */
    public void refuseP2PRTC(IEmptyCallback callback) 
    

    /**
     * 设置对方的surfaceview(当原有的surfaceview发生重建的时候)
     */
    public void bindDecodeSurface(long uid, SurfaceView view)
~~~