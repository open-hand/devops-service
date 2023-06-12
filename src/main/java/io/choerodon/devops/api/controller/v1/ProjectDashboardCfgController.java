package io.choerodon.devops.api.controller.v1;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.dashboard.ProjectDashboardCfgVO;
import io.choerodon.devops.app.service.ProjectDashboardCfgService;
import io.choerodon.devops.infra.dto.ProjectDashboardCfgDTO;
import io.choerodon.swagger.annotation.Permission;


/**
 * 项目质量评分配置表(ProjectDashboardCfg)表控制层
 *
 * @author hao.wang@zknow.com
 * @since 2023-06-12 14:38:58
 */

@RestController("projectDashboardCfgController.v1")
@RequestMapping("/v1/{organization_id}/project_dashboard_cfgs")
public class ProjectDashboardCfgController {

    @Autowired
    private ProjectDashboardCfgService projectDashboardCfgService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询项目质量评分配置")
    @GetMapping
    public ResponseEntity<ProjectDashboardCfgVO> queryByOrganizationIdId(
            @ApiParam(value = "租户ID", required = true)
            @PathVariable(value = "organization_id") Long organizationId) {
        return ResponseEntity.ok(projectDashboardCfgService.queryByOrganizationId(organizationId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "创建或更新项目质量评分配置")
    @PutMapping
    public ResponseEntity<ProjectDashboardCfgDTO> saveOrUpdateCfg(
            @ApiParam(value = "租户ID", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @RequestBody ProjectDashboardCfgVO projectDashboardCfgVO) {
        return ResponseEntity.ok(projectDashboardCfgService.saveOrUpdateCfg(organizationId, projectDashboardCfgVO));
    }
}

