package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.sonar.SonarOverviewVO;
import io.choerodon.devops.app.service.SonarAnalyseRecordService;
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


    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询项目代码检查概览")
    @PostMapping("/sonar_overview")
    public ResponseEntity<SonarOverviewVO> querySonarOverview(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return ResponseEntity.ok(sonarAnalyseRecordService.querySonarOverview(projectId));
    }


}
