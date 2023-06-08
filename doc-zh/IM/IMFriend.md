~~~c++
     
    /**
     * 添加好友
     * @param callback 回调
     * @param userId      用户id
     * @param extra    验证消息
     * @param attrs    用户自定义消息
     */
    public void addFriend(final long userId, String extra, String attrs, final IEmptyCallback callback)

    /**
     * 回应添加好友请求
     * @param callback   回调
     * @param userId     对方uid
     * @param agree      是否同意
     * @param attrs    用户自定义消息
     */
    public void ackAddFriend(long userId, final boolean agree, String attrs, final IEmptyCallback callback)

    /**
     * 删除好友
     * @param uid 用户id
     */
    public void deleteFriend(final Long uid, IEmptyCallback callback) 


    /**
     * 获取自己好友列表
     * @param callback 回调
     */
    public void getFriendList(ICallback<List<Long>> callback)
    

    /**
     * 添加黑名单
     * @param callback 回调
     * @param userIds  用户id集合
     */
    public void addBlacklist(final List<Long> userIds, final IEmptyCallback callback)

    /**
     * 删除黑名单用户
     * @param callback 回调
     * @param uids     用户id集合
     */
    public void delBlacklist(final List<Long> uids, final IEmptyCallback callback)


    /**
     * 查询黑名单
     * @param callback 回调
     */
    public void getBlacklist(final ICallback<List<Long>> callback)

    /**
     * 获取好友申请列表
     */
    public void getFriendApplyList(ICallback<List<IMApplyInfo>> callback)


    /**
     * 获取自己发出的添加好友列表
     */
    public void getFriendRequestList(ICallback<List<IMRquestInfo>> callback)
~~~