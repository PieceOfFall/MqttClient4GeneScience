package com.fall.config;

import io.github.netty.mqtt.client.DefaultMqttClientFactory;
import io.github.netty.mqtt.client.MqttClientFactory;
import io.github.netty.mqtt.client.MqttConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author FAll
 * @date 2024年12月12日 10:14
 */
@Configuration
public class MqttConfig {
    @Bean
    public MqttClientFactory initClientFactory() {
        //创建MQTT全局配置器（也可以不创建）
        MqttConfiguration mqttConfiguration = new MqttConfiguration(2);
        //创建MQTT客户端工厂
        return new DefaultMqttClientFactory(mqttConfiguration);
    }

}
