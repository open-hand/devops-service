package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;

import io.swagger.annotations.ApiParam;
import java.util.List;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.infra.dto.repo.C7nNexusComponentDTO;
import io.choerodon.devops.infra.dto.repo.C7nNexusRepoDTO;
import io.choerodon.devops.infra.dto.repo.C7nNexusServerDTO;
import io.choerodon.devops.infra.enums.CiJobScriptTypeEnum;
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


    @GetMapping("/{organizationId}/project/{projectId}/repo/maven/components")
    @ApiOperation(value = "查询项目下所有的仓库配置")
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    public ResponseEntity<List<C7nNexusComponentDTO>> listMavenComponents(@ApiParam(value = "组织ID", required = true)
                                                                          @PathVariable(name = "organizationId") Long organizationId,
                                                                          @ApiParam(value = "项目Id", required = true)
                                                                          @PathVariable(name = "projectId") Long projectId,
                                                                          @ApiParam(value = "仓库Id", required = true)
                                                                          @RequestParam(name = "repositoryId") Long repositoryId,
                                                                          @ApiParam(value = "groupId", required = false)
                                                                          @RequestParam(name = "groupId", required = false) String groupId,
                                                                          @ApiParam(value = "artifactId", required = false)
                                                                          @RequestParam(name = "artifactId", required = false) String artifactId,
                                                                          @ApiParam(value = "versionRegular", required = false)
                                                                          @RequestParam(name = "versionRegular", required = false) String versionRegular) {
        return Results.success(rdupmClient.listMavenComponents(organizationId, projectId, repositoryId, groupId, artifactId, versionRegular).getBody());
    }


    @ApiOperation(value = "choerodon-获取nexus服务下、项目下的maven仓库")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/choerodon/{organizationId}/project/{projectId}/repo/maven/list")
    public ResponseEntity<List<C7nNexusRepoDTO>> getMavenRepoByConfig(@ApiParam(value = "组织ID", required = true)
                                                                 @PathVariable(name = "organizationId") Long organizationId,
                                                                 @ApiParam(value = "项目Id", required = true)
                                                                 @PathVariable(name = "projectId") Long projectId,
                                                                 @ApiParam(value = "服务配Id", required = true)
                                                                 @RequestParam(name = "configId") Long configId) {

        return Results.success(rdupmClient.getMavenRepoByConfig(organizationId, projectId, configId, CiJobScriptTypeEnum.MAVEN.getType()).getBody());
    }

}
