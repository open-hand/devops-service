# Choerodon DevOps Service
DevOps Service通过自主整合的DevOps工具链，集成相关的开源工具，以此形成了计划、编码、测试、部署、运维以及监控的DevOps闭环。
并且只需通过简单的配置，您便能获得最佳的开发体验。


## Add Helm chart repository

``` bash    
helm repo add choerodon https://openchart.choerodon.com.cn/choerodon/c7n
helm repo update
```

## Install the Chart

```bash
$ helm install c7n/devops-service --name devops-service
```

Specify each parameter using the `--set key=value[,key=value]` argument to `helm install`.

## Uninstall the Chart

```bash
$ helm delete devops-service
```

## Configuration
Parameter | Description	| Default
--- |  ---  |  ---  
replicaCount| ReplicaSet数量 | 1
image.repository| 镜像仓库地址 | registry.cn-shanghai.aliyuncs.com/c7n/devops-service
image.pullPolicy|镜像拉取策略 | IfNotPresent
preJob.timeout|初始化job超时时间 | 1200
preJob.image|初始化job镜像 | registry.cn-shanghai.aliyuncs.com/c7n/dbtool:0.7.1
preJob.preInitDB.enable|是否执行初始化数据库job|true
preJob.preInitDB.datasource.url|初始化数据库地址|jdbc:mysql://localhost:3306/devops_service?useUnicode=true&characterEncoding=utf-8&useSSL=false
preJob.preInitDB.datasource.username|初始化数据库用户名|username
preJob.preInitDB.datasource.password|初始化数据库密码|password
preJob.preInitDB.datasources.platform.url|多数据源初始化数据库地址|jdbc:mysql://localhost:3306/devops_service?useUnicode=true&characterEncoding=utf-8&useSSL=false
preJob.preInitDB.datasources.platform.username|多数据源初始化数据库用户名|username
preJob.preInitDB.datasources.platform.password|多数据源初始化数据库密码|password
deployment.managementPort|管理端口|8061
env.open.SPRING_REDIS_HOST| redis地址|devops-redis.devops.svc
env.open.EUREKA_CLIENT_SERVICEURL_DEFAULTZONE|注册服务地址|http://register-server:8000/eureka/
env.open.SPRING_DATASOURCE_URL|数据库链接地址|jdbc:mysql://mysql.db.svc:3306/devops_service?useUnicode=true&characterEncoding=utf-8&useSSL=false
env.open.SPRING_DATASOURCE_USERNAME|数据库用户名|root
env.open.SPRING_DATASOURCE_PASSWORD|数据库密码|choerodon
env.open.SPRING_CLOUD_CONFIG_ENABLED|启用配置中心|true
env.open.SPRING_CLOUD_CONFIG_URI|配置中心地址|http://config-server.framework:8010/
env.open.SERVICES_GITLAB_URL|gitlab地址|http://git.choerodon.com.cn
env.open.SERVICES_GITLAB_SSHURL|gitlab ssh地址|git@choerodon.com.cn
env.open.SERVICES_GITLAB_PROJECTLIMIT|gitlab用户可以创建项目限制|100
env.open.SERVICES_HELM_URL|helm地址|helm.example.com
env.open.SERVICES_HARBOR_BASEURL|harbor地址|https://registry.choerodon.com.cn
env.open.SERVICES_HARBOR_USERNAME|harbor用户名|admin
env.open.SERVICES_HARBOR_PASSWORD|harbor密码|
env.open.SERVICES_HARBOR_INSECURESKIPTLSVERIFY|harbor跳过证书安全校验|true
env.open.SERVICES_SONARQUBE_URL|sonarqube地址|
env.open.SERVICES_SONARQUBE_USERNAME|sonarqube用户名|
env.open.SERVICES_SONARQUBE_PASSWORD|sonarqube密码|
env.open.SERVICES_GATEWAY_URL|gateway地址|http://api.example.com
env.open.AGENT_VERSION|agent版本|0.5.0.RELEASE
env.open.AGENT_SERVICEURL|agent连接devops ws地址|ws://devops-service.choerodon.com.cn/agent/
env.open.AGENT_REPOURL|agent仓库地址|https://openchart.choerodon.com.cn/choerodon/c7n/
env.open.AGENT_CERTMANAGERURL|certmanager仓库地址|https://openchart.choerodon.com.cn/choerodon/infra/
env.open.SKYWALKING_OPTS | skywalking 代理端配置|
metrics.path|监控地址|/actuator/prometheus
metrics.group|监控组|spring-boot
log.parser|日志|spring-boot
service.enabled|是否创建service|false
service.name|service名字|devops-service
service.type|service类型|ClusterIP
service.port|service端口|8060
ingress.enabled|是否创建域名|false
ingress.host|域名地址|devops-service.choerodon.com.cn
resources.limits.memory|资源请求限制|4Gi
resources.requests.memory|资源请求需求|2Gi

##  SkyWalking Configuration
Parameter | Description
--- |  --- 
`javaagent` | SkyWalking 代理jar包(添加则开启 SkyWalking，删除则关闭)
`skywalking.agent.application_code` | SkyWalking 应用名称
`skywalking.agent.sample_n_per_3_secs` | SkyWalking 采样率配置
`skywalking.agent.namespace` | SkyWalking 跨进程链路中的header配置
`skywalking.agent.authentication` | SkyWalking 认证token配置
`skywalking.agent.span_limit_per_segment` | SkyWalking 每segment中的最大span数配置
`skywalking.agent.ignore_suffix` | SkyWalking 需要忽略的调用配置
`skywalking.agent.is_open_debugging_class` | SkyWalking 是否保存增强后的字节码文件
`skywalking.collector.backend_service` | SkyWalking OAP 服务地址和端口配置

```bash
$ helm install c7n/devops-service \
    --set env.open.SKYWALKING_OPTS="-javaagent:/agent/skywalking-agent.jar -Dskywalking.agent.application_code=devops-service  -Dskywalking.agent.sample_n_per_3_secs=-1 -Dskywalking.collector.backend_service=oap.skywalking:11800" \
    --name devops-service
```

## 验证部署
```bash
curl -s $(kubectl get po -n c7n-system -l choerodon.io/release=devops-service -o jsonpath="{.items[0].status.podIP}"):8061/actuator/health | jq -r .status
```
出现以下类似信息即为成功部署

```bash
UP
```