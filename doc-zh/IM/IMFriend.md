~~~c++
    /**
     * 添加好友
     * @param userId   用户id
     * @param extra    验证消息
     * @param attrs    用户自定义消息
     */
    public void addFriend(long userId, String extra, String attrs, IEmptyCallback callback)

    /**
     * 回应添加好友请求
     * @param userId     对方uid
     * @param agree      是否同意
     * @param attrs    用户自定义消息
     */
    public void ackAddFriend(long userId, boolean agree, String attrs, IEmptyCallback callback)

    /**
     * 删除好友
     * @param userIds 用户id列表
     */
    public void deleteFriend(List<Long> userIds, IEmptyCallback callback)


    /**
     * 获取自己好友列表
     */
    public void getFriendList(ICallback<List<Long>> callback)

    /**
     * 添加黑名单
     * @param userIds  用户id集合
     */
    public void addBlacklist(List<Long> userIds, IEmptyCallback callback)

    /**
     * 删除黑名单用户
     * @param uids     用户id集合
     */
    public void delBlacklist(List<Long> uids, IEmptyCallback callback)

    /**
     * 查询黑名单
     */
    public void getBlacklist(ICallback<List<Long>> callback)

    /**
     * 获取好友申请列表
     */
    public void getFriendApplyList(ICallback<List<IMApplyInfo>> callback)


    /**
     * 获取自己发出的添加好友列表
     */
    public void getFriendRequestList(ICallback<List<IMRquestInfo>> callback)
    
~~~