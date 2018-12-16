package io.github.qyvlik.orderdb.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

public class OrderDBFactory {

    public static final Set<String> BLACK_GROUP_NAMES =
            new TreeSet<String>(Lists.newArrayList("sys"));

    private final Map<String, DB> dbMap = Maps.newConcurrentMap();

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
        if (createIfMissing) {
            return dbMap.computeIfAbsent(group, k -> createBucketInternal(group));
        } else {
            return dbMap.get(group);
        }
    }

    private DB createBucketInternal(String group) {
        if (StringUtils.isBlank(group)) {
            throw new RuntimeException("createDBByGroup failure : group was empty");
        }

        if (BLACK_GROUP_NAMES.contains(group)) {
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

        return db;
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
