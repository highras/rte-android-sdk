package com.highras.liveDatasLibsALL;

import android.content.DialogInterface;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.LiveDataRTE.LDEngine;
import com.LiveDataRTE.LDInterface;
import com.LiveDataRTE.LiveDataStruct;
import com.LiveDataRTE.RTCLib.IRTCPushProcessor;
import com.LiveDataRTE.RTCLib.RTCStruct;
import com.LiveDataRTE.RTMLib.IRTMPushProcessor;
import com.LiveDataRTE.RTMLib.RTMClient;

public class testp2p extends AppCompatActivity implements View.OnClickListener {

    LDEngine ldEngine;
    Utils utils = Utils.INSTANCE;
    SurfaceView myview;
    SurfaceView otherview;
    IRTCPushProcessor irtcPushProcessor = new IRTCPushProcessor() {
        @Override
        public void pushRequestP2PRTC(long uid, RTCStruct.P2PRTCType type) {
            testp2p.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(testp2p.this);
                    builder.setTitle("请求p2p rtc");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ldEngine.RTC.acceptP2PRTC(myview, otherview, new LDInterface.IEmptyCallback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(LiveDataStruct.LDAnswer answer) {

                                }
                            });
                        }
                    });
                    builder.setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ldEngine.RTC.refuseP2PRTC(new LDInterface.IEmptyCallback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(LiveDataStruct.LDAnswer answer) {

                                }
                            });
                        }
                    });
                    builder.show();
                }
            });
        }

        @Override
        public SurfaceView pushP2PRTCEvent(long uid, RTCStruct.P2PRTCType type, RTCStruct.P2PRTCEvent event) {
            mylog.log("pushP2PRTCEvent type:" + type.value() + " P2PRTCEvent:" + event.value());
            if (event == RTCStruct.P2PRTCEvent.Accept){
                return otherview;
            }
            return null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testp2p);
        findViewById(R.id.requestp2paudio).setOnClickListener(this);
        findViewById(R.id.requestp2pvideo).setOnClickListener(this);
        findViewById(R.id.cancelp2p).setOnClickListener(this);
        findViewById(R.id.closep2p).setOnClickListener(this);
        myview = findViewById(R.id.myview);
        otherview = findViewById(R.id.otherview);

        ldEngine = utils.ldEngine;
        ldEngine.setRTCPushProcessor(irtcPushProcessor);
        myview.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        myview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                mylog.log("myview created");
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

            }
        });

        otherview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                mylog.log("otherview created");
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ldEngine == null)
            return;
        ldEngine.closeEngine();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.requestp2paudio){
            ldEngine.RTC.requestP2PRTC(RTCStruct.P2PRTCType.AUDIO, 666, null, new LDInterface.IEmptyCallback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(LiveDataStruct.LDAnswer answer) {
                        mylog.log("requestp2paudio error:"+ answer.getErrInfo());
                }
            });
        }
        else if (v.getId() == R.id.requestp2pvideo){
            ldEngine.RTC.requestP2PRTC(RTCStruct.P2PRTCType.VIDEO, 666, myview, new LDInterface.IEmptyCallback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(LiveDataStruct.LDAnswer answer) {
                    mylog.log("requestp2pvideo error:"+ answer.getErrInfo());
                }
            });
        }
        else if (v.getId() == R.id.cancelp2p){
            ldEngine.RTC.cancelP2PRTC(new LDInterface.IEmptyCallback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(LiveDataStruct.LDAnswer answer) {
                    mylog.log("cancelP2PRTC error:"+ answer.getErrInfo());

                }
            });
        }
        else if (v.getId() == R.id.closep2p){
            ldEngine.RTC.closeP2PRTC(new LDInterface.IEmptyCallback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(LiveDataStruct.LDAnswer answer) {
                    mylog.log("closeP2PRTC ret " + answer.getErrInfo());

                }
            });
        }
    }
}