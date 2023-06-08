package com.LiveDataRTE;

import android.util.Log;

import com.fpnn.sdk.ErrorRecorder;
import com.fpnn.sdk.proto.Message;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class LDUtils {
    static ErrorRecorder errorRecorder = new ErrorRecorder();
//    private AtomicLong orderId = new AtomicLong();
//
//    public long genMid() {
//        long id = getCurrentMilliseconds()<<16;
//        return id + orderId.incrementAndGet();
//    }
//
//    long getCurrentSeconds() {
//        return System.currentTimeMillis() / 1000;
//    }
//
    long getCurrentMilliseconds() {
        return System.currentTimeMillis();
    }

    static public byte[] fileToByteArray(File file) {
        byte[] data;
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            int len;
            byte[] buffer = new byte[1024];
            while ((len = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            data = baos.toByteArray();
            fis.close();
            baos.close();
        } catch (Exception e) {
            Log.e("rtmsdk","fileToByteArray error " + e.getMessage());
            return null;
        }
        return data;
    }

    static public Map<String, String>  wantStringMap(Message message,String key) {
        Map<String, String> map = new HashMap<>();
        if (message  == null)
            return map;
        try {
                Map<String, String> ret = (Map<String, String>) message.want(key);
                if (ret == null)
                    return map;
                for (String value : ret.keySet())
                    map.put(value, ret.get(value));
            return map;
        }
        catch (Exception e)
        {
            errorRecorder.recordError("wantStringMap  key " + key + " error:" + e.getMessage());
        }
        return map;
    }


    static public List<Long>  wantLongList(Object values) {
        List<Long> list = new ArrayList<>();
        try {
            if (values == null)
                return list;
            List<Object> attrsList = (List<Object>) values;
            for (Object value : attrsList) {
                if (value instanceof Integer)
                    list.add(((Integer) value).longValue());
                else if (value instanceof Long)
                    list.add(((Long) value).longValue());
                else if (value instanceof BigInteger)
                    list.add(((BigInteger) value).longValue());
                else
                    list.add(Long.valueOf(String.valueOf(value)));
            }
        }
        catch (Exception e)
        {
            errorRecorder.recordError("wantLongList  values   error:" + e.getMessage());
        }
        return list;
    }



    static public List<Long> wantLongList(Message message,String key) {
        ArrayList<Long> jj = new ArrayList<>();
        try {
            if (message == null)
                return jj;
            List<Object> attrsList = (List<Object>) message.want(key);
            for (Object value : attrsList) {
                if (value instanceof Integer)
                    jj.add(((Integer) value).longValue());
                else if (value instanceof Long)
                    jj.add(((Long) value).longValue());
                else if (value instanceof BigInteger)
                    jj.add(((BigInteger) value).longValue());
                else
                    jj.add(Long.valueOf(String.valueOf(value)));
            }
        }
        catch (Exception e)
        {
            errorRecorder.recordError("wantLongList  key " + key + " error:" + e.getMessage());
        }
        return jj;
    }

    static public List<Integer>  wantIntList(Message message,String key) {
        List<Integer> list = new ArrayList<>();
        try {
            if (message == null)
                return list;
            List<Object> attrsList = (List<Object>) message.want(key);
            if (attrsList == null)
                return list;
            for (Object value : attrsList) {
                if (value instanceof Integer)
                    list.add(((Integer) value).intValue());
                else if (value instanceof Long)
                    list.add(((Long) value).intValue());
                else if (value instanceof BigInteger)
                    list.add(((BigInteger) value).intValue());
                else
                    list.add(Integer.valueOf(String.valueOf(value)));
            }
        }
        catch (Exception e) {
            errorRecorder.recordError("wantIntList  key " + key + " error:" + e.getMessage());
        }
        return list;
    }

    static void getIntList(Message message,String key, List<Integer> list) {
        if (message == null)
            return;
        try {

            List<Object> attrsList = (List<Object>) message.get(key);
            if (attrsList == null)
                return;
            for (Object value : attrsList) {
                if (value instanceof Integer)
                    list.add(((Integer) value).intValue());
                else if (value instanceof Long)
                    list.add(((Long) value).intValue());
                else if (value instanceof BigInteger)
                    list.add(((BigInteger) value).intValue());
                else
                    list.add(Integer.valueOf(String.valueOf(value)));
            }
        }catch (Exception e){
            errorRecorder.recordError("getIntList  key " + key + " error:" + e.getMessage());
        }
    }

    static public List<String>  getStringList(Message message,String key) {
        List<String> list = new ArrayList<>();
        if (message == null)
            return list;
        try {
            List<Object> attrsList = (List<Object>) message.get(key);
            if (attrsList == null)
                return list;
            for (Object value : attrsList) {
                list.add(String.valueOf(value));
            }
        }catch (Exception e){
            errorRecorder.recordError("getStringList  key " + key + " error:" + e.getMessage());
        }
        return list;
    }

    static public List<Map<String, String>> wantListHashMap(Message message, String key) {
        List<Map<String, String>> attributes = new ArrayList<>();
        try {
            List<Object> attrsList = (List<Object>) message.want(key);
            if (attrsList  == null)
                return attributes;
            for (Object value : attrsList)
                attributes.add(new HashMap<>((Map<String, String>) value));
        }
        catch (Exception e)
        {
            errorRecorder.recordError("wantListHashMap  key " + key + " error:" + e.getMessage());
        }
        return attributes;
    }

    static HashMap<Long, HashSet<Integer>> wantDeviceOption(Message message, String key) {
        HashMap<Long, HashSet<Integer>> options = new HashMap<>();
        try {
            Map<Object, ArrayList<Integer>> tmpOption = (Map<Object, ArrayList<Integer>>) message.want(key);
            if (tmpOption == null)
                return options;
            for (Object xid: tmpOption.keySet()){
                HashSet<Integer> messageTypes = new HashSet<>();
                for (Integer type: tmpOption.get(xid))
                    messageTypes.add(type);
                options.put(wantLong(xid), messageTypes);
            }
        }
        catch (Exception e)
        {
            errorRecorder.recordError("wantDeviceOption  key " + key + " error:" + e.getMessage());
        }
        return options;
    }


    static public int BooleanToInt(boolean t){
        return t?1:0;
    }

    static public boolean wantBoolean(Message quest,String key) {
//        int kk = wantInt(quest,key);
//        boolean myBoolean = kk != 0;
//        return myBoolean;

        Object obj = null;
        try {
            obj = quest.want(key);
        }
        catch (NoSuchElementException e)
        {
            errorRecorder.recordError("wantBoolean NoSuchElementException " + key);
        }
        return (obj == null)? false :(Boolean)obj;
    }

    static public String wantString(Message quest, String key) {
        Object obj = null;
        try {
            obj = quest.want(key);
        }
        catch (NoSuchElementException e)
        {
            errorRecorder.recordError("wantString NoSuchElementException " + key);
            return null;
        }
        return String.valueOf(obj);
    }

    static public String getString(Message quest, String key) {
            return quest.getString(key);
    }

    static public HashSet<Long> wantLongHashSet(Message message, String key) {
        HashSet<Long> uids = new HashSet<Long>();
        try{
            List<Object> list = (List<Object>)message.want(key);
            if (list == null)
                return uids;
            for (Object value : list) {
                if (value instanceof Integer)
                    uids.add(((Integer) value).longValue());
                else if (value instanceof Long)
                    uids.add(((Long) value).longValue());
                else if (value instanceof BigInteger)
                    uids.add(((BigInteger) value).longValue());
                else
                    uids.add(Long.valueOf(String.valueOf(value)));
            }
        }
        catch (Exception e)
        {
            errorRecorder.recordError("wantLongHashSet  key " + key + " error:" + e.getMessage());
        }
        return uids;
    }

    static public HashSet<Long> longHashSet(Object values) {
        HashSet<Long> uids = new HashSet<Long>();
        try{
            List<Object> list = (List<Object>)values;
            if (list == null)
                return uids;
            for (Object value : list) {
                if (value instanceof Integer)
                    uids.add(((Integer) value).longValue());
                else if (value instanceof Long)
                    uids.add(((Long) value).longValue());
                else if (value instanceof BigInteger)
                    uids.add(((BigInteger) value).longValue());
                else
                    uids.add(Long.valueOf(String.valueOf(value)));
            }
        }
        catch (Exception e)
        {
            errorRecorder.recordError("longHashSet  error");
        }
        return uids;
    }
    static public long getLong(Message quest,String key) {
        Object o = quest.get(key);
        if (o != null)
            return wantLong(key);
        return 0;
    }

    static public long wantLong(Message quest,String key) {
        long value = -1;
        try {
            Object obj = quest.want(key);
            value = wantLong(obj);
        }
        catch (NoSuchElementException e)
        {
//            Log.e("rtmsdk","wantLong NoSuchElementException " + key);
            errorRecorder.recordError("wantLong NoSuchElementException " + key);
        }
        return value;
    }

    static public long wantLong(Object obj) {
        long value = -1;
        if (obj instanceof Integer)
            value = ((Integer) obj).longValue();
        else if (obj instanceof Long)
            value = (Long) obj;
        else if (obj instanceof BigInteger)
            value = ((BigInteger) obj).longValue();
        else if (obj instanceof Short)
            value = ((Short) obj).longValue();
        else if (obj instanceof Byte)
            value = ((Byte) obj).longValue();
        else
            value = Long.valueOf(String.valueOf(obj));
        return value;
    }

    static public int wantInt(Message quest,String key) {
        int value = -1;
        try {
            Object obj = quest.want(key);
            value = wantInt(obj);
        }
        catch (NoSuchElementException e)
        {
            errorRecorder.recordError("wantInt NoSuchElementException " + key);
        }
        return value;
    }

    static public int wantInt(Object obj) {
        int value = -1;
        if (obj == null){
            return  value;
        }
        if (obj instanceof Integer)
            value = (Integer) obj;
        else if (obj instanceof Long)
            value = ((Long) obj).intValue();
        else if (obj instanceof BigInteger)
            value = ((BigInteger) obj).intValue();
        else if (obj instanceof Short)
            value = ((Short) obj).intValue();
        else if (obj instanceof Byte)
            value = ((Byte) obj).intValue();
        else
            value = Integer.valueOf(String.valueOf(obj));
        return value;
    }

    static public String bytesToHexString(byte[] bytes, boolean isLowerCase) {
        String from = isLowerCase ? "%02x" : "%02X";
        StringBuilder sb = new StringBuilder(bytes.length * 2);

        Formatter formatter = new Formatter(sb);
        for (byte b : bytes) {
            formatter.format(from, b);
        }
        return sb.toString();
    }

}