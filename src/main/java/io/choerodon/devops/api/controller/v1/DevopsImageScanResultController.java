package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.DevopsHostVO;
import io.choerodon.devops.api.vo.DevopsImageScanResultVO;
import io.choerodon.devops.api.vo.ImageScanResultVO;
import io.choerodon.devops.app.service.DevopsImageScanResultService;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * Created by wangxiang on 2021/3/25
 */
@RestController
@RequestMapping("/v1/projects/{project_id}")
public class DevopsImageScanResultController {

    @Autowired
    private DevopsImageScanResultService devopsImageScanResultService;


    @GetMapping("/image/info/{gitlab_pipeline_id}/{job_id}")
    @ApiOperation("查询扫描结果的基本信息")
    @Permission(level = ResourceLevel.ORGANIZATION)
    public ResponseEntity<ImageScanResultVO> queryImageInfo(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Long projectId,
            @PathVariable("gitlab_pipeline_id") Long gitlabPipelineId,
            @ApiIgnore PageRequest pageRequest) {
        return Results.success(devopsImageScanResultService.queryImageInfo(projectId, gitlabPipelineId));
    }


    @GetMapping("/image/{gitlab_pipeline_id}/{job_id}")
    @ApiOperation("根据gitlab流水线id,和jobId查询扫描结果")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @CustomPageRequest
    public ResponseEntity<Page<DevopsImageScanResultVO>> pageByOptions(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Long projectId,
            @PathVariable("gitlab_pipeline_id") Long gitlabPipelineId,
            @SortDefault.SortDefaults({
                    @SortDefault(value = "id", direction = Sort.Direction.DESC)})
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数", required = false)
            @RequestBody(required = false) String options) {
        return Results.success(devopsImageScanResultService.pageByOptions(projectId, gitlabPipelineId, pageRequest,options));
    }

}
