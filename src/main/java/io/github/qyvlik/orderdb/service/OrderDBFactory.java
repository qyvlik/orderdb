package io.github.qyvlik.orderdb.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.iq80.leveldb.impl.Iq80DBFactory.*;

public class OrderDBFactory {

    public static final Set<String> BLACK_GROUP_NAMES =
            new TreeSet<String>(Lists.newArrayList("sys"));

    public static final String GROUP_PREFIX = "group:";

    private final Map<String, DB> dbMap = Maps.newConcurrentMap();

    private DB sysDB;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private String orderDBDirectory;

    public OrderDBFactory() {

    }

    public OrderDBFactory(String orderDBDirectory) {
        this.orderDBDirectory = orderDBDirectory;
    }

    public String getOrderDBDirectory() {
        return orderDBDirectory;
    }

    public void setOrderDBDirectory(String orderDBDirectory) {
        this.orderDBDirectory = orderDBDirectory;
    }

    public DB createDBByGroup(String group, boolean createIfMissing) {
        return createDBByGroup(group, createIfMissing, false);
    }

    protected DB createDBByGroup(String group, boolean createIfMissing, boolean ignoreBlackList) {
        if (createIfMissing) {
            return dbMap.computeIfAbsent(group, k -> createBucketInternal(group, ignoreBlackList));
        } else {
            return dbMap.get(group);
        }
    }

    private DB createBucketInternal(String group, boolean ignoreBlackList) {
        if (StringUtils.isBlank(group)) {
            throw new RuntimeException("createDBByGroup failure : group was empty");
        }

        if (!ignoreBlackList && BLACK_GROUP_NAMES.contains(group)) {
            throw new RuntimeException("createDBByGroup failure : group "
                    + group + " is in blacklist");
        }

        if (group.contains("/")) {
            throw new RuntimeException("createDBByGroup failure : group "
                    + group + " contains invalidate character");
        }

        if (StringUtils.isBlank(getOrderDBDirectory())) {
            throw new RuntimeException("createDBByGroup failure : orderDBDirectory was empty");
        }

        DB db = null;

        Options options = new Options();
        options.createIfMissing(true);

        String directory = getOrderDBDirectory();

        String groupDirectory;

        if (directory.endsWith("/")) {
            groupDirectory = directory + group;
        } else {
            groupDirectory = directory + "/" + group;
        }

        try {
            db = factory.open(new File(groupDirectory), options);
        } catch (Exception e) {
            logger.error("create leveldb failure:", e);
            throw new RuntimeException(e);
        }

        if (!BLACK_GROUP_NAMES.contains(group)) {
            sysDB.put(bytes(GROUP_PREFIX + group), bytes(group));           // save group
        }

        return db;
    }

    @PostConstruct
    public void loadDBs() throws Exception {
        sysDB = createDBByGroup("sys", true, true);

        DBIterator iterator = sysDB.iterator();
        try {
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                String key = asString(iterator.peekNext().getKey());
                if (key.startsWith(GROUP_PREFIX)) {
                    String value = asString(iterator.peekNext().getValue());
                    createDBByGroup(value, true);
                }
            }
        } finally {
            // Make sure you close the iterator to avoid resource leaks.
            iterator.close();
        }
    }

    @PreDestroy
    public void closeDBs() {
        logger.debug("closeDBs start");
        for (String group : dbMap.keySet()) {
            logger.info("closeDBs start:{}", group);
            DB db = dbMap.get(group);
            try {
                db.close();
                logger.info("closeDBs end:{}", group);
            } catch (Exception e) {
                logger.error("closeDBs failure:{}", group, e);
            }
        }
        logger.debug("closeDBs end");
    }
}
