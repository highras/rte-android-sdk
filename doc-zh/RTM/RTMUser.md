~~~c++
  /**
     * 设置用户自己的公开信息和者私有信息(publicInfo,privateInfo 最长 65535)
     * @param publicInfo  公开信息
     * @param privateInfo 私有信息
     */
    public void setUserInfo(String publicInfo, String privateInfo,  IEmptyCallback callback)


    /**
     * 获取的用户公开信息和私有信息
     */
    public void getUserInfo( IGetinfoCallback callback)


    /**
     * 获取其他用户的公开信息，每次最多获取100人
     * @param callback <用户id,公开信息>回调
     * @param userIds     用户uid集合
     */
    public void getUserPublicInfo(List<Long> userIds, ICallback<Map<Long, String>> callback)


    /**
     * 查询用户在线状态
     * @param userIds   待查询的用户id集合
     * @param callback <在线用户列表>回调
     */
    public void getOnlineUsers(List<Long> userIds,  ICallback<List<Long>> callback)


    /**
     *添加key_value形式的变量（例如设置客户端信息，会保存在当前链接中）
     * @param attrs     客户端自定义属性值
     */
    void addAttributes(Map<String, String> attrs,  IEmptyCallback callback)


    /**
     * 获取用户属性
     * @param callback  用户属性回调 其中map的key
     *                  map中自动添加如下几个参数：
     *                  login：登录时间，utc时间戳
     *                  my：当前链接的attrs
     */
    void getAttributes( ICallback<List<Map<String, String>>> callback)


    /**
     * 添加debug日志
     * @param message   消息内容
     * @param attrs     消息属性信息
     */
    void addDebugLog(String message, String attrs,  IEmptyCallback callback)



    /**
     * 添加android推送设备token信息(Google FCM)
     * @param  callback
     * @param deviceToken   设备推送token
     * @param tag 一个rtm项目对应多个推送项目时，所使用的推送项目的 tag
     */
    public void addDevice(String deviceToken,  String tag, IEmptyCallback callback) 
    

    /**
     * 删除设备
     * @param deviceToken   设备推送token
     * @param tag 一个rtm项目对应多个推送项目时，所使用的推送项目的 tag
     */
    public void removeDevice( String deviceToken,  String tag, IEmptyCallback callback)



    /**
     * 设置设备推送属性(注意此接口是设置个人或群组某个类型的type不推送的设置)
     * @param type  type=0, 设置某个p2p 不推送；type=1, 设置某个group不推送
     * @param xid   当type =0 时 表示userid；当type =1时 表示groupId
     * @param messageTypes (为空，则所有mtype均不推送;否则表示指定mtype不推送)
     */
    public void addDevicePushOption( int type,  long xid, List<Integer> messageTypes,  IEmptyCallback callback)


    /**
     * 取消设备推送属性(和addDevicePushOption对应)
     * @param type  type=0, 取消p2p推送属性；type=1, 取消group推送属性
     * @param xid   当type =0 时 表示userid；当type =1时 表示groupId
     * @param messageTypes  需要取消设置的messagetype集合(如果为空表示什么都不做)
     */
    public void removeDevicePushOption(int type,  long xid, List<Integer> messageTypes, IEmptyCallback callback)


    /**
     * 获取设备推送属性(addDevicePushOption的结果)
     */
    public void getDevicePushOption(ICallback<LiveDataStruct.DevicePushOption> callback)
    

    /**
     * 获取存储的数据信息（仅能操作自己信息）(key:最长128字节，val：最长65535字节)
     * @param key      key值
     */
    public void dataGet( String key, ICallback<String> callback) 


    /**
     * 设置存储的数据信息（仅能操作自己信息）(key:最长128字节，val：最长65535字节)
     * @param key      key值
     */
    public void dataSet(String key,  String value, IEmptyCallback callback)


    /**
     * 删除存储的数据信息
     * @param key      key值
     */
    public void dataDelete( String key,  IEmptyCallback callback)
~~~