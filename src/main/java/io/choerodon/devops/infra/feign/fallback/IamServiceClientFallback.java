package io.choerodon.devops.infra.feign.fallback;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.RoleAssignmentSearchDTO;
import io.choerodon.devops.api.dto.iam.*;
import io.choerodon.devops.api.dto.iam.ProjectWithRoleDTO;
import io.choerodon.devops.api.dto.iam.RoleDTO;
import io.choerodon.devops.api.dto.iam.UserDTO;
import io.choerodon.devops.api.dto.iam.UserWithRoleDTO;
import io.choerodon.devops.domain.application.valueobject.MemberRoleV;
import io.choerodon.devops.infra.dataobject.iam.OrganizationDO;
import io.choerodon.devops.infra.dataobject.iam.ProjectDO;
import io.choerodon.devops.infra.dataobject.iam.UserDO;
import io.choerodon.devops.infra.feign.IamServiceClient;

/**
 * Created by younger on 2018/3/29.
 */
@Component
public class IamServiceClientFallback implements IamServiceClient {

    @Override
    public ResponseEntity<ProjectDO> queryIamProject(Long projectId) {
        return new ResponseEntity("error.project.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<OrganizationDO> queryOrganization() {
        return new ResponseEntity("error.organization.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<OrganizationDO> queryOrganizationById(Long organizationId) {
        return new ResponseEntity("error.organization.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<MemberRoleV> addMemberRole(Long projectId, MemberRoleV memberRoleVo) {
        return new ResponseEntity("error.memberRole.add", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<UserDO> queryByLoginName(String loginName) {
        return new ResponseEntity("error.user.get.byLoginName", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<UserDO> queryById(Long id) {
        return new ResponseEntity("error.user.get.byId", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Page<UserDO>> queryInProjectById(Long projectId, Long id) {
        return new ResponseEntity("error.userInProject.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Page<ProjectDO>> queryProjectByOrgId(Long id, int page, int size, String name, String[] params) {
        return new ResponseEntity("error.project.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<UserDO>> listUsersByIds(Long[] ids) {
        return new ResponseEntity("error.user.get.byIds", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Page<UserDO>> listUsersByEmail(Long projectId, int page, int size, String email) {
        return new ResponseEntity("error.user.get.byEmail", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<RoleDTO>> listRolesWithUserCountOnProjectLevel(
            Long projectId, RoleAssignmentSearchDTO roleAssignmentSearchDTO) {
        return new ResponseEntity("error.roles.get.byProjectId", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Page<UserDTO>> pagingQueryUsersByRoleIdOnProjectLevel(int page, int size, Long roleId,
                                                                                Long sourceId,
                                                                                RoleAssignmentSearchDTO roleAssignmentSearchDTO) {
        return new ResponseEntity("error.user.get.byRoleId", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Page<UserWithRoleDTO>> queryUserByProjectId(Long projectId, int page, int size,
                                                                      RoleAssignmentSearchDTO roleAssignmentSearchDTO) {
        return new ResponseEntity("error.user.get.byProjectId", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Page<ProjectWithRoleDTO>> listProjectWithRole(Long id, int page, int size) {
        return new ResponseEntity("error.project.role.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Page<RoleDTO>> queryRoleIdByCode(RoleSearchDTO roleSearchDTO) {
        return new ResponseEntity("error.roleId.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
