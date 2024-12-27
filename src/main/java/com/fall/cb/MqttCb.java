package com.fall.cb;

import com.fall.handler.MsgHandler;
import io.github.netty.mqtt.client.callback.MqttCallback;
import io.github.netty.mqtt.client.callback.MqttReceiveCallbackResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;


/**
 * @author FAll
 * @date 2024年12月12日 10:15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MqttCb implements MqttCallback {

    private final MsgHandler handler;

    @Override
    public void messageReceiveCallback(MqttReceiveCallbackResult receiveCallbackResult) {
        String msg = new String(receiveCallbackResult.getPayload(), StandardCharsets.UTF_8);

        handler.handleMsg(msg);

        MqttCallback.super.messageReceiveCallback(receiveCallbackResult);
    }
}
