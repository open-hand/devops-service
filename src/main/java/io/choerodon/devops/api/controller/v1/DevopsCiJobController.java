package io.choerodon.devops.api.controller.v1;

import io.choerodon.core.annotation.Permission;
import io.choerodon.core.enums.ResourceType;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.SonarQubeConfigVO;
import io.choerodon.devops.app.service.DevopsCiJobService;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:29
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/ci_jobs")
public class DevopsCiJobController {
    private DevopsCiJobService devopsCiJobService;

    public DevopsCiJobController(DevopsCiJobService devopsCiJobService) {
        this.devopsCiJobService = devopsCiJobService;
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @PostMapping("/sonar/connect")
    @ApiOperation("sonar的连接测试")
    public ResponseEntity<Boolean> sonarConnect(
            @PathVariable(name = "project_id") Long projectId,
            @RequestBody SonarQubeConfigVO sonarQubeConfigVO) {
        return ResponseEntity.ok(devopsCiJobService.sonarConnect(projectId, sonarQubeConfigVO));
    }
}
