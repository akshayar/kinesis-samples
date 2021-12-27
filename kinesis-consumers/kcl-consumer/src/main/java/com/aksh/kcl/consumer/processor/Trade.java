package com.aksh.kcl.consumer.processor;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import lombok.Data;

@Data
@DynamoDBTable(tableName = "trade-info-new")
public class Trade {
	@DynamoDBHashKey(attributeName = "tradeId")
	private String tradeId;
	private String symbol;
	private double quantity;
	private double price;
	private long timestamp;
	private String description;
	private String traderName;
	private String traderFirm;
}
