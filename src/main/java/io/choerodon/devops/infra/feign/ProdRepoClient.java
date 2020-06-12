package io.choerodon.devops.infra.feign;

import java.util.List;
import java.util.Set;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import io.choerodon.devops.infra.dto.repo.NexusMavenRepoDTO;
import io.choerodon.devops.infra.feign.fallback.ProdRepoClientFallback;

/**
 * 统一制品库相关接口
 *
 * @author zmf
 * @since 2020/6/12
 */
@FeignClient(value = "hrds-prod-repo", fallback = ProdRepoClientFallback.class)
public interface ProdRepoClient {
    @ApiOperation(value = "CI-流水线-获取项目下仓库列表")
    @GetMapping("/v1/nexus-repositorys/{organizationId}/project/{projectId}/ci/repo/list")
    ResponseEntity<List<NexusMavenRepoDTO>> getRepoByProject(
            @ApiParam(value = "组织ID", required = true)
            @PathVariable(name = "organizationId") Long organizationId,
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(name = "projectId") Long projectId,
            @ApiParam(value = "制品类型: MAVEN、NPM ", required = true)
            @RequestParam String repoType,
            @ApiParam(value = "nexus仓库类型: hosted、proxy、group", required = false)
            @RequestParam(name = "type", required = false) String type);


    @ApiOperation(value = "CI-流水线-获取项目下仓库列表-包含用户信息")
    @GetMapping("/v1/nexus-repositorys/{organizationId}/project/{projectId}/ci/repo/user/list")
    ResponseEntity<List<NexusMavenRepoDTO>> getRepoUserByProject(
            @ApiParam(value = "组织ID", required = true)
            @PathVariable(name = "organizationId") Long organizationId,
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(name = "projectId") Long projectId,
            @ApiParam(value = "仓库主键list", required = true)
            @RequestParam Set<Long> repositoryIds);
}
