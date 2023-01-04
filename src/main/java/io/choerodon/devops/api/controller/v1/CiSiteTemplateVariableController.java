package io.choerodon.devops.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.template.CiTemplateVariableVO;
import io.choerodon.devops.app.service.CiTemplateVariableBusService;
import io.choerodon.swagger.annotation.Permission;

/**
 * Created by wangxiang on 2021/12/22
 */
@RestController("ciSiteTemplateVariableController.v1")
@RequestMapping("/v1/site/{source_id}/ci_template_variable")
public class CiSiteTemplateVariableController {

    @Autowired
    private CiTemplateVariableBusService ciTemplateVariableBusService;


    @ApiOperation(value = "根据流水线模板id查询变量）")
    @Permission(level = ResourceLevel.SITE)
    @GetMapping("/ci_pipeline_template/{ci_pipeline_template_id}")
    public ResponseEntity<List<CiTemplateVariableVO>> queryCiVariableByPipelineTemplateId(
            @PathVariable(value = "source_id") Long sourceId,
            @Encrypt @PathVariable(value = "ci_pipeline_template_id") Long ciPipelineTemplateId) {
        return ResponseEntity.ok(ciTemplateVariableBusService.queryCiVariableByPipelineTemplateId(sourceId, ciPipelineTemplateId));
    }


}
