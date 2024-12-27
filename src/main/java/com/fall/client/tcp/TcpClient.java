package com.fall.client.tcp;

import com.fall.model.vo.PlcCmd;
import com.fall.model.vo.PlcSubCmd;
import com.fall.utils.NetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.function.Function;

/**
 * @author FAll
 * @date 2024年12月12日 11:16
 */
@Slf4j
@SuppressWarnings("unused")
@Component
public class TcpClient {
    @Async
    public void sendTcpMessage(String ip, int port, String message) {
        // Validate inputs
        if (ip == null || ip.isEmpty()) {
            throw new IllegalArgumentException("IP address cannot be null or empty");
        }
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }

        try (Socket socket = new Socket(ip, port); // Establish connection
             OutputStream outputStream = socket.getOutputStream()) {

            // Send the message
            outputStream.write(message.getBytes());
            outputStream.flush();
            log.info("[tcp] {}:{} [{}]", ip, port, message);
        } catch (UnknownHostException e) {
            log.error("Unknown host: " + ip, e);
        } catch (IOException e) {
            log.error("Failed to send TCP message", e);
        }
    }

    @Async
    public void sendTcpMessage(String ip, int port, byte[] hexMessage) {
        // Validate inputs
        if (ip == null || ip.isEmpty()) {
            throw new IllegalArgumentException("IP address cannot be null or empty");
        }
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }
        if (hexMessage == null || hexMessage.length == 0) {
            throw new IllegalArgumentException("Hex message cannot be null or empty");
        }

        try (Socket socket = new Socket(ip, port); // Establish connection
             OutputStream outputStream = socket.getOutputStream()) {

            // Send the hex message
            outputStream.write(hexMessage);
            outputStream.flush();
            log.info("[tcp] {}:{} [hex][{}]", ip, port, NetUtil.bytesToHex(hexMessage));
        } catch (UnknownHostException e) {
            log.error("Unknown host: " + ip, e);
        } catch (IOException e) {
            log.error("Failed to send TCP message", e);
        }
    }

    @Async
    public void sendTcpMsgAll(List<PlcCmd> plcCmdList,String cmd) {
        for (PlcCmd plcCmd : plcCmdList) {
            sendTcpMessage(plcCmd,cmd);
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                log.error("sleep has been interrupted: {}",e.getMessage());
            }
        }
    }

    @Async
    public void sendTcpMessage(PlcCmd plcCmd, String cmd) {
        String ip;
        Integer port;
        Boolean isHex;

        if (cmd == null) {
            log.error("cmd cannot be null or empty");
            return;
        }

        List<String> msgList = "reversal".equals(cmd)
                ? plcCmd.getReversalCommand()

                : "poweron".equals(cmd)
                ? plcCmd.getPoweronCommand()

                : "poweroff".equals(cmd)
                ? plcCmd.getPoweroffCommand()
                : null;


        if (msgList == null) {
            try {
                List<PlcSubCmd> subCommandList = plcCmd.getSubCommandList();
                if(subCommandList == null) throw new UnresolvedCmdException();
                PlcSubCmd subCmd = subCommandList
                        .stream()
                        .filter(plcSubCmd -> cmd.equals(plcSubCmd.getName()))
                        .findFirst()
                        .orElseThrow(UnresolvedCmdException::new);
                ip=subCmd.getIp();
                port = subCmd.getPort();
                isHex = subCmd.getIsHex();

                msgList = subCmd.getCommand();
            } catch (UnresolvedCmdException e) {
                log.debug("{} has no {} command", plcCmd.getName(), cmd);
                return;
            }
        } else {
            ip = plcCmd.getIp();
            port = plcCmd.getPort();
            isHex = plcCmd.getIsHex();
        }

        // Validate inputs
        if (ip == null || ip.isEmpty()) {
            log.error("IP address cannot be null or empty");
            return;
        }
        if (port < 1 || port > 65535) {
            log.error("Port must be between 1 and 65535");
            return;
        }

        try (Socket socket = new Socket(ip, port); // Establish connection
             OutputStream outputStream = socket.getOutputStream()) {

            // Send message
            log.info("[tcp] {}:{} [{}]", ip, port, msgList);

            Function<String, byte[]> mapFn = isHex ? NetUtil::hexStringToByteArray : String::getBytes;
            msgList.stream()
                    .map(mapFn)
                    .forEach(byteMsg -> {
                        try {
                            log.info("write {}",new String(byteMsg));
                            outputStream.write(byteMsg);
                            outputStream.flush();
                            Thread.sleep(500);
                        } catch (IOException e) {
                            log.error("Failed to send TCP message", e);
                        } catch (InterruptedException e) {
                            log.error("sleep has been interrupted: {}",e.getMessage());
                        }
                    });

        } catch (UnknownHostException e) {
            log.error("Unknown host: " + ip, e);
        } catch (IOException e) {
            log.error("Failed to send TCP message", e);
        }
    }

}
