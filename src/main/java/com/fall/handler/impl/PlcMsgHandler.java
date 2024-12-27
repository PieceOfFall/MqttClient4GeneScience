package com.fall.handler.impl;

import com.fall.client.tcp.TcpClient;
import com.fall.handler.MsgHandler;
import com.fall.model.vo.PlcCmd;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author FAll
 * @date 2024年12月23日 15:57
 */
@Slf4j
@Component
@ConfigurationProperties("plc")
public class PlcMsgHandler implements MsgHandler {
    private final TcpClient tcpClient;

    private List<PlcCmd> list;

    private Map<String, PlcCmd> plcMsgMap;

    public void setList(List<PlcCmd> list) {
        this.list = list;
        plcMsgMap = list.stream()
                .collect(Collectors.toMap(PlcCmd::getName, plcMsg -> plcMsg));
    }

    @Autowired
    public PlcMsgHandler(TcpClient tcpClient) {
        this.tcpClient = tcpClient;
    }

    @Override
    public void handleMsg(String msg) {
        log.debug("pc handler get: [{}]", msg);
        String[] splitMsg = msg.split(":");

        String target = splitMsg[0];
        String cmd = splitMsg[1];

        if (!"all".equals(target)) {
            PlcCmd plcMsg = plcMsgMap.get(target);
            if(plcMsg == null) {
                log.error("Unresolved Target: {}",target);
                return;
            }
            tcpClient.sendTcpMessage(plcMsg, cmd);
        } else {
            tcpClient.sendTcpMsgAll(list,cmd);
        }
    }
}
