package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.DevopsEnvPodContainerLogVO;
import io.choerodon.devops.app.service.DevopsEnvPodContainerService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Creator: Runge
 * Date: 2018/5/16
 * Time: 13:52
 * Description:
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/app_pod/{pod_id}/containers")
public class DevopsEnvPodContainerController {
    @Autowired
    private DevopsEnvPodContainerService containerService;

    /**
     * 获取日志信息 By Pod
     *
     * @param projectId 项目ID
     * @param podId     pod ID
     * @return List of DevopsEnvPodContainerLogVO
     */
    @Permission(type= ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取日志信息 By Pod")
    @GetMapping(value = "/logs")
    public ResponseEntity<List<DevopsEnvPodContainerLogVO>> queryLogByPod(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "pod ID", required = true)
            @PathVariable(value = "pod_id") Long podId) {
        return Optional.ofNullable(containerService.logByPodId(podId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.application.pod.get"));
    }

    /**
     * 操作 shell By Pod
     *
     * @param projectId 项目ID
     * @param podId     pod ID
     * @return List of DevopsEnvPodContainerLogVO
     */
    @Permission(type= ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取日志shell信息 By Pod")
    @GetMapping(value = "/logs/shell")
    public ResponseEntity<List<DevopsEnvPodContainerLogVO>> handleShellByPod(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "pod ID", required = true)
            @PathVariable(value = "pod_id") Long podId) {
        return Optional.ofNullable(containerService.logByPodId(podId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.application.shell.get"));
    }
}
