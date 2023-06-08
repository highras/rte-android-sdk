package com.LiveDataRTE.InternalEngine;

import com.LiveDataRTE.LiveDataStruct.ConversationType;

import java.util.HashSet;
import java.util.Set;

class DuplicatedMessageFilter {

    private static class MessageIdUnit {
        public ConversationType messageType;
        public long bizId;
        public long uid;
        public long mid;

        public MessageIdUnit(ConversationType type, long _bizId, long _uid, long _mid) {
            uid = _uid;
            mid = _mid;
            bizId = _bizId;
            messageType = type;
        }

        public int hashCode() {
            int result = 17;
            result = 31 * result + messageType.ordinal();
            result = 31 * result + (int) bizId;
            result = 31 * result + (int) uid;
            result = 31 * result + (int) mid;
            return result;
        }

        public boolean equals(Object o) {
            if (this == o)
                return true;

            if (!(o instanceof MessageIdUnit))
                return false;

            MessageIdUnit pn = (MessageIdUnit) o;
            return pn.messageType == messageType && pn.uid == uid && pn.mid == mid && pn.bizId == bizId;
        }
    }

//    private final int expireSeconds = 60;
//    private Map<MessageIdUnit, Long> midCache;
    private final int maxMessage =1000;
    private Set<MessageIdUnit> midCache;

    private Object locker;

    public DuplicatedMessageFilter() {
        midCache = new HashSet<>(maxMessage);
        locker = new Object();
    }

    public boolean CheckMessage(ConversationType type, long uid, long mid) {
        return CheckMessage(type, uid, mid, 0);
    }

    public boolean CheckMessage(ConversationType type, long uid, long mid, long bizId) {
        synchronized (locker) {
            MessageIdUnit unit = new MessageIdUnit(type, bizId, uid, mid);
            boolean findFlag = false;
            if (midCache.contains(unit)) {
                midCache.add(unit);
            } else {
                midCache.add(unit);
                findFlag = true;
            }
            if (midCache.size() >= maxMessage) {
                midCache.clear();
                midCache = null;
                midCache = new HashSet<>(maxMessage);
            }
            return findFlag;
        }
    }
}