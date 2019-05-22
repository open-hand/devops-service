package io.choerodon.devops.api.controller.v1;

import java.util.Optional;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.dto.DevopsEnvPodDTO;
import io.choerodon.devops.app.service.DevopsEnvPodService;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Created by Zenger on 2018/4/17.
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/app_pod")
public class DevopsEnvPodController {

    private DevopsEnvPodService devopsEnvPodService;

    public DevopsEnvPodController(DevopsEnvPodService devopsEnvPodService) {
        this.devopsEnvPodService = devopsEnvPodService;
    }

    /**
     * 分页查询容器管理
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param searchParam 查询参数
     * @return page of devopsEnvPodDTO
     */
    @Permission(type= ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "分页查询容器管理")
    @CustomPageRequest
    @PostMapping(value = "/list_by_options")
    public ResponseEntity<Page<DevopsEnvPodDTO>> pageByOptions(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "环境id")
            @RequestParam(required = false) Long envId,
            @ApiParam(value = "应用id")
            @RequestParam(required = false) Long appId,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return Optional.ofNullable(devopsEnvPodService.listAppPod(
                projectId, envId, appId, pageRequest, searchParam))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.application.pod.query"));
    }
}
