package io.github.qyvlik.orderdb.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import io.github.qyvlik.orderdb.entity.AppendListRequest;
import io.github.qyvlik.orderdb.entity.AppendRequest;
import io.github.qyvlik.orderdb.entity.QueueUpBinlog;
import io.github.qyvlik.orderdb.entity.QueueUpRecord;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.WriteBatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.iq80.leveldb.impl.Iq80DBFactory.asString;
import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;

@Service
public class QueueUpService {

    public static final String SEPARATOR = ":";
    public static final String QUEUE_UP_INDEX = "i";
    public static final String QUEUE_UP_LAST_INDEX = "l";
    public static final String QUEUE_UP_KEY = "k";
    public static final String QUEUE_UP_BINLOG = "b";
    public static final String QUEUE_UP_BINLOG_LAST_INDEX = "L";

    @Autowired
    @Qualifier("orderDBFactory")
    private OrderDBFactory orderDBFactory;

    private String keyNameOfGroupAndKey(String group, String key) {
        return QUEUE_UP_KEY + SEPARATOR + key;
    }

    private String keyNameOfGroupAndIndex(String group, Long index) {
        return QUEUE_UP_INDEX + SEPARATOR + index;
    }

    private String keyNameOfLastIndex(String group) {
        return QUEUE_UP_LAST_INDEX;
    }

    private String keyNameOfBinlog(String group, Long binlogIndex) {
        // GROUP:QUEUE_UP_BINLOG:i
        return QUEUE_UP_BINLOG + SEPARATOR + binlogIndex;
    }

    private String keyNameOfBinlogLastIndex(String group) {
        // GROUP:QUEUE_UP_BINLOG_LAST_INDEX
        return QUEUE_UP_BINLOG_LAST_INDEX;
    }

    private QueueUpRecord getByGroupAndKey(DB levelDB, String group, String key) {
        if (levelDB == null) {
            return null;
        }
        String fullKey = keyNameOfGroupAndKey(group, key);

        try {
            byte[] valInDB = levelDB.get(bytes(fullKey));

            if (valInDB != null) {
                Long indexOfRecord = Long.parseLong(asString(valInDB));
                return getByGroupAndIndex(levelDB, group, indexOfRecord);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private QueueUpRecord getByGroupAndIndex(DB levelDB, String group, Long index) {
        if (levelDB == null) {
            return null;
        }

        String fullKey = keyNameOfGroupAndIndex(group, index);

        try {
            byte[] valInDB = levelDB.get(bytes(fullKey));
            if (valInDB != null) {
                String val = asString(valInDB);
                return JSON.parseObject(val).toJavaObject(QueueUpRecord.class);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Long getLastIndexByGroup(DB levelDB, String group) {
        if (levelDB == null) {
            return null;
        }

        String fullKey = keyNameOfLastIndex(group);
        try {
            byte[] valInDB = levelDB.get(bytes(fullKey));
            if (valInDB != null) {
                return Long.parseLong(asString(valInDB));
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Long getBinlogLastIndexByGroup(DB levelDB, String group) {
        if (levelDB == null) {
            return null;
        }

        String fullKey = keyNameOfBinlogLastIndex(group);
        try {
            byte[] valInDB = levelDB.get(bytes(fullKey));
            if (valInDB != null) {
                return Long.parseLong(asString(valInDB));
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private QueueUpBinlog getBinlog(DB levelDB, String group, Long binlogIndex) {
        if (levelDB == null) {
            return null;
        }
        String fullKey = keyNameOfBinlog(group, binlogIndex);
        try {
            byte[] valInDB = levelDB.get(bytes(fullKey));
            if (valInDB != null) {
                return JSON.parseObject(asString(valInDB)).toJavaObject(QueueUpBinlog.class);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public QueueUpRecord getByGroupAndKey(String group, String key) {
        DB levelDB = orderDBFactory.createDBByGroup(group, false);
        return getByGroupAndKey(levelDB, group, key);
    }

    public QueueUpRecord getByGroupAndIndex(String group, Long index) {
        DB levelDB = orderDBFactory.createDBByGroup(group, false);
        return getByGroupAndIndex(levelDB, group, index);
    }

    public QueueUpBinlog getBinlogByGroupAndIndex(String group, Long index) {
        DB levelDB = orderDBFactory.createDBByGroup(group, false);
        return getBinlog(levelDB, group, index);
    }

    public Long getBinlogLastIndexByGroup(String group) {
        DB levelDB = orderDBFactory.createDBByGroup(group, false);
        return getBinlogLastIndexByGroup(levelDB, group);
    }

    public Long getLastIndexByGroup(String group) {
        DB levelDB = orderDBFactory.createDBByGroup(group, false);
        return getLastIndexByGroup(levelDB, group);
    }

    public List<QueueUpRecord> appendList(AppendListRequest request) {
        if (request.getList() == null || request.getList().size() == 0) {
            throw new RuntimeException("appendList failure : list is empty");
        }
        DB levelDB = orderDBFactory.createDBByGroup(request.getGroup(), true);

        WriteBatch writeBatch = levelDB.createWriteBatch();

        // record
        Long lastIndex = getLastIndexByGroup(levelDB, request.getGroup());
        if (lastIndex == null) {
            lastIndex = -1L;
        }
        long currentIndex = lastIndex;

        // binlog
        Long lastIndexOfBinlog = getBinlogLastIndexByGroup(levelDB, request.getGroup());
        if (lastIndexOfBinlog == null) {
            lastIndexOfBinlog = -1L;
        }
        Long currentIndexOfBinlog = lastIndexOfBinlog;

        List<QueueUpRecord> recordList = Lists.newLinkedList();

        for (AppendRequest appendRequest : request.getList()) {
            QueueUpRecord record = getByGroupAndKey(levelDB, appendRequest.getGroup(), appendRequest.getKey());

            if (record != null && request.getIgnoreExist()) {
                recordList.add(record);
                continue;
            }

            if (record != null && !request.getIgnoreExist()) {
                throw new RuntimeException("appendList failure : group:"
                        + appendRequest.getGroup()
                        + ", key:" + appendRequest.getKey()
                        + " already append");
            }

            // record
            currentIndex += 1;

            record = new QueueUpRecord(appendRequest.getGroup(),
                    appendRequest.getKey(),
                    currentIndex,
                    appendRequest.getData());

            // put group:index value
            String key_name_of_group_and_index = keyNameOfGroupAndIndex(appendRequest.getGroup(), currentIndex);
            writeBatch.put(bytes(key_name_of_group_and_index), bytes(JSON.toJSONString(record)));

            // put the group:key index
            String key_name_of_group_and_key = keyNameOfGroupAndKey(appendRequest.getGroup(), appendRequest.getKey());
            writeBatch.put(bytes(key_name_of_group_and_key), bytes(currentIndex + ""));

            recordList.add(record);

            // binlog
            currentIndexOfBinlog += 1;

            QueueUpBinlog queueUpBinlog = new QueueUpBinlog(
                    currentIndexOfBinlog,
                    QueueUpBinlog.Action.append,
                    record.getGroup(),
                    record.getKey(),
                    record.getData()
            );

            // put binlog
            String key_name_of_binlog = keyNameOfBinlog(appendRequest.getGroup(), currentIndexOfBinlog);
            writeBatch.put(bytes(key_name_of_binlog), bytes(JSON.toJSONString(queueUpBinlog)));
        }

        // put the group:lastIndex
        String key_name_of_last_index = keyNameOfLastIndex(request.getGroup());
        writeBatch.put(bytes(key_name_of_last_index), bytes(currentIndex + ""));

        // put binlog_last_index
        String key_name_of_binlog_last_index = keyNameOfBinlogLastIndex(request.getGroup());
        writeBatch.put(bytes(key_name_of_binlog_last_index), bytes(currentIndexOfBinlog + ""));

        levelDB.write(writeBatch);

        return recordList;
    }

    public QueueUpRecord append(String group, String key, Object data) {
        DB levelDB = orderDBFactory.createDBByGroup(group, true);

        QueueUpRecord record = getByGroupAndKey(levelDB, group, key);
        if (record != null) {
            return record;
        }

        Long lastIndex = getLastIndexByGroup(levelDB, group);

        if (lastIndex == null) {
            lastIndex = -1L;
        }

        long currentIndex = lastIndex + 1;

        record = new QueueUpRecord(group, key, currentIndex, data);

        WriteBatch writeBatch = levelDB.createWriteBatch();

        // put group:index value
        String key_name_of_group_and_index = keyNameOfGroupAndIndex(group, currentIndex);
        writeBatch.put(bytes(key_name_of_group_and_index), bytes(JSON.toJSONString(record)));

        // put the group:key index
        String key_name_of_group_and_key = keyNameOfGroupAndKey(group, key);
        writeBatch.put(bytes(key_name_of_group_and_key), bytes(currentIndex + ""));

        // put the group:lastIndex
        String key_name_of_last_index = keyNameOfLastIndex(group);
        writeBatch.put(bytes(key_name_of_last_index), bytes(currentIndex + ""));

        // binlog
        Long lastIndexOfBinlog = getBinlogLastIndexByGroup(levelDB, group);
        if (lastIndexOfBinlog == null) {
            lastIndexOfBinlog = -1L;
        }
        Long currentIndexOfBinlog = lastIndexOfBinlog + 1;

        QueueUpBinlog queueUpBinlog = new QueueUpBinlog(
                currentIndexOfBinlog,
                QueueUpBinlog.Action.append,
                group,
                key,
                data
        );

        // put binlog
        String key_name_of_binlog = keyNameOfBinlog(group, currentIndexOfBinlog);
        writeBatch.put(bytes(key_name_of_binlog), bytes(JSON.toJSONString(queueUpBinlog)));

        // put binlog_last_index
        String key_name_of_binlog_last_index = keyNameOfBinlogLastIndex(group);
        writeBatch.put(bytes(key_name_of_binlog_last_index), bytes(currentIndexOfBinlog + ""));

        levelDB.write(writeBatch);

        return record;
    }

    public QueueUpRecord delete(String group, String key) {
        DB levelDB = orderDBFactory.createDBByGroup(group, true);
        QueueUpRecord record = getByGroupAndKey(levelDB, group, key);
        delete(levelDB, record);
        return record;
    }

    public QueueUpRecord delete(String group, Long index) {
        DB levelDB = orderDBFactory.createDBByGroup(group, true);
        QueueUpRecord record = getByGroupAndIndex(levelDB, group, index);
        delete(levelDB, record);
        return record;
    }

    private boolean delete(DB levelDB, QueueUpRecord record) {
        if (record == null) {
            return false;
        }

        WriteBatch writeBatch = levelDB.createWriteBatch();

        // del group:key
        String key_name_of_group_key = keyNameOfGroupAndKey(record.getGroup(), record.getKey());
        writeBatch.delete(bytes(key_name_of_group_key));

        // del group:index
        String key_name_of_group_index = keyNameOfGroupAndIndex(record.getGroup(), record.getIndex());
        writeBatch.delete(bytes(key_name_of_group_index));

        // binlog
        Long lastIndexOfBinlog = getBinlogLastIndexByGroup(levelDB, record.getGroup());
        if (lastIndexOfBinlog == null) {
            lastIndexOfBinlog = -1L;
        }
        Long currentIndexOfBinlog = lastIndexOfBinlog + 1;

        QueueUpBinlog queueUpBinlog = new QueueUpBinlog(
                currentIndexOfBinlog,
                QueueUpBinlog.Action.delete,
                record.getGroup(),
                record.getKey(),
                null
        );

        // put binlog
        String key_name_of_binlog = keyNameOfBinlog(record.getGroup(), currentIndexOfBinlog);
        writeBatch.put(bytes(key_name_of_binlog), bytes(JSON.toJSONString(queueUpBinlog)));

        // put binlog_last_index
        String key_name_of_binlog_last_index = keyNameOfBinlogLastIndex(record.getGroup());
        writeBatch.put(bytes(key_name_of_binlog_last_index), bytes(currentIndexOfBinlog + ""));

        levelDB.write(writeBatch);

        return true;
    }
}
