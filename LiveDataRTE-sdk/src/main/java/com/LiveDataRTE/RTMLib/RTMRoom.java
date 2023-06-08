package com.LiveDataRTE.RTMLib;

import com.LiveDataRTE.InternalEngine.EngineClient;
import com.LiveDataRTE.LiveDataStruct.*;
import com.LiveDataRTE.LDInterface.*;

import java.util.List;
import java.util.Map;

class RTMRoom {
    EngineClient engineClient;
    SendSource sendType = SendSource.RTM;

    public void setEngineClient(EngineClient _engineClient){
        engineClient = _engineClient;
    }


    /**
     * 进入房间
     * @param roomId   房间id
     * @param callback 回调
     */
    public void enterRoom(long roomId,  IEmptyCallback callback) {
        engineClient.enterRoom(roomId, callback);
    }


    /**
     * 离开房间
     * @param roomId   房间id
     * @param callback 回调
     */
    public void leaveRoom(long roomId,  IEmptyCallback callback) {
        engineClient.leaveRoom(roomId, callback);
    }


    /**
     * 获取房间中的所有member (由于分布式系统，房间的人数会有几秒同步间隔)
     * @param roomId   房间id
     * @param callback 回调
     */
    public void getRoomMembers(long roomId,  final ICallback<List<Long>> callback) {
        engineClient.getRoomMembers(roomId, callback);
    }



    /**
     * 获取房间中的所有人数 (由于分布式系统，房间的人数会有几秒同步间隔)
     * @param roomIds   房间id集合
     * @param callback <房间id,用户人数>回调
     */
    public void getRoomMemberCount(List<Long> roomIds,  final ICallback<Map<Long,Integer>> callback) {
        engineClient.getRoomMemberCount(roomIds, callback);
    }


    /**
     * 获取用户所在的房间
     * @param callback 回调
     */
    public void getUserRooms( final ICallback<List<Long>> callback) {
        engineClient.getUserRooms(callback);
    }

    /**
     * 设置房间的公开信息和私有信息
     * @param roomId   房间id
     * @param publicInfo    房间公开信息
     * @param privateInfo   房间私有信息
     * @param callback  回调
     */
    public void setRoomInfo(long roomId, String publicInfo, String privateInfo,  IEmptyCallback callback) {
        engineClient.setRoomInfo(roomId, publicInfo,  privateInfo, callback);
    }

    /**
     * 获取房间的公开信息和私有信息 (必须在房间内)
     * @param roomId   房间id
     * @param callback <公开信息,私有信息>  回调
     */
    public void getRoomInfo(final long roomId,  final IGetinfoCallback callback) {
        engineClient.getRoomInfo(roomId, callback);
    }


    /**
     * 获取房间的公开信息，每次最多获取100个
     * @param roomIds     房间id集合
     * @param callback <Map<房间id, 公开信息>>回调
     */
    public void getRoomsPublicInfo(List<Long> roomIds,  final ICallback<Map<Long, String>> callback) {
        engineClient.getRoomsPublicInfo(roomIds, callback);
    }
}
