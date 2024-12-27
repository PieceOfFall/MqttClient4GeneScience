package com.fall.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * @author FAll
 * @date 2024年12月12日 11:11
 */
@Configuration
public class AsyncConfig {
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(15);      // 核心线程数
        executor.setMaxPoolSize(20);      // 最大线程数
        executor.setQueueCapacity(25);    // 队列容量
        executor.setThreadNamePrefix("Async-Thread-");  // 线程名前缀
        executor.initialize();
        return executor;
    }
}
