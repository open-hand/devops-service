# DevOps Service   


`DevOps Service` is the continuous delivery service of Choerodon. Current version: `0.19.0`   


DevOps Service integrated several open source tools to automate the process of `planning`, `coding`, `building`, `testing`, `deployment`, `operation` and `monitoring`.
 After a little simple configuration, you'll get the smoothest development experience.


## Feature
`DevOps Service` contains features as follows:    
- Application service management                 
- Version control (Using githubflow workflow) 
- Application service version management       
- CI dashboard                                     
- Branch management                           
- Code management                                
- Deploy management                                  
- CD pipeline management                            
- Resource management                               
- Cluster management                            

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
* `go-register-server`: Register server
* `iam-service`：iam server
* `api-gateway`: api gateway server
* `oauth-server`: oauth server
* `manager-service`: manager service
* `file-service` : file service
* `asgard-service` : asgard service
* `notify-service` : notify service

## Reporting Issues
If you find any shortcomings or bugs, please describe them in the  [issue](https://github.com/choerodon/choerodon/issues/new?template=issue_template.md).

## How to Contribute
Pull requests are welcome! [Follow](https://github.com/choerodon/choerodon/blob/master/CONTRIBUTING.md) to know for more information on how to contribute.
