package io.choerodon.devops.infra.feign;

import java.util.List;
import javax.validation.Valid;

import com.github.pagehelper.PageInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.choerodon.base.constant.PageConstant;
import io.choerodon.devops.api.dto.ProjectDTO;
import io.choerodon.devops.api.dto.RoleAssignmentSearchDTO;
import io.choerodon.devops.api.dto.iam.ProjectWithRoleDTO;
import io.choerodon.devops.api.dto.iam.RoleDTO;
import io.choerodon.devops.api.dto.iam.RoleSearchDTO;
import io.choerodon.devops.api.dto.iam.UserDTO;
import io.choerodon.devops.api.dto.iam.UserWithRoleDTO;
import io.choerodon.devops.domain.application.event.IamAppPayLoad;
import io.choerodon.devops.domain.application.valueobject.MemberRoleV;
import io.choerodon.devops.domain.application.valueobject.OrganizationSimplifyDTO;
import io.choerodon.devops.domain.application.valueobject.ProjectCreateDTO;
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
    ResponseEntity<PageInfo<UserDO>> queryInProjectById(@PathVariable("project_id") Long projectId, @PathVariable("id") Long id);

    @GetMapping(value = "/v1/organizations/{id}/projects")
    ResponseEntity<PageInfo<ProjectDO>> queryProjectByOrgId(@PathVariable("id") Long id, @RequestParam("page") int page, @RequestParam("size") int size, @RequestParam("name") String name, @RequestParam("params") String[] params);

    @PostMapping(value = "/v1/users/ids")
    ResponseEntity<List<UserDO>> listUsersByIds(@RequestBody Long[] ids);

    @GetMapping(value = "/v1/projects/{project_id}/users")
    ResponseEntity<PageInfo<UserDO>> listUsersByEmail(@PathVariable("project_id") Long projectId, @RequestParam("page") int page, @RequestParam("size") int size, @RequestParam("email") String email);

    @PostMapping(value = "/v1/projects/{project_id}/role_members/users/count")
    ResponseEntity<List<RoleDTO>> listRolesWithUserCountOnProjectLevel(@PathVariable(name = "project_id") Long sourceId,
                                                                       @RequestBody(required = false) @Valid RoleAssignmentSearchDTO roleAssignmentSearchDTO);

    @PostMapping(value = "/v1/projects/{project_id}/role_members/users")
    ResponseEntity<PageInfo<UserDTO>> pagingQueryUsersByRoleIdOnProjectLevel(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(name = "role_id") Long roleId,
            @PathVariable(name = "project_id") Long sourceId,
            @RequestParam(name = "doPage") Boolean doPage,
            @RequestBody RoleAssignmentSearchDTO roleAssignmentSearchDTO);

    @PostMapping(value = "/v1/projects/{project_id}/role_members/users/roles")
    ResponseEntity<PageInfo<UserWithRoleDTO>> queryUserByProjectId(@PathVariable("project_id") Long projectId,
                                                                   @RequestParam("page") int page,
                                                                   @RequestParam("size") int size,
                                                                   @RequestParam("doPage") Boolean doPage,
                                                                   @RequestBody @Valid RoleAssignmentSearchDTO roleAssignmentSearchDTO);

    @GetMapping(value = "/v1/users/{id}/project_roles")
    ResponseEntity<PageInfo<ProjectWithRoleDTO>> listProjectWithRole(@PathVariable("id") Long id,
                                                                     @RequestParam("page") int page,
                                                                     @RequestParam("size") int size);

    @PostMapping(value = "/v1/roles/search")
    ResponseEntity<PageInfo<RoleDTO>> queryRoleIdByCode(@RequestBody @Valid RoleSearchDTO roleSearchDTO);


    @PostMapping(value = "/v1/organizations/{organization_id}/applications")
    ResponseEntity<IamAppPayLoad> createIamApplication(@PathVariable("organization_id") Long organizationId,
                                                       @RequestBody @Valid IamAppPayLoad iamAppPayLoad);


    @PostMapping(value = "/v1/organizations/{organization_id}/applications/{id}")
    ResponseEntity<IamAppPayLoad> updateIamApplication(
            @PathVariable("organization_id") Long organizationId,
            @PathVariable("id") Long id,
            @RequestBody @Valid IamAppPayLoad iamAppPayLoad);


    @PutMapping(value = "/v1/organizations/{organization_id}/applications/{id}/disable")
    ResponseEntity<IamAppPayLoad> disableIamApplication(@PathVariable("organization_id") Long organizationId, @PathVariable("id") Long id);


    @PutMapping(value = "/v1/organizations/{organization_id}/applications/{id}/enable")
    ResponseEntity<IamAppPayLoad> enableIamApplication(@PathVariable("organization_id") Long organizationId, @PathVariable("id") Long id);


    @GetMapping(value = "/v1/organizations/{organization_id}/applications")
    ResponseEntity<PageInfo<IamAppPayLoad>> getIamApplication(@PathVariable("organization_id") Long organizationId, @RequestParam("code") String code);

    @PostMapping("/v1/organizations/{organization_id}/projects")
    ResponseEntity<ProjectDTO> createProject(@PathVariable(name = "organization_id") Long organizationId,
                                             @RequestBody @Valid ProjectCreateDTO projectCreateDTO);

    @PostMapping("/v1/organizations/all")
    ResponseEntity<PageInfo<OrganizationSimplifyDTO>> getAllOrgs(@RequestParam(defaultValue = PageConstant.PAGE, required = false, value = "page") final int page,
                                                                 @RequestParam(defaultValue = PageConstant.SIZE, required = false, value = "size") final int size);
}
