# DevOps Service   

`DevOps Service` is the core service of Choerodon. Current version: `0.15.0`   

Integrated several open source tools to automate the process of `planning`, `coding`, `building`, `testing`, and `deployment`, `operation`, `monitoring`.
 After a little simple configuration, then you'll get the most smoothest development experience.

## Feature
`DevOps Service` contains features as follows:
- Application management
- Version control (Using gitflow workflow)
- Application version management
- CI/CD dashboard
- Deploy management
- Ingress management

## Requirements
- Java8
- [GitLab Service](https://github.com/choerodon/gitlab-service)
- [Iam Service](https://github.com/choerodon/iam-service)
- [Harbor](https://vmware.github.io/harbor/cn/)
- [Kubenetes](https://kubernetes.io/)
- [MySQL](https://www.mysql.com)
- [Kafka](https://kafka.apache.org)

## Installation and Getting Started
1. init database

    ```sql
    CREATE USER 'choerodon'@'%' IDENTIFIED BY "choerodon";
    CREATE DATABASE devops_service DEFAULT CHARACTER SET utf8;
    GRANT ALL PRIVILEGES ON devops_service.* TO choerodon@'%';
    FLUSH PRIVILEGES;
    ```
1. run command `sh init-local-database.sh`
1. run command as follow or run `DevopsServiceApplication` in IntelliJ IDEA

    ```bash
    mvn clean spring-boot:run
    ```

## Dependencies
- `go-register-server`: Register server
- `config-server`：Configure server
- `kafka`
- `mysql`: devops_service database

## Reporting Issues
If you find any shortcomings or bugs, please describe them in the  [issue](https://github.com/choerodon/choerodon/issues/new?template=issue_template.md).

## How to Contribute
Pull requests are welcome! [Follow](https://github.com/choerodon/choerodon/blob/master/CONTRIBUTING.md) to know for more information on how to contribute.



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
env.open.SERVICES_GITLAB_URL|gitlab地址
env.open.SPRING_REDIS_HOST|redis地址
env.open.SERVICES_GITLAB_URL|gitlab 地址
env.open.SERVICES_GITLAB_PASSWORD|gitlab默认创建用户密码
env.open.SERVICES_GITLAB_PROJECTLIMIT|gitlab用户可以创建项目限制
env.open.SERVICES_HELM_URL|helm地址
env.open.SERVICES_HARBOR_BASEURL|harbor地址
env.open.SERVICES_HARBOR_USERNAME|harbor用户名
env.open.SERVICES_HARBOR_PASSWORD|harbor密码
env.open.SERVICES_SONARQUBE_URL|sonarqube地址
env.open.SERVICES_GATEWAY_URL|gateway地址
env.open.AGENT_VERSION|agent版本
env.open.SECURITY_BASIC_ENABLE|安全性验证
env.open.SECURITY_IGNORED|安全性忽略
env.open.AGENT_SERVICEURL|agent地址
env.open.AGENT_REPOURL|agent仓库地址
env.open.TEMPLATE_VERSION_MICROSERVICE|微服务模板版本
env.open.TEMPLATE_VERSION_MICROSERVICEFRONT|前端服务模板版本
env.open.TEMPLATE_VERSION_JAVALIB|javalib模板版本
env.open.SKYWALKING_OPTS | skywalking 代理端配置
persistence.enabled|是否启用持久化存储
persistence.existingClaim|绑定的pvc名称

