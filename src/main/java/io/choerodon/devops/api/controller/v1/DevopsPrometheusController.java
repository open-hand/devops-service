package io.choerodon.devops.api.controller.v1;

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
import io.choerodon.devops.api.vo.PrometheusVo;
import io.choerodon.devops.app.service.DevopsPrometheusService;

/**
 * @author: 25499
 * @date: 2019/10/29 8:41
 * @description:
 */
@RestController
@RequestMapping(value = "/v1/clusters/{cluster_id}/prometheus")
public class DevopsPrometheusController {

    @Autowired
    private DevopsPrometheusService devopsPrometheusService;

    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "部署prometheus")
    @PostMapping
    private ResponseEntity<PrometheusVo> deploy(
            @ApiParam(value = "集群id", required = true)
            @PathVariable(value = "cluster_id") Long clusterId,
            @ApiParam(value = "请求体", required = true)
            @RequestBody PrometheusVo prometheusVo) {
        return Optional.ofNullable(devopsPrometheusService.deploy(clusterId, prometheusVo))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.prometheus.deploy"));
    }


    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "更新prometheus")
    @PostMapping
    private ResponseEntity<PrometheusVo> update(
            @ApiParam(value = "集群id", required = true)
            @PathVariable(value = "cluster_id") Long clusterId,
            @ApiParam(value = "请求体", required = true)
            @RequestBody PrometheusVo prometheusVo) {
        return Optional.ofNullable(devopsPrometheusService.deploy(clusterId, prometheusVo))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.prometheus.deploy"));
    }

    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询prometheus部署状态")
    @GetMapping
    private ResponseEntity<String> queryDeployStatus(
            @ApiParam(value = "集群id", required = true)
            @PathVariable(value = "cluster_id") Long clusterId,
            @PathVariable(value = "prometheus_id") Long prometheus_id) {
        return Optional.ofNullable(devopsPrometheusService.queryDeployStatus(clusterId, prometheus_id))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.prometheus.deploy"));
    }

    @Permission(type = io.choerodon.base.enums.ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "卸载prometheus")
    public ResponseEntity delete(
            @ApiParam(value = "集群id", required = true)
            @PathVariable(value = "cluster_id") Long clusterId,
            @ApiParam(value = "prometheusID", required = true)
            @PathVariable(value = "prometheus_id") Long prometheusId) {
        devopsPrometheusService.delete(clusterId, prometheusId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


}
