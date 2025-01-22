package com.fall.model.vo;

import lombok.Data;

import java.util.List;

/**
 * @author FAll
 * @date 2024年12月25日 11:03
 */
@Data
public class PlcCmd {
    private String name;
    private String ip;
    private Integer port;
    private List<String> poweronCommand;
    private List<String> poweroffCommand;
    private List<String> reversalCommand;
    private Boolean isHex = false;
    private String cType = "tcp";
    private List<PlcSubCmd> subCommandList;
}
