package com.fall.model.vo;

import lombok.Data;

import java.util.List;

/**
 * @author FAll
 * @date 2024年12月26日 10:55
 */
@Data
public class PlcSubCmd {
    private String name;
    private String ip;
    private Integer port;
    private List<String> address;
    private Boolean isHex = false;
    private String cType = "tcp";
    private List<String> command;
}
