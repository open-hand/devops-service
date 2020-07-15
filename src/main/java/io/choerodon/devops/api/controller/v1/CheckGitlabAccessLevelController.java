package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.app.service.CheckGitlabAccessLevelService;
import io.choerodon.devops.infra.enums.AppServiceEvent;
import io.choerodon.swagger.annotation.Permission;

/**
 * @author scp
 * @since 2020/6/17
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/member-check")
public class CheckGitlabAccessLevelController {


    @Autowired
    private CheckGitlabAccessLevelService checkGitlabAccessLevelService;

    /**
     * 校验项目成员gitlab角色
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "项目下校验项目成员权限")
    @GetMapping(value = "/{app_service_id}")
    public ResponseEntity<Void> create(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "应用服务ID", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "操作类型", required = true)
            @RequestParam(value = "type") String type) {
        AppServiceEvent appServiceEvent = AppServiceEvent.valueOf(type);
        checkGitlabAccessLevelService.checkGitlabPermission(projectId, appServiceId, appServiceEvent);
        return Results.success();
    }
}
