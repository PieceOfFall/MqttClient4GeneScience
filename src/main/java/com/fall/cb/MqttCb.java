package com.fall.cb;

import io.github.netty.mqtt.client.callback.MqttCallback;
import io.github.netty.mqtt.client.callback.MqttReceiveCallbackResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * @author FAll
 * @date 2024年12月12日 10:15
 */
@Slf4j
@Component
public class MqttCb implements MqttCallback {
    @Override
    public void messageReceiveCallback(MqttReceiveCallbackResult receiveCallbackResult) {
        System.out.println(new String(receiveCallbackResult.getPayload()));
        MqttCallback.super.messageReceiveCallback(receiveCallbackResult);
    }
}
