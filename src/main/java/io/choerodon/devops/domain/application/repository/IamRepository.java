package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.RoleAssignmentSearchDTO;
import io.choerodon.devops.api.dto.iam.ProjectWithRoleDTO;
import io.choerodon.devops.api.dto.iam.RoleDTO;
import io.choerodon.devops.api.dto.iam.UserDTO;
import io.choerodon.devops.api.dto.iam.UserWithRoleDTO;
import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.devops.infra.feign.IamServiceClient;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by younger on 2018/3/29.
 */
public interface IamRepository {

    ProjectE queryIamProject(Long projectId);

    Organization queryOrganization();

    Organization queryOrganizationById(Long organizationId);

    UserE queryByLoginName(String userName);

    List<ProjectE> listIamProjectByOrgId(Long organizationId, String name, String[] params);

    UserE queryById(Long id);

    UserE queryByProjectAndId(Long projectId, Long id);

    List<UserE> listUsersByIds(List<Long> ids);

    UserE queryUserByUserId(Long id);

    UserE queryByEmail(Long projectId, String email);

    List<RoleDTO> listRolesWithUserCountOnProjectLevel(Long projectId, RoleAssignmentSearchDTO roleAssignmentSearchDTO);

    Page<UserDTO> pagingQueryUsersByRoleIdOnProjectLevel(PageRequest pageRequest,
                                                         RoleAssignmentSearchDTO roleAssignmentSearchDTO, Long roleId,
                                                         Long projectId);

    Page<UserWithRoleDTO> queryUserPermissionByProjectId(Long projectId, PageRequest pageRequest, Boolean doPage, String searchParams);

    List<ProjectWithRoleDTO> listProjectWithRoleDTO(Long userId);

    Page<RoleDTO> queryRoleIdByCode(String roleCode);

    void initMockIamService(IamServiceClient iamServiceClient);
}
