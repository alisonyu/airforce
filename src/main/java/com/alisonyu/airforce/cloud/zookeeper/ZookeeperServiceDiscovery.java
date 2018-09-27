package com.alisonyu.airforce.cloud.zookeeper;

import com.alisonyu.airforce.cloud.config.ZookeeperConfig;
import com.alisonyu.airforce.cloud.core.ServiceDiscovery;
import com.alisonyu.airforce.cloud.core.ServiceRecord;
import com.alisonyu.airforce.cloud.core.ServiceRecordSerializer;
import com.alisonyu.airforce.configuration.AirForceEnv;
import com.alisonyu.airforce.constant.Strings;
import io.vertx.core.impl.ConcurrentHashSet;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 1、订阅服务，注册watcher在对应的路径上
 * 2、获取对应服务，然后缓存在本地
 * 3、在consumers中注册自身
 * @author yuzhiyi
 * @date 2018/9/22 20:33
 */
public class ZookeeperServiceDiscovery implements ServiceDiscovery{

	private static final Logger logger = LoggerFactory.getLogger(ZookeeperServiceDiscovery.class);

	private static final List<ServiceRecord> NULL_LIST = Collections.unmodifiableList(new LinkedList<>());

	private static volatile ZookeeperServiceDiscovery instance;

	private ConcurrentHashSet<String> subscribeServiceSet = new ConcurrentHashSet<>();

	private ConcurrentHashMap<String,CopyOnWriteArrayList<ZookeeperServiceRecord>> services = new ConcurrentHashMap<>();

	private ConcurrentHashMap<String,TreeCache> listenerMap = new ConcurrentHashMap<>();

	private CuratorFramework client;

	private String namespace;



	private ZookeeperServiceDiscovery(CuratorFramework client){
		this.client = client;
	}

	public static ZookeeperServiceDiscovery instance(CuratorFramework client,ZookeeperConfig config){
		if (instance == null){
			synchronized (ZookeeperServiceDiscovery.class){
				if (instance == null){
					instance = new ZookeeperServiceDiscovery(client);
					instance.setNamespace(config.getNamespace());
				}
			}
		}
		return instance;
	}


	@Override
	public List<? extends ServiceRecord> getService(String serviceName) {
		return services.containsKey(serviceName) ? Collections.unmodifiableList(services.get(serviceName)) : NULL_LIST;
	}

	@Override
	public void subscribeService(String serviceName) {
		Objects.requireNonNull(serviceName,"订阅的服务名不可为空");
		if (!subscribeServiceSet.contains(serviceName)){
			subscribeServiceSet.add(serviceName);
			//1、监听服务提供者所在路径并缓存数据
			listenProvider(serviceName);
			//2、将自身注册在消费者路径上
			registerToConsumer(serviceName);
		}
	}

	private void listenProvider(String serviceName){
		final String providersPath = path(serviceName,Instant.PROVIDERS);
		TreeCache treeCache = new TreeCache(client,providersPath);
		treeCache.getListenable().addListener(new SubscribeListener(serviceName,providersPath));
		try {
			treeCache.start();
			listenerMap.put(serviceName,treeCache);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void serviceAdded(String serviceName,String path,ServiceRecord record){
		services.compute(serviceName,(key,value)->{
			CopyOnWriteArrayList<ZookeeperServiceRecord> list = value;
			if (list == null){
				list = new CopyOnWriteArrayList<>();
			}
			ZookeeperServiceRecord zookeeperServiceRecord = new ZookeeperServiceRecord(record,path);
			binaryInsert(list,zookeeperServiceRecord);
			return list;
		});
		logger.info("discovery {} service at {}:{}",new Object[]{serviceName,record.getHost(),record.getPort()});
	}

	private void serviceRemoved(String serviceName,String path,ServiceRecord record){
		services.compute(serviceName,(key,value)->{
			CopyOnWriteArrayList<ZookeeperServiceRecord> list = value;
			if (list == null){
				logger.info("尝试移除一个未订阅的服务{}",serviceName);
			}
			else{
				ZookeeperServiceRecord zookeeperServiceRecord = new ZookeeperServiceRecord(record,path);
				binaryRemove(list,zookeeperServiceRecord);
				logger.info("remove {} service at {}:{}",new Object[]{serviceName,record.getHost(),record.getPort()});
			}
			return list;
		});
	}

	private void serviceUpdated(String serviceName,String path,ServiceRecord record){
		services.compute(serviceName,(key,value)->{
			CopyOnWriteArrayList<ZookeeperServiceRecord> list = value;
			if (list == null){
				list = new CopyOnWriteArrayList<>();
			}
			ZookeeperServiceRecord zookeeperServiceRecord = new ZookeeperServiceRecord(record,path);
			binaryInsert(list,zookeeperServiceRecord);
			return list;
		});
		logger.info("update {} service at {}:{}",new Object[]{serviceName,record.getHost(),record.getPort()});
	}

	/**
	 * 根据zookeeper节点后缀递增的特性可以使用二分来进行有序插入，以及使用二分来进行查询
	 * 这样时间复杂度为O(logN)
	 */
	private void binaryInsert(List<ZookeeperServiceRecord> list,ZookeeperServiceRecord record){
		int idx = Collections.binarySearch(list,record,ZOOKEEPER_SERVICE_RECORD_COMPARATOR);
		if (idx < 0){
			idx = -idx - 1;
			list.add(idx,record);
		}else{
			//如果idx大于0，说明该Record是原来就有的，那么我们可以进行覆盖操作
			list.set(idx,record);
		}
	}

	private void binaryRemove(List<ZookeeperServiceRecord> list,ZookeeperServiceRecord record){
		int idx = Collections.binarySearch(list,record,ZOOKEEPER_SERVICE_RECORD_COMPARATOR);
		if (idx >= 0){
			list.remove(idx);
		}
	}


	private void registerToConsumer(String serviceName){
		String path = path(serviceName,Instant.CONSUMERS,Instant.SUBSCRIBER);
		ServiceRecord record = ServiceRecord.local();
		try {
			client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path,ServiceRecordSerializer.serialize(record));
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("注册 {} 失败",path);
		}

	}



	@Override
	public void unsubscribeService(String serviceName) {
		//1、取消监听器
		TreeCache treeCache = listenerMap.get(serviceName);
		if (treeCache!=null){
			treeCache.close();
		}
		//2、从订阅集合中移除
		subscribeServiceSet.remove(serviceName);
		//3、移除对应服务信息缓存
		services.remove(serviceName);
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

	class SubscribeListener implements TreeCacheListener{

		private final String providersPath;
		private final String serviceName;

		SubscribeListener(String serviceName,String providerPath){
			this.providersPath = providerPath;
			this.serviceName = serviceName;
		}

		@Override
		public void childEvent(CuratorFramework client, TreeCacheEvent event){
			ChildData data = event.getData();
			if (data != null){
				final String path = data.getPath();
				//根节点不考虑，只考虑子节点
				if (path.equals(providersPath)){
					return;
				}
				final ServiceRecord serviceRecord = ServiceRecordSerializer.deserialize(data.getData());
				//todo 考虑断线的情况？
				switch (event.getType()){
					case NODE_ADDED:
						serviceAdded(serviceName,path,serviceRecord);
						break;
					case NODE_REMOVED:
						serviceRemoved(serviceName,path,serviceRecord);
						break;
					case NODE_UPDATED:
						serviceUpdated(serviceName,path,serviceRecord);
						break;
					default:
						break;
				}
			}
		}
	}

	private static final Comparator<ZookeeperServiceRecord> ZOOKEEPER_SERVICE_RECORD_COMPARATOR = Comparator.comparing(ZookeeperServiceRecord::getzPath);

	class ZookeeperServiceRecord extends ServiceRecord{
		private String zPath;

		ZookeeperServiceRecord(ServiceRecord record,String zPath){
			setHost(record.getHost());
			setPort(record.getPort());
			setzPath(zPath);
		}

		String getzPath() {
			return zPath;
		}

		void setzPath(String zPath) {
			this.zPath = zPath;
		}

		@Override
		public String toString() {
			return "ZookeeperServiceRecord{" +
					"zPath='" + zPath + '\'' +
					", host='" + host + '\'' +
					", port=" + port +
					'}';
		}
	}


}
