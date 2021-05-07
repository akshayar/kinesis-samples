/**
 * 
 */
package com.aksh.kinesislambda.dto;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author aksha
 *
 */
@Data
@DynamoDBTable(tableName = "override")
@NoArgsConstructor
public class Customer {
	@DynamoDBHashKey(attributeName = "id")
	private String id;
	private String name;
	private int age;
}
