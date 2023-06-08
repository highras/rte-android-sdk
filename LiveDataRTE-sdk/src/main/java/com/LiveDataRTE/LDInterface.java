package com.LiveDataRTE;

import com.LiveDataRTE.LiveDataStruct.LDAnswer;

public class LDInterface {

    //返回LDAnswer的回调接口
    public interface IEmptyCallback {
        void onSuccess();
        void onError(LDAnswer answer);
    }

    //泛型接口 带有一个返回值的回调函数
    public interface ICallback<T> {
        void onSuccess(T t);
        void onError(LDAnswer answer);
    }

    //发送消息回调(带回文本审核结果)
    public interface ISendMsgCallback{
        void onSuccess(long messageTime, long messageId, String msg);
        void onError(LDAnswer answer);
    }

    //发送文件回调
    public interface ISendFileCallback{
        void onSuccess(long messageTime, long messageId);
        void onError(LDAnswer answer);
    }


    //发送文件回调
    public interface IUploadFileCallback{
        void onSuccess(String  fileUrl, long fileSize);
        void onError(LDAnswer answer);
    }



    public interface IGetinfoCallback{
        void onSuccess(String publicInfo, String privateInfo);
        void onError(LDAnswer answer);
    }


    //泛型接口 带有两个返回值的回调函数
    public interface IDoubleCallBack<T,V> {
        void onSuccess(T t, V v);
        void onError(LDAnswer answer);
    }
}
