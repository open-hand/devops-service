package io.choerodon.devops.infra.feign;

import java.util.List;
import java.util.Set;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.devops.api.vo.hrdsCode.MemberPrivilegeViewDTO;
import io.choerodon.devops.api.vo.hrdsCode.RepositoryPrivilegeViewDTO;
import io.choerodon.devops.infra.dto.repo.RdmMemberViewDTO;
import io.choerodon.devops.infra.feign.fallback.HrdsCodeRepoClientFallback;
<<<<<<< HEAD
import io.choerodon.devops.infra.feign.fallback.HzeroMessageServiceClientFallBack;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
=======
>>>>>>> origin/master

/**
 * @author scp
 * @date 2020/6/11
 * @description
 */
<<<<<<< HEAD
@FeignClient(value = "code-repo-service", fallback = HrdsCodeRepoClientFallback.class)
=======
@FeignClient(value = "hrds-code-repo", fallback = HrdsCodeRepoClientFallback.class)
>>>>>>> origin/master
public interface HrdsCodeRepoClient {

    /**
     * 查询当前用户在gitlab下的角色
     *
     * @param organizationId
     * @param projectId
     * @param repositoryIds  应用服务Id
     * @return
     */
    @PostMapping("/v1/organizations/{organizationId}/projects/{projectId}/gitlab/repositories/members/self/privilege")
    ResponseEntity<List<MemberPrivilegeViewDTO>> selfPrivilege(@PathVariable("organizationId") Long organizationId,
                                                               @PathVariable("projectId") Long projectId,
                                                               @RequestBody Set<Long> repositoryIds);

    /**
     * 查询用户在项目下有权限的应用服务
     *
     * @param organizationId
     * @param projectId
     * @param userIds        用户id
     * @return
     */
    @PostMapping("/v1/organizations/{organizationId}/projects/{projectId}/gitlab/members/repositories/within")
    ResponseEntity<List<RepositoryPrivilegeViewDTO>> listRepositoriesByPrivilege(@PathVariable("organizationId") Long organizationId,
                                                                                 @PathVariable("projectId") Long projectId,
                                                                                 @RequestBody Set<Long> userIds);

    /**
     * 查询应用服务下有权限的成员 包括项目所有者
     *
     * @param organizationId
     * @param projectId
     * @return
     */
    @GetMapping("/v1/organizations/{organizationId}/projects/{projectId}/gitlab/repositories/members/list")
    ResponseEntity<List<RdmMemberViewDTO>> listMembers(@PathVariable("organizationId") Long organizationId,
                                                       @PathVariable("projectId") Long projectId,
                                                       @RequestParam(name = "repositoryIds") Set<Long> repositoryIds,
                                                       @RequestParam(name = "repositoryName") String repositoryName,
                                                       @RequestParam(name = "realName") String realName,
                                                       @RequestParam(name = "loginName") String loginName,
                                                       @RequestParam(name = "params") String params,
                                                       @RequestParam(name = "enabled") Boolean enabled,
                                                       @RequestParam(name = "syncGitlabFlag") Boolean syncGitlabFlag,
                                                       @RequestParam(name = "glExpiresFlag") Boolean glExpiresFlag);


}
