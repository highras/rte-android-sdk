package com.LiveDataRTE.IMLib;

import com.LiveDataRTE.LiveDataStruct.ConversationType;
import com.LiveDataRTE.IMLib.IMStruct.*;

import java.util.List;


public interface IIMPushProcessor {
    //push聊天消息(具体消息内容为 RTMMessage 中的translatedInfo)
    default void pushChat(IMMessage imMessage, ConversationType conversationType){}

    //接受文件类消息 (RTMMessage 中的fileInfo结构)
    default void pushFile(IMMessage imMessage, ConversationType conversationType){}

    //请求添加好友申请通知
    default void pushAddFriend(long fromUid, String extraMessage, String attrs){}

    //对方同意添加好友通知（被同意一方会收到这个通知）
    default void pushAgreeApplyFriend(long userId, String attrs){}

    //对方拒绝添加好友通知（被拒绝一方会收到这个通知）
    default void pushRefuseApplyFriend(long userId, String attrs){}

    //好友关系建立通知
    default void pushEstablishFriend(long userId, String attrs){}

    //入群申请通知（有人申请入群时管理员会收到这个通知）
    default void pushApplyGroup(long fromUid, long groupId, String extraMessage, String attrs){}

    //同意申请入群通知(自己发送的入群申请被同意时会收到这个通知)
    default void pushAgreeApplyGroup(long fromUid, long groupId, String attrs){}

    //拒绝申请入群通知(自己发送的入群申请被拒绝时会收到这个通知)
    default void pushRefuseApplyGroup(long fromUid, long groupId, String attrs){}

    //邀请入群通知
    default void pushInviteGroup(long fromUid, long groupId, String extraMessage, String attrs){}

    //同意邀请入群通知(自己发送的入群邀请被同意时会收到这个通知)
    default void pushAgreeInviteGroup(long fromUid, long groupId, String attrs){}

    //拒绝邀请入群通知(自己发送的入群邀请被同意时会收到这个通知)
    default void pushRefuseInviteGroup(long fromUid, long groupId, String attrs){}

    //群组变更通知 0-自己成功加入 1-自己离开群组 2-解散群组 3-被踢出群组
    default void pushGroupChange(long groupId, String attrs, int changeType){}

    //群组成员变更 0-成员加入 1-成员退出
    default void pushGroupMemberChange(long groupId, long userId, int changeType){}

    //添加管理员通知 0-添加管理员 1-删除管理员
    default void pushGroupManagerChange(long gid, List<Long> uids, int changeType){}

    //群主变更
    default void pushGroupLeaderChange(long gid, long oldLeader, long newLeader){}


    //房间变更通知 0-自己成功加入 1-自己离开房间 2-解散房间 3-被踢出房间
    default void pushRoomChange(long roomId, String attrs, int changeType){}

    //房间成员变更 0-成员加入 1-成员退出
    default void pushRoomMemberChange(long roomId, long userId, int changeType){}

    //添加管理员通知 0-添加管理员 1-删除管理员
    default void pushRoomManagerChange(long gid, List<Long> uids, int changeType){}

    //房主变更
    default void pushRoomLeaderChange(long gid, long oldLeader, long newLeader){}

    //邀请加入房间通知
    default void pushInviteRoom(long fromUid, long roomId, String extraMessage, String attrs){}

    //同意邀请加入房间通知(自己发送的入房间邀请被同意时会收到这个通知)
    default void pushAgreeInviteRoom(long fromUid, long roomId, String attrs){}

    //拒绝邀请加入房间通知(自己发送的入房间邀请被同意时会收到这个通知)
    default void pushRefuseInviteRoom(long fromUid, long roomId, String attrs){}
}
