package io.github.qyvlik.orderdb.config;

import io.github.qyvlik.orderdb.service.OrderDBFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderDBConfig {

    @Value("${orderdb.directory}")
    private String orderDBDirectory;

    @Bean("orderDBFactory")
    public OrderDBFactory orderDBFactory() {
        return new OrderDBFactory(orderDBDirectory);
    }
}
