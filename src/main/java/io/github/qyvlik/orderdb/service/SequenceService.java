package io.github.qyvlik.orderdb.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.github.qyvlik.orderdb.entity.SequenceRecord;
import org.apache.commons.lang3.StringUtils;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.WriteBatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static org.iq80.leveldb.impl.Iq80DBFactory.asString;
import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;

@Service
public class SequenceService {

    public static final String SEQ = "seq:";
    public static final String SEQ_KEY = "seq-key:";
    @Autowired
    @Qualifier("levelDB")
    private DB levelDB;

    public Long sequence(String group, String key, String value) {
        try {
            byte[] valInDB = levelDB.get(bytes(group + ":" + key));
            if (valInDB == null) {

                WriteBatch writeBatch = levelDB.createWriteBatch();

                String seqNumStr = asString(levelDB.get(bytes(SEQ + group)));
                if (seqNumStr == null) {
                    seqNumStr = "0";
                    writeBatch.put(bytes(SEQ + group), bytes(seqNumStr));
                }

                Long seqNum = Long.parseLong(seqNumStr);
                seqNum += 1;

                // update counter
                writeBatch.put(bytes(SEQ + group), bytes(seqNum + ""));

                // save id, seqNum
                writeBatch.put(bytes(SEQ_KEY + group + ":" + key), bytes(seqNum + ""));

                // save seqNum, id
                writeBatch.put(bytes(SEQ + group + ":" + seqNum), bytes(key));


                // insert value
                writeBatch.put(bytes(group + ":" + key), bytes(value));

                levelDB.write(writeBatch);

                return seqNum;
            } else {
                String seqNumStr = asString(levelDB.get(bytes("seq:" + group)));
                return Long.parseLong(seqNumStr);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public SequenceRecord get(String group, String key) {
        try {
            byte[] valInDB = levelDB.get(bytes(group + ":" + key));

            String seqNumStr = asString(levelDB.get(bytes(SEQ_KEY + group + ":" + key)));

            if (valInDB == null) {
                return null;
            }

            Long seqNum = Long.parseLong(seqNumStr);
            String val = asString(valInDB);
            JSONObject obj = JSON.parseObject(val);

            return new SequenceRecord(
                    seqNum,
                    key,
                    obj
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public SequenceRecord get(String group, Long seqNum) {
        try {
            String uniqueKey = asString(levelDB.get(bytes(SEQ + group + ":" + seqNum)));

            if (StringUtils.isBlank(uniqueKey)) {
                return null;
            }

            byte[] valInDB = levelDB.get(bytes(group + ":" + uniqueKey));

            if (valInDB == null) {
                return null;
            }

            String val = asString(valInDB);
            JSONObject obj = JSON.parseObject(val);

            return new SequenceRecord(
                    seqNum,
                    uniqueKey,
                    obj
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
