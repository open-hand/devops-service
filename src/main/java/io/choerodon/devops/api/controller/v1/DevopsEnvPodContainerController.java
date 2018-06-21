package io.choerodon.devops.api.controller.v1;

import java.util.Optional;

import io.choerodon.core.iam.InitRoleCode;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.dto.DevopsEnvPodContainerDTO;
import io.choerodon.devops.api.dto.DevopsEnvPodContainerLogDTO;
import io.choerodon.devops.app.service.DevopsEnvPodContainerService;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * Creator: Runge
 * Date: 2018/5/16
 * Time: 13:52
 * Description:
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/app_pod/{podId}/containers")
public class DevopsEnvPodContainerController {
    @Autowired
    private DevopsEnvPodContainerService containerService;

    /**
     * 获取日志信息 By Pod
     *
     * @param projectId 项目ID
     * @param podId     pod ID
     * @return DevopsEnvPodContainerLogDTO
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER,
                    InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "获取日志信息 By Pod")
    @GetMapping(value = "/logs")
    public ResponseEntity<DevopsEnvPodContainerLogDTO> queryLogByPod(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "pod ID", required = true)
            @PathVariable Long podId) {
        return Optional.ofNullable(containerService.logByPodId(podId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.application.pod.query"));
    }
}
