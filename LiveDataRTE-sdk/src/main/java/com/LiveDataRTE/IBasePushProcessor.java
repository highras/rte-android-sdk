package com.LiveDataRTE;

public interface IBasePushProcessor {
    int internalReloginMaxTimes = 300;

    /**
     * RTM链接断开 (默认会自动连接 kickout除外)(备注:链接断开会自动退出之前进入的RTM房间和RTC房间,并且订阅的视频流会自动解除 需要在重连成功再次加入房间并重新订阅)
     */
     default void rtmConnectClose(long uid){}


    /**
     * RTM重连开始接口 每次重连都会判断reloginWillStart返回值 若返回false则中断重连
     * 参数说明 uid-用户id  answer-上次重连的结果  reloginCount-将要重连的次数
     * 备注:需要用户设定一些条件 比如重连间隔 最大重连次数
     */
    default boolean reloginWillStart(long uid, int reloginCount){return true;};

    /**
     * RTM重连完成(如果 successful 为false表示最终重连失败,answer会有详细的错误码和错和错误信息 为true表示重连成功)
     * 备注:当用户的token过期或被加入黑名单 重连会直接返回 不会继续判断reloginWillStart
     */
    default void reloginCompleted(long uid, boolean successful, LiveDataStruct.LDAnswer answer, int reloginCount){};


    /**
     * 被服务器踢下线(不会自动重连)
     */
    default void kickout(){}
}
