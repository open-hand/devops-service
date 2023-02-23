package io.choerodon.devops.infra.feign;

import java.util.List;
import java.util.Set;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.NexusServerConfig;
import io.choerodon.devops.api.vo.harbor.HarborCustomRepo;
import io.choerodon.devops.api.vo.harbor.HarborImageTagVo;
import io.choerodon.devops.api.vo.hrds.HarborC7nRepoImageTagVo;
import io.choerodon.devops.api.vo.hrds.HarborC7nRepoVo;
import io.choerodon.devops.api.vo.rdupm.ResponseVO;
import io.choerodon.devops.infra.dto.harbor.HarborAllRepoDTO;
import io.choerodon.devops.infra.dto.harbor.HarborRepoDTO;
import io.choerodon.devops.infra.dto.repo.C7nNexusComponentDTO;
import io.choerodon.devops.infra.dto.repo.C7nNexusRepoDTO;
import io.choerodon.devops.infra.dto.repo.C7nNexusServerDTO;
import io.choerodon.devops.infra.dto.repo.NexusMavenRepoDTO;
import io.choerodon.devops.infra.feign.fallback.RdupmClientFallback;
import io.choerodon.swagger.annotation.Permission;

/**
 * User: Mr.Wang
 * Date: 2020/6/11
 */
@FeignClient(value = "prod-repo-service", fallback = RdupmClientFallback.class)
public interface RdupmClient {

    /**
     * 查询项目下所有自定义仓库
     */
    @GetMapping("/v1/harbor-choerodon-repos/project/{projectId}/list_all_custom_repo")
    ResponseEntity<List<HarborCustomRepo>> listAllCustomRepoByProject(@PathVariable("projectId") Long projectId);


    /**
     * 查询关联的自定义仓库
     */
    @GetMapping("/v1/harbor-choerodon-repos/project/{projectId}/{appServiceId}/list_related_custom_repo")
    ResponseEntity<HarborCustomRepo> listRelatedCustomRepoByService(@ApiParam(value = "猪齿鱼项目ID", required = true)
                                                                    @PathVariable("projectId") Long projectId,
                                                                    @ApiParam(value = "应用服务ID", required = true)
                                                                    @PathVariable("appServiceId") Long appServiceId);

    /**
     * 保存关联关系
     */
    @PostMapping("/v1/harbor-choerodon-repos/project/{projectId}/{appServiceId}/save_relation")
    ResponseEntity saveRelationByService(@ApiParam(value = "猪齿鱼项目ID", required = true)
                                         @PathVariable("projectId") Long projectId,
                                         @ApiParam(value = "应用服务ID", required = true)
                                         @PathVariable("appServiceId") Long appServiceId,
                                         @ApiParam(value = "自定义仓库ID", required = true)
                                         @RequestParam("customRepoId") Long customRepoId);


    /**
     * 删除关联关系
     */
    @DeleteMapping("/v1/harbor-choerodon-repos/project/{projectId}/{appServiceId}/delete_relation")
    ResponseEntity deleteRelationByService(@ApiParam(value = "猪齿鱼项目ID", required = true)
                                           @PathVariable("projectId") Long projectId,
                                           @ApiParam(value = "应用服务ID", required = true)
                                           @PathVariable("appServiceId") Long appServiceId,
                                           @ApiParam(value = "自定义仓库ID", required = true)
                                           @RequestParam("customRepoId") Long customRepoId);


    /**
     * 删除关联关系
     */
    @DeleteMapping("/v1/harbor-choerodon-repos/project/{projectId}/{appServiceId}/delete_all_relation")
    ResponseEntity deleteAllRelationByService(@ApiParam(value = "猪齿鱼项目ID", required = true)
                                              @PathVariable("projectId") Long projectId,
                                              @ApiParam(value = "应用服务ID", required = true)
                                              @PathVariable("appServiceId") Long appServiceId);

    /**
     * 仓库配置查询接口
     * 应用服务关联了自定义仓库就返回自定义，
     * 否则返回共享自定义仓库，否则返回默认
     */
    @GetMapping("/v1/harbor-choerodon-repos/project/{projectId}/{appServiceId}/harbor_repo_config")
    ResponseEntity<HarborRepoDTO> queryHarborRepoConfig(@ApiParam(value = "猪齿鱼项目ID", required = true)
                                                        @PathVariable("projectId") Long projectId,
                                                        @ApiParam(value = "应用服务ID", required = true)
                                                        @PathVariable("appServiceId") Long appServiceId);


    @ApiOperation(value = "CI-流水线-获取项目下仓库列表")
    @GetMapping("/v1/nexus-repositorys/{organizationId}/project/{projectId}/ci/repo/list")
    ResponseEntity<List<NexusMavenRepoDTO>> getRepoByProject(
            @ApiParam(value = "组织ID", required = true)
            @PathVariable(name = "organizationId") Long organizationId,
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(name = "projectId") Long projectId,
            @ApiParam(value = "制品类型: MAVEN、NPM ", required = true)
            @RequestParam("repoType") String repoType,
            @ApiParam(value = "nexus仓库类型: hosted、proxy、group", required = false)
            @RequestParam(name = "type", required = false) String type);

    /**
     * 根据Harbor仓库ID查询仓库配置
     */
    @GetMapping("/v1/harbor-choerodon-repos/project/{projectId}/harbor_config_by_id")
    ResponseEntity<HarborRepoDTO> queryHarborRepoConfigById(@ApiParam(value = "猪齿鱼项目ID", required = true)
                                                            @PathVariable("projectId") Long projectId,
                                                            @ApiParam(value = "仓库ID", required = false)
                                                            @RequestParam("repoId") Long repoId,
                                                            @ApiParam(value = "仓库类型", required = true)
                                                            @RequestParam("repoType") String repoType);


    @ApiOperation(value = "CI-流水线-获取项目下仓库列表-包含用户信息")
    @GetMapping("/v1/nexus-repositorys/{organizationId}/project/{projectId}/ci/repo/user/list")
    ResponseEntity<List<NexusMavenRepoDTO>> getRepoUserByProject(
            @ApiParam(value = "组织ID", required = true)
            @PathVariable(name = "organizationId") Long organizationId,
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(name = "projectId") Long projectId,
            @ApiParam(value = "仓库主键list", required = true)
            @RequestParam("repositoryIds") Set<Long> repositoryIds);

    @ApiOperation(value = "查询仓库-包含默认账户信息")
    @Permission(permissionWithin = true)
    @GetMapping("/v1/nexus-repositorys/project/{projectId}/repo/{repositoryId}/with_default_user")
    ResponseEntity<String> queryRepoWithDefaultUserInfo(@PathVariable(name = "projectId") Long projectId,
                                                        @PathVariable(name = "repositoryId") Long repositoryId);


    @ApiOperation(value = "查询项目下所有Harbor仓库")
    @GetMapping("/v1/harbor-choerodon-repos/project/{projectId}/all_harbor_config")
    ResponseEntity<HarborAllRepoDTO> queryAllHarborRepoConfig(@ApiParam(value = "猪齿鱼项目ID", required = true)
                                                              @PathVariable("projectId") Long projectId);

    @ApiOperation(value = "获取项目下nexus服务列表")
    @GetMapping("/v1/nexus-repositorys/choerodon/{organizationId}/project/{projectId}/nexus/server/list")
    ResponseEntity<List<C7nNexusServerDTO>> getNexusServerByProject(@ApiParam(value = "组织ID", required = true)
                                                                    @PathVariable(name = "organizationId") Long organizationId,
                                                                    @ApiParam(value = "项目Id", required = true)
                                                                    @PathVariable(name = "projectId") Long projectId);


    @ApiOperation(value = "choerodon-获取maven仓库下的包列表")
    @GetMapping("/v1/nexus-repositorys/choerodon/{organizationId}/project/{projectId}/repo/maven/components")
    ResponseEntity<List<C7nNexusComponentDTO>> listMavenComponents(@ApiParam(value = "组织ID", required = true)
                                                                   @PathVariable(name = "organizationId") Long organizationId,
                                                                   @ApiParam(value = "项目Id", required = true)
                                                                   @PathVariable(name = "projectId") Long projectId,
                                                                   @ApiParam(value = "仓库Id", required = true)
                                                                   @RequestParam(name = "repositoryId") Long repositoryId,
                                                                   @ApiParam(value = "groupId", required = false)
                                                                   @RequestParam(name = "groupId", required = false) String groupId,
                                                                   @ApiParam(value = "artifactId", required = false)
                                                                   @RequestParam(name = "artifactId", required = false) String artifactId,
                                                                   @RequestParam(name = "version", required = false) String version);


    @ApiOperation(value = "choerodon-获取nexus服务下、项目下的maven仓库")
    @GetMapping("/v1/nexus-repositorys/choerodon/{organizationId}/project/{projectId}/repo/maven/list")
    ResponseEntity<List<C7nNexusRepoDTO>> getMavenRepoByConfig(@ApiParam(value = "组织ID", required = true)
                                                               @PathVariable(name = "organizationId") Long organizationId,
                                                               @ApiParam(value = "项目Id", required = true)
                                                               @PathVariable(name = "projectId") Long projectId,
                                                               @ApiParam(value = "服务配Id", required = true)
                                                               @RequestParam(name = "configId") Long configId,
                                                               @RequestParam(name = "type") String type);


    @ApiOperation(value = "根据项目ID获取镜像仓库列表")
    @GetMapping("/v1/harbor-choerodon-repos/listImageRepo")
    ResponseEntity<List<HarborC7nRepoVo>> listImageRepo(@ApiParam(value = "猪齿鱼项目ID", required = true) @RequestParam("projectId") Long projectId);


    @ApiOperation(value = "根据仓库类型+仓库ID+镜像名称获取获取镜像版本")
    @GetMapping("/v1/harbor-choerodon-repos/listImageTag")
    ResponseEntity<HarborC7nRepoImageTagVo> listImageTag(@ApiParam(value = "仓库类型", required = true) @RequestParam(value = "repoType") String repoType,
                                                         @ApiParam(value = "仓库ID", required = true) @RequestParam("repoId") Long repoId,
                                                         @ApiParam(value = "镜像名称", required = true) @RequestParam("imageName") String imageName,
                                                         @ApiParam(value = "镜像版本号,模糊查询") @RequestParam(required = false, value = "tagName") String tagName);

    @ApiOperation(value = "项目层/组织层--删除镜像TAG")
    @DeleteMapping(value = "/v1/harbor-choerodon-repos/image-tag/delete")
    ResponseEntity<ResponseVO> deleteImageTag(@ApiParam(value = "仓库名称") @RequestParam(name = "repoName") String repoName,
                                              @ApiParam(value = "版本号") @RequestParam(name = "tagName") String tagName);

    @ApiOperation(value = "项目层/组织层--镜像TAG列表")
    @GetMapping(value = "/v1/harbor-image-tag/list/{projectId}")
    ResponseEntity<Page<HarborImageTagVo>> pagingImageTag(@PathVariable(value = "projectId") Long projectId,
                                                          @RequestParam(value = "repoName") String repoName,
                                                          @RequestParam(value = "tagName", required = false) String tagName);

    @ApiOperation(value = "根据仓库id查询maven仓库")
    @GetMapping("/v1/nexus-repositorys/{organizationId}/project/{projectId}/maven/repo/{repositoryId}")
    ResponseEntity<C7nNexusRepoDTO> getMavenRepo(@ApiParam(value = "组织ID", required = true) @PathVariable(name = "organizationId") Long organizationId,
                                                 @ApiParam(value = "项目Id", required = true) @PathVariable(name = "projectId") Long projectId,
                                                 @ApiParam(value = "仓库主键Id", required = true) @PathVariable(name = "repositoryId") @Encrypt Long repositoryId);


    @ApiOperation(value = "查询默认的nexus仓库配置")
    @GetMapping("/v1/nexus-repositorys/{organizationId}/default/maven")
    ResponseEntity<NexusServerConfig> getDefaultMavenRepo(@ApiParam(value = "组织ID", required = true) @PathVariable(name = "organizationId") Long organizationId);


    @ApiOperation(value = "根据仓库id集合查询仓库列表")
    @PostMapping("/v1/harbor-choerodon-repos/harbor_repo_config/by_ids")
    ResponseEntity<List<HarborRepoDTO>> queryHarborReposByIds(@RequestBody Set<Long> harborConfigIds);


    @ApiOperation(value = "组织层-查询关联应用服务列表")
    @GetMapping("/v1/projects/{project_id}/custom_repos/{repo_id}/basic_info_internal")
    ResponseEntity<HarborCustomRepo> queryCustomRepoById(@PathVariable("project_id") Long projectId,
                                                         @PathVariable("repo_id") Long repoId);
}
