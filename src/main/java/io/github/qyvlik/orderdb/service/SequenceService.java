package io.github.qyvlik.orderdb.service;

import com.alibaba.fastjson.JSON;
import io.github.qyvlik.orderdb.entity.SequenceRecord;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.WriteBatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static org.iq80.leveldb.impl.Iq80DBFactory.asString;
import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;

@Service
public class SequenceService {

    public static final String SEQ_COUNTER = "seq-counter:";
    public static final String SEQ = "seq:";
    public static final String KEY = "key:";

    @Autowired
    @Qualifier("orderDBFactory")
    private OrderDBFactory orderDBFactory;

    public byte[] valKey(String group, String key) {
        return bytes(KEY + group + ":" + key);
    }

    public byte[] valSeq(String group, Long seqNum) {
        return bytes(SEQ + group + ":" + seqNum);
    }

    public byte[] seqCounter(String group) {
        return bytes(SEQ_COUNTER + group);
    }

    // use a single thread to write
    public SequenceRecord sequence(String group, String key, Object data) {
        DB levelDB = orderDBFactory.createDBByGroup(group, true);

        try {
            SequenceRecord record = getByKey(group, key);

            if (record != null) {
                return record;
            }

            byte[] valKey = valKey(group, key);

            byte[] seqCounter = seqCounter(group);

            WriteBatch writeBatch = levelDB.createWriteBatch();

            String seqNumStr = asString(levelDB.get(seqCounter));
            if (seqNumStr == null) {
                seqNumStr = "0";
                writeBatch.put(seqCounter, bytes(seqNumStr));
            }

            Long seqNum = Long.parseLong(seqNumStr);
            seqNum += 1;

            // update counter
            writeBatch.put(seqCounter, bytes(seqNum + ""));

            record = new SequenceRecord(group, seqNum, asString(valKey), data);

            // valKey
            byte[] val = bytes(JSON.toJSONString(record));
            byte[] valSeq = valSeq(group, seqNum);

            // save key, value
            writeBatch.put(valKey, valSeq);

            // save seq, valKey
            writeBatch.put(valSeq, val);

            levelDB.write(writeBatch);

            return record;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public SequenceRecord getByKey(String group, String key) {
        DB levelDB = orderDBFactory.createDBByGroup(group, false);

        if (levelDB == null) {
            return null;
        }

        try {
            return getByFullKey(levelDB, levelDB.get(valKey(group, key)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public SequenceRecord getBySequence(String group, Long seqNum) {
        DB levelDB = orderDBFactory.createDBByGroup(group, false);

        if (levelDB == null) {
            return null;
        }

        try {
            return getByFullKey(levelDB, valSeq(group, seqNum));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public long getLatestSequence(String group) {
        DB levelDB = orderDBFactory.createDBByGroup(group, false);

        if (levelDB == null) {
            return 0L;
        }

        byte[] seqCounter = seqCounter(group);
        String seqNumStr = asString(levelDB.get(seqCounter));
        if (seqNumStr == null) {
            return 0;
        }
        return Long.parseLong(seqNumStr);
    }

    private SequenceRecord getByFullKey(DB levelDB, byte[] valKey) {
        if (valKey == null) {
            return null;
        }

        try {
            byte[] valInDB = levelDB.get(valKey);

            if (valInDB != null) {
                String val = asString(valInDB);
                return JSON.parseObject(val).toJavaObject(SequenceRecord.class);
            }

            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
