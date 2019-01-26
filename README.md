# airforce
### 如何安装？

```shell
git clone https://github.com/alisonyu/airforce.git
mvn clean
mvn install
```



### Quick Start

在你的应用引入依赖

```xml
<dependency>
	<groupId>com.alisonyu</groupId>
    <artifactId>airforce</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

编写一个Web接口

```java
public class DemoRestVerticle extends AirForceVerticle {

    @GET
    @Path("/hello")
    public Flowable<String> hello(@QueryParam("name")String name){
        return Flowable.just("hello "+name);
    }

    @GET
    @Path("/echo")
    public Future<String> echo(@QueryParam("content")String content){
        Future<String> future = Future.future();
        future.complete(content);
        return future;
    }

}
```



在resources下编写配置文件 airforce.properties

```properties
app.name=airforce
server.port=9099
```



编写并启动Main函数

```java
public class DemoApplication {

    public static void main(String[] args) {
        AirForceBuilder.build()
                .airforceVerticles(Sets.newHashSet(DemoRestVerticle.class))
                .run(DemoApplication.class,args);
    }
}
```



测试

```shell
curl localhost:9099/hello?name=airforce
curl localhost:9099/echo?content=airforce
```





### AirForceVerticle

继承AbstractVerticle，提供Rest接口和异步服务的能力



### 接口风格

Vertx是一个全异步的网络框架，因此无论是服务接口还是Rest接口，需要使用返回Flowable 或者是 Future（Vertx）来表示你的返回结果



### Web

基于Vertx Web进行封装，使用vertx web来进行路由管理，最后的处理是通过EventBus本地分派到对应的Verticle中执行。

Web接口的注解配置基于JSR311风格











