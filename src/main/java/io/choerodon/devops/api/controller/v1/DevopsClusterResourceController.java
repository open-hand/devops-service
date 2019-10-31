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
import io.choerodon.devops.api.vo.PrometheusVo;
import io.choerodon.devops.app.service.DevopsClusterResourceService;
import io.choerodon.devops.infra.dto.DevopsClusterResourceDTO;

/**
 * @author zhaotianxin
 * @since 2019/10/29
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}")
public class DevopsClusterResourceController {
    @Autowired
    private DevopsClusterResourceService devopsClusterResourceService;

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下创建cert_manager")
    @PostMapping("/cert_manager/deploy")
    public ResponseEntity deploy(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群id", required = true)
            @RequestParam(name = "cluster_id", required = true) Long clusterId,
            @RequestBody DevopsClusterResourceDTO devopsClusterResourceDTO) {
        devopsClusterResourceService.operateCertManager(clusterId, null, null);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询组件")
    @GetMapping
    public ResponseEntity<List<ClusterResourceVO>> query(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群id", required = true)
            @RequestParam(name = "cluster_id", required = true) Long clusterId) {
        return Optional.ofNullable(devopsClusterResourceService.listClusterResource(clusterId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.cert.manager.insert"));
    }


    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下卸载cert_manager")
    @DeleteMapping("/cert_manager/unload")
    public ResponseEntity<Boolean> unload(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群id", required = true)
            @RequestParam(name = "cluster_id", required = true) Long clusterId) {
        return new ResponseEntity<Boolean>(devopsClusterResourceService.deleteCertManager(clusterId), HttpStatus.OK);
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
    private ResponseEntity<PrometheusVo> create(
            @ApiParam(value = "集群id", required = true)
            @RequestParam(name = "cluster_id", required = true) Long clusterId,
            @ApiParam(value = "请求体", required = true)
            @RequestBody PrometheusVo prometheusVo) {
        return Optional.ofNullable(devopsClusterResourceService.createOrUpdate(clusterId, prometheusVo))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.prometheus.create"));
    }


    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "升级prometheus")
    @PutMapping("/prometheus/update")
    private ResponseEntity<PrometheusVo> update(
            @ApiParam(value = "集群id", required = true)
            @RequestParam(name = "cluster_id", required = true) Long clusterId,
            @ApiParam(value = "请求体", required = true)
            @RequestBody PrometheusVo prometheusVo) {
        return Optional.ofNullable(devopsClusterResourceService.createOrUpdate(clusterId, prometheusVo))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.prometheus.update"));
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询prometheus部署状态")
    @GetMapping("/prometheus/queryStatus")
    private ResponseEntity<ClusterResourceVO> queryDeployStatus(
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群id", required = true)
            @RequestParam(name = "cluster_id", required = true) Long clusterId,
            @RequestParam(name = "prometheus_id", required = true) Long prometheusId) {
        return Optional.ofNullable(devopsClusterResourceService.queryDeployProcess(projectId, clusterId, prometheusId))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.prometheus.deploy"));
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "卸载prometheus")
    @DeleteMapping("/prometheus/unload")
    public ResponseEntity delete(
            @ApiParam(value = "集群id", required = true)
            @RequestParam(name = "cluster_id", required = true) Long clusterId,
            @ApiParam(value = "prometheusID", required = true)
            @RequestParam(name = "prometheus_id", required = true) Long prometheusId) {
        devopsClusterResourceService.deletePrometheus(clusterId, prometheusId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}


