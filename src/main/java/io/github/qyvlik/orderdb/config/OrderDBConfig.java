package io.github.qyvlik.orderdb.config;

import io.github.qyvlik.orderdb.modules.executor.WritableExecutor;
import io.github.qyvlik.orderdb.modules.durable.OrderDBFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class OrderDBConfig {

    @Value("${orderdb.disk.directory}")
    private String orderDBDiskDirectory;

    @Value("${orderdb.disk.scopeLimit}")
    private Long orderDBDiskScopeLimit;

    @Value("${orderdb.admin.password}")
    private String orderDBAdminPassword;

    @Value("${orderdb.leveldb.blocksize}")
    private Integer levelDBBlockSize;


    @Bean("orderDBFactory")
    public OrderDBFactory orderDBFactory() {
        return new OrderDBFactory(orderDBDiskDirectory,
                orderDBDiskScopeLimit,
                orderDBAdminPassword,
                levelDBBlockSize);
    }
}
