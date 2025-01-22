package com.fall.client.tcp;

import com.fall.model.vo.PlcCmd;
import com.fall.model.vo.PlcSubCmd;
import com.fall.utils.NetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author FAll
 * @date 2024年12月12日 11:16
 */
@Slf4j
@SuppressWarnings("unused")
@Component
public class NetClient {
    @Async
    public void sendMsg(String ip, int port, String message) {
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
    public void sendMsg(String ip, int port, byte[] hexMessage) {
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
    public void sendTcpMsgAll(List<PlcCmd> plcCmdList, String cmd) {
        for (PlcCmd plcCmd : plcCmdList) {
            sendMsg(plcCmd, cmd);
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                log.error("sleep has been interrupted: {}", e.getMessage());
            }
        }
    }

    @Async
    public void sendMsg(PlcCmd plcCmd, String cmd) {

        Boolean isHex;
        String clientType;

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

        List<String> addressList = new ArrayList<>();
        if (msgList == null) {
            try {
                List<PlcSubCmd> subCommandList = plcCmd.getSubCommandList();
                if (subCommandList == null) throw new UnresolvedCmdException();
                PlcSubCmd subCmd = subCommandList
                        .stream()
                        .filter(plcSubCmd -> cmd.equals(plcSubCmd.getName()))
                        .findFirst()
                        .orElseThrow(UnresolvedCmdException::new);
                String ip = subCmd.getIp();
                Integer port = subCmd.getPort();
                isHex = subCmd.getIsHex();
                clientType = subCmd.getCType();


                if (NetUtil.validateAddress(ip, port)) {
                    addressList.add(ip + ":" + port);
                }

                List<String> address = subCmd.getAddress();
                if (address != null && NetUtil.validateAddress(address)) {
                    addressList.addAll(subCmd.getAddress());
                }

                if (addressList.isEmpty()) {
                    log.error("No Valid Address Found For {}", plcCmd.getName() + ":" + cmd);
                    return;
                }

                msgList = subCmd.getCommand();
            } catch (UnresolvedCmdException e) {
                log.debug("{} has no {} command", plcCmd.getName(), cmd);
                return;
            }
        } else {
            String ip = plcCmd.getIp();
            Integer port = plcCmd.getPort();
            isHex = plcCmd.getIsHex();
            clientType = plcCmd.getCType();

            if (!NetUtil.validateAddress(ip, port))
                return;

            addressList.add(ip + ":" + port);
        }

        log.info("Target Address: {}", addressList);
        for (String address : addressList) {
            String[] splitAddress = address.split(":");
            String socketIp = splitAddress[0];

            int socketPort = Integer.parseInt(splitAddress[1]);

            Function<String, byte[]> mapFn = isHex ? NetUtil::hexStringToByteArray : String::getBytes;

            // Send message
            log.info("[{}] {}:{} {}", clientType, socketIp, socketPort, msgList);

            if ("tcp".equals(clientType)) {
                tcpSendMsgList(socketIp, socketPort, msgList, mapFn);
            } else if ("udp".equals(clientType)) {
                udpSendMsgList(socketIp, socketPort, msgList, mapFn);
            } else {
                log.error("Unknown Client Type: {}", clientType);
            }
        }
    }


    private static void tcpSendMsgList(String socketIp, Integer socketPort,
                                       List<String> msgList, Function<String, byte[]> mapFn) {
        try (Socket socket = new Socket(socketIp, socketPort); // Establish connection
             OutputStream outputStream = socket.getOutputStream()) {

            msgList.stream()
                    .map(mapFn)
                    .forEach(byteMsg -> {
                        try {
                            log.info("write {}", new String(byteMsg));
                            outputStream.write(byteMsg);
                            outputStream.flush();
                            Thread.sleep(500);
                        } catch (IOException e) {
                            log.error("Failed to send TCP message", e);
                        } catch (InterruptedException e) {
                            log.error("sleep has been interrupted: {}", e.getMessage());
                        }
                    });

        } catch (UnknownHostException e) {
            log.error("Unknown host: " + socketIp, e);
        } catch (IOException e) {
            log.error("Failed to send TCP message", e);
        }
    }

    private static void udpSendMsgList(String socketIp, Integer socketPort,
                                       List<String> msgList, Function<String, byte[]> mapFn) {
        {
            DatagramSocket socket;
            try {
                socket = new DatagramSocket(50000);
            } catch (SocketException e) {
                log.error("Failed to send UDP message", e);
                return;
            }
            msgList.stream()
                    .map(mapFn)
                    .forEach(byteMsg -> {
                        log.info("write {}", new String(byteMsg));
                        // 构造数据报包，用来将长度为 length 的包发送到指定主机上的指定端口号。
                        DatagramPacket packet = null;
                        try {
                            packet = new DatagramPacket(byteMsg, byteMsg.length,
                                    InetAddress.getByName(socketIp), socketPort);
                        } catch (UnknownHostException e) {
                            log.error("Unknown Host");
                        }
                        // 从此套接字发送数据报包
                        try {
                            socket.send(packet);
                        } catch (IOException e) {
                            log.error("Failed to send UDP message", e);
                        }
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            log.error("sleep has been interrupted: {}", e.getMessage());
                        }
                    });
            // 关闭此数据报套接字。
            socket.close();
        }

    }
}
