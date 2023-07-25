~~~c++
    /**
     * 进入房间（如果需要密码 extra需要填入）
     * @param roomId   房间id
     * @param extra 密码
     * @param attrs 自定义内容
     */
    public void enterRoom(long roomId,   String extra, String attrs, IEmptyCallback callback)
    


    /**
     * 邀请进入房间(只有房主或者管理员可以)
     * @param roomId  房间id
     * @param userIds  用户id列表
     * @param extra    邀请留言
     * @param attrs    用户自定义信息
     */
    public void inviteIntoRoom(long roomId, List<Long> userIds, String extra, String attrs, IEmptyCallback callback)
    
    

    /**
     * 回应邀请加入房间请求(被邀请用户)
     * @param roomId  房间id
     * @param from   邀请者Id
     * @param agree    是否同意
     * @param attrs    用户自定义信息
     */
    public void ackInvitedIntoRoom(long roomId, long from, boolean agree, String attrs, IEmptyCallback callback)
    


    /**
     * 移除房间成员(只有房主或管理员有权移除成员)
     * @param roomId 房间id
     * @param userIds 用户id列表
     */
    public void removeRoomMembers(long roomId, List<Long> userIds, IEmptyCallback callback)
    
    

    /**
     * 退出房间
     * @param roomId    房间id
     */
    public void leaveRoom(long roomId, IEmptyCallback callback)
    
    

    /**
     * 解散房间(仅房主可以操作)
     * @param roomId    房间id
     */
    public void dismissRoom(long roomId, IEmptyCallback callback)
    


    /**
     * 房主转让(转让后原房主身份变更为普通群员)
     * @param roomId    房间id
     * @param userId   用户id
     */
    public void changeRoomLeader(long roomId, long userId, IEmptyCallback callback)
    
    

    /**
     * 添加管理员(只有房主有权添加管理员)
     * @param roomId    房间id
     * @param userIds  用户id列表
     */
    public void addRoomManagers(long roomId, List<Long> userIds, IEmptyCallback callback)
    
    

    /**
     * 移除管理员(只有房主有权移除管理员)
     * @param roomId  房间id
     * @param userIds  管理员用户id列表
     */
    public void removeRoomManagers(long roomId, List<Long> userIds, IEmptyCallback callback)
    
    
    
    /**获取房间成员
     * @param roomId    房间id
     */
    public void getRoomMembers(long roomId, ICallback<List<IMRoomMemberInfo>> callback)
    

    /**
     * 获取房间成员数量 该方法不需要是房间成员就可以获取的到
     * @param roomId    房间id
     */
    public void getRoomMembersCount(long roomId, ICallback<Integer> callback)
    

    /**
     * 获取用户所在的房间
     */
    public void getUserRooms( ICallback<List<Long>> callback)



    /**
     * 获取房间信息
     * @param roomIds  房间id
     */
    public void getRoomInfos(List<Long> roomIds, ICallback<List<IMRoomInfo>> callback)
    
    

    /**
     * 获取入房间邀请列表
     */
    public void getRoomInviteList(ICallback<List<IMInviteInfo>> callback)
~~~