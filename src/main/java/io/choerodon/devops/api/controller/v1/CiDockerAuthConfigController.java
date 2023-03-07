package io.choerodon.devops.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.app.service.CiDockerAuthConfigService;
import io.choerodon.devops.infra.dto.CiDockerAuthConfigDTO;
import io.choerodon.swagger.annotation.Permission;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/12/22 10:59
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/docker_auth_configs")
public class CiDockerAuthConfigController {

    @Autowired
    private CiDockerAuthConfigService ciDockerAuthConfigService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "根据流水线id查询Docker认证配置")
    @GetMapping
    public ResponseEntity<List<CiDockerAuthConfigDTO>> listByPipelineId(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "流水线Id", required = true)
            @RequestParam(value = "pipeline_id") Long pipelineId) {
        return ResponseEntity.ok(ciDockerAuthConfigService.listByPipelineId(pipelineId));
    }
}
