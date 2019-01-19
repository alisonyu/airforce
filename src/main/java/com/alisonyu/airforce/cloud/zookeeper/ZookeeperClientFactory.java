//package com.alisonyu.airforce.cloud.zookeeper;
//
//import com.alisonyu.airforce.cloud.config.ZookeeperConfig;
//import org.apache.curator.RetryPolicy;
//import org.apache.curator.framework.CuratorFramework;
//import org.apache.curator.framework.CuratorFrameworkFactory;
//import org.apache.curator.framework.imps.CuratorFrameworkState;
//import org.apache.curator.retry.RetryForever;
//
//import java.util.Arrays;
//import java.util.Objects;
//
///**
// * 构建ZookeeperClient，ZooKeeperClient应该缓存
// * @author yuzhiyi
// * @date 2018/9/21 10:54
// */
//public class ZookeeperClientFactory {
//
//	private static volatile ZookeeperConfig config;
//	private static volatile CuratorFramework client;
//
//	public static CuratorFramework create(ZookeeperConfig config){
//		Objects.requireNonNull(config,"Zookeeper配置不能为空");
//		if (ZookeeperClientFactory.config != config || client==null){
//			synchronized (ZookeeperClientFactory.class){
//				ZookeeperClientFactory.config = config;
//				String connectString = buildConnectString(config.getServers());
//				client = CuratorFrameworkFactory.builder()
//						.connectString(connectString)
//						.connectionTimeoutMs(config.getConnectionTimeoutMs())
//						.sessionTimeoutMs(config.getSessionTimeoutMs())
//						.retryPolicy(buildRetryPolicy(config.getRetryIntervalSleepTime()))
//						.build();
//			}
//		}
//		if (client.getState() == CuratorFrameworkState.LATENT){
//			client.start();
//		}
//		return client;
//	}
//
//	private static String buildConnectString(String[] servers){
//		Objects.requireNonNull(servers,"没有指定zookeeper的地址");
//		StringBuilder sb = new StringBuilder();
//		for (String server : servers) {
//			sb.append(server).append(',');
//		}
//		return sb.substring(0,sb.length()-1);
//	}
//
//	private static RetryPolicy buildRetryPolicy(int retryInterval){
//		return new RetryForever(retryInterval);
//	}
//
//}
