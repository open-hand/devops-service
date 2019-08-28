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
import io.choerodon.devops.api.vo.iam.ProjectWithRoleVO;
import io.choerodon.devops.api.vo.iam.RoleSearchVO;
import io.choerodon.devops.api.vo.iam.RoleVO;
import io.choerodon.devops.api.vo.iam.UserWithRoleVO;
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

    @PostMapping(value = "/v1/project/{projectId}/memberRoles/single")
    ResponseEntity<MemberRoleVO> addMemberRole(@PathVariable("projectId") Long projectId, @RequestBody @Valid MemberRoleVO memberRoleVo);

    @GetMapping(value = "/v1/users")
    ResponseEntity<IamUserDTO> queryByLoginName(@RequestParam("login_name") String loginName);

    @GetMapping(value = "/v1/users/{id}/info")
    ResponseEntity<IamUserDTO> queryById(@PathVariable("id") Long id);

    @GetMapping(value = "v1/projects/{project_id}/users?id={id}")
    ResponseEntity<PageInfo<IamUserDTO>> queryInProjectById(@PathVariable("project_id") Long projectId, @PathVariable("id") Long id);

    @GetMapping(value = "/v1/organizations/{id}/projects")
    ResponseEntity<PageInfo<ProjectDTO>> queryProjectByOrgId(@PathVariable("id") Long id, @RequestParam("page") int page, @RequestParam("size") int size, @RequestParam("name") String name, @RequestParam("params") String[] params);

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
    ResponseEntity<List<IamUserDTO>> listUsersByIds(@RequestBody Long[] ids);

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

    @PostMapping(value = "/v1/roles/search")
    ResponseEntity<PageInfo<RoleVO>> queryRoleIdByCode(@RequestBody @Valid RoleSearchVO roleSearchVO);


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
    ResponseEntity<PageInfo<ProjectDTO>> listProject(@PathVariable(name = "organization_id") Long organizationId,
                                                     @RequestParam Map<String, Object> pageRequest,
                                                     @RequestParam(name = "name", required = false) String name,
                                                     @RequestParam(name = "code", required = false) String code,
                                                     @RequestParam(name = "type_name", required = false) String typeName,
                                                     @RequestParam(name = "enabled", required = false) Boolean enabled,
                                                     @RequestParam(name = "category", required = false) String category,
                                                     @RequestParam(name = "params", required = false) String[] params);

    @PostMapping("/v1/organizations/all")
    ResponseEntity<PageInfo<OrganizationSimplifyVO>> getAllOrgs(
            @RequestParam(defaultValue = PageConstant.PAGE, required = false, value = "page") final int page,
            @RequestParam(defaultValue = PageConstant.SIZE, required = false, value = "size") final int size);


    @GetMapping(value = "/v1/applications/{id}/project")
    ResponseEntity<ProjectDTO> queryProjectByAppId(@PathVariable("id") Long id);

    @GetMapping(value = "/v1/applications/{id}")
    ResponseEntity<ApplicationDTO> queryAppById(@PathVariable(value = "id") Long id);

    @PutMapping(value = "/v1/projects/{project_id}/publish_apps/{id}/fail")
    ResponseEntity<Boolean> publishFail(@PathVariable("project_id") Long projectId,
                                        @PathVariable("id") Long id,
                                        @RequestParam("errorCode") String errorCode);

    @GetMapping(value = "/v1/organizations/{organization_id}/projects/projects_with_applications")
    ResponseEntity<PageInfo<ProjectDTO>> pagingProjectByOptions(@PathVariable("organization_id") Long organizationId,
                                                                @RequestParam(value = "doPage", defaultValue = "false") Boolean doPage,
                                                                @RequestParam("page") int page,
                                                                @RequestParam("size") int size,
                                                                @RequestParam(value = "params", required = false) String[] params);

    @PostMapping(value = "/v1/applications/{publish_app_version_id}/complete_downloading")
    ResponseEntity completeDownloadApplication(@PathVariable("publish_app_version_id") Long publishAppVersionId,
                                               @RequestBody Set<Long> serviceVersionIds);
}
