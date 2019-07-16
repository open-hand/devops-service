package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.ProjectVO;
import io.choerodon.devops.api.vo.RoleAssignmentSearchDTO;
import io.choerodon.devops.api.vo.iam.ProjectWithRoleDTO;
import io.choerodon.devops.api.vo.iam.UserVO;
import io.choerodon.devops.api.vo.iam.UserWithRoleDTO;
import io.choerodon.devops.domain.application.valueobject.OrganizationSimplifyDTO;
import io.choerodon.devops.domain.application.valueobject.ProjectCreateDTO;
import io.choerodon.devops.infra.dto.iam.IamAppDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:57 2019/7/11
 * Description:
 */
public interface IamService {

    ProjectDTO queryIamProject(Long projectId);

    OrganizationDTO queryOrganization();

    OrganizationDTO queryOrganizationById(Long organizationId);

    IamUserDTO queryByLoginName(String userName);

    List<ProjectDTO> listIamProjectByOrgId(Long organizationId, String name, String[] params);

    PageInfo<ProjectDTO> queryProjectByOrgId(Long organizationId, int page, int size, String name, String[] params);

    List<IamUserDTO> listUsersByIds(List<Long> ids);

    IamUserDTO queryUserByUserId(Long id);

    IamUserDTO queryByEmail(Long projectId, String email);

    PageInfo<IamUserDTO> pagingQueryUsersByRoleIdOnProjectLevel(PageRequest pageRequest,
                                                            RoleAssignmentSearchDTO roleAssignmentSearchDTO, Long roleId,
                                                            Long projectId, Boolean doPage);

    PageInfo<UserWithRoleDTO> queryUserPermissionByProjectId(Long projectId, PageRequest pageRequest, Boolean doPage);

    List<ProjectWithRoleDTO> listProjectWithRoleDTO(Long userId);

    Long queryRoleIdByCode(String roleCode);

    List<Long> getAllMemberIdsWithoutOwner(Long projectId);

    List<IamUserDTO> getAllMember(Long projectId);

    Boolean isProjectOwner(Long userId, ProjectDTO projectDTO);

    IamAppDTO createIamApp(Long organizationId, IamAppDTO iamAppDTO);

    IamAppDTO updateIamApp(Long organizationId, Long id, IamAppDTO iamAppDTO);

    IamAppDTO queryIamAppByCode(Long organizationId, String code);

    ProjectDTO createProject(Long organizationId, ProjectCreateDTO projectCreateDTO);

    PageInfo<OrganizationSimplifyDTO> getAllOrgs(Integer page, Integer size);
}
