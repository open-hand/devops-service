package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.app.service.HarborService;
import io.choerodon.devops.infra.dto.DevopsConfigDTO;
import io.choerodon.devops.infra.dto.harbor.HarborRepoConfigDTO;
import io.choerodon.swagger.annotation.Permission;

/**
 * User: Mr.Wang
 * Date: 2020/6/11
 */
@RestController
@RequestMapping("/v1/harbor")
public class HarborController {

    @Autowired
    private HarborService harborService;

    @GetMapping("/{projectId}/repo/list")
    @ApiOperation(value = "查询项目下所有的仓库配置")
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    public ResponseEntity<List<HarborRepoConfigDTO>> listAllCustomRepoByProject(
            @ApiParam(value = "猪齿鱼项目ID", required = true)
            @PathVariable("projectId") Long projectId) {
        List<HarborRepoConfigDTO> list = harborService.listAllCustomRepoByProject(projectId);
        return Results.success(list);
    }

    @PostMapping("/repo/list_by_versionIds")
    @ApiOperation(value = "根据应用服务版本id查询harbor配置/内部接口，market-service用")
    @Permission(permissionWithin = true)
    public ResponseEntity<Map<Long, DevopsConfigDTO>> listRepoConfigByAppVersionIds(
            @RequestBody List<Long> appVersionIds) {
        return Results.success(harborService.listRepoConfigByAppVersionIds(appVersionIds));
    }

}
