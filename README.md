# DevOps Service
`DevOps Service` is the core service of Choerodon.  

Integrated several open source tools to automate the process of `planning`, `coding`, `building`, `testing`, and `deployment`, `operation`, `monitoring`.
 After a little simple configuration, then you'll get the most smoothest development experience.

## Feature
- **Application Management**
- **Version Control** (Using Gitflow Workflow)
- **Application Version Management**
- **CI/CD Dashboard**
- **Deploy Management**
- **Ingress Management**

## Requirements
- Java8
- [GitLab Service](https://github.com/choerodon/gitlab-service)
- [Iam Service](https://github.com/choerodon/iam-service)
- [Harbor](https://vmware.github.io/harbor/cn/)
- [kubenetes](https://kubernetes.io/)

## Installation and Getting Started
1. init database
    ```sql
    CREATE USER 'choerodon'@'%' IDENTIFIED BY "choerodon";
    CREATE DATABASE devops_service DEFAULT CHARACTER SET utf8;
    GRANT ALL PRIVILEGES ON devops_service.* TO choerodon@'%';
    FLUSH PRIVILEGES;
    ```
1. run command `sh init-local-database.sh`
1. run command as follow or run `DevopsServiceApplication` in `IntelliJ IDEA`
    ```bash
    mvn clean spring-boot:run
    ```

## Dependencies
- go-register-server: Register server
- config-server：Configure server
- kafka
- mysql: devops_service database