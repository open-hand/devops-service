package io.choerodon.devops.api.controller.v1;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.dto.PipelineValueDTO;
import io.choerodon.devops.app.service.PipelineValueService;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Optional;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:56 2019/4/10
 * Description:
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/pipeline_value")
public class PipelineValueController {
    @Autowired
    private PipelineValueService pipelineValueService;

    /**
     * 项目下获取流水线配置
     *
     * @param projectId   项目Id
     * @param pageRequest 分页参数
     * @param params      查询参数
     * @return
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下获取流水线配置")
    @CustomPageRequest
    @PostMapping("/list_by_options")
    public ResponseEntity<Page<PipelineValueDTO>> listByOptions(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用Id", required = false)
            @RequestParam(value = "app_id", required = false) Long appId,
            @ApiParam(value = "环境Id", required = false)
            @RequestParam(value = "env_id", required = false) Long envId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(pipelineValueService.listByOptions(projectId, appId, envId, pageRequest, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.pipeline.value.list"));
    }

    /**
     * 项目下创建流水线配置
     *
     * @param projectId        项目Id
     * @param pipelineValueDTO 配置信息
     * @return
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下创建流水线配置")
    @PostMapping
    public ResponseEntity<PipelineValueDTO> createOrUpdate(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "PipelineValueDTO")
            @ApiIgnore PipelineValueDTO pipelineValueDTO) {
        return Optional.ofNullable(pipelineValueService.createOrUpdate(pipelineValueDTO))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.pipeline.value.createOrUpdate"));
    }

    /**
     * 项目下查询配置详情
     *
     * @param projectId 项目Id
     * @param valueId   配置Id
     * @return
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下查询配置详情")
    @GetMapping
    public ResponseEntity<PipelineValueDTO> queryById(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "valueId", required = true)
            @ApiIgnore(value = "value_id") Long valueId) {
        return Optional.ofNullable(pipelineValueService.queryById(valueId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.pipeline.value.queryById"));
    }

    /**
     * 项目下删除配置
     *
     * @param projectId 项目Id
     * @param valueId   配置Id
     * @return
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下删除配置")
    @DeleteMapping
    public ResponseEntity<Boolean> delete(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "valueId", required = true)
            @ApiIgnore(value = "value_id") Long valueId) {
        return Optional.ofNullable(pipelineValueService.delete(projectId, valueId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.pipeline.value.delete"));
    }


    /**
     * 名称校验
     *
     * @param projectId
     * @param name
     * @return
     */
    @ApiOperation(value = "名称校验")
    @GetMapping("/check_name")
    public void checkName(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "名称", required = true)
            @RequestParam(value = "name") String name) {
        pipelineValueService.checkName(projectId, name);
    }
}
