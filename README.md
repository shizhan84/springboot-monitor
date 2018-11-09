Spring Boot Admin 2.0.X Example
========================
* 纯监控服务端例子,不涉及监控数据持久化存储
* 为了性能更快监控服务本身的日志也没有打印到文件中
* 增加了授权账号密码

sprintboot-demo-client 的配置文件

``` yml
server:
  port: 8800
  servlet:
    context-path: /demo-client
spring:
  security:
    user:
      name: client
      password: 123456
  boot:
    admin:
      client:
        url: http://monitor.yingyinglicai.top #can access the protected client endpoints
        username: server       #These two are needed so that the client
        password: 123456   #can register at the protected server api
        instance:
          metadata:
            user.name: ${spring.security.user.name}        #These two are needed so that the server
            user.password: ${spring.security.user.password}
          service-url: http://127.0.0.1:${server.port}${server.servlet.context-path}
```

 * Check the [reference documentation](http://codecentric.github.io/spring-boot-admin/current/).
