package com.alisonyu.airforce.cloud.core;

import org.junit.Test;

import static org.junit.Assert.*;

public class ServiceRecordSerializerTest {

	@Test
	public void serialize() {
		ServiceRecord record = new ServiceRecord();
		record.setHost("127.0.0.1");
		record.setPort(9090);
		byte[] bytes = ServiceRecordSerializer.serialize(record);
		ServiceRecord record1 = ServiceRecordSerializer.deserialize(bytes);
		assertEquals(record,record1);
	}

	@Test
	public void deserialize() {
	}
}