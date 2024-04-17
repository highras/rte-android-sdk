package com.highras.liveDatasLibsALL;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import com.livedata.rtc.RTCEngine;

import org.angmarch.views.NiceSpinner;
import org.json.JSONObject;

import java.util.LinkedList;

public class LiveDataTest extends AppCompatActivity  implements View.OnClickListener {
    NiceSpinner checkbutton;
    int REQUEST_CODE_CONTACT = 101;
    final LinkedList<Utils.CItem1> testtypevalue = new LinkedList<Utils.CItem1>(){{
        add(new Utils.CItem1("宁夏测试","nx"));
    }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livedatatest);
        findViewById(R.id.rtctest).setOnClickListener(this);
        findViewById(R.id.rtmtest).setOnClickListener(this);
        findViewById(R.id.imtest).setOnClickListener(this);
        checkbutton = findViewById(R.id.selecttype);
        checkbutton.attachDataSource(testtypevalue);
        checkbutton.setBackgroundResource(R.drawable.shape_nicespinner);
        checkbutton.setSelectedIndex(0);

        String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.CAMERA};
        //验证是否许可权限
        for (String str : permissions) {
            if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                //申请权限
                this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
            }
        }
    }

    void setAddress(){
        Utils.CItem1 c1 = (Utils.CItem1)(checkbutton.getSelectedItem());
        Utils.INSTANCE.address = c1.Value;
    }
    @Override
    public void onClick(View v) {
        Class obj = null;
        if (v.getId() == R.id.imtest) {

            setAddress();
            obj = livedata_imtest.class;
        }
        else if (v.getId() == R.id.rtmtest){
            setAddress();
            obj = livedata_rtmtest.class;
        }
        else if (v.getId() == R.id.rtctest){
            setAddress();
            obj = LoginActivity.class;
        }
        Intent intent = new Intent(LiveDataTest.this, obj);
        startActivity(intent);
    }
}