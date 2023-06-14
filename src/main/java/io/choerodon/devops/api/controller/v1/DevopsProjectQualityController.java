package io.choerodon.devops.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.SonarAnalyseIssueAuthorRankVO;
import io.choerodon.devops.api.vo.SonarAnalyseIssueAuthorVO;
import io.choerodon.devops.api.vo.sonar.SonarOverviewVO;
import io.choerodon.devops.app.service.SonarAnalyseRecordService;
import io.choerodon.devops.app.service.SonarAnalyseUserIssueAuthorService;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * @author crockitwood
 * @date 2019-02-18
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/project_quality")
public class DevopsProjectQualityController {
    @Autowired
    private SonarAnalyseRecordService sonarAnalyseRecordService;
    @Autowired
    private SonarAnalyseUserIssueAuthorService sonarAnalyseUserIssueAuthorService;


    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询项目代码检查概览")
    @PostMapping("/sonar_overview")
    public ResponseEntity<SonarOverviewVO> querySonarOverview(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return ResponseEntity.ok(sonarAnalyseRecordService.querySonarOverview(projectId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询团队成员问题数")
    @GetMapping("/member_issues")
    public ResponseEntity<Page<SonarAnalyseIssueAuthorVO>> listMemberIssue(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @RequestParam(value = "app_service_id") Long appServiceId,
            PageRequest pageRequest) {
        return ResponseEntity.ok(sonarAnalyseUserIssueAuthorService.listMemberIssue(projectId, appServiceId, pageRequest));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询团队成员bug排名")
    @GetMapping("/member_bug_rank")
    public ResponseEntity<List<SonarAnalyseIssueAuthorRankVO>> listMemberBugRank(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @RequestParam(value = "app_service_id") Long appServiceId) {
        return ResponseEntity.ok(sonarAnalyseUserIssueAuthorService.listMemberBugRank(projectId, appServiceId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询团队成员漏洞排名")
    @GetMapping("/member_vuln_rank")
    public ResponseEntity<List<SonarAnalyseIssueAuthorRankVO>> listMemberVulnRank(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @RequestParam(value = "app_service_id") Long appServiceId) {
        return ResponseEntity.ok(sonarAnalyseUserIssueAuthorService.listMemberVulnRank(projectId, appServiceId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询团队成员代码异味排名")
    @GetMapping("/member_code_smell_rank")
    public ResponseEntity<List<SonarAnalyseIssueAuthorRankVO>> listMemberCodeSmellRank(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @RequestParam(value = "app_service_id") Long appServiceId) {
        return ResponseEntity.ok(sonarAnalyseUserIssueAuthorService.listMemberCodeSmellRank(projectId, appServiceId));
    }

}
