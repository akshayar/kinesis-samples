package com.aksh.kinesislambda.dto;

import lombok.Data;

@Data
public class TradeInfo {
    private long tradeId;
    private String symbol;
    private double quantity;
    private double price;
    private long timestamp;
    private String description;
    private String traderName;
    private String traderFirm;
    private String orderId;
    private String portfolioId;
    private String customerId;
    private boolean buy;
    private long orderTimestamp;
    private double currentPosition;
    private double buyPrice;
    private double sellPrice;
    private double profit;

}
