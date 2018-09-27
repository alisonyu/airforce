package com.alisonyu.airforce.cloud.core;

import io.vertx.core.json.JsonObject;

/**
 * 最简单的序列化表现形式
 * ServiceRecord <-> Json <-> byte[]
 * @author yuzhiyi
 * @date 2018/9/21 16:12
 */
public class ServiceRecordSerializer {

	public static byte[] serialize(ServiceRecord serviceRecord){
		return JsonObject.mapFrom(serviceRecord).toBuffer().getBytes();
	}

	public static ServiceRecord deserialize(byte[] bytes){
		String json = new String(bytes);
		return new JsonObject(json).mapTo(ServiceRecord.class);
	}


}
