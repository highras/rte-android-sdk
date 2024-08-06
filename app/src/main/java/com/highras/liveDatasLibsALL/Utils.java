package  com.highras.liveDatasLibsALL;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.LiveDataRTE.IBasePushProcessor;
import com.LiveDataRTE.LDEngine;
import com.LiveDataRTE.LDInterface.*;
import com.LiveDataRTE.LiveDataConfig;
import com.LiveDataRTE.LiveDataStruct.*;
import com.LiveDataRTE.RTCLib.RTCStruct.*;
import com.LiveDataRTE.RTMLib.RTMErrorCode;
import com.fpnn.sdk.ErrorRecorder;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public enum Utils {
    INSTANCE;
    public long currentUserid = 1234;
    public long currentRoomid;
    public String  currentLan = "";
    public String  nickName = "";

    static class CItem1 {
        public String ID = "";
        public String Value = "";

        public boolean equals(Object obj) {
            if (obj instanceof  CItem1) {
                if (this.ID.equals(((CItem1) obj).ID) && this.Value.equals(((CItem1) obj).Value)) {
                    return true;
                }
                else {
                    return false;
                }
            }
            return false;
        }

        public CItem1(String _ID, String _Value) {
            ID = _ID;
            Value = _Value;
        }

        @Override
        public String toString() {
            // TODO Auto-generated method stub
            return ID;
        }

        public String getID() {
            return ID;
        }

        public String getValue() {
            return Value;
        }
    }

    public String address = "";
    public final int rtmPort = 13321;

    public String rtmEndpoint;

    final HashMap<String, ProjectInfo> testAddress = new HashMap(){{
        put("nx",new ProjectInfo(80000071,"rtm-nx-front.ilivedata.com","cXdlcnR5"));
        }
    };

    public  void toast(final Activity activity, final String str){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(activity, str, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER,0,0);
                LinearLayout linearLayout = (LinearLayout) toast.getView();
                if (linearLayout!=null) {
                    TextView messageTextView = (TextView) linearLayout.getChildAt(0);
                    messageTextView.setTextSize(16);//设置toast字体大小
                }
                toast.show();
            }
        });
    }


    public String getJson(String fileName, Context context) {
        //将json数据变成字符串
        StringBuilder stringBuilder = new StringBuilder();
        try {
            //获取assets资源管理器
            AssetManager assetManager = context.getAssets();
            //通过管理器打开文件并读取
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public  static void alertDialog(final Activity activity, final String str){
        alertDialog(activity, str, false);
    }


    public  byte[] toByteArray(InputStream input) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n = 0;
        while (true) {
            try {
                if (!(-1 != (n = input.read(buffer)))) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }

    public  static void alertDialog(final Activity activity, final String str, boolean finish){
        if (activity == null || activity.isDestroyed() || activity.isFinishing()) {
            return;
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage(str).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (finish)
                            activity.finish();
                    }
                });
                if (activity == null || activity.isDestroyed() || activity.isFinishing()) {
                    return;
                }
                builder.show();
            }
        });
    }



    public ErrorRecorder errorRecorder = new TestErrorRecorder();
    public LDEngine ldEngine;


    private static String getType(Object a) {
        return a.getClass().toString();
    }

    public void login(Activity activity, IEmptyCallback callback) {
        ProjectInfo info = testAddress.get(address);
        rtmEndpoint = info.host +  ":" + rtmPort;
        if (ldEngine != null && ldEngine.getUid()!=currentUserid){
            ldEngine.closeEngine();
        }

        if (activity instanceof Activity){
//            mylog.log("对的");
        }
        String pid = "11000001";
        String uid = "1234";
        LiveDataConfig liveDataConfig = new LiveDataConfig();
        liveDataConfig.keepRTCBackGround = true;
            ldEngine = LDEngine.CreateEngine(rtmEndpoint, info.pid, currentUserid, new IBasePushProcessor() {}, activity, liveDataConfig);
//            ldEngine = LDEngine.CreateEngine(rtmEndpoint, info.pid, currentUserid, new IBasePushProcessor() {}, null, liveDataConfig);


        new Thread(new Runnable() {
            @Override
            public void run() {
                long ts = System.currentTimeMillis()/1000;
                String realToken = ApiSecurityExample.genHMACToken(info.pid, currentUserid, ts, info.key);
                ldEngine.login(realToken, "zh", null, ts, callback);
            }
        }).start();
    }

    public  void doPost(String url, JSONObject jsonObject, IHttpCallback httpCallback) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Charset", "UTF-8");
            // 设置文件类型:
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            // 设置接收类型否则返回415错误
            conn.setRequestProperty("accept", "application/json");
            conn.setRequestMethod("POST");
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(5000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            OutputStream out = conn.getOutputStream();
            out.write(jsonObject.toString().getBytes());
            out.flush();
            if (conn.getResponseCode() == 200) {
                // 获取响应的输入流对象
                InputStream is = conn.getInputStream();
                // 创建字节输出流对象
                ByteArrayOutputStream message = new ByteArrayOutputStream();
                // 定义读取的长度
                int len = 0;
                // 定义缓冲区
                byte buffer[] = new byte[1024];
                // 按照缓冲区的大小，循环读取
                while ((len = is.read(buffer)) != -1) {
                    // 根据读取的长度写入到os对象中
                    message.write(buffer, 0, len);
                }
                // 释放资源
                is.close();
                message.close();
                // 返回字符串
                String msg = new String(message.toByteArray());
                httpCallback.onSuccess(msg);
            } else {
                httpCallback.onError(conn.getResponseCode(), conn.getResponseMessage());
            }
        } catch (Exception e) {
            httpCallback.onError(110, e.getMessage());
        }
    }


    String getToken(long uid) {
        int port = 0;
        String token = "";
        if (address.equals("test"))
            port = 8099;
        else if (address.equals("nx"))
            port = 8090;
        else if (address.equals("intl"))
            port = 8098;

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String tourl = "http://161.189.171.91:" + port + "?uid=" + uid;

        try {
            URL url = new URL(tourl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setDoInput(true); // 同意输入流，即同意下载
            httpURLConnection.setUseCaches(false); // 不使用缓冲
            httpURLConnection.setRequestMethod("GET"); // 使用get请求
            httpURLConnection.setConnectTimeout(20 * 1000);
            httpURLConnection.setReadTimeout(20 * 1000);
            httpURLConnection.connect();

            int code = httpURLConnection.getResponseCode();

            if (code == 200) { // 正常响应
                InputStream inputStream = httpURLConnection.getInputStream();

                byte[] buffer = new byte[4096];
                int n = 0;
                while (-1 != (n = inputStream.read(buffer))) {
                    output.write(buffer, 0, n);
                }

                inputStream.close();
            }
            else {
                errorRecorder.recordError("http return error " + code);
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorRecorder.recordError("gettoken error :" + e.getMessage());
        }
        return output.toString();
    }

    public static Boolean isEmpty(String data) {
        if (data == null || data.length() == 0)
            return true;
        else return false;
    }

    //泛型接口 带有一个返回值的回调函数 (请优先判断answer的错误码 泛型值有可能为null)
    public  interface MyCallback<T> {
        void onsucess(T t);
        void onError(LDAnswer answer);
    }


    public void realEnterRoom(final long roomId, RTCRoomType roomtype, Activity activity, MyCallback<RTCRoomInfo> callback) {
        ldEngine.RTC.enterRTCRoom(roomId, currentLan, new ICallback<RTCRoomInfo>() {
            @Override
            public void onSuccess(RTCRoomInfo roomInfo) {
                ldEngine.RTC.getRTCRoomMembers(roomId, new ICallback<RTCRoomInfo>() {
                    @Override
                    public void onSuccess(RTCRoomInfo roomInfo) {
                        callback.onsucess(roomInfo);
                    }

                    @Override
                    public void onError(LDAnswer answer) {
                        callback.onError(answer);
                    }
                });
            }

            @Override
            public void onError(LDAnswer answer) {
                if (answer.errorCode == RTMErrorCode.RTM_EC_VOICE_ROOM_NOT_EXIST.value()){
                    ldEngine.RTC.createRTCRoom(roomId, roomtype, currentLan, new IEmptyCallback() {
                        @Override
                        public void onSuccess() {
                            RTCRoomInfo roomInfo = new RTCRoomInfo();
                            roomInfo.uids = new ArrayList<>();
                            roomInfo.uids.add(currentUserid);
                            callback.onsucess(roomInfo);
;                        }

                        @Override
                        public void onError(LDAnswer answer) {
                            alertDialog(activity,"创建RTC房间-" + roomId + "失败：" + answer.getErrInfo(), true);
                        }
                    });
                }
                else{
                    alertDialog(activity,"进入RTC房间-" + roomId + "失败：" + answer.getErrInfo(), true);
                }
            }
        });
    }
}
