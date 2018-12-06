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
    @Qualifier("levelDB")
    private DB levelDB;

    public byte[] valKey(String group, String key) {
        return bytes(KEY + group + ":" + key);
    }

    public byte[] valSeq(String group, Long seqNum) {
        return bytes(SEQ + group + ":" + seqNum);
    }

    public byte[] seqCounter(String group) {
        return bytes(SEQ_COUNTER + group);
    }

    public synchronized long sequence(String group, String key, Object data) {
        try {
            byte[] valKey = valKey(group, key);
            byte[] valInDB = levelDB.get(valKey);

            if (valInDB != null) {
                String val = asString(valInDB);
                SequenceRecord record = JSON.parseObject(val).toJavaObject(SequenceRecord.class);
                return record.getSequenceId();
            }

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

            SequenceRecord record = new SequenceRecord();
            record.setSequenceId(seqNum);
            record.setUniqueKey(asString(valKey));
            record.setData(data);

            byte[] val = bytes(JSON.toJSONString(record));

            // save key, value
            writeBatch.put(valKey, val);

            // save seq, vale
            writeBatch.put(valSeq(group, seqNum), val);

            levelDB.write(writeBatch);

            return seqNum;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public SequenceRecord get(String group, String key) {
        try {
            byte[] valKey = valKey(group, key);
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

    public SequenceRecord get(String group, Long seqNum) {
        try {
            byte[] valKey = valSeq(group, seqNum);
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
