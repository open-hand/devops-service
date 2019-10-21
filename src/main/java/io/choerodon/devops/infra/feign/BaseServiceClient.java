package io.choerodon.devops.infra.feign;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.Valid;

import com.github.pagehelper.PageInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.base.constant.PageConstant;
import io.choerodon.devops.api.vo.OrganizationSimplifyVO;
import io.choerodon.devops.api.vo.RoleAssignmentSearchVO;
import io.choerodon.devops.api.vo.iam.*;
import io.choerodon.devops.api.vo.kubernetes.MemberRoleVO;
import io.choerodon.devops.api.vo.kubernetes.ProjectCreateDTO;
import io.choerodon.devops.infra.dto.iam.*;
import io.choerodon.devops.infra.feign.fallback.BaseServiceClientFallback;

/**
 * Created by younger on 2018/3/29.
 */

@FeignClient(value = "base-service", fallback = BaseServiceClientFallback.class)
public interface BaseServiceClient {

    @GetMapping(value = "/v1/projects/{projectId}")
    ResponseEntity<ProjectDTO> queryIamProject(@PathVariable("projectId") Long projectId);

    @GetMapping("/v1/organizations/self")
    ResponseEntity<OrganizationDTO> queryOrganization();

    @GetMapping("/v1/organizations/{organizationId}")
    ResponseEntity<OrganizationDTO> queryOrganizationById(@PathVariable("organizationId") Long organizationId);

    @GetMapping("v1/organizations")
    ResponseEntity<PageInfo<OrganizationDTO>>  listOrganizations(@RequestParam("page") Integer page,
                                                                @RequestParam("size") Integer size);


    @PostMapping(value = "/v1/project/{projectId}/memberRoles/single")
    ResponseEntity<MemberRoleVO> addMemberRole(@PathVariable("projectId") Long projectId, @RequestBody @Valid MemberRoleVO memberRoleVo);

    @GetMapping(value = "/v1/users")
    ResponseEntity<IamUserDTO> queryByLoginName(@RequestParam("login_name") String loginName);

    @GetMapping(value = "/v1/users/{id}/info")
    ResponseEntity<IamUserDTO> queryById(@PathVariable("id") Long id);

    @GetMapping(value = "v1/projects/{project_id}/users?id={id}")
    ResponseEntity<PageInfo<IamUserDTO>> queryInProjectById(@PathVariable("project_id") Long projectId, @PathVariable("id") Long id);

    @GetMapping(value = "/v1/organizations/{organization_id}/applications")
    ResponseEntity<List<ApplicationDTO>> queryAppsByOrgId(@PathVariable("organization_id") Long organizationId,
                                                          @RequestParam(value = "doPage", required = false) Boolean doPage,
                                                          @RequestParam("page") int page,
                                                          @RequestParam("size") int size,
                                                          @RequestParam(value = "name", required = false) String name,
                                                          @RequestParam(value = "code", required = false) String code,
                                                          @RequestParam(value = "type", required = false) String type,
                                                          @RequestParam(value = "params", required = false) String[] params);

    @PostMapping(value = "/v1/users/ids")
    ResponseEntity<List<IamUserDTO>> listUsersByIds(@RequestBody Long[] ids, @RequestParam(value = "only_enabled") Boolean onlyEnabled);

    @GetMapping(value = "/v1/projects/{project_id}/users")
    ResponseEntity<PageInfo<IamUserDTO>> listUsersByEmail(@PathVariable("project_id") Long projectId, @RequestParam("page") int page, @RequestParam("size") int size, @RequestParam("email") String email);

    @PostMapping(value = "/v1/projects/{project_id}/role_members/users/count")
    ResponseEntity<List<RoleVO>> listRolesWithUserCountOnProjectLevel(@PathVariable(name = "project_id") Long sourceId,
                                                                      @RequestBody(required = false) @Valid RoleAssignmentSearchVO roleAssignmentSearchVO);

    @PostMapping(value = "/v1/projects/{project_id}/role_members/users")
    ResponseEntity<PageInfo<IamUserDTO>> pagingQueryUsersByRoleIdOnProjectLevel(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(name = "role_id") Long roleId,
            @PathVariable(name = "project_id") Long sourceId,
            @RequestParam(name = "doPage") Boolean doPage,
            @RequestBody RoleAssignmentSearchVO roleAssignmentSearchVO);

    @PostMapping(value = "/v1/projects/{project_id}/role_members/users/roles")
    ResponseEntity<PageInfo<UserWithRoleVO>> queryUserByProjectId(@PathVariable("project_id") Long projectId,
                                                                  @RequestParam("page") int page,
                                                                  @RequestParam("size") int size,
                                                                  @RequestParam("doPage") Boolean doPage,
                                                                  @RequestBody @Valid RoleAssignmentSearchVO roleAssignmentSearchVO);

    @GetMapping(value = "/v1/users/{id}/project_roles")
    ResponseEntity<PageInfo<ProjectWithRoleVO>> listProjectWithRole(@PathVariable("id") Long id,
                                                                    @RequestParam("page") int page,
                                                                    @RequestParam("size") int size);

    @GetMapping(value = "/v1/roles/search")
    ResponseEntity<PageInfo<RoleVO>> queryRoleIdByCode(@RequestParam(value = "code", required = false) String code);


    @PostMapping(value = "/v1/organizations/{organization_id}/applications")
    ResponseEntity<IamAppDTO> createIamApplication(@PathVariable("organization_id") Long organizationId,
                                                   @RequestBody @Valid IamAppDTO appDTO);


    @PostMapping(value = "/v1/organizations/{organization_id}/applications/{id}")
    ResponseEntity<IamAppDTO> updateIamApplication(
            @PathVariable("organization_id") Long organizationId,
            @PathVariable("id") Long id,
            @RequestBody @Valid IamAppDTO appDTO);


    @PutMapping(value = "/v1/organizations/{organization_id}/applications/{id}/disable")
    ResponseEntity<IamAppDTO> disableIamApplication(@PathVariable("organization_id") Long organizationId, @PathVariable("id") Long id);


    @PutMapping(value = "/v1/organizations/{organization_id}/applications/{id}/enable")
    ResponseEntity<IamAppDTO> enableIamApplication(@PathVariable("organization_id") Long organizationId, @PathVariable("id") Long id);


    @GetMapping(value = "/v1/organizations/{organization_id}/applications")
    ResponseEntity<PageInfo<IamAppDTO>> getIamApplication(@PathVariable("organization_id") Long organizationId, @RequestParam("code") String code);

    @PostMapping("/v1/organizations/{organization_id}/projects")
    ResponseEntity<ProjectDTO> createProject(@PathVariable(name = "organization_id") Long organizationId,
                                             @RequestBody @Valid ProjectCreateDTO projectCreateDTO);


    @GetMapping("/v1/organizations/{organization_id}/projects")
    ResponseEntity<PageInfo<ProjectDTO>> pageProjectsByOrgId(@PathVariable(name = "organization_id") Long organizationId,
                                                             @RequestParam Map<String, Object> pageRequest,
                                                             @RequestParam(name = "name", required = false) String name,
                                                             @RequestParam(name = "code", required = false) String code,
                                                             @RequestParam(name = "enabled", required = false) Boolean enabled,
                                                             @RequestParam(value = "params", required = false) String params);

    @PostMapping("/v1/organizations/all")
    ResponseEntity<PageInfo<OrganizationSimplifyVO>> getAllOrgs(
            @RequestParam(defaultValue = PageConstant.PAGE, required = false, value = "page") final int page,
            @RequestParam(defaultValue = PageConstant.SIZE, required = false, value = "size") final int size);


    @GetMapping(value = "/v1/applications/{id}/project")
    ResponseEntity<ProjectDTO> queryProjectByAppId(@PathVariable("id") Long id);

    @GetMapping(value = "/v1/applications/{id}")
    ResponseEntity<ApplicationDTO> queryAppById(@PathVariable(value = "id") Long id);

    @GetMapping(value = "/v1/applications/list")
    ResponseEntity<List<ApplicationDTO>> getAppByIds(@RequestParam(value = "app_ids") Set<Long> appIds);

    @PutMapping(value = "/v1/projects/{project_id}/publish_version_infos/{id}/fail")
    ResponseEntity<Boolean> publishFail(@PathVariable("project_id") Long projectId,
                                        @PathVariable("id") Long id,
                                        @RequestParam("error_code") String errorCode,
                                        @RequestParam("fix_flag") Boolean fixFlag);

    @GetMapping(value = "/v1/organizations/{organization_id}/projects/projects_with_applications")
    ResponseEntity<PageInfo<ProjectDTO>> pagingProjectByOptions(@PathVariable("organization_id") Long organizationId,
                                                                @RequestParam(value = "doPage", defaultValue = "false") Boolean doPage,
                                                                @RequestParam("page") int page,
                                                                @RequestParam("size") int size,
                                                                @RequestParam(value = "params", required = false) String[] params);

    @PostMapping(value = "/v1/applications/{app_download_recode_id}/complete_downloading")
    ResponseEntity<String> completeDownloadApplication(@PathVariable("app_download_recode_id") Long appDownloadRecordId,
                                                       @RequestParam("app_version_id") Long appVersionId,
                                                       @RequestParam("organization_id") Long organizationId,
                                                       @RequestBody List<AppDownloadDevopsReqVO> appDownloadDevopsReqVOS);

    @PutMapping(value = "/v1/applications/{app_download_record_id}/fail_downloading")
    ResponseEntity<String> failToDownloadApplication(@PathVariable("app_download_record_id") Long appDownloadRecordId,
                                                     @RequestParam("app_version_id") Long appVersionId,
                                                     @RequestParam("organization_id") Long organizationId);

    @GetMapping(value = "/v1/remote_token/authorization/check/latest")
    ResponseEntity<RemoteTokenAuthorizationVO> checkLatestToken();

    @PostMapping(value = "/v1/projects/ids")
    ResponseEntity<List<ProjectDTO>> queryByIds(@RequestBody Set<Long> ids);

    @GetMapping(value = "/v1/projects/{project_id}/publish_applications/list_by_ids")
    ResponseEntity<List<ApplicationDTO>> listApplicationInfoByAppIds(@PathVariable("project_id") Long projectId,
                                                                     @RequestParam(value = "ids") Set<Long> serviceIds);

    @GetMapping(value = "/v1/projects/{project_id}/applications/{application_id}/services/ids")
    ResponseEntity<Set<Long>> listAppServiceByAppId(@PathVariable("project_id") Long projectId,
                                                    @PathVariable("application_id") Long applicationId);

    /**
     * 根据组织Id及项目code查询项目
     *
     * @param organizationId 组织Id
     * @param projectCode    项目code
     * @return 根据组织Id及项目code查询项目
     */
    @GetMapping(value = "/v1/organizations/{organization_id}/projects/by_code")
    ResponseEntity<ProjectDTO> queryProjectByCodeAndOrgId(@PathVariable(name = "organization_id") Long organizationId,
                                                          @RequestParam(name = "code") String projectCode);

    @GetMapping("/v1/organizations/{organization_id}/services/{app_type}")
    ResponseEntity<Set<Long>> listService(@PathVariable("organization_id") Long organizationId, @PathVariable("app_type") String appType);

    @GetMapping("/v1/organizations/{organization_id}/services/{app_type}/versions")
    ResponseEntity<Set<Long>> listSvcVersion(@PathVariable("organization_id") Long organizationId, @PathVariable("app_type") String appType);

    @GetMapping("/v1/organizations/{organization_id}/users/search")
    ResponseEntity<PageInfo<IamUserDTO>> pagingQueryUsersWithRolesOnOrganizationLevel(
            @PathVariable(name = "organization_id") Long organizationId,
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(required = false, value = "loginName") String loginName,
            @RequestParam(required = false, value = "realName") String realName,
            @RequestParam(required = false, value = "roleName") String roleName,
            @RequestParam(required = false, value = "enabled") Boolean enabled,
            @RequestParam(required = false, value = "locked") Boolean locked,
            @RequestParam(required = false, value = "params") String params);

    /**
     * 项目层批量分配权限
     *
     * @param projectId      项目id
     * @param memberRoleDTOS 权限分配信息
     * @return 分配结果
     */
    @PostMapping(value = "/v1/projects/{project_id}/users/assign_roles")
    ResponseEntity<List<MemberRoleDTO>> assignUsersRolesOnProjectLevel(
            @PathVariable(name = "project_id") Long projectId,
            @RequestBody List<MemberRoleDTO> memberRoleDTOS);
}
