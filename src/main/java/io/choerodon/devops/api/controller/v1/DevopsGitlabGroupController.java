package io.choerodon.devops.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.app.service.DevopsGitService;
import io.choerodon.devops.infra.dto.gitlab.GroupDTO;
import io.choerodon.swagger.annotation.Permission;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/8/17 14:35
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/groups")
public class DevopsGitlabGroupController {

    @Autowired
    private DevopsGitService devopsGitService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询用户是owner角色的group列表, 排除当前项目的")
    @GetMapping(value = "/owned_expect_current")
    public ResponseEntity<List<GroupDTO>> listOwnedGroupExpectCurrent(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "搜索参数", required = false)
            @RequestParam(value = "search", required = false) String search) {
        return ResponseEntity.ok(devopsGitService.listOwnedGroupExpectCurrent(projectId, search));
    }
}