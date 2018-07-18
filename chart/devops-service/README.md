# Quick start

部署文件的渲染模板，我们下文将定义一些变量，helm执行时会将变量渲染进模板文件中。

## _helpers.tpl

这个文件我们用来进行标签模板的定义，以便在上文提到的位置进行标签渲染。

标签总共分为三个部分: 平台、微服务、监控。

### 平台标签

#### deployment 级:

```
{{- define "service.labels.standard" -}}
choerodon.io/release: {{ .Release.Name | quote }}
{{- end -}}
```
平台管理实例需要的实例ID。

### 微服务标签

#### pod 级:

```
{{- define "service.microservice.labels" -}}
choerodon.io/version: {{ .Chart.Version | quote }}
choerodon.io/service: {{ .Chart.Name | quote }}
choerodon.io/metrics-port: {{ .Values.deployment.managementPort | quote }}
{{- end -}}
```
微服务注册中心进行识别时所需要的版本号、项目名称、管理端口。

### 监控和日志标签

#### deployment 级:

```
{{- define "service.logging.deployment.label" -}}
choerodon.io/logs-parser: {{ .Values.logs.parser | quote }}
{{- end -}}
```
日志管理所需要的应用标签。该标签指定应用程序的日志格式，内置格式有`nginx`,`spring-boot`,`docker`对于spring-boot微服务请使用`spring-boot`，如果不需要收集日志请移除此段代码，并删除模板文件关于`service.logging.deployment.label`的引用。

#### pod 级:

```
{{- define "service.monitoring.pod.annotations" -}}
choerodon.io/metrics-group: {{ .Values.metrics.group | quote }}
choerodon.io/metrics-path: {{ .Values.metrics.path | quote }}
{{- end -}}
```
性能指标管理所需要的应用类别以及监控指标路径。其中`metrics-group`将应用按照某个关键字分组，并在grafana配置实现分组展示。`metrics-path`指定收集应用的指标数据路径。
如果不需要监控请移除此段代码

## values.yaml

这个文件中的键值对，即为我们上文中所引用的变量。

将所以有变量集中在一个文件中，方便部署的时候进行归档以及灵活替换。

同时，helm命令支持使用 `--set FOO_BAR=FOOBAR` 参数对values 文件中的变量进行赋值，可以进一步简化部署流程。


## 参数对照表

参数名 | 含义 
--- |  --- 
service.enable|是否创建service
preJob.preConfig.mysql{}|初始化配置所需manager_service数据库信息
preJob.preInitDB.mysql{}|初始化数据库所需数据库信息
env.open.SPRING_DATASOURCE_URL|数据库链接地址
env.open.SPRING_DATASOURCE_USERNAME|数据库用户名
env.open.SPRING_DATASOURCE_PASSWORD|数据库密码
env.open.SPRING_CLOUD_CONFIG_ENABLED|启用配置中心
env.open.SPRING_CLOUD_CONFIG_URI|配置中心地址
env.open.EUREKA_CLIENT_SERVICEURL_DEFAULTZONE|注册服务地址
env.open.SPRING_CLOUD_STREAM_KAFKA_BINDER_BROKERS|kafka地址
env.open.SPRING_CLOUD_STREAM_KAFKA_BINDER_ZK_NODES|zookeeper地址
env.open.SERVICES_GITLAB_URL|gitlab地址
env.open.SPRING_REDIS_HOST|redis地址
env.open.CHOERODON_EVENT_CONSUMER_KAFKA_BOOTSTRAP_SERVERS|kafka bootstrap 地址
env.open.SERVICES_GITLAB_URL|gitlab 地址
env.open.SERVICES_GITLAB_PASSWORD|gitlab默认创建用户密码
env.open.SERVICES_GITLAB_PROJECTLIMIT|gitlab用户可以创建项目限制
env.open.SERVICES_HELM_URL|helm地址
env.open.SERVICES_HARBOR_BASEURL|harbor地址
env.open.SERVICES_HARBOR_USERNAME|harbor用户名
env.open.SERVICES_HARBOR_PASSWORD|harbor密码
env.open.SERVICES_SONARQUBE_URL|sonarqube地址
env.open.SERVICES_GATEWAY_URL|gateway地址
env.open.AGENT_VERSION|agengt版本
env.open.SECURITY_BASIC_ENABLE|安全性验证
env.open.SECURITY_IGNORED|安全性忽略
env.open.AGENT_SERVICEURL|agent地址
env.open.AGENT_REPOURL|agent仓库地址
env.open.TEMPLATE_VERSION_MICROSERVICE|微服务模板版本
env.open.TEMPLATE_VERSION_MICROSERVICEFRONT|前端服务模板版本
env.open.TEMPLATE_VERSION_JAVALIB|javalib模板版本
persistence.enabled|是否启用持久化存储
persistence.existingClaim|绑定的pvc名称
