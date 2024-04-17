package com.highras.liveDatasLibsALL;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.LiveDataRTE.IBasePushProcessor;
import com.LiveDataRTE.LDEngine;
import com.LiveDataRTE.LDInterface.*;
import com.LiveDataRTE.LiveDataStruct.*;
import com.LiveDataRTE.RTCLib.IRTCPushProcessor;
import com.LiveDataRTE.RTCLib.RTCStruct.*;
import com.fpnn.sdk.ErrorRecorder;
import com.fpnn.sdk.FunctionalAnswerCallback;
import com.fpnn.sdk.TCPClient;
import com.fpnn.sdk.proto.Answer;
import com.fpnn.sdk.proto.Quest;
import com.highras.liveDatasLibsALL.adapter.VoiceMemberAdapter;
import com.highras.liveDatasLibsALL.model.VoiceMember;
import com.livedata.rtc.RTCEngine;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TestVoiceActivity extends AppCompatActivity {
    private static final String TAG = "TestVoiceActivity";
    Timer timer;
    public TestErrorRecorderVoice voicerecoder = new TestErrorRecorderVoice();
    boolean micStatus = false;
    boolean usespeaker = true;
    boolean audioOutputStatus = true;
    TCPClient rttcclient = null;
    LinearLayout leave;
    MediaPlayer mediaPlayer = new MediaPlayer();
    LinearLayout mic;
    Utils utils = Utils.INSTANCE;
    LDEngine ldEngine;
    LinearLayout speaker;
    LinearLayout audiooutputlinelayout;
    ImageView speakerImageView;
    ImageView muteImageView;
    ImageView audiooutputImageView;
    ImageView back;
    TextView muteTextView;
    TextView logView;
    long activityRoom = 0;
    long userid = 0;
    Activity myactivity = this;
    TextView roomshow;
    TextView udpRTTshow;
    TextView tcpRTTshow;
    Button clear;
    String nickName;

    Chronometer chronometer;

    VoiceMemberAdapter voiceMemberAdapter;

    private <T extends View> T $(int resId) {
        return (T) super.findViewById(resId);
    }

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (ldEngine != null && ldEngine.isOnline()) {
                voiceMemberAdapter.notifyDataSetChanged();
            }
            handler.postDelayed(this, 1000);
        }
    };


    void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        stopTimer();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        leaveRoom();
    }

    private void leaveRoom() {
        if (ldEngine == null)
            return;
        chronometer.stop();
        ldEngine.RTC.leaveRTCRoom(activityRoom, new IEmptyCallback() {
            @Override
            public void onSuccess() {
                activityRoom = 0;
                ldEngine.closeEngine();
            }

            @Override
            public void onError(LDAnswer answer) {
                utils.alertDialog(myactivity, "leaveRTCRoom error:"+ answer.getErrInfo());
            }
        });
    }

    @Override
    public Resources getResources() {
        // 字体大小不跟随系统
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.fontScale != 1)
            getResources();
        super.onConfigurationChanged(newConfig);
    }

    Toolbar toolbar;

    RelativeLayout soundRelayout;
    TextView soundText;
    List<VoiceMember> memberlist = new ArrayList<>();

    void addLog(final String msg) {
        myactivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (logView) {
                    String realmsg = "[" + (new SimpleDateFormat("MM-dd HH:mm:ss.SSS").format(new Date())) + "] " + msg + "\n";
                    logView.append(realmsg);
                }
            }
        });
    }

    private void initMediaPlayer() {
        try {
            AssetFileDescriptor fd = getAssets().openFd("zh.wav");
            mediaPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            mediaPlayer.setLooping(true);//设置为循环播放
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.testvoice);
        utils = Utils.INSTANCE;
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        customToolbarAndStatusBarBackgroundColor(true);
        mic = $(R.id.mic);
        back = $(R.id.back);
        muteImageView = $(R.id.muteImageView);
        muteImageView.setSelected(false);
        speaker = $(R.id.speaker);
        audiooutputlinelayout = $(R.id.audiooutputlinelayout);
        speakerImageView = $(R.id.speakerImageView);
        audiooutputImageView = $(R.id.audiooutputview);
        leave = $(R.id.leave);
        muteTextView = $(R.id.muteTextView);
        muteTextView.setSelected(true);
        roomshow = $(R.id.roomnum);
        udpRTTshow = $(R.id.UDPRTTshow);
        tcpRTTshow = $(R.id.TCPRTTshow);
        logView = $(R.id.logview);
        logView.setTextSize(14);
        logView.setTextColor(this.getResources().getColor(R.color.white));
        logView.setMovementMethod(ScrollingMovementMethod.getInstance());
        clear = $(R.id.clearlog);
        if (utils.ldEngine == null)
            return;
        ldEngine = utils.ldEngine;
        ldEngine.setBasePushProcessor(irtcBaseProcessor);
        ldEngine.setRTCPushProcessor(irtcPushProcessor);
        audiooutputlinelayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAudioOutputStatus();
            }
        });



        chronometer = $(R.id.caltimer);
        activityRoom = utils.currentRoomid;
        userid = utils.currentUserid;
        audiooutputImageView.setSelected(true);
        speakerImageView.setSelected(true);
        nickName = utils.nickName;

        initMediaPlayer();

//        addLog("buffsize "+ AudioTrack.getMinBufferSize(48000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT));
        soundText = $(R.id.nameTextView);
        soundRelayout = $(R.id.sound_relayout);
        Timer timer = new Timer();
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                synchronized (logView) {
                    logView.setText("");
                }
            }
        });
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (rttcclient == null)
                    rttcclient = TCPClient.create(utils.rtmEndpoint);
                Quest quest = new Quest("*ping");
                long sendTime = System.currentTimeMillis();
                rttcclient.sendQuest(quest, new FunctionalAnswerCallback() {
                    @Override
                    public void onAnswer(Answer answer, int errorCode) {
                        long recieveTime = System.currentTimeMillis();
                        long RTTTime = recieveTime - sendTime;
//                        mylog.log("*ping");
                        showRTMRTT(RTTTime);
//                        tcpRTTshow.setText("RTM:" + RTTTime);
                    }
                });
                long udpTime = RTCEngine.getRTTTime();
                showRTCRTT(udpTime);
            }
        }, 1, 2000);
        leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                leaveRoom();
                finish();
            }
        });


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        speaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                if (ldEngine == null || !ldEngine.isOnline())
                    return;
                setSpeakerStatus();
            }
        });

        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ldEngine == null || !ldEngine.isOnline() || activityRoom == 0)
                    return;
                setMicStatus(!micStatus);
            }
        });
//        handler.postDelayed(runnable, 1000);

        RecyclerView recyclerView = findViewById(R.id.room_member_recycle);
        recyclerView.setLayoutManager(new MyGridLayoutManager(this, 2));
        voiceMemberAdapter = new VoiceMemberAdapter(this, memberlist);
        recyclerView.setAdapter(voiceMemberAdapter);


        utils.realEnterRoom(activityRoom, RTCRoomType.AUDIO,this, new Utils.MyCallback<RTCRoomInfo>() {
            @Override
            public void onsucess(RTCRoomInfo rtcRoomInfo) {
                if (rtcRoomInfo.uids.size()>0){
                    startVoice(rtcRoomInfo, false, activityRoom);
                }
            }

            @Override
            public void onError(LDAnswer answer) {
                addLog("进入房间失败:"+answer.getErrInfo());
            }
        });

    }


    private void createDialog(String msg) {
        try {
            if (this.isFinishing()) {
                Log.d(TAG, "activity is finishing");
                return;
            }
            AlertDialog dialog = new AlertDialog.Builder(this).
                    setTitle("进入房间").
                    setCancelable(false).setMessage(msg).
                    setPositiveButton("确 认", (dlg, whichButton) -> {
                        finish();
                    }).create();
            dialog.show();
        } catch (Exception e) {
            Log.d(TAG, "createDialog: " + e.getMessage());
        }
    }

    public class TestErrorRecorderVoice extends ErrorRecorder {
        public TestErrorRecorderVoice() {
            super.setErrorRecorder(this);
        }

        @Override
        public void recordError(Exception e) {
            String msg = "Exception:" + e;
            addLog(msg);
        }

        @Override
        public void recordError(String message) {
            addLog(message);
        }

        @Override
        public void recordError(String message, Exception e) {
            String msg = String.format("Error: %s, exception: %s", message, e);
            addLog(msg);
        }
    }

    void closeInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getWindowToken(), 0);
    }

    void setMicStatus(boolean status) {
        if (!status) {
            ldEngine.RTC.closeMic();
            muteTextView.setText("麦克风关闭");
            muteImageView.setSelected(false);
        } else {
            ldEngine.RTC.openMic();
            muteTextView.setText("麦克风开启");
            muteImageView.setSelected(true);
        }
        micStatus = status;
    }

    void setSpeakerStatus() {
        usespeaker = !usespeaker;
        if (!usespeaker) {
            speakerImageView.setSelected(false);
        } else {
            speakerImageView.setSelected(true);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (usespeaker)
                    ldEngine.RTC.switchAudioOutput(true);
                else
                    ldEngine.RTC.switchAudioOutput(false);
            }
        }).start();
    }

    void setAudioOutputStatus() {
        audioOutputStatus = !audioOutputStatus;
        if (!audioOutputStatus) {
            audiooutputImageView.setSelected(false);
        } else {
            audiooutputImageView.setSelected(true);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (audioOutputStatus)
                    ldEngine.RTC.openAudioOutput();
                else
                    ldEngine.RTC.closeAudioOutput();
            }
        }).start();
    }


    void startVoice(RTCRoomInfo info, boolean relogin, long roomId) {
        startVoice(roomId, relogin, info);
    }

    void startVoice(long roomId, boolean relogin, RTCRoomInfo info) {
        activityRoom = roomId;
        LDAnswer ret = ldEngine.RTC.setActivityRoom(activityRoom);
        if (ret.errorCode != 0) {
            addLog("set activity error " + ret.getErrInfo());
            return;
        }
        if (relogin) {
            if (!usespeaker) {
                ldEngine.RTC.switchAudioOutput(false);
            }
            if (micStatus) {
                ldEngine.RTC.openMic();
            } else {
                ldEngine.RTC.closeMic();
            }
        }

        myactivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chronometer.start();
                memberlist.clear();
                roomshow.setText("房间id-" + roomId + "    用户-" + nickName + "(" +ldEngine.getUid()+")");
                if (info != null && info.uids != null) {
                    info.uids.forEach(aLong -> {
                        VoiceMember m = new VoiceMember(aLong, "");
                        if (aLong != userid) memberlist.add(m);
                    });
                    voiceMemberAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    public void showRTCRTT(long rtt) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                udpRTTshow.setText("RTC:" + rtt);
            }
        });
    }

    public void showRTMRTT(long rtt) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tcpRTTshow.setText("RTM:" + rtt);
            }
        });
    }


    protected void customToolbarAndStatusBarBackgroundColor(boolean darkTheme) {
        int toolbarBackgroundColorResId = darkTheme ? R.color.purple_500 : R.color.white;
        setTitleBackgroundResource(toolbarBackgroundColorResId, darkTheme);
    }

    /**
     * 设置状态栏和标题栏的颜色
     *
     * @param resId 颜色资源id
     */
    protected void setTitleBackgroundResource(int resId, boolean dark) {
        toolbar.setBackgroundResource(resId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, resId));
        }
        setStatusBarTheme(this, dark);
    }

    /**
     * Changes the System Bar Theme.
     */
    public static void setStatusBarTheme(final Activity pActivity, final boolean pIsDark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Fetch the current flags.
            final int lFlags = pActivity.getWindow().getDecorView().getSystemUiVisibility();
            // Update the SystemUiVisibility dependening on whether we want a Light or Dark theme.
            pActivity.getWindow().getDecorView().setSystemUiVisibility(pIsDark ? (lFlags & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) : (lFlags | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR));
        }
    }

    private VoiceMember containsMember(long userid) {
        for (VoiceMember member: memberlist){
            if (member.getUid() == userid){
                return member;
            }
        }
        return  null;
    }

    private void removeMember(long userid) {
        memberlist.removeIf(item -> item.getUid() == userid);
    }

    private void setVoice(long userid) {
        memberlist.forEach(voiceMember -> {
            if (userid == voiceMember.getUid()) {
                voiceMember.setPreviousVoiceTime(System.currentTimeMillis());
            }
        });
    }

    IBasePushProcessor irtcBaseProcessor = new IBasePushProcessor() {
        @Override
        public boolean reloginWillStart(long uid, int reloginCount) {
            return true;
        }

        @Override
        public void reloginCompleted(long uid, boolean successful, LDAnswer answer, int reloginCount) {
            if (successful) {
                if (activityRoom <= 0 || ldEngine == null)
                    return;
                addLog("RTM重连成功");
                ldEngine.RTC.enterRTCRoom(activityRoom, "", new ICallback<RTCRoomInfo>() {
                    @Override
                    public void onSuccess(RTCRoomInfo rtcRoomInfo) {
                        addLog("重新进入RTC房间成功");
                        ldEngine.RTC.getRTCRoomMembers(activityRoom, new ICallback<RTCRoomInfo>() {
                            @Override
                            public void onSuccess(RTCRoomInfo rtcRoomInfo) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        startVoice(activityRoom, true, rtcRoomInfo);
                                    }
                                });

                            }

                            @Override
                            public void onError(LDAnswer answer) {
                                addLog("getRTCRoomMembers 失败:" + answer.getErrInfo());
                            }
                        });
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        TestVoiceActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                soundRelayout.setVisibility(View.VISIBLE);
                                soundText.setText("重新进入房间" + reloginCount + "次失败");
                            }
                        });
                    }
                });
            } else {
                TestVoiceActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        soundRelayout.setVisibility(View.VISIBLE);
                        soundText.setText("重连失败");
                        addLog("重连失败 " + answer.getErrInfo());
                    }
                });
                micStatus = false;
            }
            TestVoiceActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    soundRelayout.setVisibility(View.GONE);
                    soundText.setText("");
                }
            });
        }

        @Override
        public void rtmConnectClose(long uid) {
            TestVoiceActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addLog("客户端断开连接");
                    soundRelayout.setVisibility(View.VISIBLE);
                    soundText.setText("客户端断开连接");
                }
            });
        }

        @Override
        public void kickout() {
            TestVoiceActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addLog("被服务器踢下线");
                    soundRelayout.setVisibility(View.VISIBLE);
                    soundText.setText("被服务器踢下线");
                }
            });
        }
    };


    IRTCPushProcessor irtcPushProcessor = new IRTCPushProcessor() {
        @Override
        public void pushEnterRTCRoom(long roomId, long userId, long time) {
            if (containsMember(userId) == null) {
                TestVoiceActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addLog( "(" + userId + ")" + " 进入房间");
                        memberlist.add(new VoiceMember(userId, ""));
                        voiceMemberAdapter.notifyDataSetChanged();
                    }
                });
            }
        }

        @Override
        public void pushExitRTCRoom(long roomId, long userId, long time) {
            VoiceMember member = containsMember(userId);
            if (member!= null) {
                TestVoiceActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addLog(member.getNickName() + "(" + userId + ") 离开房间");
                        removeMember(userId);
                        voiceMemberAdapter.notifyDataSetChanged();
                    }
                });
            }
        }


        @Override
        public void pushRTCRoomClosed(long roomId) {
            addLog( "pushRTCRoomClosed: 房间" + roomId + "已关闭");
        }

        @Override
        public void pushKickoutRTCRoom(final long roomId) {
            addLog( "pushKickoutRTCRoom: 被踢出语音房间" + roomId);
        }


        @Override
        public void pushInviteIntoRTCRoom(long roomId, long userId) {
            IRTCPushProcessor.super.pushInviteIntoRTCRoom(roomId, userId);
        }


        @Override
        public void voiceSpeak(long uid) {
            myactivity.runOnUiThread(() -> {
                setVoice(uid);
                voiceMemberAdapter.notifyDataSetChanged();
            });
        }
    };
}
