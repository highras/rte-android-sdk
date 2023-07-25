~~~c++
     
    /**
     * 获取用户信息
     * @param userIds 用户id列表
     */
    public void getUserInfos(List<Long> userIds, ICallback<List<IMUserInfo>> callback)
    
    

    /**
     * 添加android推送设备token信息(Google FCM)
     * @param deviceToken   设备推送token
     */
    public void addDevice(String deviceToken,  IEmptyCallback callback)
    
    
    /**
     * 删除设备，
     * @param deviceToken   设备推送token
     */
    public void removeDevice( String deviceToken,  IEmptyCallback callback)



    /**设置设备推送属性(注意此接口是设置个人或群组某个类型的type不推送的设置)
     * @param type  type=0, 设置某个p2p 不推送；type=1, 设置某个group不推送
     * @param xid   当type =0 时 表示userid；当type =1时 表示groupId
     * @param messageTypes (为null则所有mtype均不推送;否则表示指定mtype不推送)
     */
    public void addDevicePushOption( int type,  long xid, List<Integer> messageTypes,  IEmptyCallback callback)
    


    /**取消设备推送属性(和addDevicePushOption对应)
     * @param type  type=0, 取消p2p推送属性；type=1, 取消group推送属性
     * @param xid   当type =0 时 表示userid；当type =1时 表示groupId
     * @param messageTypes  需要取消设置的messagetype集合(如果为空表示什么都不做)
     */
    public void removeDevicePushOption(int type,  long xid, List<Integer> messageTypes, IEmptyCallback callback)


    /**获取设备推送属性(addDevicePushOption的结果)
     */
    public void getDevicePushOption(ICallback<LiveDataStruct.DevicePushOption> callback)
~~~