package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;

import io.swagger.annotations.ApiParam;
import java.util.List;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.infra.dto.repo.C7nNexusComponentDTO;
import io.choerodon.devops.infra.dto.repo.C7nNexusServerDTO;
import io.choerodon.devops.infra.feign.RdupmClient;
import io.choerodon.swagger.annotation.Permission;

@RestController
@RequestMapping("/v1/nexus")
public class NexusController {

    @Autowired
    private RdupmClient rdupmClient;


    @ApiOperation(value = "choerodon-获取项目下nexus服务列表")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/choerodon/{organizationId}/project/{projectId}/nexus/server/list")
    public ResponseEntity<List<C7nNexusServerDTO>> getNexusServerByProject(@ApiParam(value = "组织ID", required = true)
                                                                           @PathVariable(name = "organizationId") Long organizationId,
                                                                           @ApiParam(value = "项目Id", required = true)
                                                                           @PathVariable(name = "projectId") Long projectId) {

        return Results.success(rdupmClient.getNexusServerByProject(organizationId, projectId).getBody());
    }

//
//    @GetMapping("/{organizationId}/project/{projectId}/repo/maven/components")
//    @ApiOperation(value = "查询项目下所有的仓库配置")
//    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
//    public ResponseEntity<List<C7nNexusComponentDTO>> listMavenComponents() {
//
//    }

}
