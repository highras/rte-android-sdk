package com.highras.liveDatasLibsALL;

import com.LiveDataRTE.IBasePushProcessor;

public class
MyRTMPushProcessor implements IBasePushProcessor {
/*    @Override
    public boolean reloginWillStart(long uid, int reloginCount) {
        mylog.log(uid + " 开始重连第 " + reloginCount + "次");
        return true;
    }

    @Override
    public void reloginCompleted(long uid, boolean successful, LiveDataStruct.LDAnswer answer, int reloginCount) {
        mylog.log(uid + " 重连完成 " + answer.getErrInfo());

        Iterator<OnProcessorListener> iterator = Constants.onProcessorListeners.iterator();
        while (iterator.hasNext()) {
            iterator.next().reloginCompleted(uid, successful, answer, reloginCount);
        }
    }

    @Override
    public void rtmConnectClose(long uid) {
        Iterator<OnProcessorListener> iterator = Constants.onProcessorListeners.iterator();
        while (iterator.hasNext()) {
            iterator.next().rtmConnectClose(uid);
        }
    }*/
}
