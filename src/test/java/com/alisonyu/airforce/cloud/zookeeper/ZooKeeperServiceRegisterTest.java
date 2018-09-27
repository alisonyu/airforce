package com.alisonyu.airforce.cloud.zookeeper;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.junit.Test;

import static org.junit.Assert.*;

public class ZooKeeperServiceRegisterTest {

	@Test
	public void test() throws Exception {
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,3);
		CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181",retryPolicy);
		client.start();
		String seq = client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath("/test/123");
		System.out.println(seq);
	}

}