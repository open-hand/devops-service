English | [简体中文](./README.md)

# DevOps Service

`DevOps Service` is the continuous delivery service of Choerodon. Current version: `0.19.0`

DevOps Service integrated several open source tools to automate the process of `planning`, `coding`, `building`, `testing`, `deployment`, `operation` and `monitoring`.
 After a little simple configuration, you'll get the smoothest development experience.


## Feature
`DevOps Service` contains features as follows:    
- `Application Service Management`: Manage the application service
- `Application service version management`: Manage the versions released during the `Continuous Integration` process of the services, which can be deployed directly in the `Kubernetes` cluster
- `Code management & version control`: Manage the code of the application service with version control
- `Branch management`: Able to operate the git branches of the application service
- `Code Quality Analysis`：data of code quality are collected during `CI`, `SonarQube` is integrated.
- `Continuous Integration Overview`: Glance at the `Continuous Integration` process
- `Deployment Management`: Deploy the versions from `CI` by `GitOps`
- `Continuous Deployment Pipeline Management`: Achieve the `Continuous Deployment` by workflow
- `Resource Management`: Manage the resource(e.g., secret)
- `Cluster Management`: Manage the cluster of `Kubernetes`

## Requirements
- [JAVA](https://www.java.com/en/): `DevOps Service` is based on java8
- [GitLab](https://about.gitlab.com/): `GitLab` is used as code repository. At the same time, `Continuous Integration` based on `GitLab Runner` is used to complete code compilation, unit test execution, code quality analysis, docker image generation, helm chart packaging, service version releasing and other automated processes
- [Harbor](https://vmware.github.io/harbor/cn/): Enterprise Docker registry service for hosting the docker images for the service versions
- [Kubernetes](https://kubernetes.io/): Container orchestration management tool for deploying the helm chart packages corresponding to the service versions
- [ChartMuseum](https://chartmuseum.com/): Helm Chart Repository server., which is used to store the helm chart package corresponding to the service versions
- [Sonarqube](https://www.sonarqube.org/): SonarQube empowers all developers to write cleaner and safer code for application services
- [MySQL](https://www.mysql.com): one of the most popular relational databases, for data persistence of `DevOps Service`
- [Redis](https://redis.io/): In-memory database for data caching and partial non-persistent data storage

## Dependencies

* `go-register-server`: Register server, in place of `eureka-server`
* `iam-service`：Iam service
* `api-gateway`: Gateway service
* `oauth-server`: Oauth service
* `manager-service`: Manager service
* `asgard-service` : Transaction consistency service
* `notify-service` : Notify service
* `gitlab-service`：Service to communicate with gitlab
* `workflow-service`：Workflow service
* `agile-service`：Agile service

## Service Configuration

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
        maximum-pool-size: 15
    redis:
      host: localhost
    http:
      encoding:
        charset: UTF-8
        force: true
        enabled: true
  services:
    harbor:
      baseUrl: "harbor.example.com" # harbor url
      username: "123456" # harbor username
      password: "123456" # password corresponding to harbor user
      insecureSkipTlsVerify: false
    gitlab:
      url: "gitlab.example.com" # gitlab url
      sshUrl: "gitlab.example.com" # gitlab url for ssh operations
      password: 12345678 # default password for user created by gitlab, length no less than 8
      projectLimit: 100 # the limit of the project number that a user can create
    helm:
      url: "helm.example.com" # the repository url to place helm charts
    gateway:
      url: "http://api.example.com" # gateway url
  choerodon:
    saga:
      consumer:
        core-thread-num: 20
        max-thread-num:  20 # consumer thread number
        poll-interval: 3 # the interval for polling messages, default 1s
        enabled: true # whether to enable consumer client
    schedule:
      consumer:
        enabled: true # enable schedule consume
        thread-num: 1 # thread number for consuming
        poll-interval-ms: 1000 # the interval for polling messages, default 1000ms
  agent:
    version: "0.5.0" # expect choerodon-agent version for this devops-service version
    serviceUrl: "agent.example.com" # url for choerodon-agent to connect devops-service
    certManagerUrl: "agent.example.com" # the location to store the CertManager for installation
    repoUrl: "helm.example.com" # the location to store the agent package itself for installation
  eureka:
    instance:
      preferIpAddress: true
      leaseRenewalIntervalInSeconds: 1
      leaseExpirationDurationInSeconds: 3
    client:
      serviceUrl:
        defaultZone: http://localhost:8000/eureka/
      registryFetchIntervalSeconds: 1
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
  ```

## Installation and Getting Started
1. create database `devops_service`, create user `choerodon` and grant permission:

   ```sql
   CREATE USER 'choerodon'@'%' IDENTIFIED BY "choerodon";
   CREATE DATABASE devops_service DEFAULT CHARACTER SET utf8;
   GRANT ALL PRIVILEGES ON devops_service.* TO choerodon@'%';
   FLUSH PRIVILEGES;
   ```

2. pull source code of `DevOps Service`:

   ```sh
   git clone https://github.com/choerodon/devops-service.git
   ```

3. Execute command in the project root directory: `sh init-database.sh`

4. Run with the following commands or run `DevopsServiceApplication` directly in the integrated environment:

   ```sh
   mvn clean spring-boot:run
   ```

## Links

[ChangeLog](./CHANGELOG.en-US.md)

## Reporting Issues
If you find any shortcomings or bugs, please describe them in the  [issue](https://github.com/choerodon/devops-service/issues/new).

## How to Contribute
Pull requests are welcomed! [Follow](https://github.com/choerodon/choerodon/blob/master/CONTRIBUTING.md) to know for more information on how to contribute.
