package com.highras.liveDatasLibsALL;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.LiveDataRTE.IBasePushProcessor;
import com.LiveDataRTE.LDEngine;
import com.LiveDataRTE.LDInterface;
import com.LiveDataRTE.LiveDataStruct;
import com.LiveDataRTE.RTCLib.RTCStruct;
import com.LiveDataRTE.RTMLib.RTMErrorCode;
import com.LiveDataRTE.VoiceRoomLib.RTCEventHandle;
import com.fpnn.sdk.ErrorRecorder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class VoiceroomtestActivity extends AppCompatActivity implements View.OnClickListener {
    Utils utils = Utils.INSTANCE;
    LDEngine ldEngine;
    long activityRoom = 0;
    long userid = 0;
    SeekBar bgmPosProgress;
    SeekBar bgmVolumeProgress;
    int posbgm = 0;
    int volumebgm = 0;
    TextView posshow;
    TextView volumeshow;
    TextView logView;

    void addLog(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (logView) {
                    String realmsg = "[" + (new SimpleDateFormat("MM-dd HH:mm:ss.SSS").format(new Date())) + "] " + msg + "\n";
                    logView.append(realmsg);
                }
            }
        });
    }

    class  jjjj extends ErrorRecorder{
        public  void recordError(String message) {
            addLog(message);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_voiceroomtest);

        if (utils.ldEngine == null)
            return;
        ldEngine = utils.ldEngine;
        activityRoom = utils.currentRoomid;
        userid = utils.currentUserid;
        posshow = findViewById(R.id.posshow);
        volumeshow = findViewById(R.id.volumeshow);

        findViewById(R.id.openmic).setOnClickListener(this);
        findViewById(R.id.closemic).setOnClickListener(this);
        findViewById(R.id.closevoice).setOnClickListener(this);
        findViewById(R.id.openvoice).setOnClickListener(this);
        findViewById(R.id.openstream).setOnClickListener(this);
        findViewById(R.id.closestream).setOnClickListener(this);
        findViewById(R.id.leaveroom).setOnClickListener(this);
        findViewById(R.id.startbgm).setOnClickListener(this);
        findViewById(R.id.pauseBGM).setOnClickListener(this);
        findViewById(R.id.resumeBGM).setOnClickListener(this);
        findViewById(R.id.audioeffect).setOnClickListener(this);
        logView = findViewById(R.id.logview);
        bgmVolumeProgress = findViewById(R.id.bgmVolumeProgress);
        bgmPosProgress = findViewById(R.id.bgmPosProgress);



        bgmVolumeProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                volumebgm = seekBar.getProgress();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                volumeshow.setText("volume:"+volumebgm);
                ldEngine.VoiceRoomClient.setBGNVolume(volumebgm);
            }
        });


        bgmPosProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                posbgm = seekBar.getProgress();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                posshow.setText("pos:"+posbgm);
                ldEngine.VoiceRoomClient.setBGMPos(posbgm);
                mylog.log("设置BGM位置:" + posbgm);
            }
        });
        ldEngine.setErrorRecoder(new jjjj());
        ldEngine.VoiceRoomClient.enterRTCVoiceRoom(activityRoom, new LDInterface.ICallback<List<Long>>() {
            @Override
            public void onSuccess(List<Long> longs) {
                ldEngine.VoiceRoomClient.pushStream();
            }

            @Override
            public void onError(LiveDataStruct.LDAnswer answer) {
                if (answer.errorCode == RTMErrorCode.RTM_EC_VOICE_ROOM_NOT_EXIST.value()){
                    ldEngine.VoiceRoomClient.createRTCRoom(activityRoom, new LDInterface.IEmptyCallback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(LiveDataStruct.LDAnswer answer) {
                            mylog.log( "onResult: 创建房间" + activityRoom + " 失败:" + answer.getErrInfo() );
                            return;

                        }
                    });
                }else{
                    mylog.log( "onResult: 进入房间" + activityRoom + " 失败:" + answer.getErrInfo() );
                    return;
                }
            }
        });
        ldEngine.VoiceRoomClient.setEventHandle(new RTCEventHandle() {
            @Override
            public void onParseMusicError(String err) {
                mylog.log("startBGM error " + err);
            }

            @Override
            public void onBGMStart(int duartionTime) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bgmPosProgress.setMax(duartionTime);
                    }
                });
                mylog.log("onBGMStart duartionTime " + duartionTime);
            }

            @Override
            public void onBGMEnd() {
                mylog.log("onBGMEnd ");
            }

            @Override
            public void speakVolume(HashMap<Long, Integer> userVolume) {
                for (long uid: userVolume.keySet()){
                    mylog.log("userid " + uid + " volume " + userVolume.get(uid));
                }
            }
        });
        ldEngine.setBasePushProcessor(irtcBaseProcessor);

    }

    IBasePushProcessor irtcBaseProcessor = new IBasePushProcessor() {
        @Override
        public boolean reloginWillStart(long uid, int reloginCount) {
            return true;
        }

        @Override
        public void reloginCompleted(long uid, boolean successful, LiveDataStruct.LDAnswer answer, int reloginCount) {
            if (successful) {
                if (activityRoom <= 0 || ldEngine == null)
                    return;
                mylog.log("RTM重连成功");
                ldEngine.VoiceRoomClient.enterRTCVoiceRoom(activityRoom, new LDInterface.ICallback<List<Long>>() {
                    @Override
                    public void onSuccess(List<Long> longs) {
                        mylog.log("重新进入语聊房房间成功");
                    }

                    @Override
                    public void onError(LiveDataStruct.LDAnswer answer) {
                        mylog.log("重新进入语聊房房间失败");

                    }
                });
            } else {
                mylog.log("重新失败:" + answer.getErrInfo());
            }

        }

        @Override
        public void rtmConnectClose(long uid) {
            mylog.log("客户端断开连接");
        }

        @Override
        public void kickout() {
            mylog.log("被服务器踢下线");
        }
    };


    @Override
    public void onClick(View v) {
        int id = v.getId();
        
        if (id == R.id.openmic){
            ldEngine.VoiceRoomClient.openMic();
            
        }else if (id == R.id.closemic){
            ldEngine.VoiceRoomClient.closeMic();
        }else if (id == R.id.openvoice){
            ldEngine.VoiceRoomClient.openAudioOutput();

        }else if (id == R.id.closevoice){
            ldEngine.VoiceRoomClient.closeAudioOutput();

        }else if (id == R.id.openstream){
            ldEngine.VoiceRoomClient.pushStream();

        }else if (id == R.id.closestream){
            ldEngine.VoiceRoomClient.closeStream();

        }else if (id == R.id.leaveroom){
            finish();
        }
        else if (id == R.id.startbgm){
            ldEngine.VoiceRoomClient.startAudioMixing("/sdcard/Download/test.mp3",0, true);
        }
        else if (id == R.id.audioeffect){
            ldEngine.VoiceRoomClient.startAudioEffect("/sdcard/Download/zhangsheng.mp3");
//            ldEngine.VoiceRoomClient.startAudioEffect("/sdcard/Download/zhaoizlong.mp3");
        }
        else if (id == R.id.pauseBGM){
            ldEngine.VoiceRoomClient.pauseBGM();
        }
        else if (id == R.id.resumeBGM){
            ldEngine.VoiceRoomClient.resumeBGM();
        }
    /*    else if (id == R.id.movepos){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            EditText editText = new EditText(this);
            builder.setView(editText);
            builder.setMessage("设置bgm位置").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        int pos  = Integer.parseInt(editText.getText().toString());
                        ldEngine.VoiceRoomClient.setBGMPos(pos);
                    } catch (NumberFormatException ex) {
                        mylog.log("设置位置错误:" + ex.getMessage());
                    }
                    catch (Exception e){
                        mylog.log("设置群组房间id错误:" + e.getMessage());
                    }
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.show();
        }*/
    }

    private void leaveRoom() {
        if (ldEngine == null)
            return;
        ldEngine.VoiceRoomClient.leaveRTCRoom(activityRoom, new LDInterface.IEmptyCallback() {
            @Override
            public void onSuccess() {
                activityRoom = 0;
                ldEngine.closeEngine();
            }

            @Override
            public void onError(LiveDataStruct.LDAnswer answer) {
                ldEngine.closeEngine();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        leaveRoom();
    }
}