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
           