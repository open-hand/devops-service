package io.choerodon.devops.api.controller.v1;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.app.service.ProjectService;
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
 * @author crockitwood
 * @date 2019-02-18
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}")
public class DevopsProjectController {


    @Autowired
    private ProjectService projectService;

    /**
     * 查询项目Gitlab Group是否创建成功
     * 用作Demo数据初始化时查询状态
     *
     * @param projectId     项目id
     */
    @Permission(type= ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询项目Gitlab Group是否创建成功")
    @GetMapping("/check_gitlab_group")
    public ResponseEntity<Boolean> queryProjectGroupReady(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return new ResponseEntity<>(projectService.queryProjectGitlabGroupReady(projectId), HttpStatus.OK);

    }
}
