package io.github.qyvlik.orderdb.config;

import io.github.qyvlik.jsonrpclite.core.common.ITaskQueue;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

@Configuration
public class OrderDBConfig {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${orderdb.directory}")
    private String levelDBDirectory;

    private DB levelDB;

    @Bean("levelDB")
    public DB levelDB() {
        Options options = new Options();
        options.createIfMissing(true);
        DB db = null;
        try {
            db = factory.open(new File(levelDBDirectory), options);
        } catch (Exception e) {
            logger.error("create leveldb failure:", e);
            throw new RuntimeException(e);
        }

        levelDB = db;
        return db;
    }

    @Bean("writeExecutor")
    public Executor writeExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    @PreDestroy
    public void closeDB() {
        if (levelDB != null) {
            try {
                levelDB.close();
            } catch (Exception e) {

            }
        }
    }
}
