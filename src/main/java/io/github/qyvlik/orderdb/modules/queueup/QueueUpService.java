package io.github.qyvlik.orderdb.modules.queueup;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import io.github.qyvlik.orderdb.entity.QueueUpBinlog;
import io.github.qyvlik.orderdb.entity.QueueUpRecord;
import io.github.qyvlik.orderdb.entity.request.AppendListRequest;
import io.github.qyvlik.orderdb.entity.request.AppendRequest;
import io.github.qyvlik.orderdb.modules.durable.OrderDBFactory;
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

    private String keyNameOfScopeAndKey(String scope, String key) {
        return QUEUE_UP_KEY + SEPARATOR + key;
    }

    private String keyNameOfScopeAndIndex(String scope, Long index) {
        return QUEUE_UP_INDEX + SEPARATOR + index;
    }

    private String keyNameOfLastIndex(String scope) {
        return QUEUE_UP_LAST_INDEX;
    }

    private String keyNameOfBinlog(String scope, Long binlogIndex) {
        // scope:QUEUE_UP_BINLOG:i
        return QUEUE_UP_BINLOG + SEPARATOR + binlogIndex;
    }

    private String keyNameOfBinlogLastIndex(String scope) {
        // scope:QUEUE_UP_BINLOG_LAST_INDEX
        return QUEUE_UP_BINLOG_LAST_INDEX;
    }

    private QueueUpRecord getByScopeAndKey(DB levelDB, String scope, String key) {
        if (levelDB == null) {
            return null;
        }
        String fullKey = keyNameOfScopeAndKey(scope, key);

        try {
            byte[] valInDB = levelDB.get(bytes(fullKey));

            if (valInDB != null) {
                Long indexOfRecord = Long.parseLong(asString(valInDB));
                return getByScopeAndIndex(levelDB, scope, indexOfRecord);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private QueueUpRecord getByScopeAndIndex(DB levelDB, String scope, Long index) {
        if (levelDB == null) {
            return null;
        }

        String fullKey = keyNameOfScopeAndIndex(scope, index);

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

    private Long getLastIndexByScope(DB levelDB, String scope) {
        if (levelDB == null) {
            return null;
        }

        String fullKey = keyNameOfLastIndex(scope);
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

    private Long getBinlogLastIndexByScope(DB levelDB, String scope) {
        if (levelDB == null) {
            return null;
        }

        String fullKey = keyNameOfBinlogLastIndex(scope);
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

    private QueueUpBinlog getBinlog(DB levelDB, String scope, Long binlogIndex) {
        if (levelDB == null) {
            return null;
        }
        String fullKey = keyNameOfBinlog(scope, binlogIndex);
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

    public QueueUpRecord getByScopeAndKey(String scope, String key) {
        DB levelDB = orderDBFactory.createDBByScope(scope, false);
        return getByScopeAndKey(levelDB, scope, key);
    }

    public QueueUpRecord getByScopeAndIndex(String scope, Long index) {
        DB levelDB = orderDBFactory.createDBByScope(scope, false);
        return getByScopeAndIndex(levelDB, scope, index);
    }

    public QueueUpBinlog getBinlogByScopeAndIndex(String scope, Long index) {
        DB levelDB = orderDBFactory.createDBByScope(scope, false);
        return getBinlog(levelDB, scope, index);
    }

    public Long getBinlogLastIndexByScope(String scope) {
        DB levelDB = orderDBFactory.createDBByScope(scope, false);
        return getBinlogLastIndexByScope(levelDB, scope);
    }

    public Long getLastIndexByScope(String scope) {
        DB levelDB = orderDBFactory.createDBByScope(scope, false);
        return getLastIndexByScope(levelDB, scope);
    }

    public void redoBinlog(List<QueueUpBinlog> binlogList) {

    }


    public List<QueueUpRecord> appendList(AppendListRequest request) {
        if (request.getList() == null || request.getList().size() == 0) {
            throw new RuntimeException("appendList failure : list is empty");
        }
        DB levelDB = orderDBFactory.createDBByScope(request.getScope(), true);

        WriteBatch writeBatch = levelDB.createWriteBatch();

        // record
        Long lastIndex = getLastIndexByScope(levelDB, request.getScope());
        if (lastIndex == null) {
            lastIndex = -1L;
        }
        long currentIndex = lastIndex;

        // binlog
        Long lastIndexOfBinlog = getBinlogLastIndexByScope(levelDB, request.getScope());
        if (lastIndexOfBinlog == null) {
            lastIndexOfBinlog = -1L;
        }
        Long currentIndexOfBinlog = lastIndexOfBinlog;

        List<QueueUpRecord> recordList = Lists.newLinkedList();

        // todo check the request.getList() repeat
        for (AppendRequest appendRequest : request.getList()) {
            QueueUpRecord record = getByScopeAndKey(levelDB, appendRequest.getScope(), appendRequest.getKey());

            if (record != null && request.getIgnoreExist()) {
                recordList.add(record);
                continue;
            }

            if (record != null && !request.getIgnoreExist()) {
                throw new RuntimeException("appendList failure : scope:"
                        + appendRequest.getScope()
                        + ", key:" + appendRequest.getKey()
                        + " already append");
            }

            // record
            currentIndex += 1;

            record = new QueueUpRecord(appendRequest.getScope(),
                    appendRequest.getKey(),
                    currentIndex,
                    appendRequest.getData());

            // put scope:index value
            String key_name_of_scope_and_index = keyNameOfScopeAndIndex(appendRequest.getScope(), currentIndex);
            writeBatch.put(bytes(key_name_of_scope_and_index), bytes(JSON.toJSONString(record)));

            // put the scope:key index
            String key_name_of_scope_and_key = keyNameOfScopeAndKey(appendRequest.getScope(), appendRequest.getKey());
            writeBatch.put(bytes(key_name_of_scope_and_key), bytes(currentIndex + ""));

            recordList.add(record);

            // binlog
            currentIndexOfBinlog += 1;

            QueueUpBinlog queueUpBinlog = new QueueUpBinlog(
                    currentIndexOfBinlog,
                    QueueUpBinlog.Action.append,
                    record.getScope(),
                    record.getKey(),
                    record.getIndex(),
                    record.getData()
            );

            // put binlog
            String key_name_of_binlog = keyNameOfBinlog(appendRequest.getScope(), currentIndexOfBinlog);
            writeBatch.put(bytes(key_name_of_binlog), bytes(JSON.toJSONString(queueUpBinlog)));
        }

        // put the scope:lastIndex
        String key_name_of_last_index = keyNameOfLastIndex(request.getScope());
        writeBatch.put(bytes(key_name_of_last_index), bytes(currentIndex + ""));

        // put binlog_last_index
        String key_name_of_binlog_last_index = keyNameOfBinlogLastIndex(request.getScope());
        writeBatch.put(bytes(key_name_of_binlog_last_index), bytes(currentIndexOfBinlog + ""));

        levelDB.write(writeBatch);

        return recordList;
    }

    public QueueUpRecord append(String scope, String key, Object data) {
        DB levelDB = orderDBFactory.createDBByScope(scope, true);

        QueueUpRecord record = getByScopeAndKey(levelDB, scope, key);
        if (record != null) {
            return record;
        }

        Long lastIndex = getLastIndexByScope(levelDB, scope);

        if (lastIndex == null) {
            lastIndex = -1L;
        }

        long currentIndex = lastIndex + 1;

        record = new QueueUpRecord(scope, key, currentIndex, data);

        WriteBatch writeBatch = levelDB.createWriteBatch();

        // put scope:index value
        String key_name_of_scope_and_index = keyNameOfScopeAndIndex(scope, currentIndex);
        writeBatch.put(bytes(key_name_of_scope_and_index), bytes(JSON.toJSONString(record)));

        // put the scope:key index
        String key_name_of_scope_and_key = keyNameOfScopeAndKey(scope, key);
        writeBatch.put(bytes(key_name_of_scope_and_key), bytes(currentIndex + ""));

        // put the scope:lastIndex
        String key_name_of_last_index = keyNameOfLastIndex(scope);
        writeBatch.put(bytes(key_name_of_last_index), bytes(currentIndex + ""));

        // binlog
        Long lastIndexOfBinlog = getBinlogLastIndexByScope(levelDB, scope);
        if (lastIndexOfBinlog == null) {
            lastIndexOfBinlog = -1L;
        }
        Long currentIndexOfBinlog = lastIndexOfBinlog + 1;

        QueueUpBinlog queueUpBinlog = new QueueUpBinlog(
                currentIndexOfBinlog,
                QueueUpBinlog.Action.append,
                scope,
                key,
                currentIndex,
                data
        );

        // put binlog
        String key_name_of_binlog = keyNameOfBinlog(scope, currentIndexOfBinlog);
        writeBatch.put(bytes(key_name_of_binlog), bytes(JSON.toJSONString(queueUpBinlog)));

        // put binlog_last_index
        String key_name_of_binlog_last_index = keyNameOfBinlogLastIndex(scope);
        writeBatch.put(bytes(key_name_of_binlog_last_index), bytes(currentIndexOfBinlog + ""));

        levelDB.write(writeBatch);

        return record;
    }

    public QueueUpRecord delete(String scope, String key) {
        DB levelDB = orderDBFactory.createDBByScope(scope, true);
        QueueUpRecord record = getByScopeAndKey(levelDB, scope, key);
        delete(levelDB, record);
        return record;
    }

    public QueueUpRecord delete(String scope, Long index) {
        DB levelDB = orderDBFactory.createDBByScope(scope, true);
        QueueUpRecord record = getByScopeAndIndex(levelDB, scope, index);
        delete(levelDB, record);
        return record;
    }

    private boolean delete(DB levelDB, QueueUpRecord record) {
        if (record == null) {
            return false;
        }

        WriteBatch writeBatch = levelDB.createWriteBatch();

        // del scope:key
        String key_name_of_scope_key = keyNameOfScopeAndKey(record.getScope(), record.getKey());
        writeBatch.delete(bytes(key_name_of_scope_key));

        // del scope:index
        String key_name_of_scope_index = keyNameOfScopeAndIndex(record.getScope(), record.getIndex());
        writeBatch.delete(bytes(key_name_of_scope_index));

        // binlog
        Long lastIndexOfBinlog = getBinlogLastIndexByScope(levelDB, record.getScope());
        if (lastIndexOfBinlog == null) {
            lastIndexOfBinlog = -1L;
        }
        Long currentIndexOfBinlog = lastIndexOfBinlog + 1;

        QueueUpBinlog queueUpBinlog = new QueueUpBinlog(
                currentIndexOfBinlog,
                QueueUpBinlog.Action.delete,
                record.getScope(),
                record.getKey(),
                null,
                null
        );

        // put binlog
        String key_name_of_binlog = keyNameOfBinlog(record.getScope(), currentIndexOfBinlog);
        writeBatch.put(bytes(key_name_of_binlog), bytes(JSON.toJSONString(queueUpBinlog)));

        // put binlog_last_index
        String key_name_of_binlog_last_index = keyNameOfBinlogLastIndex(record.getScope());
        writeBatch.put(bytes(key_name_of_binlog_last_index), bytes(currentIndexOfBinlog + ""));

        levelDB.write(writeBatch);

        return true;
    }
}
