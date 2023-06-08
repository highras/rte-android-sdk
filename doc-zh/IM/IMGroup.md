~~~c++
    /**
     * 加入群组（如果群组允许任何人加入，直接加入成功；如果需要验证才能加入，需要群主和管理员进行处理)
     * @param extra    附言备注
     * @param attrs    用户自定义信息
     * @param callback IMCallback回调
     */
    public void joinGroup(long groupId, String extra, JSONObject attrs, IEmptyCallback callback)
    

    /**
     * 回应入群请求
     * @param groupId  群组id
     * @param agree    是否同意
     * @param fromUid  申请者uid
     * @param attrs    用户自定义信息
     * @param callback 回调
     */
    public void ackJoinGroup(long groupId, long fromUid, boolean agree, JSONObject attrs, final IEmptyCallback callback)
    

    /**
     * 邀请入群(服务端结合群权限设置处理，权限分为：不允许普通成员邀请、允许普通成员邀请)
     * @param groupId  群组id
     * @param userIds  用户id列表
     * @param extra    邀请留言
     * @param attrs    用户自定义信息
     * @param callback 回调
     */
    public void inviteIntoGroup(long groupId, List<Long> userIds, String extra, JSONObject attrs, IEmptyCallback callback)


    /**
     * 回应邀请入群(被邀请用户)
     * @param groupId  群组id
     * @param userId   邀请者用户Id
     * @param agree    是否同意
     * @param callback 回调
     */
    public void ackInvitedIntoGroup(long groupId, long userId, boolean agree, JSONObject attrs, IEmptyCallback callback)


    /**
     * 移除群组成员(只有群主或管理员有权移除成员)
     * @param groupId  群组id
     * @param userIds  用户id列表
     * @param callback 回调
     */
    public void removeGroupMembers(final long groupId, final List<Long> userIds, final IEmptyCallback callback)


    /**
     * 群主转让(转让后原群主身份变更为普通群员)
     * @param groupId  群组id
     * @param userId   用户id
     * @param callback 回调
     */
    public void changeGroupLeader(final long groupId, final long userId, final IEmptyCallback callback)
    

    /**
     * 添加管理员(只有群主有权添加管理员)
     * @param groupId  群组id
     * @param userIds  用户id列表
     * @param callback 回调
     */
    public void addGroupManagers(final long groupId, final List<Long> userIds, final IEmptyCallback callback)
    

    /**
     * 移除管理员(只有群主有权移除管理员)
     * @param groupId  群组id
     * @param userIds  管理员用户id列表
     * @param callback 回调
     */
    public void removeGroupManagers(final long groupId, final List<Long> userIds, final IEmptyCallback callback)


    /**
     * 退出群组(群主不能退出，如果为群主，将返回错误，需要先将群主身份转让)
     * @param groupId  群组id
     * @param callback 回调
     */
    public void leaveGroup(final long groupId, final IEmptyCallback callback)
    

    /**
     * 解散群组(仅群主可以操作)
     * @param groupId  群组id
     * @param callback 回调
     */
    public void dismissGroup(final long groupId, final IEmptyCallback callback)


    /**
     * 获取群组信息
     * @param groupIds  群组id
     * @param callback 回调
     */
    public void getGroupInfos(List<Long> groupIds, final ICallback<List<IMGroupInfo>> callback)


    /**
     * @param groupId  群组id
     * @param callback
     */
    public void getGroupMembers(final long groupId, final ICallback<List<IMGroupMemberInfo>> callback)
    

    /**
     * 获取群组成员数量 该方法不需要是群成员就可以获取的到
     * @param groupId 群组id
     */
    public void getGroupMembersCount(long groupId, ICallback<Integer> callback)


    /**
     * 获取用户所在的群组
     * @param callback 回调
     */
    public void getUserGroups(ICallback<List<Long>> callback)


    /**
     * 获取入群邀请列表
     * @param callback
     */
    public void getInviteGroupList(ICallback<List<IMInviteInfo>> callback)
    

    /**
     * 获取群组申请列表(管理员和群主调用)
     */
    public void getGroupdApplyList(long groupId, ICallback<List<IMApplyInfo>> callback)
    

    /**
     * 获取自己发出的入群申请列表
     */
    public void getGroupRequestList(ICallback<List<IMRquestInfo>> callback)
~~~