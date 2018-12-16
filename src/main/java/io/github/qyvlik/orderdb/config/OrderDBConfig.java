package io.github.qyvlik.orderdb.config;

import io.github.qyvlik.orderdb.method.executor.WritableExecutor;
import io.github.qyvlik.orderdb.service.OrderDBFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class OrderDBConfig {

    @Value("${orderdb.disk.directory}")
    private String orderDBDiskDirectory;

    @Value("${orderdb.disk.groupLimit}")
    private Long orderDBDiskGroupLimit;

    @Value("${orderdb.admin.password}")
    private String orderDBAdminPassword;


    @Bean("recordPushExecutor")
    public Executor recordPushExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    @Bean("writableExecutor")
    public WritableExecutor writableExecutor() {
        return new WritableExecutor();
    }

    @Bean("orderDBFactory")
    public OrderDBFactory orderDBFactory() {
        return new OrderDBFactory(orderDBDiskDirectory,
                orderDBDiskGroupLimit,
                orderDBAdminPassword);
    }
}
