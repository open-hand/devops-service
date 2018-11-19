package io.choerodon.devops.infra.feign;

import java.util.List;
import javax.validation.Valid;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.RoleAssignmentSearchDTO;
import io.choerodon.devops.api.dto.iam.*;
import io.choerodon.devops.domain.application.valueobject.MemberRoleV;
import io.choerodon.devops.infra.dataobject.iam.OrganizationDO;
import io.choerodon.devops.infra.dataobject.iam.ProjectDO;
import io.choerodon.devops.infra.dataobject.iam.UserDO;
import io.choerodon.devops.infra.feign.fallback.IamServiceClientFallback;

/**
 * Created by younger on 2018/3/29.
 */

@FeignClient(value = "iam-service", fallback = IamServiceClientFallback.class)
public interface IamServiceClient {

    @GetMapping(value = "/v1/projects/{projectId}")
    ResponseEntity<ProjectDO> queryIamProject(@PathVariable("projectId") Long projectId);

    @GetMapping("/v1/organizations/self")
    ResponseEntity<OrganizationDO> queryOrganization();

    @GetMapping("/v1/organizations/{organizationId}")
    ResponseEntity<OrganizationDO> queryOrganizationById(@PathVariable("organizationId") Long organizationId);

    @PostMapping(value = "/v1/project/{projectId}/memberRoles/single")
    ResponseEntity<MemberRoleV> addMemberRole(@PathVariable("projectId") Long projectId, @RequestBody @Valid MemberRoleV memberRoleVo);

    @GetMapping(value = "/v1/users")
    ResponseEntity<UserDO> queryByLoginName(@RequestParam("login_name") String loginName);

    @GetMapping(value = "/v1/users/{id}/info")
    ResponseEntity<UserDO> queryById(@PathVariable("id") Long id);

    @GetMapping(value = "v1/projects/{project_id}/users?id={id}")
    ResponseEntity<Page<UserDO>> queryInProjectById(@PathVariable("project_id") Long projectId, @PathVariable("id") Long id);

    @GetMapping(value = "/v1/organizations/{id}/projects")
    ResponseEntity<Page<ProjectDO>> queryProjectByOrgId(@PathVariable("id") Long id, @RequestParam("page") int page, @RequestParam("size") int size, @RequestParam("name") String name, @RequestParam("params") String[] params);

    @PostMapping(value = "/v1/users/ids")
    ResponseEntity<List<UserDO>> listUsersByIds(@RequestBody Long[] ids);

    @GetMapping(value = "/v1/projects/{project_id}/users")
    ResponseEntity<Page<UserDO>> listUsersByEmail(@PathVariable("project_id") Long projectId, @RequestParam("page") int page, @RequestParam("size") int size, @RequestParam("email") String email);

    @PostMapping(value = "/v1/projects/{project_id}/role_members/users/count")
    ResponseEntity<List<RoleDTO>> listRolesWithUserCountOnProjectLevel(@PathVariable(name = "project_id") Long sourceId,
                                                                       @RequestBody(required = false) @Valid RoleAssignmentSearchDTO roleAssignmentSearchDTO);

    @PostMapping(value = "/v1/projects/{project_id}/role_members/users")
    ResponseEntity<Page<UserDTO>> pagingQueryUsersByRoleIdOnProjectLevel(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(name = "role_id") Long roleId,
            @PathVariable(name = "project_id") Long sourceId,
            @RequestParam(name = "doPage") Boolean doPage,
            @RequestBody RoleAssignmentSearchDTO roleAssignmentSearchDTO);

    @PostMapping(value = "/v1/projects/{project_id}/role_members/users/roles")
    ResponseEntity<Page<UserWithRoleDTO>> queryUserByProjectId(@PathVariable("project_id") Long projectId,
                                                               @RequestParam("page") int page,
                                                               @RequestParam("size") int size,
                                                               @RequestParam("doPage") Boolean doPage,
                                                               @RequestBody @Valid RoleAssignmentSearchDTO roleAssignmentSearchDTO);

    @GetMapping(value = "/v1/users/{id}/project_roles")
    ResponseEntity<Page<ProjectWithRoleDTO>> listProjectWithRole(@PathVariable("id") Long id,
                                                                 @RequestParam("page") int page,
                                                                 @RequestParam("size") int size);

    @PostMapping(value = "/v1/roles/search")
    ResponseEntity<Page<RoleDTO>> queryRoleIdByCode(@RequestBody @Valid RoleSearchDTO roleSearchDTO);
}
