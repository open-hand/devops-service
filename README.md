# DevOps Service
`DevOps Service` is the core service of Choerodon.
Integrated several open source tools to automate the process of planning, coding, building, testing, and deployment, operation, monitoring. After a little simple configuration, then you'll get the most smoothest development experience.

## Feature
- **Application Management**
- **Version Control** (Using Gitflow Workflow)
- **Application Version Management**
- **CI/CD Dashboard**
- **Deploy Management**
- **Ingress Management**

## Requirements
- Java8
- [GitLab Service](GitlabServiceLink)
- [Iam Service](IamServiceLink)
- [Harbor](HarborLink)
- [kubenetes](K8sLink)

## To get the code

```
git clone
```
## Installation and Getting Started
```bash
mvn clean spring-boot:run
```

## Dependencies
- commons-io:commons-io
- com.alibaba:fastjson
- com.fasterxml.jackson.dateformat:jackson-dataformat-yaml
- com.squareup.retrofit2:converter-jackson
- com.squareup.retrofit2:retrofit
- junit:junit
- mysql:mysql-connector-java
- io.choerodon:choerodon-socket-helper
- io.choerodon:choerodon-starter-bus
- io.choerodon:choerodon-starter-core
- io.choerodon:choerodon-starter-event-consumer
- io.choerodon:choerodon-starter-event-producer
- io.choerodon:choerodon-starter-feign-replay
- io.choerodon:choerodon-starter-hitoa
- io.choerodon:choerodon-starter-mybatis-mapper
- io.choerodon:choerodon-starter-oauth-resource
- io.choerodon:choerodon-starter-swagger
- io.kubernetes:client-java
- org.apache.commons:commons-compress
- org.eclipse.jgit:org.eclipse.jgit
- org.springframework.boot:spring-boot-configuration-processor
- org.springframework.boot:spring-boot-starter-actuator
- org.springframework.boot:spring-boot-starter-aop
- org.springframework.boot:spring-boot-starter-data-redis
- org.springframework.boot:spring-boot-starter-undertow
- org.springframework.boot:spring-boot-starter-web
- org.springframework.boot:spring-boot-starter-websocket
- org.springframework.cloud:spring-cloud-config-client
- org.springframework.cloud:spring-cloud-netflix-hystrix-stream
- org.springframework.cloud:spring-cloud-starter-bus-kafka
- org.springframework.cloud:spring-cloud-starter-eureka
- org.springframework.cloud:spring-cloud-starter-zuul
- org.springframework.cloud:spring-cloud-sleuth-stream
- org.springframework.cloud:spring-cloud-stream-binder-kafka
- org.springframework.retry:spring-retry