package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.app.service.AppExternalConfigService;
import io.choerodon.devops.infra.dto.AppExternalConfigDTO;
import io.choerodon.swagger.annotation.Permission;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/9/30 10:01
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/app_external_configs")
public class AppExternalConfigController {

    @Autowired
    private AppExternalConfigService appExternalConfigService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "修改外部认证配置")
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @PathVariable(value = "id") Long id,
            @RequestBody @Validated AppExternalConfigDTO appExternalConfigDTO) {
        appExternalConfigService.update(projectId, id, appExternalConfigDTO);
        return ResponseEntity.noContent().build();
    }
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询外部认证配置")
    @GetMapping("/{id}")
    public ResponseEntity<AppExternalConfigDTO> query(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @PathVariable(value = "id") Long id) {
        return ResponseEntity.ok(appExternalConfigService.baseQueryWithoutPasswordAndToken(id));
    }
}
