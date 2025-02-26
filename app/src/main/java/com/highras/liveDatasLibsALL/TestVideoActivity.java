package com.highras.liveDatasLibsALL;

import static android.view.View.VISIBLE;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.LiveDataRTE.IBasePushProcessor;
import com.LiveDataRTE.LDEngine;
import com.LiveDataRTE.LDInterface.*;
import com.LiveDataRTE.LiveDataStruct.*;
import com.LiveDataRTE.RTCLib.IRTCPushProcessor;
import com.LiveDataRTE.RTCLib.RTCStruct.*;
import com.fpnn.sdk.ErrorRecorder;
import com.highras.liveDatasLibsALL.adapter.MemberAdapter;
import com.highras.liveDatasLibsALL.common.DisplayUtils;
import com.highras.liveDatasLibsALL.common.MyUtils;
import com.highras.liveDatasLibsALL.model.Member;
import com.livedata.rtc.RTCEngine;


import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author fengzi
 * @date 2022/2/17 19:37
 */

public class TestVideoActivity extends BaseActivity implements View.OnClickListener {

    class TestErrorVideoRecorder extends ErrorRecorder {
        public TestErrorVideoRecorder(){
            super.setErrorRecorder(this);
        }

        public void recordError(Exception e) {
            addlog("Exception:" + e);
        }

        public void recordError(String message) {
            addlog("Error:" + message);
        }

        public void recordError(String message, Exception e) {
            addlog(String.format("Error: %s, exception: %s", message, e));
        }
    }


    private static final String TAG = "sdktest";

    LinkedHashMap<Long, Member> memberlists = new LinkedHashMap<>();
    LinkedHashMap<Long, View> userSurfaces = new LinkedHashMap<>();
    MyHandler myHandler = new MyHandler(this);
    long activityRoom = 0;
    private SurfaceView previewSurfaceView = null;
    long myuid = 0;
    String mynickName;
    //是否启用双声道
    Boolean channelNum = false;
    //视频质量
    int videoQulity = 1;
    //摄像头是否开启
    Utils utils = Utils.INSTANCE;
    LDEngine ldEngine;
    public boolean cameraOpen = false;
    boolean micStatus = false;
    boolean usespeaker = true;
    boolean voiceStatus = false;
    boolean useFront = true;
    TestErrorVideoRecorder errorRecorder = new TestErrorVideoRecorder();
    LinearLayout linearlayout;
    AudioManager am;
    ImageView back;
    ImageView mic_image;
    ImageView camera_image;
    TextView mic_text;
    TextView camera_text;
    TextView roomshow;
    Chronometer chronometer;
    int itemWidth = 0;
    int itemHeight = 0;

    TextView changeView;
    TextView logview;
    TextView realLogView;

    TextView alertshow;
    ArrayList<String> realLog =  new ArrayList<>();
    private final List<View> viewList = new LinkedList<>();
    int currentPage = 0;
    int pageSize = 3;

    @Override
    protected void afterViews() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        activityRoom = utils.currentRoomid;
        myuid = utils.currentUserid;
        mynickName = utils.nickName;
        Toolbar toolbar = findViewById(R.id.toolbar);
        am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        linearlayout = findViewById(R.id.linearlayout);
        mic_image = findViewById(R.id.mic_image);
        mic_text = findViewById(R.id.mic_text);
        camera_image = findViewById(R.id.camera_image);
        camera_text = findViewById(R.id.camera_text);
        TextView current_member = findViewById(R.id.current_member);
        current_member.setText(showName(myuid, mynickName ));
//        toolbar.setTitle("房间-" + activityRoom);

        roomshow = findViewById(R.id.roomnum);
//        roomshow.setText("房间-" + activityRoom);
        chronometer = findViewById(R.id.caltimer);

        back = findViewById(R.id.back);
        back.setOnClickListener(this);

        ldEngine = utils.ldEngine;
        ldEngine.setBasePushProcessor(iBasePushProcessor);
        ldEngine.setRTCPushProcessor(irtcPushProcessor);

        CrashHandler.getInstance().init(this);


        previewSurfaceView = findViewById(R.id.preview_surface);
        previewSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        previewSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated( SurfaceHolder surfaceHolder) {
                int width = previewSurfaceView.getWidth();
                int heigh = previewSurfaceView.getHeight();

                float ratio =  (float) width / heigh;
                if (ratio < 0.749 ||  ratio> 0.759){
                    width = Integer.valueOf(heigh * 3 /4);

                }

                RelativeLayout.LayoutParams real = new RelativeLayout.LayoutParams(width, heigh);

                real.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
                real.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                previewSurfaceView.setLayoutParams(real);

//                ViewGroup.LayoutParams params= previewSurfaceView.getLayoutParams();
//                mylog.log("width " + previewSurfaceView.getWidth());
//                mylog.log("heigh " + previewSurfaceView.getHeight());
            }

            @Override
            public void surfaceChanged( SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed( SurfaceHolder surfaceHolder) {

            }
        });
        changeView = findViewById(R.id.change_btn);
        changeView.setOnClickListener(this);

        View inflate = LayoutInflater.from(this).inflate(R.layout.logview, null);

        realLogView = inflate.findViewById(R.id.sholog);
        alertshow = findViewById(R.id.alertshow);
        alertshow.setTextSize(16);
        logview = findViewById(R.id.logbtn);
        logview.setOnClickListener(this);
        itemWidth = DisplayUtils.getScreenWidth(this) / pageSize;
        itemHeight = itemWidth * 4 / 3;
        linearlayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, itemHeight));


        ldEngine.RTC.setPreview(previewSurfaceView);
        utils.realEnterRoom(activityRoom, RTCRoomType.VIDEO, this, new Utils.MyCallback<RTCRoomInfo>() {
            @Override
            public void onsucess(RTCRoomInfo rtcRoomInfo) {
                voiceStatus = true;
                addlog( "onResult: 进入房间" + activityRoom + " 成功" );
                Message message = Message.obtain();
                message.what = 1;
                message.obj = rtcRoomInfo;
                myHandler.sendMessage(message);
            }

            @Override
            public void onError(LDAnswer answer) {
                addlog( "onResult: 进入房间" + activityRoom + " 失败:" + answer.getErrInfo() );
            }
        });
    }

    @Override
    protected int contentLayout() {
        return R.layout.testvideo1;
    }

    void addlog(String msg){
        String realmsg = "[" + (new SimpleDateFormat("MM-dd HH:mm:ss.SSS").format(new Date())) + "] " + msg + "\n";
        mylog.log(realmsg);
        synchronized (realLog){
            realLog.add(0,realmsg);
        }
//        this.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                realLogView.append(msg + "\n");
//            }
//        });
    }


    public void onclick(View view) {
        if (view.getId() == R.id.mic_relayout) {
            setMicStatus();
        } else if (view.getId() == R.id.camera_relayout) {
            setCameraStatus();
        } else if (view.getId() == R.id.user_relayout) {
            showUsersList();
        } else if (view.getId() == R.id.camera_switch_image) {
            cameraSwitch();
        }
//        else if (view.getId() == R.id.output_relayout) {
//            audioOutputSwitch();
//        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.logbtn){
            showlog();
        }
        else if (v.getId() == R.id.change_btn) {
            if (memberlists.size() <= linearlayout.getChildCount()){
                return;
            }
            changePage();
        }
        else if (v.getId() == R.id.back) {
            finish();
        }
    }

    private void changePage() {
        changeView.setEnabled(false);
        HashSet<Long> removeUids = new HashSet<>();
        List<Long> subscribeUids = new LinkedList<>();
        List<Long> all = new LinkedList<>(memberlists.keySet());
        List<Long> usershow = new LinkedList<>(userSurfaces.keySet());
        all.removeAll(usershow);
        Collections.shuffle(all);
        if (all.size() >0 && all.size() <= 2) //还有2个人
        {
            long reUid = 0;
            if (all.size() == 2) {
                reUid = usershow.get(usershow.size() - 2);
                removeUids.add(reUid);
                linearlayout.removeView(userSurfaces.get(reUid));
                userSurfaces.remove(reUid);
            }
            reUid = usershow.get(usershow.size()-1);
            removeUids.add(reUid);
            linearlayout.removeView(userSurfaces.get(reUid));
            userSurfaces.remove(reUid);
            subscribeUids = all ;
        }
        else{
            removeUids = new HashSet<>(usershow); //删除这页
            subscribeUids.add(all.get(0));
            subscribeUids.add(all.get(1));
            subscribeUids.add(all.get(2));

            linearlayout.removeAllViews();
            userSurfaces.clear();
        }

        ldEngine.RTC.unsubscribeVideo(activityRoom, removeUids, new IEmptyCallback() {
            @Override
            public void onSuccess() {
                addlog("unsubscribeVideo 成功");
            }

            @Override
            public void onError(LDAnswer answer) {
                addlog("unsubscribeVideo 失败:" + answer.getErrInfo());
            }
        });

        for (Long realid : subscribeUids){
            realSubscribeVideos(realid);
        }
        changeView.setEnabled(true);
    }


    private void removeMember(long removeid) {
        memberlists.remove(removeid);

        if (userSurfaces.get(removeid) != null){
            linearlayout.removeView(userSurfaces.get(removeid));
            userSurfaces.remove(removeid);

            if (memberlists.size() > userSurfaces.size()){
                List<Long> all = new LinkedList<>(memberlists.keySet());
                List<Long> usershow = new LinkedList<>(userSurfaces.keySet());
                all.removeAll(usershow);
                Collections.shuffle(all);
                long newUid =  all.get(0);
                if (newUid >0 && !userSurfaces.containsKey(newUid))
                    realSubscribeVideos(newUid);
            }
        }
    }

    private String showName(Long userid, String nickname){
        return nickname + "(" + userid + ")";
    }

    IBasePushProcessor iBasePushProcessor = new IBasePushProcessor() {
        @Override
        public boolean reloginWillStart(long uid, int reloginCount, LDAnswer answer) {
            addlog( "reloginWillStart: 用户 " + uid + " 开始重连第 " + reloginCount + "次");
            return true;
        }

        @Override
        public void reloginCompleted(long uid, boolean successful, LDAnswer answer, int reloginCount) {
            addlog( "reloginCompleted: " + showName(myuid,mynickName) + " 重连结束 共 " + reloginCount + "次，结果 " + transRet(answer));
            if (successful) {
                TestVideoActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        alertshow.setText("");
                        alertshow.setVisibility(View.GONE);
                    }
                });
                ldEngine.RTC.enterRTCRoom(activityRoom, "", new ICallback<RTCRoomInfo>() {
                    @Override
                    public void onSuccess(RTCRoomInfo rtcRoomInfo) {
                        if (cameraOpen)
                            ldEngine.RTC.openCamera();

                        if (!userSurfaces.isEmpty()) {
                            HashMap<Long, SurfaceView> subscribeMap = new HashMap<>();
                            userSurfaces.forEach((aLong, view) -> {
                                subscribeMap.put(aLong, (SurfaceView) view.findViewById(R.id.member_surface));
                            });

                            ldEngine.RTC.subscribeVideos(activityRoom, subscribeMap, new IEmptyCallback() {
                                @Override
                                public void onSuccess() {
                                    addlog("onResult: " + "重新订阅 " + userSurfaces.keySet().toString() + " 视频流成功");
                                }

                                @Override
                                public void onError(LDAnswer answer) {
                                    addlog("onResult: " + "重新订阅 " + userSurfaces.keySet().toString() + " 失败:"+answer.getErrInfo());
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(LDAnswer answer) {

                    }
                });
            } else {
                TestVideoActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String realmsg = "[" + (new SimpleDateFormat("MM-dd HH:mm:ss").format(new Date())) + "] RTM 重连失败 " + answer.getErrInfo() +"\n";
                        mylog.log(realmsg);
                        realLog.add(0,realmsg);
                        alertshow.setText("RTM 重连失败 " + answer.getErrInfo());
                        alertshow.setVisibility(View.VISIBLE);
                    }
                });
            }
        }

        @Override
        public void rtmConnectClose(long uid) {
            addlog("链接已断开 请检查网络!");
            TestVideoActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    alertshow.setText("链接已断开 请检查网络!");
                    alertshow.setVisibility(VISIBLE);
                }
            });
        }

        @Override
        public void kickout() {
            addlog("用户在另一个设备登陆");
            TestVideoActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    alertshow.setText("用户在另一个设备登陆 链接已断开!");
                    alertshow.setVisibility(VISIBLE);
                }
            });
        }

    };
    IRTCPushProcessor irtcPushProcessor =new IRTCPushProcessor() {
        @Override
        public void pushEnterRTCRoom(final long roomId, final long userId, long time) {
            HashSet<Long> tem = new HashSet<>();
            tem.add(userId);
            Member m = new Member(userId, "");
            memberlists.put(userId, m);
            myHandler.sendEmptyMessage(4);
            Message message = Message.obtain();
            message.what = 2;
            message.obj = m;
            myHandler.sendMessage(message);
            addlog( "pushEnterRTCRoom: " + showName(userId, m.nickName) + "进入房间 " + roomId);
        }

        @Override
        public void pushExitRTCRoom(final long roomId, final long userId, long time) {
            addlog( "pushExitRTCRoom: " + showName(userId, memberlists.get(userId).nickName) + "退出房间 " + roomId);
            myHandler.sendEmptyMessage(4);
            Message message = Message.obtain();
            message.what = 3;
            message.obj = userId;
            myHandler.sendMessage(message);
        }

        @Override
        public void pushRTCRoomClosed(long roomId) {
            realLeaveRoom();
            addlog( "pushRTCRoomClosed: 房间" + roomId + "已关闭");
        }

        @Override
        public void pushKickoutRTCRoom(final long roomId) {
            realLeaveRoom();
            addlog( "pushKickoutRTCRoom: 被踢出语音房间" + roomId);
        }
    };

    void setCameraStatus(boolean status) {
        cameraOpen = status;
        if (!cameraOpen) {
            camera_image.setImageResource(R.mipmap.camera_close);
//            camera_text.setText("开摄像头");
            ldEngine.RTC.closeCamera();
            addlog( "setCameraStatus: 关闭摄像头");
        } else {
            camera_image.setImageResource(R.mipmap.camera_open);
//            camera_text.setText("关摄像头");
            ldEngine.RTC.openCamera();
            addlog( "setCameraStatus: 打开摄像头");
        }
    }

    void setMicStatus(boolean status) {
        if (!status) {
            mic_image.setImageResource(R.mipmap.mic_close);
//            mic_text.setText("解除静音");
            ldEngine.RTC.closeMic();
            addlog( "setMicStatus:关闭麦克风");
        } else {
            mic_image.setImageResource(R.mipmap.mic_open);
//            mic_text.setText("静音");
            ldEngine.RTC.openMic();
            addlog( "setMicStatus: 打开麦克风");
        }
        micStatus = status;
    }

    void realLeaveRoom() {
        if (ldEngine == null)
            return;
        chronometer.stop();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                linearlayout.removeAllViews();
                userSurfaces.clear();
            }
        });
        ldEngine.RTC.leaveRTCRoom(activityRoom, new IEmptyCallback() {
            @Override
            public void onSuccess() {
                ldEngine.closeEngine();
            }

            @Override
            public void onError(LDAnswer answer) {

            }
        });
        activityRoom = 0;
    }

    String transRet(LDAnswer answer) {
        return (answer.errorCode == 0 ? "成功" : "失败-" + answer.getErrInfo());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realLog.clear();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        realLeaveRoom();
//        linearlayout.removeAllViews();
//        userSurfaces.clear();
//
//        client.leaveRTCRoom(videoRoom.get(), new UserInterface.IRTMEmptyCallback() {
//            @Override
//            public void onResult(RTMStruct.RTMAnswer answer) {
//                if (answer.errorCode == 0)
//                    realLeaveRoom();
//            }
//        });
    }


    private void realSubscribeVideos(long inituid){
        View view = getLayoutInflater().inflate(R.layout.member_item, null);
        TextView textView = view.findViewById(R.id.member_name);
        SurfaceView surfaceView = view.findViewById(R.id.member_surface);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(itemWidth, itemHeight);
        view.setLayoutParams(lp);
        surfaceView.setLayoutParams(lp);
        MyUtils.setSurfaceViewCorner(surfaceView, 30);
        String initname = memberlists.get(inituid).nickName;
        textView.setText(showName(inituid,initname ));
        linearlayout.addView(view);

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated( SurfaceHolder holder) {
                if (userSurfaces.get(inituid) != null){
                    ldEngine.RTC.bindDecodeSurface(inituid, surfaceView);

                }else {
                    ldEngine.RTC.subscribeVideos(activityRoom, new HashMap<Long, SurfaceView>() {{
                        put(inituid, surfaceView);
                    }}, new IEmptyCallback() {
                        @Override
                        public void onSuccess() {
                            addlog("surfaceCreated: 订阅 " + showName(inituid, initname) + " 视频流成功");
                            userSurfaces.put(inituid, view);
                        }

                        @Override
                        public void onError(LDAnswer answer) {
                            addlog("surfaceCreated: 订阅 " + showName(inituid, initname) + " 视频流失败:" + answer.getErrInfo());
                            linearlayout.removeView(view);

                        }
                    });
                }
            }

            @Override
            public void surfaceChanged( SurfaceHolder holder, int format, int width, int height) {
                addlog("surfaceChanged ");
            }

            @Override
            public void surfaceDestroyed( SurfaceHolder holder) {
                addlog("surfaceDestroyed ");
            }
        });
    }

    private void addMember(Member member){
        synchronized (userSurfaces) {
            if (userSurfaces.size() >= 3)
                return;
        }
        realSubscribeVideos(member.uid);
    }


    private void initMember(RTCRoomInfo info) {
        roomshow.setText("房间-" + activityRoom);
        chronometer.start();
        setCameraStatus();
        setMicStatus();
        List<Long> uids = info.uids;
        if (uids != null) {
            for (Long iniuid : uids) {
                if (iniuid == myuid)
                    continue;
                memberlists.put(iniuid, new Member(iniuid, ""));
            }
            initview();
        }
    }

    private void initview() {
        for (Long inituid: memberlists.keySet()){
            synchronized (linearlayout) {
                if (linearlayout.getChildCount() >= 3)
                    return;
            }
            realSubscribeVideos(inituid);
        }
    }

    private static class MyHandler extends Handler {
        private final WeakReference<Activity> weakReference;

        public MyHandler(Activity activity) {
            this.weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage( Message msg) {
            TestVideoActivity activity = (TestVideoActivity) weakReference.get();
            if (msg.what == 1) {
                activity.initMember((RTCRoomInfo) msg.obj);
            }
            if (msg.what == 2) {
                activity.addMember((Member) msg.obj);
            }
            if (msg.what == 3) {
                activity.removeMember((Long) msg.obj);
            }
            if (msg.what == 4) {
                activity.refreshMemberList();
            }
        }
    }

    private void setMicStatus() {
        setMicStatus(!micStatus);
    }

    private void setCameraStatus() {
        setCameraStatus(!cameraOpen);
    }

    private void cameraSwitch() {
        if (!cameraOpen)
            return;
        useFront = !useFront;
        ldEngine.RTC.switchCamera(useFront);
    }

    MemberAdapter memberAdapter;


    private void showlog(){
        Dialog dialog = new Dialog(this, R.style.ActionSheetDialogStyle);
        //填充对话框的布局
        View inflate = LayoutInflater.from(this).inflate(R.layout.logview, null);
//        realLogView.append();

        TextView logview = inflate.findViewById(R.id.sholog);
        logview.setMovementMethod(ScrollingMovementMethod.getInstance());
        logview.setVerticalScrollBarEnabled(true);
        logview.append(realLog.toString());
        int offset=logview.getLineCount()*logview.getLineHeight();
        mylog.log("offset " + offset + " logview.getHeight() " + logview.getHeight());

        if(offset>logview.getHeight()){
            logview.scrollTo(0,offset-logview.getLineHeight());
        }

//        logview.append("呵呵");
        //将布局设置给Dialog
        dialog.setContentView(inflate);
        //获取当前Activity所在的窗体
        Window dialogWindow = dialog.getWindow();
        //设置Dialog从窗体底部弹出
        dialogWindow.setGravity(Gravity.TOP);
        //获得窗体的属性
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = DisplayUtils.getScreenHeight(this)*3 / 4;
        dialogWindow.setAttributes(lp);
        dialog.show();//显示对话框
    }

    private void showUsersList() {
        Dialog dialog = new Dialog(this, R.style.ActionSheetDialogStyle);
        //填充对话框的布局
        View inflate = LayoutInflater.from(this).inflate(R.layout.memberlist_layout, null);
        //初始化控件
        RecyclerView recyclerView = inflate.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        memberAdapter = new MemberAdapter(this, new LinkedList<>(memberlists.values()));
        recyclerView.setAdapter(memberAdapter);
        memberAdapter.setOnClickListener((position, item) -> {

        });
        //将布局设置给Dialog
        dialog.setContentView(inflate);
        //获取当前Activity所在的窗体
        Window dialogWindow = dialog.getWindow();
        //设置Dialog从窗体底部弹出
        dialogWindow.setGravity(Gravity.BOTTOM);
        //获得窗体的属性
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = DisplayUtils.getScreenHeight(this) / 2;
        dialogWindow.setAttributes(lp);
        dialog.show();//显示对话框
    }

    private void refreshMemberList() {
        Optional.ofNullable(memberAdapter).ifPresent(memberAdapter -> {
            memberAdapter.notifyDataSetChanged();
        });
    }
}