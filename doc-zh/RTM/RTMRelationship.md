~~~c++
     /**
     * 添加好友 (每次最多添加100人)
     * @param userIds   用户id集合
     * @param callback 回调
     */
    public void addFriends(List<Long> userIds, IEmptyCallback callback)

    /**
     * 查询自己好友
     * @param callback 回调
     */
    public void getFriendList( final ICallback<List<Long>> callback)


    /**
     * 删除好友 (每次最多删除100人)
     * @param userIds   用户id集合
     * @param callback 回调
     */
    public void deleteFriends(List<Long> userIds,  IEmptyCallback callback)


    /**
     * 添加黑名单 (拉黑用户，每次最多添加100人，拉黑后对方不能给自己发消息，自己可以给对方发，双方能正常获取session)
     * @param userIds   用户id集合
     * @param callback 回调
     */
    public void addBlacklist( List<Long> userIds,  IEmptyCallback callback)


    /**
     * 删除黑名单用户 (每次最多添加100人)
     * @param userIds   用户id集合
     * @param callback 回调
     */
    public void deleteBlacklist( List<Long> userIds,  IEmptyCallback callback)


    /**
     * 查询黑名单
     * @param callback 回调
     */
    public void getBlacklist(final ICallback<List<Long>> callback)
    
    /**
     * 获取群组人数
     * @param groupId   群组id
     * @param isOnline 是否获取在线人数
     * @param callback  <总人数，在线人数>回调
     * */
    public void getGroupMemberCount(long groupId, boolean isOnline, final IDoubleCallBack<Integer, Integer> callback)

    /**
     * 获取群组用户列表
     * @param groupId   群组id
     * @param isOnline 是否获取在线人数列表
     * @param callback  <用户列表,用户在线列表>回调
     * */
    public void getGroupMembers(long groupId, boolean isOnline, final IDoubleCallBack<List<Long>, List<Long>> callback)

    /**
     * 添加群组用户 (注意 调用接口的用户必须在群组里)
     * @param groupId   群组id
     * @param userIds      用户id集合
     * @param callback  回调
     * */
    public void addGroupMembers(long groupId, List<Long> userIds, final IEmptyCallback callback)


    /**
     * 删除群组用户
     * @param groupId   群组id
     * @param userIds   用户id集合
     * @param callback  回调
     * */
    public void removeGroupMembers(long groupId,List<Long> userIds,  final IEmptyCallback callback)


    /**
     * 获取用户所在的群组
     * @param callback  IRTMCallback回调
     * */
    public void getUserGroups( final ICallback<List<Long>> callback)


    /**
     * 获取群组的公开信息和私有信息 (需要调用者是群组成员)
     * @param groupId   群组id
     * @param callback  <公开信息,私有信息>回调
     */
    public void getGroupInfo(final long groupId,final IGetinfoCallback callback)

    /**
     * 设置群组的公开信息或者私有信息
     * @param groupId   群组id
     * @param publicInfo    群组公开信息
     * @param privateInfo   群组 私有信息
     * @param callback  回调
     */
    public void setGroupInfo(long groupId, String publicInfo, String privateInfo, IEmptyCallback callback)


    /**
     * 获取群组的公开信息，每次最多获取100个群组
     * @param groupIds        群组id集合
     *@param callback     <群组id，群组公开信息>结构
     */
    public void getGroupsPublicInfo(List<Long> groupIds, final ICallback<Map<Long, String>> callback)
    
    /**
     * 进入房间
     * @param roomId   房间id
     * @param callback 回调
     */
    public void enterRoom(long roomId,  IEmptyCallback callback)


    /**
     * 离开房间
     * @param roomId   房间id
     * @param callback 回调
     */
    public void leaveRoom(long roomId,  IEmptyCallback callback)


    /**
     * 获取房间中的所有member (由于分布式系统，房间的人数会有几秒同步间隔)
     * @param roomId   房间id
     * @param callback 回调
     */
    public void getRoomMembers(long roomId,  final ICallback<List<Long>> callback)



    /**
     * 获取房间中的所有人数 (由于分布式系统，房间的人数会有几秒同步间隔)
     * @param roomIds   房间id集合
     * @param callback <房间id,用户人数>回调
     */
    public void getRoomMemberCount(List<Long> roomIds,  final ICallback<Map<Long,Integer>> callback)


    /**
     * 获取用户所在的房间
     * @param callback 回调
     */
    public void getUserRooms( final ICallback<List<Long>> callback)

    /**
     * 设置房间的公开信息和私有信息
     * @param roomId   房间id
     * @param publicInfo    房间公开信息
     * @param privateInfo   房间私有信息
     * @param callback  回调
     */
    public void setRoomInfo(long roomId, String publicInfo, String privateInfo,  IEmptyCallback callback)

    /**
     * 获取房间的公开信息和私有信息 (必须在房间内)
     * @param roomId   房间id
     * @param callback <公开信息,私有信息>  回调
     */
    public void getRoomInfo(final long roomId,  final IGetinfoCallback callback)


    /**
     * 获取房间的公开信息，每次最多获取100个
     * @param roomIds     房间id集合
     * @param callback <Map<房间id, 公开信息>>回调
     */
    public void getRoomsPublicInfo(List<Long> roomIds,  final ICallback<Map<Long, String>> callback)
~~~