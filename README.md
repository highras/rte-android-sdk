### LiveDataEngine-sdk 使用文档
- [依赖集成](#依赖集成)
- [使用说明](#使用说明)
- [使用示例](#使用示例)
- [接口说明](#接口说明)


### 依赖集成
- 最低支持Android版本为5.0
- RTM功能 LiveDataRTE-sdk.aar
- 带有RTC实时音频功能 LiveDataRTE-rtc-sdk.aar

### 使用说明
- 需要的权限
  ~~~
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.INTERNET"/>
  
  RTC需要的权限：
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
  ~~~
  
  
- 说明:
    - LDEngine里分为RTM,IM,RTC,ValueAdd,VoicRoomClient对象
    - RTM为基础的信令传输 
    - RTC为实时音视频功能
    - ValueAdd为增值服务(翻译,审核)
    - VoicRoomClient为语聊房场景
  - 服务器push消息:请实现自己需要的push系列函数(所有push函数在子线程执行，如需更新ui请自行切回主线程)
  - 各项服务配置和增值服务可以在后台配置，请登陆管理后台预览详细的配置参数
  - room和group的区别 group在服务端会持久化 room是非持久化(用户下线或者RTM链接断开会自动离开room)
  - room默认不支持多房间（当用户进入第二个房间会自动退出第一个房间） 用户可以在控制台开启支持多房间配置
  - sdk支持自动重连 不需要手动管理链接
  - 关于登录
     ~~~
    (登录验证token分为客户端生成和服务端拉取两种方式) 服务端拉取需要接入服务端server-sdk去我们的后端拉取
    客户端生成token需要在管理控制台自己填入base64的公钥 一般选择HMAC生成方式即可 客户端生成的实例代码见ApiSecurityExample.java中的genHMACToken
     ~~~

  RTC说明:
  - 开启RTC功能需要先登陆成功
  - 可以进入多个实时语音房间 但必须只有一个当前活跃的房间(必须调用setActivityRoom设置当前活跃房间才能正常接收和发送语音)
  - 视频房间和实时翻译语音房间只能进入一个
  - 需要订阅才能正常接收对方视频流
  - 链接断开，进入的实时音视频房间会自动退出，需要在重连完成后再次进入房间 订阅的视频流需要重新订阅
  - 如果需要后台保持功能 请在初始化LDEngine 传入LiveDataConfig 将keepRTCBackGround置位true
- 如需代码混淆 请在proguard-rules.pro 中添加
    ~~~
    -keep class com.LiveDataRTE.**{*;}
    -keep class org.msgpack.core.**{*;}
    如使用加密功能请添加
    -keep class org.spongycastle.**{*;}
    ~~~

- 用户可以重写日志类 收集和获取sdk内部的错误信息(强烈建议重载日志类) 例如
    ~~~
     public class TestErrorRecorder extends ErrorRecorder {
        public TestErrorRecorder(){
            super.setErrorRecorder(this);
        }
    
        public void recordError(Exception e) {
            Log.i("log","Exception:" + e);
        }
    
        public void recordError(String message) {
            Log.i("log","Error:" + message);
        }
    
        public void recordError(String message, Exception e) {
            Log.i("log",String.format("Error: %s, exception: %s", message, e));
        }
    }
    ~~~

### 使用示例
 ~~~
    ldEngine = LDEngine.CreateEngine(rtmEndpoint, pid, userid, new IBasePushProcessor() {}, activity);
      
      IRTMPushProcessor irtmPushProcessor = new IRTMPushProcessor(){
          ....//重写自己需要处理的业务接口
      }
      ldEngine.setRTMPushProcessor(irtmPushProcessor);
    
    设置日志收集类
    ldEngine.setErrorRecoder(new TestErrorRecorder())
    
    登录：
      ldEngine.login(String token)
          
      RTC功能：
      ldEngine.RTC.setRTCPushProcessor(new IRTCPushProcessor())
      ldEngine.RTC.enterRTCRoom(roomId);
      ldEngine.RTC.openMic();
      
      增值服务：
      ldEngine.ValueAdded.translate...
~~~

##  接口说明
### [RTM接口]
- [服务端push接口](doc-zh/RTM/RTMPush.md)
- [发送聊天以及消息类接口](doc-zh/RTM/RTMChat.md)
- [房间/群组/好友接口](doc-zh/RTM/RTMRelationship.md)
- [用户系统命令接口](doc-zh/RTM/RTMUser.md)

#### [RTC语聊房接口](doc-zh/RTCVoiceRoom-zh.md)
#### [RTC接口](doc-zh/RTC-zh.md)
#### [录音离线消息接口](doc-zh/LDRecordAudio.md)
#### [RTM错误码](doc-zh/ErrorCode.md)
