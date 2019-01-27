package com.alisonyu.airforce.common.tool;

/**
 * 用于计时
 * @author yuzhiyi
 * @date 2018/9/14 10:49
 */
public class TimeMeter {

	private long beginTime;
	private long endTime;

	public void start(){
		beginTime = System.currentTimeMillis();
	}

	public long end(){
		endTime = System.currentTimeMillis();
		long t = endTime - beginTime;
		beginTime = endTime;
		return t;
	}

}
