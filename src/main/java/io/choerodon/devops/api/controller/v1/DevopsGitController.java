package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.app.service.DevopsGitService;
import io.choerodon.swagger.annotation.Permission;

/**
 * Creator: Runge
 * Date: 2018/7/2
 * Time: 14:21
 * Description:
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/apps/{application_id}/git")
public class DevopsGitController {

    @Autowired
    private DevopsGitService devopsGitService;


    /**
     * 创建标签
     *
     * @param projectId     项目ID
     * @param applicationId 应用ID
     * @param tag           标签名称
     * @param ref           参考名称
     * @return null
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "创建标签")
    @PostMapping("/tags")
    public ResponseEntity start(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用id", required = true)
            @PathVariable(value = "application_id") Long applicationId,
            @ApiParam(value = "标签名称", required = true)
            @RequestParam String tag,
            @ApiParam(value = "参考名称", required = true)
            @RequestParam String ref) {
        devopsGitService.createTag(projectId, applicationId, tag, ref);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
