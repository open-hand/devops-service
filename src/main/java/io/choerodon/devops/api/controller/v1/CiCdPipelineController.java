package io.choerodon.devops.api.controller.v1;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.validator.DevopsCiPipelineAdditionalValidator;
import io.choerodon.devops.api.vo.CiCdPipelineRecordVO;
import io.choerodon.devops.api.vo.CiCdPipelineVO;
import io.choerodon.devops.api.vo.HostConnectionVO;
import io.choerodon.devops.api.vo.PipelineFrequencyVO;
import io.choerodon.devops.api.vo.pipeline.ExecuteTimeVO;
import io.choerodon.devops.app.service.CiCdPipelineRecordService;
import io.choerodon.devops.app.service.DevopsCdPipelineRecordService;
import io.choerodon.devops.app.service.DevopsCiPipelineService;
import io.choerodon.devops.infra.dto.CiCdPipelineDTO;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * 〈功能简述〉
 * 〈CI流水线Controller〉
 *
 * @author wanghao
 * @Date 2020/4/2 17:57
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/cicd_pipelines")
public class CiCdPipelineController {

    private final DevopsCiPipelineService devopsCiPipelineService;
    private final CiCdPipelineRecordService ciCdPipelineRecordService;
    private final DevopsCdPipelineRecordService devopsCdPipelineRecordService;

    public CiCdPipelineController(DevopsCiPipelineService devopsCiPipelineService, CiCdPipelineRecordService ciCdPipelineRecordService, DevopsCdPipelineRecordService devopsCdPipelineRecordService) {
        this.devopsCiPipelineService = devopsCiPipelineService;
        this.ciCdPipelineRecordService = ciCdPipelineRecordService;
        this.devopsCdPipelineRecordService = devopsCdPipelineRecordService;
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下创建流水线")
    @PostMapping
    public ResponseEntity<CiCdPipelineDTO> create(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @RequestBody @Valid CiCdPipelineVO ciCdPipelineVO) {
        DevopsCiPipelineAdditionalValidator.additionalCheckPipeline(ciCdPipelineVO);
        return ResponseEntity.ok(devopsCiPipelineService.create(projectId, ciCdPipelineVO));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下更新流水线")
    @PutMapping("/{pipeline_id}")
    public ResponseEntity<CiCdPipelineDTO> update(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt(ignoreUserConflict = true)
            @ApiParam(value = "流水线Id", required = true)
            @PathVariable(value = "pipeline_id") Long pipelineId,
            @RequestBody @Valid CiCdPipelineVO ciCdPipelineVO) {
        DevopsCiPipelineAdditionalValidator.additionalCheckPipeline(ciCdPipelineVO);
        return ResponseEntity.ok(devopsCiPipelineService.update(projectId, pipelineId, ciCdPipelineVO));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询cicd流水线配置")
    @GetMapping("/{pipeline_id}")
    public ResponseEntity<CiCdPipelineVO> query(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt(ignoreUserConflict = true)
            @ApiParam(value = "流水线Id", required = true)
            @PathVariable(value = "pipeline_id") Long pipelineId) {
        return ResponseEntity.ok(devopsCiPipelineService.query(projectId, pipelineId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询项目下流水线")
    @PostMapping("/query")
    @CustomPageRequest
    public ResponseEntity<Page<CiCdPipelineVO>> listByProjectIdAndAppName(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @RequestParam(value = "searchParam", required = false) String searchParam,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest) {
        return ResponseEntity.ok(devopsCiPipelineService.listByProjectIdAndAppName(projectId, searchParam, pageRequest));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "停用流水线")
    @PutMapping("/{pipeline_id}/disable")
    public ResponseEntity<CiCdPipelineDTO> disablePipeline(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt(ignoreUserConflict = true)
            @PathVariable(value = "pipeline_id") Long pipelineId) {
        return ResponseEntity.ok(devopsCiPipelineService.disablePipeline(projectId, pipelineId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "启用流水线")
    @PutMapping("/{pipeline_id}/enable")
    public ResponseEntity<CiCdPipelineDTO> enablePipeline(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt(ignoreUserConflict = true)
            @PathVariable(value = "pipeline_id") Long pipelineId) {
        return ResponseEntity.ok(devopsCiPipelineService.enablePipeline(projectId, pipelineId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "删除流水线")
    @DeleteMapping("/{pipeline_id}")
    public ResponseEntity<Void> deletePipeline(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt(ignoreUserConflict = true)
            @PathVariable(value = "pipeline_id") Long pipelineId) {
        devopsCiPipelineService.deletePipeline(projectId, pipelineId);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "全新执行GitLab流水线")
    @PostMapping(value = "/{pipeline_id}/execute")
    public ResponseEntity<Boolean> executeNew(
            @Encrypt(ignoreUserConflict = true)
            @PathVariable(value = "pipeline_id") Long pipelineId,
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @RequestParam(value = "gitlab_project_id") Long gitlabProjectId,
            @ApiParam(value = "分支名", required = true)
            @RequestParam(value = "ref") String ref,
            @RequestParam(value = "tag", defaultValue = "false") Boolean tag,
            @RequestBody Map<String, String> variables) {
        ciCdPipelineRecordService.executeNew(projectId, pipelineId, gitlabProjectId, ref, tag, variables);
        return ResponseEntity.noContent().build();
    }


    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "测试主机连接")
    @PostMapping(value = "/test_connection")
    public ResponseEntity<Boolean> testConnection(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @RequestBody HostConnectionVO hostConnectionVO) {
        return Results.success(devopsCdPipelineRecordService.testConnection(hostConnectionVO));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "devops图表，查询项目下流水线名称")
    @GetMapping(value = "/devops/pipeline")
    public ResponseEntity<List<CiCdPipelineDTO>> devopsPipline(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return Results.success(devopsCiPipelineService.devopsPipline(projectId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "devops图表 获取流水触发次数报表")
    @GetMapping(value = "/trigger")
    public ResponseEntity<PipelineFrequencyVO> listPipelineTrigger(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "pipeline_id")
            @RequestParam(value = "pipeline_id", required = false) Long pipelineId,
            @ApiParam(value = "start_time")
            @RequestParam(value = "start_time") Date startTime,
            @ApiParam(value = "end_time")
            @RequestParam(value = "end_time") Date endTime) {
        return Optional.ofNullable(devopsCiPipelineService.listPipelineTrigger(pipelineId, startTime, endTime))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.pipeline.trigger.get"));
    }


    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "devops图表 项目下分页查询流水线按触发记录")
    @GetMapping(value = "/trigger/page")
    @CustomPageRequest
    public ResponseEntity<Page<CiCdPipelineRecordVO>> pagePipelineTrigger(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "pipeline_id")
            @RequestParam(value = "pipeline_id", required = false) Long pipelineId,
            @ApiParam(value = "start_time")
            @RequestParam(value = "start_time") Date startTime,
            @ApiParam(value = "end_time")
            @RequestParam(value = "end_time") Date endTime,
            @ApiParam(value = "分页参数")
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest) {
        return Optional.ofNullable(devopsCiPipelineService.pagePipelineTrigger(pipelineId, startTime, endTime, pageRequest))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.pipeline.trigger.page.get"));
    }

    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "devops图表 流水线执行时长图（根据流水线列表，时间）")
    @PostMapping(value = "/execute/time")
    public ResponseEntity<ExecuteTimeVO> pipelineExecuteTime(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "pipeline_ids")
            @RequestBody(required = true) List<Long> pipelineIds,
            @ApiParam(value = "start_time")
            @RequestParam(value = "start_time") Date startTime,
            @ApiParam(value = "end_time")
            @RequestParam(value = "end_time") Date endTime) {
        return Optional.ofNullable(devopsCiPipelineService.pipelineExecuteTime(pipelineIds, startTime, endTime))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.pipeline.execute.time.get"));
    }

    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "devops图表 流水线执行时长图表格查询")
    @PostMapping(value = "/execute/time/page")
    public ResponseEntity<Page<CiCdPipelineRecordVO>> pagePipelineExecuteTime(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "pipeline_ids")
            @RequestBody(required = true) List<Long> pipelineIds,
            @ApiParam(value = "start_time")
            @RequestParam(value = "start_time") Date startTime,
            @ApiParam(value = "end_time")
            @RequestParam(value = "end_time") Date endTime,
            @ApiParam(value = "分页参数")
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest) {
        return Optional.ofNullable(devopsCiPipelineService.pagePipelineExecuteTime(pipelineIds, startTime, endTime, pageRequest))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.pipeline.execute.time.get"));
    }


}
