package io.github.qyvlik.orderdb.modules.durable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.qyvlik.orderdb.utils.SecurityDataHashUtils;
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

    public static final Set<String> BLACK_SCOPE_NAMES =
            new TreeSet<String>(Lists.newArrayList("sys"));

    public static final String SCOPE_PREFIX = "scope:";
    public static final String USER_PREFIX = "user:";

    private final Map<String, DB> dbMap = Maps.newConcurrentMap();

    private DB sysDB;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private String orderDBDiskDirectory;
    private Long orderDBDiskScopeLimit;
    private String initPassword;

    public OrderDBFactory() {

    }

    public OrderDBFactory(String orderDBDiskDirectory, Long orderDBDiskScopeLimit, String initPassword) {
        this.orderDBDiskDirectory = orderDBDiskDirectory;
        this.orderDBDiskScopeLimit = orderDBDiskScopeLimit;
        this.initPassword = initPassword;
    }

    public String getOrderDBDiskDirectory() {
        return orderDBDiskDirectory;
    }

    public Long getOrderDBDiskScopeLimit() {
        return orderDBDiskScopeLimit;
    }

    public DB getSysDB() {
        return sysDB;
    }

    public Map<String, DB> getDbMap() {
        return dbMap;
    }

    public DB createDBByScope(String scope, boolean createIfMissing) {
        if (StringUtils.isBlank(scope)) {
            throw new RuntimeException("scope is empty");
        }

        final String regexStr = "^[\\.0-9a-zA-Z _-]+$";
        if (!scope.matches(regexStr)) {
            throw new RuntimeException("scope not match:" + regexStr);
        }

        if (getOrderDBDiskScopeLimit() < dbMap.size() - 1) {
            throw new RuntimeException("createDBByScope failure : scope count more than "
                    + orderDBDiskScopeLimit);
        }

        return createDBByScope(scope, createIfMissing, false);
    }

    protected DB createDBByScope(String scope, boolean createIfMissing, boolean ignoreBlackList) {
        if (createIfMissing) {
            return dbMap.computeIfAbsent(scope, k -> createBucketInternal(scope, ignoreBlackList));
        } else {
            return dbMap.get(scope);
        }
    }

    private DB createBucketInternal(String scope, boolean ignoreBlackList) {
        if (StringUtils.isBlank(scope)) {
            throw new RuntimeException("createDBByScope failure : scope was empty");
        }

        if (!ignoreBlackList && BLACK_SCOPE_NAMES.contains(scope)) {
            throw new RuntimeException("createDBByScope failure : scope "
                    + scope + " is in blacklist");
        }

        if (scope.contains("/")) {
            throw new RuntimeException("createDBByScope failure : scope "
                    + scope + " contains invalidate character");
        }

        if (StringUtils.isBlank(getOrderDBDiskDirectory())) {
            throw new RuntimeException("createDBByScope failure : orderDBDiskDirectory was empty");
        }

        DB db = null;

        Options options = new Options();
        options.createIfMissing(true);

        String directory = getOrderDBDiskDirectory();

        String scopeDirectory;

        if (directory.endsWith("/")) {
            scopeDirectory = directory + scope;
        } else {
            scopeDirectory = directory + "/" + scope;
        }

        if (!BLACK_SCOPE_NAMES.contains(scope)) {
            sysDB.put(bytes(SCOPE_PREFIX + scope), bytes(scope));           // save scope
        }

        try {
            db = factory.open(new File(scopeDirectory), options);
        } catch (Exception e) {
            logger.error("create leveldb failure:", e);
            throw new RuntimeException(e);
        }

        return db;
    }

    public boolean checkPassword(String username, String password) {
        byte[] usernameBytes = bytes(USER_PREFIX + username);
        String passwordHashInDB = asString(sysDB.get(usernameBytes));
        try {
            String passwordHash = SecurityDataHashUtils.createHash(password);
            return passwordHashInDB.equals(passwordHash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean updatePassword(String username, String oldPassword, String newPassword) {
        if (!checkPassword(username, oldPassword)) {
            return false;
        }

        byte[] usernameBytes = bytes(USER_PREFIX + username);

        try {
            String passwordHash = SecurityDataHashUtils.createHash(newPassword);
            sysDB.put(usernameBytes, bytes(passwordHash));
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void createUser(String username, String password) {
        byte[] usernameBytes = bytes(USER_PREFIX + username);
        String passwordHashInDB = asString(sysDB.get(usernameBytes));
        if (StringUtils.isBlank(passwordHashInDB)) {
            try {
                String passwordHash = SecurityDataHashUtils.createHash(password);
                sysDB.put(usernameBytes, bytes(passwordHash));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void createAdminUser() {
        createUser("admin", this.initPassword);
    }

    @PostConstruct
    public void loadDBs() throws Exception {
        sysDB = createDBByScope("sys", true, true);

        createAdminUser();      // init password

        DBIterator iterator = sysDB.iterator();
        try {
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                String key = asString(iterator.peekNext().getKey());
                if (key.startsWith(SCOPE_PREFIX)) {
                    String value = asString(iterator.peekNext().getValue());
                    createDBByScope(value, true);
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
        for (String scope : dbMap.keySet()) {
            logger.info("closeDBs start:{}", scope);
            DB db = dbMap.get(scope);
            try {
                db.close();
                logger.info("closeDBs end:{}", scope);
            } catch (Exception e) {
                logger.error("closeDBs failure:{}", scope, e);
            }
        }
        logger.debug("closeDBs end");
    }
}
