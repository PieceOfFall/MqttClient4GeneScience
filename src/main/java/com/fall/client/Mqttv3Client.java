package com.fall.client;

import com.fall.cb.MqttCb;
import io.github.netty.mqtt.client.MqttClient;
import io.github.netty.mqtt.client.MqttClientFactory;
import io.github.netty.mqtt.client.MqttConnectParameter;
import io.github.netty.mqtt.client.constant.MqttVersion;
import io.github.netty.mqtt.client.msg.MqttWillMsg;
import io.netty.handler.codec.mqtt.MqttQoS;
import jakarta.annotation.PostConstruct;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * @author FAll
 * @date 2024年12月12日 10:07
 */
@Component
@ConfigurationProperties("mqtt")
public class Mqttv3Client {

    private final MqttClientFactory mqttClientFactory;
    private final MqttCb ctrlMqttCallback;

    @Autowired
    public Mqttv3Client(MqttClientFactory mqttClientFactory,
                        MqttCb ctrlMqttCallback) {
        this.mqttClientFactory = mqttClientFactory;
        this.ctrlMqttCallback = ctrlMqttCallback;
    }

    @Setter
    private String clientId;

    @Setter
    private String ip;

    @Setter
    private Integer port;

    @Setter
    private Integer qos;

    @Setter
    private List<String> subscribe;

    @PostConstruct
    public void connect() {
        //创建连接参数，设置客户端ID
        MqttConnectParameter mqttConnectParameter = new MqttConnectParameter(clientId + UUID.randomUUID());
        //设置客户端版本（默认为3.1.1）
        mqttConnectParameter.setMqttVersion(MqttVersion.MQTT_3_1_1);
        //是否自动重连
        mqttConnectParameter.setAutoReconnect(true);
        //Host
        mqttConnectParameter.setHost(ip);
        //端口
        mqttConnectParameter.setPort(port);
        //是否使用SSL/TLS
        mqttConnectParameter.setSsl(false);
        //遗嘱消息
        MqttWillMsg mqttWillMsg = new MqttWillMsg("backend-disconnect", new byte[]{}, MqttQoS.AT_LEAST_ONCE);
        mqttConnectParameter.setWillMsg(mqttWillMsg);
        //是否清除会话
        mqttConnectParameter.setCleanSession(true);
        //心跳间隔
        mqttConnectParameter.setKeepAliveTimeSeconds(60);
        //连接超时时间
        mqttConnectParameter.setConnectTimeoutSeconds(30);
        //创建一个客户端
        MqttClient mqttClient = mqttClientFactory.createMqttClient(mqttConnectParameter);
        mqttClient.addMqttCallback(ctrlMqttCallback);
        mqttClient.connect();

        for (String sub : subscribe) {
            mqttClient.subscribe(sub,MqttQoS.valueOf(qos));
        }

    }
}
