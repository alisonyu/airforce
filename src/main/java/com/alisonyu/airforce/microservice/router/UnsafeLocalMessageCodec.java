package com.alisonyu.airforce.microservice.router;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

/**
 * 本地EventBus传递POJO
 * @author yuzhiyi
 * @date 2018/10/7 9:27
 */
public class UnsafeLocalMessageCodec<S> implements MessageCodec<S,S> {


	@Override
	public void encodeToWire(Buffer buffer, S s) {

	}

	@Override
	public S decodeFromWire(int pos, Buffer buffer) {
		return null;
	}

	/**
	 * 因为是本地传递，在这里不做拷贝保护，直接传递
	 */
	@Override
	public S transform(S s) {
		return s;
	}

	@Override
	public String name() {
		return UnsafeLocalMessageCodec.class.getName();
	}

	@Override
	public byte systemCodecID() {
		return -1;
	}
}
