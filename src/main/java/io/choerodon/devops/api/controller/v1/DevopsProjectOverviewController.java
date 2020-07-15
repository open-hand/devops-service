package io.choerodon.devops.api.controller.v1;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.CountVO;
import io.choerodon.devops.app.service.DevopsProjectOverview;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

/**
 * 〈功能简述〉
 * 〈CI流水线Controller〉
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
        return Optional.ofNullable(devopsProjectOverview.getEnvStatusCount(projectId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.devops.project.overview.env.count"));
    }

    // 启用与停用应用服务数量
    @ApiOperation("获得启用与停用应用服务数量")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/app_service")
    public ResponseEntity<Map<String, Long>> getAppServiceStatusCount(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Long projectId) {
        return Optional.ofNullable(devopsProjectOverview.getAppServiceStatusCount(projectId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.devops.project.overview.appService.count"));
    }

    // 项目下代码提交次数统计
    @ApiOperation("项目下代码提交次数统计")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/commit_count")
    public ResponseEntity<CountVO> getCommitCount(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Long projectId) {
        return Optional.ofNullable(devopsProjectOverview.getCommitCount(projectId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.devops.project.overview.commit.count"));
    }

    // 项目下应用服务迭代部署次数
    @ApiOperation("项目下应用服务迭代部署次数")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/deploy_count")
    public ResponseEntity<CountVO> getDeployCount(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Long projectId) {
        return Optional.ofNullable(devopsProjectOverview.getDeployCount(projectId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.devops.project.overview.deploy.count"));
    }
}
