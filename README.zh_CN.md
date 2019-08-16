# DevOps Service   


`DevOps Service` DevOps Service是Choerodon平台实现持续交付的基础. 当前版本为: `0.19.0`   


DevOps Service通过自主整合的DevOps工具链，集成相关的开源工具，以此形成了计划、编码、测试、部署、运维以及监控的DevOps闭环。
并且只需通过简单的配置，您便能获得最佳的开发体验。


## Feature
`DevOps Service` 含有以下功能:    
- 应用服务管理                
- 版本控制
- 应用服务版本管理      
- 持续集成概览                                  
- 分支管理                         
- 代码管理                                
- 部署管理                                 
- 持续部署流水线管理                          
- 资源管理                                
- 集群管理                           

## Requirements
- [JAVA](https://www.java.com/en/)
- [Harbor](https://vmware.github.io/harbor/cn/)
- [Kubenetes](https://kubernetes.io/)
- [Helm](https://helm.sh/)
- [Sonarqube](https://www.sonarqube.org/)
- [MySQL](https://www.mysql.com)
- [Redis](https://redis.io/)

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
* `go-register-server`: 注册中心
* `iam-service`：用户服务
* `api-gateway`: 网关服务
* `oauth-server`: 授权服务
* `manager-service`: 管理服务
* `file-service` : 文件服务
* `asgard-service` : 事务一致性服务
* `notify-service` : 通知服务

## Reporting Issues
如果您发现任何缺陷或bug，请及时告知我们  [issue](https://github.com/choerodon/choerodon/issues/new?template=issue_template.md).

## How to Contribute
欢迎您通过 [Follow](https://github.com/choerodon/choerodon/blob/master/CONTRIBUTING.md) 了解更多关于如何贡献的信息！
