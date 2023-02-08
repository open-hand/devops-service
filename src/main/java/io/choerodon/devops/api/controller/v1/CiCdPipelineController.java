package io.choerodon.devops.api.controller.v1;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.validator.DevopsCiPipelineAdditionalValidator;
import io.choerodon.devops.api.vo.CiCdPipelineRecordVO;
import io.choerodon.devops.api.vo.CiCdPipelineVO;
import io.choerodon.devops.api.vo.PipelineFrequencyVO;
import io.choerodon.devops.api.vo.PipelineInstanceReferenceVO;
import io.choerodon.devops.api.vo.pipeline.ExecuteTimeVO;
import io.choerodon.devops.app.service.DevopsCiPipelineService;
import io.choerodon.devops.infra.dto.CiCdPipelineDTO;
import io.choerodon.devops.infra.dto.DevopsCiPipelineFunctionDTO;
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

    @Autowired
    private DevopsCiPipelineService devopsCiPipelineService;

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下创建流水线")
    @PostMapping
    public ResponseEntity<CiCdPipelineDTO> create(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @RequestBody @Valid CiCdPipelineVO ciCdPipelineVO) {
        DevopsCiPipelineAdditionalValidator.additionalCheckPipeline(ciCdPipelineVO);
        DevopsCiPipelineAdditionalValidator.validateBranch(ciCdPipelineVO.getRelatedBranches());
        return ResponseEntity.ok(devopsCiPipelineService.create(projectId, ciCdPipelineVO));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下更新流水线")
    @PutMapping("/{pipeline_id}")
    public ResponseEntity<CiCdPipelineDTO> update(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
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
            @ApiParam(value = "流水线Id", required = true)
            @PathVariable(value = "pipeline_id") Long pipelineId,
            @RequestParam(value = "delete_cd_info", defaultValue = "false") Boolean deleteCdInfo) {
        return ResponseEntity.ok(devopsCiPipelineService.query(projectId, pipelineId, deleteCdInfo));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询cicd流水线基础配置")
    @GetMapping("/{pipeline_id}/basic_info")
    public ResponseEntity<CiCdPipelineVO> queryBasicInfoById(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "流水线Id", required = true)
            @PathVariable(value = "pipeline_id") Long pipelineId) {
        return ResponseEntity.ok(devopsCiPipelineService.queryById(pipelineId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询流水线当前gitalb-ci.yaml文件内容")
    @GetMapping("/{pipeline_id}/gitlab_ci_yaml")
    public ResponseEntity<String> queryGitlabCiYamlById(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "流水线Id", required = true)
            @PathVariable(value = "pipeline_id") Long pipelineId) {
        return ResponseEntity.ok(devopsCiPipelineService.queryGitlabCiYamlById(pipelineId));
    }


    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询项目下流水线")
    @PostMapping("/query")
    @CustomPageRequest
    public ResponseEntity<Page<CiCdPipelineVO>> listByProjectIdAndAppName(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @RequestParam(value = "searchParam", required = false) String searchParam,
            @ApiParam(value = "是否启用")
            @RequestParam(value = "enableFlag", required = false) Boolean enableFlag,
            @ApiParam(value = "最近执行状态")
            @RequestParam(value = "status", required = false) String status,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest) {
        return ResponseEntity.ok(devopsCiPipelineService.listByProjectIdAndAppName(projectId, searchParam, pageRequest, enableFlag, status));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "停用流水线")
    @PutMapping("/{pipeline_id}/disable")
    public ResponseEntity<CiCdPipelineDTO> disablePipeline(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @PathVariable(value = "pipeline_id") Long pipelineId) {
        return ResponseEntity.ok(devopsCiPipelineService.disablePipeline(projectId, pipelineId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "启用流水线")
    @PutMapping("/{pipeline_id}/enable")
    public ResponseEntity<CiCdPipelineDTO> enablePipeline(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @PathVariable(value = "pipeline_id") Long pipelineId) {
        return ResponseEntity.ok(devopsCiPipelineService.enablePipeline(projectId, pipelineId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "删除流水线")
    @DeleteMapping("/{pipeline_id}")
    public ResponseEntity<Void> deletePipeline(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @PathVariable(value = "pipeline_id") Long pipelineId) {
        devopsCiPipelineService.deletePipeline(projectId, pipelineId);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "全新执行GitLab流水线")
    @PostMapping(value = "/{pipeline_id}/execute")
    public ResponseEntity<Boolean> executeNew(
            @PathVariable(value = "pipeline_id") Long pipelineId,
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @RequestParam(value = "gitlab_project_id") Long gitlabProjectId,
            @ApiParam(value = "分支名", required = true)
            @RequestParam(value = "ref") String ref,
            @RequestParam(value = "tag", defaultValue = "false") Boolean tag,
            @RequestBody Map<String, String> variables) {
        DevopsCiPipelineAdditionalValidator.additionalCheckVariablesKey(variables);
        devopsCiPipelineService.executeNew(projectId, pipelineId, gitlabProjectId, ref, variables);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "devops图表，查询项目下流水线名称")
    @GetMapping(value = "/devops/pipeline")
    public ResponseEntity<List<CiCdPipelineDTO>> devopsPipline(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return Results.success(devopsCiPipelineService.devopsPipline(projectId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "devops图表 获取流水触发次数报表")
    @GetMapping(value = "/trigger")
    public ResponseEntity<PipelineFrequencyVO> listPipelineTrigger(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "pipeline_id")
            @RequestParam(value = "pipeline_id", required = false) Long pipelineId,
            @ApiParam(value = "start_time")
            @RequestParam(value = "start_time") Date startTime,
            @ApiParam(value = "end_time")
            @RequestParam(value = "end_time") Date endTime) {
        return ResponseEntity.ok(devopsCiPipelineService.listPipelineTrigger(pipelineId, startTime, endTime));
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
            @ApiParam(value = "pipeline_id")
            @RequestParam(value = "pipeline_id", required = false) Long pipelineId,
            @ApiParam(value = "start_time")
            @RequestParam(value = "start_time") Date startTime,
            @ApiParam(value = "end_time")
            @RequestParam(value = "end_time") Date endTime,
            @ApiParam(value = "分页参数")
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest) {
        return ResponseEntity.ok(devopsCiPipelineService.pagePipelineTrigger(pipelineId, startTime, endTime, pageRequest));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "devops图表 流水线执行时长图（根据流水线列表，时间）")
    @PostMapping(value = "/execute/time")
    public ResponseEntity<ExecuteTimeVO> pipelineExecuteTime(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "pipeline_ids")
            @RequestBody(required = true) List<Long> pipelineIds,
            @ApiParam(value = "start_time")
            @RequestParam(value = "start_time") Date startTime,
            @ApiParam(value = "end_time")
            @RequestParam(value = "end_time") Date endTime) {
        return ResponseEntity.ok(devopsCiPipelineService.pipelineExecuteTime(pipelineIds, startTime, endTime));
    }

    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "devops图表 流水线执行时长图表格查询")
    @PostMapping(value = "/execute/time/page")
    public ResponseEntity<Page<CiCdPipelineRecordVO>> pagePipelineExecuteTime(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "pipeline_ids")
            @RequestBody(required = true) List<Long> pipelineIds,
            @ApiParam(value = "start_time")
            @RequestParam(value = "start_time") Date startTime,
            @ApiParam(value = "end_time")
            @RequestParam(value = "end_time") Date endTime,
            @ApiParam(value = "分页参数")
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest) {
        return ResponseEntity.ok(devopsCiPipelineService.pagePipelineExecuteTime(pipelineIds, startTime, endTime, pageRequest));
    }


    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询runner指引需要的参数")
    @GetMapping(value = "/runner_guide")
    public ResponseEntity<Map<String, String>> runnerGuide(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return ResponseEntity.ok(devopsCiPipelineService.runnerGuide(projectId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "批量查询测试任务关联的流水线")
    @PostMapping(value = "/list_task_pipeline_referrance")
    public ResponseEntity<List<PipelineInstanceReferenceVO>> listTaskReferencePipelineInfo(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "API测试任务id", required = true)
            @RequestBody Set<Long> taskIds) {
        return ResponseEntity.ok(devopsCiPipelineService.listTaskReferencePipelineInfo(projectId, taskIds));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, permissionWithin = true)
    @ApiOperation(value = "列出所有任务配置关联的流水线名称")
    @GetMapping(value = "/list_pipeline_name_reference_by_config_id")
    public ResponseEntity<List<String>> listPipelineNameReferenceByConfigId(@ApiParam(value = "项目 ID", required = true)
                                                                            @PathVariable(value = "project_id") Long projectId,
                                                                            @ApiParam(value = "API测试任务配置ID", required = true)
                                                                            @RequestParam("config_id") Long taskConfigId) {
        return Results.success(devopsCiPipelineService.listPipelineNameReferenceByConfigId(projectId, taskConfigId));
    }

    @ApiOperation("查询测试套件是否关联流水线")
    @GetMapping("/suite_related_with_pipeline")
    public ResponseEntity<Boolean> doesApiTestSuiteRelatedWithPipeline(@ApiParam(value = "项目Id", required = true)
                                                                       @PathVariable(value = "project_id") Long projectId,
                                                                       @RequestParam(value = "suite_id") Long suiteId) {
        return Results.success(devopsCiPipelineService.doesApiTestSuiteRelatedWithPipeline(projectId, suiteId));
    }


    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询流水线下定义的函数")
    @PostMapping(value = "/{pipeline_id}/functions")
    public ResponseEntity<List<DevopsCiPipelineFunctionDTO>> listFunctionsByDevopsPipelineId(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @PathVariable(value = "pipeline_id") Long pipelineId,
            @RequestParam(value = "include_default", defaultValue = "false") Boolean includeDefault) {
        return ResponseEntity.ok(devopsCiPipelineService.listFunctionsByDevopsPipelineId(projectId, pipelineId, includeDefault));
    }

}
