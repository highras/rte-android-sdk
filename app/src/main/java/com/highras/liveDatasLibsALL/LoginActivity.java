package com.highras.liveDatasLibsALL;

import android.content.SharedPreferences;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.Random;

import com.LiveDataRTE.DecodeMusicFile;
import com.LiveDataRTE.LiveDataStruct.*;
import com.LiveDataRTE.LDInterface.*;

import lib.demo.spinner.MaterialSpinner;


public class LoginActivity extends BaseActivity {
    private static final String TAG = "rtcsdk";
    MaterialSpinner niceSpinner;
    Utils utils;

    private <T extends View> T $(int resId) {
        return (T) super.findViewById(resId);
    }

    MyRTMPushProcessor myRTMPushProcessor = new MyRTMPushProcessor();

    EditText userID_edit;
    EditText roomID_edit;

    @Override
    protected int contentLayout() {
        return R.layout.activity_login;
    }

    @Override
    protected void setToolbar() {
        super.setToolbar();
        customToolbarAndStatusBarBackgroundColor(false);
    }

    boolean checkNess(){
        Object sroomid = roomID_edit.getText();
        if (sroomid == null) {
            utils.alertDialog(this, "请输入房间号");
            return false;
        }

        Object suserid = userID_edit.getText();
        if (suserid == null) {
            utils.alertDialog(this, "请输入用户id");
            return false;
        }
        if (utils.isEmpty( sroomid.toString())) {
            try {
                utils.alertDialog(this, Constants.languageObj.getString("roomidHint"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }
        if (utils.isEmpty(userID_edit.getText().toString())) {
            try {
                utils.alertDialog(this, Constants.languageObj.getString("useridHint"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }
        long roomid = 0;

        try {
            roomid = Long.parseLong(String.valueOf(sroomid));
        } catch (NumberFormatException ex) {
            utils.alertDialog(this,"请输入正确的房间号");
            return false;
        }
        utils.currentRoomid = roomid;

        long userid = 0;
        try {
            userid = Long.parseLong(String.valueOf(suserid));
        } catch (NumberFormatException ex) {
            utils.alertDialog(this,"请输入正确的用户id");
            return false;
        }
        if (roomid == 0 || userid ==0) {
            utils.alertDialog(this, "请输入正确的用户id或者房间号");
            return false;
        }
        utils.currentRoomid = roomid;
        utils.currentUserid = userid;
        utils.currentLan = Constants.LANGUAGE_VALUE.get(niceSpinner.getSelectedIndex());
        return true;
    }

    private void saveUserData(long uid, String name) {
        SharedPreferences sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();//获取编辑器
        editor.putString(String.valueOf(uid), name);
        editor.commit();
    }

    @Override
    protected void afterViews() {
        super.afterViews();
        utils = Utils.INSTANCE;


        niceSpinner = $(R.id.spinner);
        userID_edit = $(R.id.userID_edit);
        roomID_edit = $(R.id.roomID_edit);
        RelativeLayout startTransVoice = $(R.id.startTransVoice);
        RelativeLayout startVoice = $(R.id.startVoice);
        RelativeLayout startVideo = $(R.id.startVideo);

        LinkedList<String> data = new LinkedList<>(Constants.LANGUAGE);
        niceSpinner.setItems(data);
        niceSpinner.setBackgroundResource(R.drawable.shape_nicespinner);
        niceSpinner.setOnItemSelectedListener((view, position, id, item) -> setLanguage());
        userID_edit.setText(String.valueOf(getRandom()));
        startTransVoice.setOnClickListener(view -> {
            if (!checkNess()){
                return;
            }
            utils.login(LoginActivity.this, new IEmptyCallback() {
                @Override
                public void onSuccess() {
                    Intent intent = new Intent(LoginActivity.this, transVoice.class);
                    startActivity(intent);
                }

                @Override
                public void onError(LDAnswer answer) {
                    utils.alertDialog(LoginActivity.this,"登录失败 " + answer.getErrInfo());

                }
            });
            saveUserData(utils.currentUserid, utils.nickName);
        });

        startVoice.setOnClickListener(view -> {
            if (!checkNess()){
                return;
            }
            utils.login(this, new IEmptyCallback() {
                @Override
                public void onSuccess() {
                    Intent intent = new Intent(LoginActivity.this, TestVoiceActivity.class);
                    saveUserData(utils.currentUserid, utils.nickName);
                    startActivity(intent);
                }

                @Override
                public void onError(LDAnswer answer) {
                    Utils.alertDialog(LoginActivity.this,"登录失败 " + answer.getErrInfo());

                }
            });
        });

        startVideo.setOnClickListener(view -> {
            if (!checkNess()){
                return;
            }
            utils.login(this, new IEmptyCallback() {
                @Override
                public void onSuccess() {
                    Intent intent = new Intent(LoginActivity.this, TestVideoActivity.class);
                    saveUserData(utils.currentUserid, utils.nickName);
                    startActivity(intent);
                }

                @Override
                public void onError(LDAnswer answer) {
                    Utils.alertDialog(LoginActivity.this,"登录失败 " + answer.getErrInfo());

                }
            });
        });

        $(R.id.p2ptest).setOnClickListener(view -> {
            if (!checkNess()){
                return;
            }
            utils.login(this, new IEmptyCallback() {
                @Override
                public void onSuccess() {
                    Intent intent = new Intent(LoginActivity.this, testp2p.class);
                    saveUserData(utils.currentUserid, utils.nickName);
                    startActivity(intent);
                }

                @Override
                public void onError(LDAnswer answer) {
                    Utils.alertDialog(LoginActivity.this,"登录失败 " + answer.getErrInfo());

                }
            });
        });

        $(R.id.voiceroom).setOnClickListener(view -> {
//            DecodeMusicFile.decodeMusicFile("/sdcard/Download/lixiang.mp3");
            if (!checkNess()){
                return;
            }
            utils.login(this, new IEmptyCallback() {
                @Override
                public void onSuccess() {
                    Intent intent = new Intent(LoginActivity.this, VoiceroomtestActivity.class);
                    saveUserData(utils.currentUserid, utils.nickName);
                    startActivity(intent);
                }

                @Override
                public void onError(LDAnswer answer) {
                    Utils.alertDialog(LoginActivity.this,"登录失败 " + answer.getErrInfo());

                }
            });
        });

        setLanguage();
    }

    private void setLanguage() {
        String fileName = Constants.LANGUAGE_SHOW_MAP.get(Constants.LANGUAGE_VALUE.get(niceSpinner.getSelectedIndex()));
        String content = utils.getJson(fileName, this);
        try {
            Constants.languageObj = new JSONObject(content);
            refreshUI();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void refreshUI() {
        try {
            TextView userIdTv = findViewById(R.id.userId_tv);
            TextView roomIdTv = findViewById(R.id.roomId_tv);
            TextView languageTv = findViewById(R.id.languagetv);
            TextView nickTV = findViewById(R.id.nickName_tv);
            TextView startAudioText = findViewById(R.id.startVoiceText);
            userIdTv.setText(Constants.languageObj.getString("userid"));
            roomIdTv.setText(Constants.languageObj.getString("roomid"));
            languageTv.setText(Constants.languageObj.getString("language"));
            startAudioText.setText(Constants.languageObj.getString("startVoice"));
            userID_edit.setHint(Constants.languageObj.getString("useridHint"));
            roomID_edit.setHint(Constants.languageObj.getString("roomidHint"));

        } catch (Exception e) {
            Log.d(TAG, "refreshUI: " + e.getMessage());
        }
    }


    private int getRandom() {
        int max = 10000;
        int min = 1000;
        Random random = new Random();
        return random.nextInt(max) % (max - min + 1) + min;
    }
}