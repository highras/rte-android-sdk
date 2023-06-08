package com.LiveDataRTE.RTCLib;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class RTCStruct {
    //P2PRTC event
    public enum P2PRTCEvent {
        CancelRequest(1), //对方取消请求
        RingOff(2), //对方挂断
        Accept(3),//对方接受请求
        Refuse(4),//对方拒绝请求
        NoResponse(5);//对方无应答

        private int value;

        P2PRTCEvent(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        public static P2PRTCEvent intToEnum(int type){
            switch (type) {
                case 1:
                    return CancelRequest;
                case 2:
                    return RingOff;
                case 3:
                    return Accept;
                case 4:
                    return Refuse;
                case 5:
                    return NoResponse;
                default:
                    return NoResponse;
            }
        }
    }

    public enum CaptureLevle
    {
        Normal(1),  //一般 320 * 240 15fps 300kbps(默认)
        MIddle(2),  //中等质量 320 * 240 30fps 500kbps
        High(3);    //高质量 640*480 30fps 500kbps
        private int value;

        CaptureLevle(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }
    }

    public enum RTCRoomType{
        AUDIO        (1), //普通语音房间
        VIDEO        (2), //普通视频房间
        TRANSLATE    (3); //实时翻译语音房间
        private int value;
        RTCRoomType (int value) {
            this.value = value;
        }
        public int value() {
            return value;
        }
        public static RTCRoomType intToEnum(int type){
            switch (type) {
                case 1:
                    return AUDIO;
                case 2:
                    return VIDEO;
                case 3:
                    return TRANSLATE;
                default:
                    return AUDIO;
            }
        }
    }


    public enum P2PRTCType{
        AUDIO        (1), //语音通话
        VIDEO        (2); //视频通话
        private int value;
        P2PRTCType (int value) {
            this.value = value;
        }
        public int value() {
            return value;
        }
        public static P2PRTCType intToEnum(int type){
            switch (type) {
                case 1:
                    return AUDIO;
                case 2:
                    return VIDEO;
                default:
                    return AUDIO;
            }
        }
    }


    public static class RTCRoomInfo{
        public RTCStruct.RTCRoomType roomType; //房间类型 1-voice 2-video 3-语聊房
        public long roomId; //房间id
        public long owner;//房主
        public List<Long> uids; //房间的成员uid
        public List<Long> managers; //房间的管理员id
    }
}
