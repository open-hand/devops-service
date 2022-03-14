package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.CiCdPipelineVO;
import io.choerodon.devops.api.vo.pipeline.PipelineTemplateCompositeVO;
import io.choerodon.devops.app.service.PipelineTemplateService;
import io.choerodon.devops.infra.dto.PipelineTemplateDTO;
import io.choerodon.swagger.annotation.Permission;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/12/1 14:29
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/pipeline_templates")
public class ProjectPipelineTemplateController {

    @Autowired
    private PipelineTemplateService pipelineTemplateService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询项目下可用的流水线模板列表")
    @GetMapping
    public ResponseEntity<PipelineTemplateCompositeVO> listTemplateWithLanguage(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return ResponseEntity.ok(pipelineTemplateService.listTemplateWithLanguage(projectId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "根据模板id查询流水线配置信息")
    @GetMapping("{template_id}")
    public ResponseEntity<CiCdPipelineVO> queryPipelineInfoByTemplateId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "模板ID", required = true)
            @PathVariable(value = "template_id") Long templateId) {
        return ResponseEntity.ok(pipelineTemplateService.queryPipelineInfoByTemplateId(projectId, templateId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "根据模板id查询流水线配置信息")
    @GetMapping("{template_id}/basic_info")
    public ResponseEntity<PipelineTemplateDTO> queryBasicInfoById(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "模板ID", required = true)
            @PathVariable(value = "template_id") Long templateId) {
        return ResponseEntity.ok(pipelineTemplateService.baseQuery(templateId));
    }
}
