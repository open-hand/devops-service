package io.choerodon.devops.api.controller.v1;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.AppServiceReqVO;
import io.choerodon.devops.app.service.DevopsClusterResourceService;
import io.choerodon.devops.infra.dto.DevopsClusterDTO;
import io.choerodon.devops.infra.dto.DevopsClusterResourceDTO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * @author zhaotianxin
 * @since 2019/10/29
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/cert_manager")
public class DevopsCertManagerController {
    @Autowired
    private DevopsClusterResourceService devopsClusterResourceService;

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下创建cert_manager")
    @PostMapping
    public ResponseEntity install(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群id", required = true)
            @RequestParam(name = "cluster_id", required = true) Long clusterId,
            @RequestBody DevopsClusterResourceDTO devopsClusterResourceDTO) {
        devopsClusterResourceService.operateCertManager(devopsClusterResourceDTO, clusterId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下查询cert_manager")
    @GetMapping
    public ResponseEntity<DevopsClusterResourceDTO> query(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群id", required = true)
            @RequestParam(name = "cluster_id", required = true) Long clusterId) {
        return Optional.ofNullable(devopsClusterResourceService.queryCertManager(clusterId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.cert.manager.insert"));
    }


    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下卸载cert_manager")
    @DeleteMapping
    public ResponseEntity<Boolean> uninstall(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "集群id", required = true)
            @RequestParam(name = "cluster_id", required = true) Long clusterId) {
        return new ResponseEntity<Boolean>(devopsClusterResourceService.deleteCertManager(clusterId), HttpStatus.OK);
    }

}


