# airforce
### 解决什么问题？

* 希望使用Vertx开发项目，但是希望使用类似Spring Boot的注解式的方式来组织路由
* 利用Vertx Eventbus，使用类似Dubbo的风格发布和消费服务
* 希望在Vertx 项目中引入Spring，使用Spring Boot 封装好的组件



### 如何安装？

```shell
git clone https://github.com/alisonyu/airforce.git
mvn clean
mvn install
```



### Quick Start

##### 在你的应用引入依赖

```xml
<dependency>
	<groupId>com.alisonyu</groupId>
    <artifactId>airforce</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

##### 编写一个Web接口

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
    
    @GET
    @Path("/blockingHello")
    @Sync
    public String blockingHello(@QueryParam("name")String name){
        return "hello "+name;
    }
    
}
```



**在resources下编写配置文件 airforce.properties**

```properties
app.name=airforce
server.port=9099
```



**编写并启动Main函数**

```java
public class DemoApplication {

    public static void main(String[] args) {
        AirForceContext context = AirForceContextBuilder.create()
                                    .args(args)
                                    .emberHttpServer(true)
                                    .init();
        context.deployVerticle(()->new DemoRestVerticle(),new DeploymentOptions(),null);
    }
}
```



**测试**

```shell
curl localhost:9099/hello?name=airforce
curl localhost:9099/echo?content=airforce
```



##### 发布和消费服务

**发布服务**

底层是异步执行的，请使用Flowable\<T>或者Future\<T>来表示异步执行的结果

使用@ServiceProvider来标识这个类是一个服务类

返回类型可以是基础类型或者用户自定义的类，在这里使用了FST进行序列化和反序列化

```java
@ServiceProvider
public class HelloServiceVerticle extends AirForceVerticle implements HelloService{

    @Override
    public Flowable<String> hello(String content) {
        return Flowable.just("hello "+content);
    }

    @Override
    public Future<String> echo(String content) {
        Future<String> f = Future.future();
        f.complete(content);
        return f;
    }
    
}
```



**消费服务**

使用ConsumerProvider#getConsumer来获取服务代理实例，然后就可以透明化的调用了

消费服务底层是使用Vertx EventBus去实现的

如果是服务的方法返回值是Flowable\<T>形式的，不要使用任何blocking的API,例如blockingFirst，这会将EventLoop的线程阻塞

```java
public class ConsumerDemo extends AirForceVerticle {

    private HelloService helloService;

    @Override
    public void start() throws Exception {
        super.start();
        this.helloService = ConsumerProvider.getConsumer(vertx,HelloService.class);
    }

    @GET
    @Path("hello/hello")
    public Flowable<String> hello(@QueryParam("content")String content){
        return helloService.hello(content);
    }

    @GET
    @Path("hello/echo")
    public Future<String> echo(@QueryParam("content")String content){
        return helloService.echo(content);
    }
    
}
```



**编写并启动main函数**

```java
public class DemoApplication {

    private static Logger logger = LoggerFactory.getLogger(DemoApplication.class);

    public static void main(String[] args) {
        AirForceContext context = AirForceContextBuilder.create()
                                    .args(args)
                                    .emberHttpServer(true)
                                    .init();
        
        context.deployVerticle(()->new HelloServiceVerticle(),new DeploymentOptions().setInstances(4),as ->{
            if (as.succeeded()){
                logger.info("helloService deploy successfully!");
            }else{
                logger.error("helloService deploy error!",as.cause());
            }
        } );
        context.deployVerticle(()->new ConsumerDemo(),new DeploymentOptions(),as -> {
            if (as.succeeded()){
                logger.info("consumer demo depploy successfully!");
            }else{
                logger.error("consume demo deploy error!",as.cause());
            }
        });
    }
```





