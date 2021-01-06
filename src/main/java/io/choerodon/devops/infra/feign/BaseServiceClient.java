package io.choerodon.devops.infra.feign;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.OrgAdministratorVO;
import io.choerodon.devops.api.vo.ResourceLimitVO;
import io.choerodon.devops.api.vo.RoleAssignmentSearchVO;
import io.choerodon.devops.api.vo.iam.UserVO;
import io.choerodon.devops.infra.dto.iam.*;
import io.choerodon.devops.infra.feign.fallback.BaseServiceClientFallback;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by younger on 2018/3/29.
 */

@FeignClient(value = "choerodon-iam", fallback = BaseServiceClientFallback.class)
public interface BaseServiceClient {

    @GetMapping(value = "/choerodon/v1/projects/{projectId}")
    ResponseEntity<ProjectDTO> queryIamProject(@PathVariable("projectId") Long projectId,
                                               @RequestParam(value = "with_category_info") Boolean withCategoryInfo,
                                               @RequestParam(value = "with_user_info") Boolean withUserInfo,
                                               @RequestParam(value = "with_agile_info") Boolean withAgileInfo);

    /**
     * @param organizationId 组织id
     * @param withMoreInfo   获取更详细的组织配置信息以及用户信息
     * @return
     */
    @GetMapping(value = "/choerodon/v1/organizations/{organizationId}")
    ResponseEntity<Tenant> queryOrganizationById(@PathVariable("organizationId") Long organizationId,
                                                 @RequestParam(value = "with_more_info") Boolean withMoreInfo);

    /**
     * 根据id集合查询组织
     *
     * @param ids id集合，去重
     * @return 组织集合
     */
    @PostMapping(value = "/choerodon/v1/organizations/ids")
    ResponseEntity<List<Tenant>> queryOrgByIds(@RequestBody Set<Long> ids);

    @GetMapping(value = "/choerodon/v1/users")
    ResponseEntity<IamUserDTO> queryByLoginName(@RequestParam("login_name") String loginName);

    @GetMapping(value = "/choerodon/v1/users/{id}/info")
    ResponseEntity<IamUserDTO> queryById(@PathVariable("id") Long id);

    @PostMapping(value = "/choerodon/v1/users/ids")
    ResponseEntity<String> listUsersByIds(@RequestBody Long[] ids, @RequestParam(value = "only_enabled") Boolean onlyEnabled);

    @GetMapping(value = "/choerodon/v1/projects/{project_id}/users")
    ResponseEntity<Page<IamUserDTO>> listUsersByEmail(@PathVariable("project_id") Long projectId, @RequestParam("page") int page, @RequestParam("size") int size, @RequestParam("email") String email);

    @PostMapping(value = "/choerodon/v1/users/projects/{project_id}/gitlab_role/users")
    ResponseEntity<List<IamUserDTO>> listUsersWithGitlabLabel(
            @PathVariable(name = "project_id") Long projectId,
            @RequestBody RoleAssignmentSearchVO roleAssignmentSearchVO,
            @RequestParam(name = "label_name") String labelName);

    @GetMapping(value = "/choerodon/v1/organizations/{organization_id}/projects")
    ResponseEntity<Page<ProjectDTO>> pageProjectsByOrgId(@PathVariable(name = "organization_id") Long organizationId,
                                                         @RequestParam Map<String, Object> pageable,
                                                         @RequestParam(name = "name", required = false) String name,
                                                         @RequestParam(name = "code", required = false) String code,
                                                         @RequestParam(name = "enabled", required = false) Boolean enabled,
                                                         @RequestParam(value = "params", required = false) String params);


    @PostMapping(value = "/choerodon/v1/projects/ids")
    ResponseEntity<List<ProjectDTO>> queryByIds(@RequestBody Set<Long> ids);


    /**
     * 根据组织Id及项目code查询项目
     *
     * @param organizationId 组织Id
     * @param projectCode    项目code
     * @return 根据组织Id及项目code查询项目
     */
    @GetMapping(value = "/choerodon/v1/organizations/{organization_id}/projects/by_code")
    ResponseEntity<ProjectDTO> queryProjectByCodeAndOrgId(@PathVariable(name = "organization_id") Long organizationId,
                                                          @RequestParam(name = "code") String projectCode);

    /**
     * 组织下创client
     *
     * @param clientVO clientVO
     * @return 分配结果
     */
    @PostMapping(value = "/choerodon/v1/organizations/{organization_id}/clients")
    ResponseEntity<ClientVO> createClient(@PathVariable("organization_id") Long organizationId,
                                          @RequestBody @Valid ClientVO clientVO);

    /**
     * 组织下删除client
     *
     * @param organizationId 组织id
     * @param clientId       clientId
     * @return 分配结果
     */
    @DeleteMapping(value = "/choerodon/v1/organizations/{organization_id}/clients/{client_id}")
    ResponseEntity deleteClient(@PathVariable("organization_id") Long organizationId, @PathVariable("client_id") Long clientId);

    /**
     * 根据集群Id和组织Id查询client
     *
     * @param organizationId
     * @param clientId
     * @return
     */
    @GetMapping(value = "/v1/{organization_id}/clients/{client_id}")
    ResponseEntity<ClientVO> queryClientBySourceId(@PathVariable("organization_id") Long organizationId, @PathVariable("client_id") Long clientId);


    @GetMapping(value = "/choerodon/v1/users/{id}/projects/{project_id}/check_is_gitlab_owner")
    ResponseEntity<Boolean> checkIsGitlabProjectOwner(
            @PathVariable("id") Long id,
            @PathVariable("project_id") Long projectId);

    @ApiOperation(value = "判断用户是否为组织层root")
    @GetMapping(value = "/choerodon/v1/users/{id}/projects/{project_id}/check_is_gitlab_org_owner")
    ResponseEntity<Boolean> checkIsGitlabOrgOwner(@PathVariable("id") Long id,
                                                  @PathVariable("project_id") Long projectId);


    /**
     * 判断用户是否是平台root用户
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/choerodon/v1/users/{id}/check_is_root")
    ResponseEntity<Boolean> checkIsRoot(@PathVariable("id") Long id);

    /**
     * 判段用户是否是组织root用户
     *
     * @param organizationId
     * @param userId
     * @return
     */
    @GetMapping(value = "/choerodon/v1/organizations/{organization_id}/users/{user_id}/check_is_root")
    ResponseEntity<Boolean> checkIsOrgRoot(@PathVariable(name = "organization_id") Long organizationId,
                                           @PathVariable(name = "user_id") Long userId);

    /**
     * 校验用户是否是项目所有者
     *
     * @param id        用户id
     * @param projectId 项目id
     * @return true表示是
     */
    @GetMapping(value = "/choerodon/v1/users/{id}/projects/{project_id}/check_is_owner")
    ResponseEntity<Boolean> checkIsProjectOwner(
            @PathVariable("id") Long id,
            @PathVariable("project_id") Long projectId);

    @GetMapping(value = "/choerodon/v1/projects/{project_id}/owner/list")
    ResponseEntity<List<IamUserDTO>> listProjectOwnerByProjectId(@PathVariable("project_id") Long projectId);

    /**
     * 判断组织是否是注册组织
     *
     * @param organizationId 组织id
     * @return true表示是
     */
    @GetMapping(value = "/choerodon/v1/organizations/{tenant_id}/check_is_register")
    ResponseEntity<Boolean> checkOrganizationIsRegister(@PathVariable(name = "tenant_id") Long organizationId);


    @GetMapping(value = "/choerodon/v1/organizations/{organization_id}/org_administrator")
    ResponseEntity<Page<OrgAdministratorVO>> listOrgAdministrator(
            @PathVariable("organization_id") Long organizationId,
            @RequestParam("size") Integer size);

    /**
     * 查询组织下指定角色的id
     */
    @GetMapping(value = "/choerodon/v1/organizations/{organization_id}/roles")
    ResponseEntity<List<RoleDTO>> getRoleByCode(
            @PathVariable("organization_id") Long organizationId,
            @RequestParam("role_code") String code,
            @RequestParam("label_name") String labelName);

    /**
     * 查询资源限制
     */
    @GetMapping("/choerodon/v1/organizations/resource_limit")
    ResponseEntity<ResourceLimitVO> queryResourceLimit();

    /**
     * 根据名称查询客户端
     */
    @GetMapping("/choerodon/v1/organizations/{organization_id}/clients/query_by_name")
    ResponseEntity<ClientVO> queryClientByName(@PathVariable("organization_id") Long organizationId,
                                               @RequestParam(value = "client_name") String clientName);

    /**
     * 批量根据项目id查询用户在这个项目下拥有的角色标签, 如果在某个项目下没有角色, 不会包含该项目的纪录
     *
     * @param userId     用户id
     * @param projectIds 项目id
     * @return 标签
     */
    @PostMapping("/choerodon/v1/users/{user_id}/project_role_labels")
    ResponseEntity<List<UserProjectLabelVO>> listRoleLabelsForUserInTheProject(
            @PathVariable("user_id") Long userId,
            @RequestBody Set<Long> projectIds);

    @GetMapping("/choerodon/v1/organizations/{organization_id}/users/{user_id}/owned_projects")
    ResponseEntity<List<ProjectDTO>> listOwnedProjects(@PathVariable("organization_id") Long organizationId,
                                                       @PathVariable("user_id") Long userId);

    @GetMapping("/choerodon/v1/sync/user")
    ResponseEntity<List<UserVO>> listUserByCreationDate();

    /**
     * 查询项目下的用户
     */
    @GetMapping("/choerodon/v1/projects/{project_id}/all/users")
    ResponseEntity<List<IamUserDTO>> queryUserByProjectId(@PathVariable("project_id") Long projectId);

    /**
     * 查询所有root
     */
    @GetMapping("/choerodon/v1/users/root")
    ResponseEntity<List<IamUserDTO>> queryRoot();

    /**
     * @return {@link UserCountVO}
     */
    @ApiOperation(value = "查询平台中所有用户的数量")
    @GetMapping(value = "/choerodon/v1/users/all_user_count")
    ResponseEntity<String> countAllUsers();

    /**
     * @return {@link Set<Long>}
     */
    @ApiOperation(value = "查询平台中所有用户的id")
    @GetMapping(value = "/choerodon/v1/users/all_user_ids")
    ResponseEntity<String> listAllUserIds();
}
