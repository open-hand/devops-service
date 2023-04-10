package io.choerodon.devops.api.controller.v1;

import java.util.Map;
import java.util.Set;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.CountVO;
import io.choerodon.devops.api.vo.MergeRequestVO;
import io.choerodon.devops.app.service.DevopsProjectOverview;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * 〈功能简述〉
 * 项目下统计
 *
 * @author lihao
 * @Date 2020/7/8 15:56
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/overview")
public class DevopsProjectOverviewController {

    @Autowired
    private DevopsProjectOverview devopsProjectOverview;

    // 连接与未连接环境数量
    @ApiOperation("获得连接与未连接环境数量")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/env")
    public ResponseEntity<Map<String, Long>> getEnvStatusCount(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Long projectId) {
        return ResponseEntity.ok(devopsProjectOverview.getEnvStatusCount(projectId));
    }

    // 启用与停用应用服务数量
    @ApiOperation("获得启用与停用应用服务数量")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/app_service")
    public ResponseEntity<Map<String, Long>> getAppServiceStatusCount(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Long projectId) {
        return ResponseEntity.ok(devopsProjectOverview.getAppServiceStatusCount(projectId));
    }

    // 项目下代码提交次数统计
    @ApiOperation("项目下代码提交次数统计")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/commit_count")
    public ResponseEntity<CountVO> getCommitCount(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Long projectId) {
        return ResponseEntity.ok(devopsProjectOverview.getCommitCount(projectId));
    }

    // 项目下应用服务迭代部署次数
    @ApiOperation("项目下应用服务迭代部署次数")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/deploy_count")
    public ResponseEntity<CountVO> getDeployCount(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Long projectId) {
        return ResponseEntity.ok(devopsProjectOverview.getDeployCount(projectId));
    }


    //项目概览统计流水线触发次数
    @ApiOperation("项目下应用服务迭代部署次数")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/ci_count")
    public ResponseEntity<CountVO> getCiCount(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Long projectId) {
        return ResponseEntity.ok(devopsProjectOverview.getCiCount(projectId));
    }

    @ApiOperation("查看项目下所有待审核合并请求")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/merge_request_to_be_checked")
    @CustomPageRequest
    public ResponseEntity<Page<MergeRequestVO>> getMergeRequestToBeChecked(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "选择的应用服务id") @RequestParam(value = "app_service_ids", required = false) Set<Long> appServiceIdsToSearch,
            @ApiParam(value = "搜索参数") @RequestParam(value = "param", required = false) String param,
            @ApiIgnore PageRequest pageRequest
    ) {
        return Results.success(devopsProjectOverview.getMergeRequestToBeChecked(projectId, appServiceIdsToSearch, param, pageRequest));
    }
}
