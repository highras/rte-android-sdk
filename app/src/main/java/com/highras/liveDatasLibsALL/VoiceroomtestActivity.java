package com.highras.liveDatasLibsALL;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
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
    boolean micstatus = false;
    boolean outputstatus = false;
    boolean pushflag = true;

    void addLog(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (logView) {
                    mylog.log(msg);
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
        findViewById(R.id.stopbgm).setOnClickListener(this);
        findViewById(R.id.getbgmpos).setOnClickListener(this);

        findViewById(R.id.audioeffect).setOnClickListener(this);
        findViewById(R.id.audioeffect1).setOnClickListener(this);
        findViewById(R.id.audioeffectpause).setOnClickListener(this);
        findViewById(R.id.audioeffectpause1).setOnClickListener(this);
        findViewById(R.id.resumeaudioeffect).setOnClickListener(this);
        findViewById(R.id.resumeaudioeffect1).setOnClickListener(this);
        findViewById(R.id.stopaudioeffect).setOnClickListener(this);
        findViewById(R.id.stopaudioeffect1).setOnClickListener(this);
        findViewById(R.id.pauseaudioeffectall).setOnClickListener(this);
        findViewById(R.id.resumeaudioeffectall).setOnClickListener(this);
        findViewById(R.id.stopaudioeffectall).setOnClickListener(this);

        logView = findViewById(R.id.logview);
        logView.setTextSize(14);
        logView.setTextColor(this.getResources().getColor(R.color.white));
        logView.setMovementMethod(ScrollingMovementMethod.getInstance());
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
                ldEngine.VoiceRoomClient.setBGMVolume(volumebgm);
//                ldEngine.VoiceRoomClient.setAudioEffectVolume(volumebgm);
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
            }
        });
        ldEngine.setErrorRecoder(new jjjj());
        ldEngine.VoiceRoomClient.enterRTCVoiceRoom(activityRoom, new LDInterface.ICallback<List<Long>>() {
            @Override
            public void onSuccess(List<Long> longs) {
                outputstatus = true;
            }

            @Override
            public void onError(LiveDataStruct.LDAnswer answer) {
                if (answer.errorCode == RTMErrorCode.RTM_EC_VOICE_ROOM_NOT_EXIST.value()){
                    ldEngine.VoiceRoomClient.createRTCRoom(activityRoom, new LDInterface.IEmptyCallback() {
                        @Override
                        public void onSuccess() {
                            outputstatus = true;
                        }

                        @Override
                        public void onError(LiveDataStruct.LDAnswer answer) {
                            addLog( "onResult: 创建房间" + activityRoom + " 失败:" + answer.getErrInfo() );
                            return;

                        }
                    });
                }else{
                    addLog( "onResult: 进入房间" + activityRoom + " 失败:" + answer.getErrInfo() );
                    return;
                }
            }
        });
        ldEngine.VoiceRoomClient.setRTCEventHandle(new RTCEventHandle() {
            @Override
            public void onParseMusicError(String err) {
                mylog.log("startBGM error " + err);
            }


            @Override
            public void myspeakVolume(int volume) {
                mylog.log("自己的音量 " + volume);
            }

            @Override
            public void onBGMStart() {
                addLog("bgm start");
            }

            @Override
            public void onBGMEnd() {
               addLog("onBGMEnd ");
            }

            @Override
            public void onParseAudioEffectError(int id, String err) {
                addLog("ParseAudioEffectError id  "+ id + " " + err);
            }

            @Override
            public void onAudioEffectStart(int id, int duartionTime) {
                addLog("onBAudioEffectStart id  "+ id + " time:" + duartionTime);
            }

            @Override
            public void onAudioEffectEnd(int id) {
                addLog("onAudioEffectEnd id  "+ id);
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
        public boolean reloginWillStart(long uid, int reloginCount, LiveDataStruct.LDAnswer answer) {
            return true;
        }

        @Override
        public void reloginCompleted(long uid, boolean successful, LiveDataStruct.LDAnswer answer, int reloginCount) {
            if (successful) {
                if (activityRoom <= 0 || ldEngine == null)
                    return;
                addLog("RTM重连成功");
                ldEngine.VoiceRoomClient.enterRTCVoiceRoom(activityRoom, new LDInterface.ICallback<List<Long>>() {
                    @Override
                    public void onSuccess(List<Long> longs) {
                        addLog("重新进入语聊房房间成功");
                        if (micstatus)
                            ldEngine.VoiceRoomClient.openMic();
                        else
                            ldEngine.VoiceRoomClient.closeMic();

                        if (outputstatus)
                            ldEngine.VoiceRoomClient.openAudioOutput();
                        else
                            ldEngine.VoiceRoomClient.closeAudioOutput();

                        if (pushflag)
                            ldEngine.VoiceRoomClient.pushStream();
                        else
                            ldEngine.VoiceRoomClient.closeStream();
                    }

                    @Override
                    public void onError(LiveDataStruct.LDAnswer answer) {
                        addLog("重新进入语聊房房间失败");

                    }
                });
            } else {
                addLog("RTM重连失败:" + answer.getErrInfo());
            }

        }

        @Override
        public void rtmConnectClose(long uid) {
            addLog("客户端断开连接");
        }

        @Override
        public void kickout() {
            addLog("被服务器踢下线");
        }
    };


    @Override
    public void onClick(View v) {
        int id = v.getId();
        
        if (id == R.id.openmic){
            ldEngine.VoiceRoomClient.openMic();
            micstatus = true;
            
        }else if (id == R.id.closemic){
            ldEngine.VoiceRoomClient.closeMic();
            micstatus = false;
        }else if (id == R.id.openvoice){
            ldEngine.VoiceRoomClient.openAudioOutput();
            outputstatus = true;

        }else if (id == R.id.closevoice){
            ldEngine.VoiceRoomClient.closeAudioOutput();
            outputstatus = false;

        }else if (id == R.id.openstream){
            ldEngine.VoiceRoomClient.pushStream();
            pushflag = true;

        }else if (id == R.id.closestream){
            ldEngine.VoiceRoomClient.closeStream();
            pushflag = false;
        }else if (id == R.id.leaveroom){
            finish();
        }
        else if (id == R.id.startbgm){
            int time = ldEngine.VoiceRoomClient.startAudioMixing("/sdcard/Download/test.mp3",0, 1);
            if (time >0){
                bgmPosProgress.setMax(time);
            }
        }
        else if (id == R.id.audioeffect){
            ldEngine.VoiceRoomClient.startAudioEffect(1,"/sdcard/Download/zhangsheng.mp3",-1);
//            ldEngine.VoiceRoomClient.startAudioEffect("/sdcard/Download/zhaoizlong.mp3");
        }
        else if (id == R.id.audioeffect1){
            ldEngine.VoiceRoomClient.startAudioEffect(2,"/sdcard/Download/zhaozilong.mp3",1);
        }
        else if (id == R.id.audioeffectpause){
            ldEngine.VoiceRoomClient.pauseAudioEffect(1);
        }
        else if (id == R.id.audioeffectpause1){
            ldEngine.VoiceRoomClient.pauseAudioEffect(2);
        }
        else if (id == R.id.resumeaudioeffect){
            ldEngine.VoiceRoomClient.resumeAudioEffect(1);
        }
        else if (id == R.id.resumeaudioeffect1){
            ldEngine.VoiceRoomClient.resumeAudioEffect(2);
        }
        else if (id == R.id.stopaudioeffect){
            ldEngine.VoiceRoomClient.stopAudioEffect(1);
        }
        else if (id == R.id.stopaudioeffect1){
            ldEngine.VoiceRoomClient.stopAudioEffect(2);
        }
        else if (id == R.id.pauseaudioeffectall){
            ldEngine.VoiceRoomClient.pauseAudioEffect(0);
        }
        else if (id == R.id.resumeaudioeffectall){
            ldEngine.VoiceRoomClient.resumeAudioEffect(0);
        }
        else if (id == R.id.stopaudioeffectall){
            ldEngine.VoiceRoomClient.stopAudioEffect(0);
        }
        else if (id == R.id.pauseBGM){
            ldEngine.VoiceRoomClient.pauseBGM();
        }
        else if (id == R.id.resumeBGM){
            ldEngine.VoiceRoomClient.resumeBGM();
        }
        else if (id == R.id.stopbgm){
            ldEngine.VoiceRoomClient.stopBGM();
        }
        else if (id == R.id.getbgmpos){
            int time = ldEngine.VoiceRoomClient.getBGMCurrPos();
            addLog(time+"");
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