# airforce
a hign performance microservice framework base on vert.x 

## 如何安装？

```shell
git clone https://github.com/alisonyu/airforce.git
mvn clean
mvn install
```

注：该项目还在Demo阶段，不推荐使用。

## Quick Start

在你的应用引入依赖

```xml
<dependency>
	<groupId>com.alisonyu</groupId>
    <artifactId>airforce</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```



在你的resource目录添加配置

airForceConfiguration.json

```json
{
    "server":{
        "port":9090
    }
}
```



建立一个启动类

```java
public class ServerBootstrap {

	public static void main(String[] args){
		AirForceApplication.run(ServerBootstrap.class,args);
	}
	
}
```



写一个接口

```java
public class HelloRestVerticle extends AbstractRestVerticle{
    
    @GET
    @Path("hello")
    public String hello(@QueryParam("name") String name){
        return "welcome to airForce world ! hello "+name;
    }
    
}
```



启动就完成了，你可以试一下使用Postman进行访问http://localhost:9090/hello?name="airforce"

进行访问



## 设计理念

1、本框架是基于Vert.x Web组件构建的Web框架，其中网络通讯交由Vert.x去处理，然后使用JSR-311注解的方式去进行开发，但是并不屏蔽Vert.x的实现，是为了RestVerticle之间还可以使用Vertx的性质，例如Eventbus和Verticle内部的线程安全性（Actor 模型）。

2、在提供便捷的开发同时，启动速度尽可能快速，同时占内存竟可能的少，在启动时间和内存占用将大幅度比SpringBoot Web框架少。

3、使用异步响应式开发，API接口的返回值支持Future，将会支持Rxjava。当然除了异步，也支持同步返回，可以使用@Sync修饰API接口方法，这个接口将会在Worker线程执行。

4、集成SpringBoot，用户可以选择启用Spring容器服务，启用Spring将可以对RestVerticle进行依赖注入。集成Spring是因为在现实生活中太多框架是集成Spring的，但是Spring的服务大多数都是同步，因此请将这些同步请求包装成异步。但是值得注意的是RestVerticle对象并不归Spring容器管理，只是单纯的对Spring容器的对象进行消费。



## 使用文档















