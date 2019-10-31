package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.ClusterResourceVO;
import io.choerodon.devops.api.vo.DevopsPrometheusVO;
import io.choerodon.devops.app.service.DevopsClusterResourceService;

/**
 * @author zhaotianxin
 * @since 2019/10/29
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/cluster_resource")
public class DevopsClusterResourceController {
    @Autowired
    private DevopsClusterResourceService devopsClusterResourceService;

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下创建cert_manager")
    @PostMapping("/cert_manager/deploy")
    public ResponseEntity deployCertManager(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群id", required = true)
            @RequestParam(name = "cluster_id", required = true) Long clusterId) {
        devopsClusterResourceService.operateCertManager(clusterId, null, null);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询组件")
    @GetMapping
    public ResponseEntity<List<ClusterResourceVO>> listClusterResource(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群id", required = true)
            @RequestParam(name = "cluster_id", required = true) Long clusterId) {
        return Optional.ofNullable(devopsClusterResourceService.listClusterResource(clusterId, projectId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.cert.manager.insert"));
    }


    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下卸载cert_manager")
    @DeleteMapping("/cert_manager/unload")
    public ResponseEntity<Boolean> unloadCertManager(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群id", required = true)
            @RequestParam(name = "cluster_id", required = true) Long clusterId) {
        return new ResponseEntity<Boolean>(devopsClusterResourceService.deleteCertManager(clusterId),HttpStatus.OK);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "校验cert_manager能否被卸载")
    @GetMapping("/cert_manager/check")
    public ResponseEntity<Boolean> checkCertManager(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群id", required = true)
            @RequestParam(name = "cluster_id", required = true) Long clusterId) {
        return new ResponseEntity<Boolean>(devopsClusterResourceService.checkCertManager(clusterId), HttpStatus.OK);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "集群下安装prometheus")
    @PostMapping("/prometheus/create")
    public ResponseEntity create(
            @ApiParam(value = "集群id", required = true)
            @RequestParam(name = "cluster_id", required = true) Long clusterId,
            @ApiParam(value = "请求体", required = true)
            @RequestBody DevopsPrometheusVO prometheusVo) {
        devopsClusterResourceService.createOrUpdate(clusterId,prometheusVo);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "升级prometheus")
    @PutMapping("/prometheus/update")
    public ResponseEntity update(
            @ApiParam(value = "集群id", required = true)
            @RequestParam(name = "cluster_id", required = true) Long clusterId,
            @ApiParam(value = "请求体", required = true)
            @RequestBody DevopsPrometheusVO prometheusVo) {
        devopsClusterResourceService.createOrUpdate(clusterId,prometheusVo);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询集群下prometheus")
    @GetMapping("/prometheus")
    public ResponseEntity<DevopsPrometheusVO> query(
            @ApiParam(value = "集群id", required = true)
            @RequestParam(name = "cluster_id", required = true) Long clusterId) {

        return Optional.ofNullable(devopsClusterResourceService.queryPrometheus(clusterId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.query.prometheus"));
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询prometheus部署状态")
    @GetMapping("/prometheus/deploy_status")
    public ResponseEntity<ClusterResourceVO> getDeployStatus(
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群id", required = true)
            @RequestParam(name = "cluster_id", required = true) Long clusterId) {
        return Optional.ofNullable(devopsClusterResourceService.queryDeployProcess(clusterId))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.prometheus.deploy"));
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "卸载prometheus")
    @DeleteMapping("/prometheus/unload")
    public ResponseEntity delete(
            @ApiParam(value = "集群id", required = true)
            @RequestParam(name = "cluster_id", required = true) Long clusterId) {
        devopsClusterResourceService.deletePrometheus(clusterId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询grafana URL")
    @GetMapping("/grafana_url")
    public ResponseEntity<String> getGrafanaUrl(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群id", required = true)
            @RequestParam(name = "cluster_id") Long clusterId,
            @ApiParam(value = "接口type", required = true)
            @RequestParam(name = "type") String type) {
        return Optional.ofNullable(devopsClusterResourceService.getGrafanaUrl(clusterId, type))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.grafana.url.get"));
    }
}


