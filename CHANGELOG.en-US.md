# Changelog
All notable changes to choerodon will be documented in this file.
 
## [0.6.0] - 2018-06-10
### Added
- `Release management` section, including `application release` and `application market`. 
- Service/Ingress status, operation type and status in `service` and `ingress` to track their running conditions.
- `Container log` in `container` to track its running status. 
- Review function in `application deployment` for users to check their operation. 
- Upgrade reminder of env-agent in `environment pipeline`. 
- Resource consistency mechanism in env-agent.
- Message sending failure and timeout confirmation mechanism in env-agent. 

### Changed
- Reconfigure `application deployment`, delete instance query and add `application instance`. 
- Distinguish Service domestic port and target port in `Service`.
- Update three predefined `application templates` to be more useable. 
- Modify some APIs based on the more standardized naming rule. 
- Optimize the instance scan mechanism.

### Removed
- "Rapid Deployment" functionalities.

### Fixed
- Logical bug in version check and error shown in frontend in `branch management`. 
- Inconsistent status after devops-service and env-agent restart.
- Orgnization admin not in Gitlab template group.
- Some other bugs.