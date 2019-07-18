package io.choerodon.devops.api.controller.v1;

import java.util.Optional;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.annotation.Permission;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.DevopsEnvPodVO;
import io.choerodon.devops.app.service.DevopsEnvPodService;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Created by Zenger on 2018/4/17.
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/pods")
public class DevopsEnvPodController {

    @Autowired
    private DevopsEnvPodService devopsEnvPodService;

    /**
     * 分页查询环境下pod
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param searchParam 查询参数
     * @return page of DevopsEnvironmentPodVO
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "分页查询环境下pod")
    @CustomPageRequest
    @PostMapping(value = "/page_by_options")
    public ResponseEntity<PageInfo<DevopsEnvPodVO>> pageByOptions(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "环境id")
            @RequestParam(value = "env_id", required = false) Long envId,
            @ApiParam(value = "应用id")
            @RequestParam(value = "app_id", required = false) Long appId,
            @ApiParam(value = "应用id")
            @RequestParam(value = "instance_id", required = false) Long instanceId,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return Optional.ofNullable(devopsEnvPodService.pageByOptions(
                projectId, envId, appId, instanceId, pageRequest, searchParam))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.application.pod.query"));
    }
}
