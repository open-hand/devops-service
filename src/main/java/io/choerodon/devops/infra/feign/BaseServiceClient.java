package io.choerodon.devops.infra.feign;

import com.github.pagehelper.PageInfo;

import io.choerodon.core.annotation.Permission;
import io.choerodon.devops.api.vo.OrganizationSimplifyVO;
import io.choerodon.devops.api.vo.RoleAssignmentSearchVO;
import io.choerodon.devops.api.vo.iam.*;
import io.choerodon.devops.api.vo.kubernetes.MemberRoleVO;
import io.choerodon.devops.api.vo.kubernetes.ProjectCreateDTO;
import io.choerodon.devops.infra.dto.iam.*;
import io.choerodon.devops.infra.feign.fallback.BaseServiceClientFallback;

import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.CacheResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    ResponseEntity<PageInfo<OrganizationDTO>> listOrganizations(@RequestParam("page") Integer page,
                                                                @RequestParam("size") Integer size);


    @PostMapping(value = "/v1/project/{projectId}/memberRoles/single")
    ResponseEntity<MemberRoleVO> addMemberRole(@PathVariable("projectId") Long projectId, @RequestBody @Valid MemberRoleVO memberRoleVo);

    @GetMapping(value = "/v1/users")
    ResponseEntity<IamUserDTO> queryByLoginName(@RequestParam("login_name") String loginName);

    @GetMapping(value = "/v1/users/{id}/info")
    ResponseEntity<IamUserDTO> queryById(@PathVariable("id") Long id);

    @GetMapping(value = "v1/projects/{project_id}/users?id={id}")
    ResponseEntity<PageInfo<IamUserDTO>> queryInProjectById(@PathVariable("project_id") Long projectId, @PathVariable("id") Long id);


    @PostMapping(value = "/v1/users/ids")
    ResponseEntity<List<IamUserDTO>> listUsersByIds(@RequestBody Long[] ids, @RequestParam(value = "only_enabled") Boolean onlyEnabled);

    @GetMapping(value = "/v1/projects/{project_id}/users")
    ResponseEntity<PageInfo<IamUserDTO>> listUsersByEmail(@PathVariable("project_id") Long projectId, @RequestParam("page") int page, @RequestParam("size") int size, @RequestParam("email") String email);

    @PostMapping(value = "/v1/projects/{project_id}/role_members/users/count")
    ResponseEntity<List<RoleVO>> listRolesWithUserCountOnProjectLevel(@PathVariable(name = "project_id") Long sourceId,
                                                                      @RequestBody(required = false) @Valid RoleAssignmentSearchVO roleAssignmentSearchVO);

    @PostMapping(value = "/v1/projects/{project_id}/gitlab_role/users")
    ResponseEntity<List<IamUserDTO>> listUsersWithGitlabLabel(
            @PathVariable(name = "project_id") Long projectId,
            @RequestBody RoleAssignmentSearchVO roleAssignmentSearchVO,
            @RequestParam(name = "label_name") String labelName);

    @GetMapping(value = "/v1/users/{id}/project_roles")
    ResponseEntity<PageInfo<ProjectWithRoleVO>> listProjectWithRole(@PathVariable("id") Long id,
                                                                    @RequestParam("page") int page,
                                                                    @RequestParam("size") int size);

    @GetMapping(value = "/v1/roles/search")
    ResponseEntity<PageInfo<RoleVO>> queryRoleIdByCode(@RequestParam(value = "code", required = false) String code);


    @PostMapping("/v1/organizations/{organization_id}/projects")
    ResponseEntity<ProjectDTO> createProject(@PathVariable(name = "organization_id") Long organizationId,
                                             @RequestBody @Valid ProjectCreateDTO projectCreateDTO);


    @GetMapping("/v1/organizations/{organization_id}/projects")
    ResponseEntity<PageInfo<ProjectDTO>> pageProjectsByOrgId(@PathVariable(name = "organization_id") Long organizationId,
                                                             @RequestParam Map<String, Object> pageable,
                                                             @RequestParam(name = "name", required = false) String name,
                                                             @RequestParam(name = "code", required = false) String code,
                                                             @RequestParam(name = "enabled", required = false) Boolean enabled,
                                                             @RequestParam(value = "params", required = false) String params);

    @GetMapping(value = "/v1/applications/{id}")
    ResponseEntity<ApplicationDTO> queryAppById(@PathVariable(value = "id") Long id);

    @PutMapping(value = "/v1/projects/{project_id}/publish_version_infos/{id}/fail")
    ResponseEntity<Boolean> publishFail(@PathVariable("project_id") Long projectId,
                                        @PathVariable("id") Long id,
                                        @RequestParam("error_code") String errorCode,
                                        @RequestParam("fix_flag") Boolean fixFlag);

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

    /**
     * 组织下创client
     *
     * @param organizationId 组织id
     * @param clientVO       clientVO
     * @return 分配结果
     */
    @PostMapping(value = "/v1/organizations/{organization_id}/clients/wih_type")
    ResponseEntity<ClientDTO> createClient(@PathVariable("organization_id") Long organizationId,
                                           @RequestBody @Valid ClientVO clientVO);

    /**
     * 组织下删除client
     *
     * @param organizationId 组织id
     * @param clientId       clientId
     * @return 分配结果
     */
    @DeleteMapping(value = "/v1/organizations/{organization_id}/clients/{client_id}")
    ResponseEntity deleteClient(@PathVariable("organization_id") Long organizationId, @PathVariable("client_id") Long clientId);

    /**
     * 根据集群Id和组织Id查询client
     *
     * @param organizationId
     * @param sourceId
     * @return
     */
    @GetMapping(value = "/v1/organizations/{organization_id}/clients/source/{source_id}")
    ResponseEntity<ClientDTO> queryClientBySourceId(@PathVariable("organization_id") Long organizationId, @PathVariable("source_id") Long sourceId);


    @GetMapping("/v1/users/{id}/projects/{project_id}/check_is_gitlab_owner")
    ResponseEntity<Boolean> checkIsGitlabProjectOwner(
            @PathVariable("id") Long id,
            @PathVariable("project_id") Long projectId);

    @ApiOperation(value = "判断用户是否为组织层root")
    @GetMapping("/v1/users/{id}/projects/{project_id}/check_is_gitlab_org_owner")
    ResponseEntity<Boolean> checkIsGitlabOrgOwner(@PathVariable("id") Long id,
                                                  @PathVariable("project_id") Long projectId);

    /**
     * 根据用户名查询用户信息
     *
     * @param loginName 登录名
     * @return 用户信息
     */
    @GetMapping("/v1/users")
    ResponseEntity<IamUserDTO> query(@RequestParam(name = "login_name") String loginName);


    @ApiOperation(value = "查询所有的Root用户 / DevOps服务迁移数据需要")
    @GetMapping("/v1/users/admin_all")
    ResponseEntity<List<IamUserDTO>> queryAllAdminUsers();

    @ApiOperation(value = "查询所有的组织管理员 / 修复数据时用到")
    @GetMapping("/v1/users/admin_org_all")
    ResponseEntity<List<IamUserDTO>> queryAllOrgRoot();


}
