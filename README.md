简体中文 | [English](./README.en_US.md)
 
# DevOps Service  

`DevOps Service` DevOps Service是Choerodon平台实现持续交付的基础. 当前版本为: `2.2.0-beta`

DevOps Service通过自主整合的DevOps工具链，集成相关的开源工具，以此形成了计划、编码、测试、部署、运维以及监控的DevOps闭环。 并且只需通过简单的配置，您便能获得最佳的开发体验。

> 注意：原本的前端代码已经移动到[这里](https://github.com/open-hand/choerodon-front-devops)

## 特性
`DevOps Service` 含有以下功能:    

- `应用服务管理` ：对应用服务进行管理
- `应用服务版本管理`：对`持续集成`（Continuous Integration）过程中产生的可以直接在`Kubernetes`集群中进行部署的服务版本进行管理
- `代码管理及版本控制`：对服务的代码进行版本控制和管理
- `分支管理`：能够对服务的`Git`分支进行相应的操作
- `代码质量监测`：在`CI`过程中进行代码质量数据收集，集成`sonarqube`对代码质量进行监测
- `持续集成概览`：查看服务的持续集成过程
- `部署管理`：对持续集成所产生的服务版本通过`GitOps`进行部署
- `持续部署流水线管理`：使用工作流实现持续部署
- `资源管理`：对部署的资源（如：网络，域名，密文等）进行管理
- `集群管理`：管理`Kubernetes`集群

## 前置要求
- [JAVA](https://www.java.com/en/)：`DevOps Service`基于Java8进行开发
- [GitLab](https://about.gitlab.com/)：`DevOps Service`使用`GitLab`进行代码的托管。同时，通过基于`GitLab Runner`实现持续集成以完成代码编译，单元测试执行，代码质量分析，docker镜像生成，helm chart打包，服务版本发布等自动化过程
- [Harbor](https://vmware.github.io/harbor/cn/)：企业级Docker registry 服务，用于存放服务版本所对应的docker镜像
- [Kubernetes](https://kubernetes.io/)：容器编排管理工具，用于部署服务版本所对应的helm chart包
- [ChartMuseum](https://chartmuseum.com/)：Helm Chart仓库，用于存放服务版本所对应的helm chart包
- [Sonarqube](https://www.sonarqube.org/)：管理代码质量的开放平台，用于管理服务的代码质量
- [MySQL](https://www.mysql.com)：主流数据库之一，用于`DevOps Service`的数据持久化
- [Redis](https://redis.io/)：内存数据库，用于数据缓存和部分非持久化数据存储

## 服务依赖

* `choerodon-register`: 注册中心，在线上环境代替本地的`eureka-server`
* `choerodon-iam`：用户服务，与用户有关的操作依赖与此服务
* `choerodon-gateway`: 网关服务
* `choerodon-oauth`: 授权服务
* `choerodon-asgard` : 事务一致性服务
* `choerodon-message` : 通知服务
* `gitlab-service`：gitlab服务
* `workflow-service`：工作流服务
* `agile-service`：敏捷服务，查询与分支有关的敏捷Issue需要依赖此服务

## 服务配置

* `bootstrap.yml`:

  ```yaml
  server:
    port: 8060
  spring:
    application:
      name: devops-service
    cloud:
      config:
        failFast: true
        retry:
          maxAttempts: 6
          multiplier: 1.5
          maxInterval: 2000
        uri: localhost:8010
        enabled: false
    mvc:
      static-path-pattern: /**
    resources:
      static-locations: classpath:/static,classpath:/public,classpath:/resources,classpath:/META-INF/resources,file:/dist
  management:
    server:
      port: 8061
    endpoints:
      web:
        exposure:
          include: '*'
  ```

* `application.yml`:

  ```yaml
  spring:
    datasource:
      url: jdbc:mysql://localhost/devops_service?useUnicode=true&characterEncoding=utf-8&useSSL=false
      username: choerodon
      password: choerodon
      hikari:
        maximum-pool-size: 15 # 数据库连接池连接数
    redis:
      host: localhost
      database: ${SPRING_REDIS_DATABASE:1}
    http:
      encoding:
        charset: UTF-8
        force: true
        enabled: true
  services:
    harbor:
      baseUrl: "harbor.example.com" # harbor地址
      username: "123456" # harbor用户名
      password: "123456" # 对应harbor用户名的密码
      insecureSkipTlsVerify: false
    gitlab:
      url: "gitlab.example.com" # gitlab地址
      sshUrl: "gitlab.example.com" # 用于ssh操作的gitlab地址
      projectLimit: 100 # gitlab用户可以创建的项目的数量限制
    helm:
      url: "helm.example.com" # 存放helm chart包的仓库地址
    gateway:
      url: "http://api.example.com" # 网关地址
  hzero:
    service:
      platform:
        name: zknow-platform
      oauth:
        name: zknow-oauth
      iam:
        name: zknow-iam
      file:
        name: zknow-file
      message:
        name: zknow-message
      admin:
        name: zknow-admin
      swagger:
        name: zknow-swagger
      gateway:
        name: zknow-gateway
      monitor:
        name: zknow-monitor
    websocket:
      # 用于连接websocket的路径
      websocket: /websocket
      # 与当前服务的redis数据库一致
      redisDb: ${SPRING_REDIS_DATABASE:1}
      # 后端长连通信密钥
      secretKey: devops_ws
  choerodon:
    saga:
      consumer:
        core-thread-num: 20
        max-thread-num:  20 # 消费线程数
        poll-interval: 3 # 拉取消息的间隔(秒)，默认1秒
        enabled: true # 是否启用消费端
    schedule:
      consumer:
        enabled: true # 启用任务调度消费端
        thread-num: 1 # 任务调度消费线程数
        poll-interval-ms: 1000 # 拉取间隔，默认1000毫秒
    resource:
      jwt:
        ignore: /workflow/**, /sonar/**, /ci, /sonar/info, /v2/api-docs, /agent/**, /ws/**, /gitlab/email, /webhook/**, /v2/choerodon/**, /choerodon/**, /actuator/**, /prometheus, /devops/**, /pre_stop, /websocket
  agent:
    version: "0.5.0" # devops-service此版本所预期的 choerodon-agent 的版本
    serviceUrl: "agent.example.com" # 用于 choerodon-agent 连接 devops-service 的地址
    certManagerUrl: "agent.example.com" # 存放CertManager的地址，用于安装
    repoUrl: "helm.example.com" # 存放agent的地址，用于安装
  mybatis:
    mapperLocations: classpath*:/mapper/*.xml
    configuration:
      mapUnderscoreToCamelCase: true
  feign:
    hystrix:
      shareSecurityContext: true
      command:
        default:
          execution:
            isolation:
              thread:
                timeoutInMilliseconds: 30000
  ribbon:
    ConnectTimeout: 50000
    ReadTimeout: 50000

  asgard-servie:
    ribbon:
      ConnectTimeout: 50000
      ReadTimeout: 50000

  devops:
    ansible:
      image: registry.cn-shanghai.aliyuncs.com/c7n/kubeadm-ha:0.1.0
    # helm 下载地址
    helm:
      download-url: https://file.choerodon.com.cn/kubernetes-helm/v3.2.4/helm-v3.2.4-linux-amd64.tar.gz
    # 流水线生成Gitlab Ci文件中默认的runner 镜像地址
    ci:
      default:
        image: registry.cn-shanghai.aliyuncs.com/c7n/cibase:0.9.1
      pipeline:
        sync:
          executor:
            # 核心线程池大小
            corePoolSize: 5
            # 最大线程池大小
            maxPoolSize: 8
          unterminated:
            # ci流水线对未终结的流水线进行数据补偿的时间阈值, 单位: 毫秒 (默认600秒)
            thresholdMilliSeconds: 600000
          pending:
            # ci流水线对pending的流水线进行数据补偿的时间阈值, 单位: 毫秒 (默认600秒)
            thresholdMilliSeconds: 600000
          jobEmpty:
            # ci流水线对非跳过状态的且没有job信息流水线进行数据补偿的时间阈值, 单位: 毫秒 (默认600秒)
            thresholdMilliSeconds: 600000
          refresh:
            # redisKey的过期时间, 用于控制同一条流水线的刷新间隔, 减少对gitlab的访问次数
            periodSeconds: 60
    # 批量部署的请求条数限制
    batch:
      deployment:
        maxSize: 20

  # websocket的最大缓冲区大小，单位字节byte
  websocket:
    buffer:
      maxTextMessageSize: 4194304
      maxBinaryMessageSize: 4194304
  ```


## 安装和启动步骤

1. 创建数据库`devops_service`，创建用户`choerodon`，并为用户分配权限：

   ```sql
   CREATE USER 'choerodon'@'%' IDENTIFIED BY "choerodon";
   CREATE DATABASE devops_service DEFAULT CHARACTER SET utf8;
   GRANT ALL PRIVILEGES ON devops_service.* TO choerodon@'%';
   FLUSH PRIVILEGES;
   ```

2. 拉取`DevOps Service`代码到本地：

   ```sh
   git clone https://github.com/choerodon/devops-service.git
   ```

3. 在项目根目录执行命令： `mvn clean package spring-boot:repackage -Dmaven.test.skip=true && bash init-database.sh`

4. 使用下列命令运行或直接在集成环境中运行 `DevopsServiceApplication` 

   ```sh
   mvn clean spring-boot:run
   ```

## 链接

[更新日志](./CHANGELOG.zh-CN.md)

## 反馈途径

如果您发现任何缺陷或bug，请及时 [issue](https://github.com/choerodon/devops-service/issues/new)告知我们 。

## 如何参与

欢迎参与我们的项目，了解更多有关如何[参与贡献](https://github.com/choerodon/choerodon/blob/master/CONTRIBUTING.md)的信息。 

