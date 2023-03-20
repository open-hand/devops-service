package io.choerodon.devops.infra.feign;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.ResourceLimitVO;
import io.choerodon.devops.api.vo.RoleAssignmentSearchVO;
import io.choerodon.devops.infra.dto.iam.*;
import io.choerodon.devops.infra.feign.fallback.BaseServiceClientFallback;

/**
 * Created by younger on 2018/3/29.
 */

@FeignClient(value = "choerodon-base", fallback = BaseServiceClientFallback.class)
public interface BaseServiceClient {
    @GetMapping(value = "/choerodon/v1/projects/{project_id}/immutable")
    @ApiOperation(value = "按照项目Id查询项目的不可变信息")
    ResponseEntity<String> immutableProjectInfoById(@PathVariable(name = "project_id") Long id);

    @GetMapping(value = "/choerodon/v1/projects/{projectId}")
    ResponseEntity<ProjectDTO> queryIamProject(@PathVariable("projectId") Long projectId,
                                               @RequestParam(value = "with_category_info") Boolean withCategoryInfo,
                                               @RequestParam(value = "with_user_info") Boolean withUserInfo,
                                               @RequestParam(value = "with_agile_info") Boolean withAgileInfo,
                                               @RequestParam(value = "with_work_group") Boolean withWorkGroup,
                                               @RequestParam(value = "with_project_classfication") Boolean withProjectClassfication);

    @GetMapping(value = "/choerodon/v1/projects/{projectId}/basic_info_with_cache")
    ResponseEntity<ProjectDTO> queryIamProjectBasicInfo(@PathVariable("projectId") Long projectId);


    /**
     * @param organizationId 组织id
     * @return
     */
    @GetMapping(value = "/choerodon/v1/organizations/{organizationId}/basic_info")
    ResponseEntity<Tenant> queryOrganizationBasicInfoWithCache(@PathVariable("organizationId") Long organizationId);

    @GetMapping(value = "/choerodon/v1/projects/{project_id}/users")
    ResponseEntity<String> listUsersByEmail(@PathVariable("project_id") Long projectId, @RequestParam("page") int page, @RequestParam("size") int size, @RequestParam("email") String email);

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

    @GetMapping(value = "/choerodon/v1/projects/{project_id}/users/search")
    ResponseEntity<Page<IamUserDTO>> pagingQueryUsersWithRolesOnProjectLevel(
            @PathVariable(name = "project_id") Long projectId,
            @RequestParam(value = "page") int page,
            @RequestParam(value = "size") int size,
            @RequestParam(value = "params") String params);


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

    @GetMapping(value = "/choerodon/v1/users/{id}/projects/{project_id}/check_is_gitlab_owner")
    ResponseEntity<Boolean> checkIsGitlabProjectOwner(
            @PathVariable("id") Long id,
            @PathVariable("project_id") Long projectId);

    @ApiOperation(value = "判断用户是否为组织层root")
    @GetMapping(value = "/choerodon/v1/users/{id}/projects/{project_id}/check_is_gitlab_org_owner")
    ResponseEntity<Boolean> checkIsGitlabOrgOwner(@PathVariable("id") Long id,
                                                  @PathVariable("project_id") Long projectId);

    @ApiOperation(value = "判断用户是否为组织层root")
    @PostMapping(value = "/choerodon/v1/users/projects/{project_id}/check_users_are_gitlab_owner")
    ResponseEntity<Map<Long, Boolean>> checkUsersAreGitlabProjectOwner(@RequestBody Set<Long> id,
                                                                       @PathVariable("project_id") Long projectId);


    @ApiOperation("校验用户是否是gitlab组织层owner或者项目层的owner")
    @GetMapping("/choerodon/v1/users/{id}/projects/{project_id}/check_is_org_or_proj_gitlab_owner")
    ResponseEntity<Boolean> checkIsOrgOrProjectGitlabOwner(
            @PathVariable("id") Long id,
            @PathVariable("project_id") Long projectId);

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
    @GetMapping("/choerodon/v1/organizations/{organization_id}/resource_limit")
    ResponseEntity<ResourceLimitVO> queryResourceLimit(@PathVariable("organization_id") Long organizationId);

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

    /**
     * 查询项目下的用户
     */
    @GetMapping("/choerodon/v1/projects/{project_id}/all/users")
    ResponseEntity<List<IamUserDTO>> queryUserByProjectId(@PathVariable("project_id") Long projectId);


    /**
     * @return {@link Set<Long>}
     */
    @ApiOperation(value = "查询平台中所有用户的id")
    @GetMapping(value = "/choerodon/v1/users/all_user_ids")
    ResponseEntity<String> listAllUserIds();


    @ApiOperation(value = "查询平台中所有用户的id")
    @GetMapping(value = "/choerodon/v1/projects/{project_id}/list_project_category")
    ResponseEntity<List<String>> listProjectCategoryById(@PathVariable(name = "project_id") Long projectId);

    @GetMapping("/choerodon/v1/projects/list/ids_in_org")
    @ApiOperation("根据组织id查询项目的id集合")
    ResponseEntity<String> listProjectIdsInOrg(@RequestParam("tenant_id") Long tenantId);

    @GetMapping(value = "/choerodon/v1/organizations/{organization_id}/users/{user_id}/projects")
    @ApiOperation(value = "查询当前组织下用户的项目列表")
    ResponseEntity<List<ProjectDTO>> listProjectsByUserId(@PathVariable(name = "organization_id") Long organizationId,
                                                          @PathVariable(name = "user_id") Long userId);

    /* 其他 */
    // -------------------------------
    @ApiOperation(value = "查询项目下指定角色的用户列表")
    @GetMapping(value = "/choerodon/v1/projects/{project_id}/users/{role_lable}")
    ResponseEntity<List<IamUserDTO>> listProjectUsersByProjectIdAndRoleLabel(
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "角色标签", required = true)
            @PathVariable("role_lable") String roleLable);
}
