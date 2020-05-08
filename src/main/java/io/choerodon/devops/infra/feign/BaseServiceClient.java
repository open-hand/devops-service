package io.choerodon.devops.infra.feign;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.Valid;
import javax.websocket.server.PathParam;

import io.choerodon.core.domain.Page;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.devops.api.vo.OrgAdministratorVO;
import io.choerodon.devops.api.vo.RoleAssignmentSearchVO;
import io.choerodon.devops.api.vo.iam.AppDownloadDevopsReqVO;
import io.choerodon.devops.api.vo.iam.ProjectWithRoleVO;
import io.choerodon.devops.api.vo.iam.RemoteTokenAuthorizationVO;
import io.choerodon.devops.api.vo.iam.RoleVO;
import io.choerodon.devops.api.vo.kubernetes.MemberRoleVO;
import io.choerodon.devops.api.vo.kubernetes.ProjectCreateDTO;
import io.choerodon.devops.infra.dto.iam.*;
import io.choerodon.devops.infra.feign.fallback.BaseServiceClientFallback;

/**
 * Created by younger on 2018/3/29.
 */

@FeignClient(value = "hzero-iam", fallback = BaseServiceClientFallback.class)
public interface BaseServiceClient {

    @GetMapping(value = "/choerodon/v1/projects/{projectId}")
    ResponseEntity<ProjectDTO> queryIamProject(@PathVariable("projectId") Long projectId);

    @GetMapping(value = "/choerodon/v1/organizations/self")
    ResponseEntity<OrganizationDTO> queryOrganization();

    @GetMapping(value = "/choerodon/v1/organizations/{organizationId}")
    ResponseEntity<OrganizationDTO> queryOrganizationById(@PathVariable("organizationId") Long organizationId);

    /**
     * 根据id集合查询组织
     *
     * @param ids id集合，去重
     * @return 组织集合
     */
    @PostMapping(value = "/choerodon/v1/organizations/ids")
    ResponseEntity<List<OrganizationDTO>> queryOrgByIds(@RequestBody Set<Long> ids);

    @GetMapping(value = "/choerodon/v1/organizations")
    ResponseEntity<Page<OrganizationDTO>> listOrganizations(@RequestParam("page") Integer page,
                                                            @RequestParam("size") Integer size);


    @PostMapping(value = "/choerodon/v1/project/{projectId}/memberRoles/single")
    ResponseEntity<MemberRoleVO> addMemberRole(@PathVariable("projectId") Long projectId, @RequestBody @Valid MemberRoleVO memberRoleVo);

    @GetMapping(value = "/choerodon/v1/users")
    ResponseEntity<IamUserDTO> queryByLoginName(@RequestParam("login_name") String loginName);

    @GetMapping(value = "/choerodon/v1/users/{id}/info")
    ResponseEntity<IamUserDTO> queryById(@PathVariable("id") Long id);

    @GetMapping(value = "/choerodon/v1/projects/{project_id}/users?id={id}")
    ResponseEntity<Page<IamUserDTO>> queryInProjectById(@PathVariable("project_id") Long projectId, @PathVariable("id") Long id);


    @PostMapping(value = "/choerodon/v1/users/ids")
    ResponseEntity<List<IamUserDTO>> listUsersByIds(@RequestBody Long[] ids, @RequestParam(value = "only_enabled") Boolean onlyEnabled);

    @GetMapping(value = "/choerodon/v1/projects/{project_id}/users")
    ResponseEntity<Page<IamUserDTO>> listUsersByEmail(@PathVariable("project_id") Long projectId, @RequestParam("page") int page, @RequestParam("size") int size, @RequestParam("email") String email);

    @PostMapping(value = "/choerodon/v1/projects/{project_id}/role_members/users/count")
    ResponseEntity<List<RoleVO>> listRolesWithUserCountOnProjectLevel(@PathVariable(name = "project_id") Long sourceId,
                                                                      @RequestBody(required = false) @Valid RoleAssignmentSearchVO roleAssignmentSearchVO);

    @PostMapping(value = "/choerodon/v1/projects/{project_id}/gitlab_role/users")
    ResponseEntity<List<IamUserDTO>> listUsersWithGitlabLabel(
            @PathVariable(name = "project_id") Long projectId,
            @RequestBody RoleAssignmentSearchVO roleAssignmentSearchVO,
            @RequestParam(name = "label_name") String labelName);

    @GetMapping(value = "/choerodon/v1/users/{id}/project_roles")
    ResponseEntity<Page<ProjectWithRoleVO>> listProjectWithRole(@PathVariable("id") Long id,
                                                                    @RequestParam("page") int page,
                                                                    @RequestParam("size") int size);

    @GetMapping(value = "/choerodon/v1/roles/search")
    ResponseEntity<Page<RoleVO>> queryRoleIdByCode(@RequestParam(value = "code", required = false) String code);


    @PostMapping(value = "/choerodon/v1/organizations/{organization_id}/projects")
    ResponseEntity<ProjectDTO> createProject(@PathVariable(name = "organization_id") Long organizationId,
                                             @RequestBody @Valid ProjectCreateDTO projectCreateDTO);


    @GetMapping(value = "/choerodon/v1/organizations/{organization_id}/projects")
    ResponseEntity<Page<ProjectDTO>> pageProjectsByOrgId(@PathVariable(name = "organization_id") Long organizationId,
                                                             @RequestParam Map<String, Object> pageable,
                                                             @RequestParam(name = "name", required = false) String name,
                                                             @RequestParam(name = "code", required = false) String code,
                                                             @RequestParam(name = "enabled", required = false) Boolean enabled,
                                                             @RequestParam(value = "params", required = false) String params);

    @GetMapping(value = "/choerodon/v1/applications/{id}")
    ResponseEntity<ApplicationDTO> queryAppById(@PathVariable(value = "id") Long id);

    @PutMapping(value = "/choerodon/v1/projects/{project_id}/publish_version_infos/{id}/fail")
    ResponseEntity<Boolean> publishFail(@PathVariable("project_id") Long projectId,
                                        @PathVariable("id") Long id,
                                        @RequestParam("error_code") String errorCode,
                                        @RequestParam("fix_flag") Boolean fixFlag);

    @PostMapping(value = "/choerodon/v1/applications/{app_download_recode_id}/complete_downloading")
    ResponseEntity<String> completeDownloadApplication(@PathVariable("app_download_recode_id") Long appDownloadRecordId,
                                                       @RequestParam("app_version_id") Long appVersionId,
                                                       @RequestParam("organization_id") Long organizationId,
                                                       @RequestBody List<AppDownloadDevopsReqVO> appDownloadDevopsReqVOS);

    @PutMapping(value = "/choerodon/v1/applications/{app_download_record_id}/fail_downloading")
    ResponseEntity<String> failToDownloadApplication(@PathVariable("app_download_record_id") Long appDownloadRecordId,
                                                     @RequestParam("app_version_id") Long appVersionId,
                                                     @RequestParam("organization_id") Long organizationId);

    @GetMapping(value = "/choerodon/v1/remote_token/authorization/check/latest")
    ResponseEntity<RemoteTokenAuthorizationVO> checkLatestToken();

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

    @GetMapping(value = "/choerodon/v1/organizations/{organization_id}/services/{app_type}")
    ResponseEntity<Set<Long>> listService(@PathVariable("organization_id") Long organizationId, @PathVariable("app_type") String appType);

    @GetMapping(value = "/choerodon/v1/organizations/{organization_id}/services/{app_type}/versions")
    ResponseEntity<Set<Long>> listSvcVersion(@PathVariable("organization_id") Long organizationId, @PathVariable("app_type") String appType);

    /**
     * 组织下创client
     *
     * @param organizationId 组织id
     * @param clientVO       clientVO
     * @return 分配结果
     */
    @PostMapping(value = "/choerodon/v1/{organizationId}/clients")
    ResponseEntity<ClientDTO> createClient(@PathVariable("organizationId") Long organizationId,
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
     * @param sourceId
     * @return
     */
    @GetMapping(value = "/choerodon/v1/organizations/{organization_id}/clients/source/{source_id}")
    ResponseEntity<ClientDTO> queryClientBySourceId(@PathVariable("organization_id") Long organizationId, @PathVariable("source_id") Long sourceId);


    @GetMapping(value = "/choerodon/v1/users/{id}/projects/{project_id}/check_is_gitlab_owner")
    ResponseEntity<Boolean> checkIsGitlabProjectOwner(
            @PathVariable("id") Long id,
            @PathVariable("project_id") Long projectId);

    @ApiOperation(value = "判断用户是否为组织层root")
    @GetMapping(value = "/choerodon/v1/users/{id}/projects/{project_id}/check_is_gitlab_org_owner")
    ResponseEntity<Boolean> checkIsGitlabOrgOwner(@PathVariable("id") Long id,
                                                  @PathVariable("project_id") Long projectId);

    /**
     * 根据用户名查询用户信息
     *
     * @param loginName 登录名
     * @return 用户信息
     */
    @GetMapping(value = "/choerodon/v1/users")
    ResponseEntity<IamUserDTO> query(@RequestParam(name = "login_name") String loginName);


    @ApiOperation(value = "查询所有的Root用户 / DevOps服务迁移数据需要")
    @GetMapping(value = "/choerodon/v1/users/admin_all")
    ResponseEntity<List<IamUserDTO>> queryAllAdminUsers();

    @ApiOperation(value = "查询所有的组织管理员 / 修复数据时用到")
    @GetMapping(value = "/choerodon/v1/users/admin_org_all")
    ResponseEntity<List<IamUserDTO>> queryAllOrgRoot();


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
     * 判断组织是否是新组织
     *
     * @param organizationId
     * @return
     */
    @GetMapping(value = "/choerodon/v1/organizations/{organization_id}/check_is_new")
    ResponseEntity<Boolean> checkOrganizationIsNew(@PathVariable(name = "organization_id") Long organizationId);


    @GetMapping(value = "/choerodon/v1/organizations/{organization_id}/org_administrator")
    ResponseEntity<Page<OrgAdministratorVO>> listOrgAdministrator(
            @PathVariable("organization_id") Long organizationId,
            @RequestParam("size") Integer size);

    /**
     * 查询组织下指定角色的id
     * @param organizationId
     * @param label
     * @return
     */
    @GetMapping(value = "/choerodon/v1/organizations/{organization_id}/roles")
    ResponseEntity<Long> getRoleId(
            @PathVariable("organization_id") Long organizationId,
            @PathParam("label") String label);
}
