package io.choerodon.devops.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.ClusterResourceVO;
import io.choerodon.devops.api.vo.DevopsPrometheusVO;
import io.choerodon.devops.api.vo.PrometheusStageVO;
import io.choerodon.devops.app.service.DevopsClusterResourceService;
import io.choerodon.swagger.annotation.Permission;

/**
 * @author zhaotianxin
 * @since 2019/10/29
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/cluster_resource")
public class DevopsClusterResourceController {
    @Autowired
    private DevopsClusterResourceService devopsClusterResourceService;

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下创建cert_manager")
    @PostMapping("/cert_manager/deploy")
    public ResponseEntity<Void> deployCertManager(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "集群id", required = true)
            @RequestParam(name = "cluster_id", required = true) Long clusterId) {
        devopsClusterResourceService.createCertManager(projectId, clusterId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询组件")
    @GetMapping
    public ResponseEntity<List<ClusterResourceVO>> listClusterResource(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "集群id", required = true)
            @RequestParam(name = "cluster_id", required = true) Long clusterId) {
        return ResponseEntity.ok(devopsClusterResourceService.listClusterResource(clusterId, projectId));
    }


    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下卸载cert_manager")
    @DeleteMapping("/cert_manager/unload")
    public ResponseEntity<Boolean> unloadCertManager(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "集群id", required = true)
            @RequestParam(name = "cluster_id", required = true) Long clusterId) {
        return new ResponseEntity<>(devopsClusterResourceService.deleteCertManager(projectId, clusterId), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "校验集群下的环境中是否存在使用CertManager申请或上传的证书")
    @GetMapping("/cert_manager/check")
    public ResponseEntity<Boolean> checkCertManager(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "集群id", required = true)
            @RequestParam(name = "cluster_id", required = true) Long clusterId) {
        return new ResponseEntity<>(devopsClusterResourceService.checkCertManager(clusterId), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "集群下安装prometheus")
    @PostMapping("/prometheus")
    public ResponseEntity<Boolean> createPrometheus(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "集群id", required = true)
            @RequestParam(name = "cluster_id") Long clusterId,
            @ApiParam(value = "请求体")
            @RequestBody @Validated DevopsPrometheusVO prometheusVo) {
        return ResponseEntity.ok(devopsClusterResourceService.createPrometheus(projectId, clusterId, prometheusVo));
    }


    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "升级prometheus")
    @PutMapping("/prometheus")
    public ResponseEntity<Boolean> updatePrometheus(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群id", required = true)
            @Encrypt
            @RequestParam(name = "cluster_id") Long clusterId,
            @ApiParam(value = "请求体", required = true)
            @RequestBody DevopsPrometheusVO prometheusVo) {
        return ResponseEntity.ok(devopsClusterResourceService.updatePrometheus(projectId, clusterId, prometheusVo));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询集群下prometheus")
    @GetMapping("/prometheus")
    public ResponseEntity<DevopsPrometheusVO> queryPrometheus(
            @Encrypt
            @ApiParam(value = "集群id", required = true)
            @RequestParam(name = "cluster_id") Long clusterId) {
        return ResponseEntity.ok(devopsClusterResourceService.queryPrometheus(clusterId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询prometheus部署状态")
    @GetMapping("/prometheus/deploy_status")
    public ResponseEntity<PrometheusStageVO> getPrometheusDeployStatus(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群id", required = true)
            @Encrypt
            @RequestParam(name = "cluster_id") Long clusterId) {
        return ResponseEntity.ok(devopsClusterResourceService.queryDeployStage(clusterId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "卸载prometheus")
    @DeleteMapping("/prometheus/unload")
    public ResponseEntity<Boolean> deletePrometheus(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "集群id", required = true)
            @RequestParam(name = "cluster_id", required = true) Long clusterId) {
        return ResponseEntity.ok(devopsClusterResourceService.uninstallPrometheus(projectId, clusterId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "重试prometheus操作")
    @DeleteMapping("/prometheus/retry")
    public ResponseEntity<Boolean> retryInstallPrometheus(
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "集群id", required = true)
            @RequestParam(name = "cluster_id", required = true) Long clusterId) {
        devopsClusterResourceService.retryInstallPrometheus(projectId, clusterId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询grafana URL")
    @GetMapping("/grafana_url")
    public ResponseEntity<String> getGrafanaUrl(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "集群id", required = true)
            @RequestParam(name = "cluster_id") Long clusterId,
            @ApiParam(value = "接口type", required = true)
            @RequestParam(name = "type") String type) {
        return new ResponseEntity<>(devopsClusterResourceService.getGrafanaUrl(projectId, clusterId, type), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询环境关联的集群是否安装cert-manager")
    @GetMapping("/cert_manager/check_by_env_id")
    public ResponseEntity<Boolean> queryCertManagerByEnvId(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境id", required = true)
            @RequestParam(name = "env_id") Long envId) {
        return new ResponseEntity<>(devopsClusterResourceService.queryCertManagerByEnvId(envId), HttpStatus.OK);
    }

}


