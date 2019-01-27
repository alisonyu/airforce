package com.alisonyu.airforce.cluster.zookeeper;

import com.alisonyu.airforce.cluster.config.ZookeeperConfig;
import com.alisonyu.airforce.cluster.core.ServiceRecord;
import com.alisonyu.airforce.cluster.core.ServiceRecordSerializer;
import com.alisonyu.airforce.cluster.core.ServiceRegister;
import com.alisonyu.airforce.common.constant.Strings;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * ZooKeeperServiceRegister应该是一个单例，
 * @author yuzhiyi
 * @date 2018/9/21 15:07
 */
public class ZooKeeperServiceRegister implements ServiceRegister {

	private static Logger logger = LoggerFactory.getLogger(ZooKeeperServiceRegister.class);
	private static volatile ZooKeeperServiceRegister instance;

	private CuratorFramework client;
	private String namespace;
	private String seq;

	private ZooKeeperServiceRegister(CuratorFramework client){
		this.client = client;
	}

	public static ZooKeeperServiceRegister instance(CuratorFramework client,ZookeeperConfig config){
		if (instance==null){
			synchronized (ZooKeeperServiceRegister.class){
				if (instance == null){
					instance = new ZooKeeperServiceRegister(client);
					instance.setNamespace(config.getNamespace());
				}
			}
		}
		return instance;
	}

	@Override
	public void createService(String serviceName) {
		//todo 使用分段分布式锁来提高概率
		//为避免同时创建服务，我们先获取分布式锁，然后再进行操作
		logger.info("创建{}服务",serviceName);
		InterProcessMutex lock = new InterProcessMutex(client,Instant.REGISTER_LOCK_PATH);
		try {
			lock.acquire();
			if (Objects.isNull(client.checkExists().forPath(path(serviceName)))){
				client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path(serviceName,Instant.CONSUMERS));
				client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path(serviceName,Instant.PROVIDERS));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				lock.release();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void registerService(String serviceName, ServiceRecord record) {
		Objects.requireNonNull(serviceName,"服务名不能为空");
		Objects.requireNonNull(record,"ServiceRecord不能为空");
		//同步是为了不要重复注册
		synchronized (this){
			if (seq != null){
				return;
			}
			try {
				ensureServiceExist(serviceName);
				byte[] payload = ServiceRecordSerializer.serialize(record);
				seq = client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path(serviceName,Instant.PROVIDERS,Instant.SERVICE),payload);
				logger.info("service:{},host:{},port:{},registered in {}",new Object[]{serviceName,record.getHost(),record.getPort(),seq});
				logger.info("向zookeeper注册{}服务成功",serviceName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void unregisterService(String serviceName, ServiceRecord record) {
		synchronized (this){
			if (seq == null){
				return;
			}
			try {
				client.delete().forPath(seq);
				seq = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void deleteService(String serviceName) {
		try {
			client.delete().deletingChildrenIfNeeded().forPath(Strings.SLASH +serviceName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void ensureServiceExist(String serviceName) throws Exception {
		if (Objects.isNull((client.checkExists().forPath(path(serviceName))))){
			createService(serviceName);
		}
	}



	/**
	 *  检测格式，使得namespace是以/开头的
	 */
	private void setNamespace(String namespace){
		StringBuilder sb = new StringBuilder(namespace);
		if (!namespace.startsWith(Strings.SLASH)){
			sb.insert(0,Strings.SLASH);
		}
		if (namespace.endsWith(Strings.SLASH)){
			sb.substring(0,namespace.length()-1);
		}
		this.namespace = sb.toString();
	}

	private String path(String...ss){
		StringBuilder sb = new StringBuilder(namespace);
		for (String s : ss) {
			sb.append(Strings.SLASH);
			sb.append(s);
		}
		return sb.toString();
	}



}
