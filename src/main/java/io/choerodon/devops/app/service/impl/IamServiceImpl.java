package io.choerodon.devops.app.service.impl;

import static io.choerodon.core.iam.InitRoleCode.PROJECT_MEMBER;
import static io.choerodon.core.iam.InitRoleCode.PROJECT_OWNER;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.exception.FeignException;
import io.choerodon.devops.api.vo.OrganizationSimplifyVO;
import io.choerodon.devops.api.vo.ProjectCreateVO;
import io.choerodon.devops.api.vo.RoleAssignmentSearchVO;
import io.choerodon.devops.api.vo.iam.ProjectWithRoleVO;
import io.choerodon.devops.api.vo.iam.RoleSearchVO;
import io.choerodon.devops.api.vo.iam.RoleVO;
import io.choerodon.devops.api.vo.iam.UserWithRoleVO;
import io.choerodon.devops.api.vo.kubernetes.ProjectCreateDTO;
import io.choerodon.devops.app.service.IamService;
import io.choerodon.devops.infra.dto.iam.IamAppDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.BaseServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  11:00 2019/7/11
 * Description:
 */
@Service
public class IamServiceImpl implements IamService {
    private static final Logger LOGGER = LoggerFactory.getLogger(IamServiceImpl.class);

    @Autowired
    private BaseServiceClient baseServiceClient;

    public IamServiceImpl(BaseServiceClient baseServiceClient) {
        this.baseServiceClient = baseServiceClient;
    }

    @Override
    public ProjectDTO queryIamProject(Long projectId) {
        ResponseEntity<ProjectDTO> projectDO = baseServiceClient.queryIamProject(projectId);
        if (!projectDO.getStatusCode().is2xxSuccessful()) {
            throw new CommonException("error.project.get");
        }
        return projectDO.getBody();
    }

    @Override
    public OrganizationDTO queryOrganization() {
        ResponseEntity<OrganizationDTO> organization = baseServiceClient.queryOrganization();
        if (organization.getStatusCode().is2xxSuccessful()) {
            return organization.getBody();
        } else {
            throw new CommonException("error.organization.get");
        }
    }

    @Override
    public OrganizationDTO queryOrganizationById(Long organizationId) {
        ResponseEntity<OrganizationDTO> organization = baseServiceClient.queryOrganizationById(organizationId);
        if (organization.getStatusCode().is2xxSuccessful()) {
            return organization.getBody();
        } else {
            throw new CommonException("error.organization.get");
        }
    }

    @Override
    public IamUserDTO queryByLoginName(String userName) {
        try {
            ResponseEntity<IamUserDTO> responseEntity = baseServiceClient.queryByLoginName(userName);
            return responseEntity.getBody();
        } catch (FeignException e) {
            LOGGER.error("get user by longin name {} error", userName);
            return null;
        }
    }

    @Override
    public List<ProjectDTO> listIamProjectByOrgId(Long organizationId, String name, String[] params) {
        List<ProjectDTO> returnList = new ArrayList<>();
        int page = 0;
        int size = 0;
        ResponseEntity<PageInfo<ProjectDTO>> pageResponseEntity =
                baseServiceClient.queryProjectByOrgId(organizationId, page, size, name, null);
        PageInfo<ProjectDTO> projectDOPage = pageResponseEntity.getBody();
        List<ProjectDTO> projectEList = projectDOPage.getList();
        if (!projectEList.isEmpty()) {
            returnList.addAll(projectEList);
        }
        return returnList;
    }

    @Override
    public PageInfo<ProjectDTO> queryProjectByOrgId(Long organizationId, int page, int size, String name, String[] params) {
        try {
            ResponseEntity<PageInfo<ProjectDTO>> pageInfoResponseEntity = baseServiceClient.queryProjectByOrgId(organizationId, page, size, name, params);
            return pageInfoResponseEntity.getBody();
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    @Override
    public List<ProjectWithRoleVO> listProjectWithRoleDTO(Long userId) {
        List<ProjectWithRoleVO> returnList = new ArrayList<>();
        int page = 0;
        int size = 0;
        ResponseEntity<PageInfo<ProjectWithRoleVO>> pageResponseEntity =
                baseServiceClient.listProjectWithRole(userId, page, size);
        PageInfo<ProjectWithRoleVO> projectWithRoleDTOPage = pageResponseEntity.getBody();
        if (!projectWithRoleDTOPage.getList().isEmpty()) {
            returnList.addAll(projectWithRoleDTOPage.getList());
        }
        return returnList;
    }

    @Override
    public List<IamUserDTO> listUsersByIds(List<Long> ids) {
        List<IamUserDTO> userES = new ArrayList<>();
        if (ids != null && !ids.isEmpty()) {
            Long[] newIds = new Long[ids.size()];
            try {
                userES = baseServiceClient.listUsersByIds(ids.toArray(newIds)).getBody();
            } catch (Exception e) {
                throw new CommonException("error.users.get", e);
            }
        }
        return userES;
    }

    @Override
    public IamUserDTO queryUserByUserId(Long id) {
        List<Long> ids = new ArrayList<>();
        ids.add(id);
        List<IamUserDTO> userES = this.listUsersByIds(ids);
        if (userES != null && !userES.isEmpty()) {
            return userES.get(0);
        }
        return null;
    }

    @Override
    public PageInfo<IamUserDTO> pagingQueryUsersByRoleIdOnProjectLevel(PageRequest pageRequest,
                                                                       RoleAssignmentSearchVO roleAssignmentSearchVO,
                                                                       Long roleId, Long projectId, Boolean doPage) {
        try {
            return baseServiceClient.pagingQueryUsersByRoleIdOnProjectLevel(pageRequest.getPage(), pageRequest.getSize(), roleId,
                    projectId, doPage, roleAssignmentSearchVO).getBody();
        } catch (FeignException e) {
            LOGGER.error("get users by role id {} and project id {} error", roleId, projectId);
        }
        return null;
    }

    @Override
    public PageInfo<UserWithRoleVO> queryUserPermissionByProjectId(Long projectId, PageRequest pageRequest,
                                                                   Boolean doPage) {
        try {
            RoleAssignmentSearchVO roleAssignmentSearchVO = new RoleAssignmentSearchVO();
            ResponseEntity<PageInfo<UserWithRoleVO>> userEPageResponseEntity = baseServiceClient
                    .queryUserByProjectId(projectId,
                            pageRequest.getPage(), pageRequest.getSize(), doPage, roleAssignmentSearchVO);
            return userEPageResponseEntity.getBody();
        } catch (FeignException e) {
            LOGGER.error("get user permission by project id {} error", projectId);
            return null;
        }
    }

    @Override
    public IamUserDTO queryByEmail(Long projectId, String email) {
        try {
            ResponseEntity<PageInfo<IamUserDTO>> userDOResponseEntity = baseServiceClient
                    .listUsersByEmail(projectId, 0, 0, email);
            if (userDOResponseEntity.getBody().getList().isEmpty()) {
                return null;
            }
            return userDOResponseEntity.getBody().getList().get(0);
        } catch (FeignException e) {
            LOGGER.error("get user by email {} error", email);
            return null;
        }
    }

    @Override
    public Long queryRoleIdByCode(String roleCode) {
        try {
            RoleSearchVO roleSearchVO = new RoleSearchVO();
            roleSearchVO.setCode(roleCode);
            return baseServiceClient.queryRoleIdByCode(roleSearchVO).getBody().getList().get(0).getId();
        } catch (FeignException e) {
            LOGGER.error("get role id by code {} error", roleCode);
            return null;
        }
    }

    @Override
    public List<Long> getAllMemberIdsWithoutOwner(Long projectId) {
        // 获取项目成员id
        Long memberId = this.queryRoleIdByCode(PROJECT_MEMBER);
        // 获取项目所有者id
        Long ownerId = this.queryRoleIdByCode(PROJECT_OWNER);
        // 项目下所有项目成员
        List<Long> memberIds =

                this.pagingQueryUsersByRoleIdOnProjectLevel(new PageRequest(0, 0), new RoleAssignmentSearchVO(), memberId,
                        projectId, false).getList().stream().map(IamUserDTO::getId).collect(Collectors.toList());
        // 项目下所有项目所有者
        List<Long> ownerIds =
                this.pagingQueryUsersByRoleIdOnProjectLevel(new PageRequest(0, 0), new RoleAssignmentSearchVO(), ownerId,

                        projectId, false).getList().stream().map(IamUserDTO::getId).collect(Collectors.toList());
        return memberIds.stream().filter(e -> !ownerIds.contains(e)).collect(Collectors.toList());
    }

    @Override
    public List<IamUserDTO> getAllMember(Long projectId) {
        // 获取项目成员id
        Long memberId = this.queryRoleIdByCode(PROJECT_MEMBER);
        // 获取项目所有者id
        Long ownerId = this.queryRoleIdByCode(PROJECT_OWNER);
        // 项目下所有项目成员

        List<IamUserDTO> list = this.pagingQueryUsersByRoleIdOnProjectLevel(new PageRequest(0, 0), new RoleAssignmentSearchVO(), memberId,
                projectId, false).getList();
        List<Long> memberIds = list.stream().filter(IamUserDTO::getEnabled).map(IamUserDTO::getId).collect(Collectors.toList());
        // 项目下所有项目所有者
        this.pagingQueryUsersByRoleIdOnProjectLevel(new PageRequest(0, 0), new RoleAssignmentSearchVO(), ownerId,

                projectId, false).getList().stream().filter(IamUserDTO::getEnabled).forEach(t -> {
            if (!memberIds.contains(t.getId())) {
                list.add(t);
            }
        });
        return list;
    }

    @Override
    public Boolean isProjectOwner(Long userId, ProjectDTO projectDTO) {
        List<ProjectWithRoleVO> projectWithRoleVOList = listProjectWithRoleDTO(userId);
        List<RoleVO> roleVOS = new ArrayList<>();
        projectWithRoleVOList.stream().filter(projectWithRoleDTO ->
                projectWithRoleDTO.getName().equals(projectDTO.getName())).forEach(projectWithRoleDTO ->
                roleVOS.addAll(projectWithRoleDTO.getRoles()
                        .stream().filter(roleDTO -> roleDTO.getCode().equals(PROJECT_OWNER))
                        .collect(Collectors.toList())));
        return !roleVOS.isEmpty();
    }

    @Override
    public IamAppDTO createIamApp(Long organizationId, IamAppDTO iamAppDTO) {
        ResponseEntity<IamAppDTO> iamAppPayLoadResponseEntity = null;
        try {
            iamAppPayLoadResponseEntity = baseServiceClient.createIamApplication(organizationId, iamAppDTO);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
        IamAppDTO result = iamAppPayLoadResponseEntity.getBody();
        if (result == null || result.getProjectId() == null) {
            throw new CommonException("error.code.exist");
        }
        return result;
    }

    @Override
    public IamAppDTO updateIamApp(Long organizationId, Long id, IamAppDTO iamAppDTO) {
        ResponseEntity<IamAppDTO> iamAppPayLoadResponseEntity = null;
        try {
            iamAppPayLoadResponseEntity = baseServiceClient.updateIamApplication(organizationId, id, iamAppDTO);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
        return iamAppPayLoadResponseEntity.getBody();
    }

    @Override
    public IamAppDTO queryIamAppByCode(Long organizationId, String code) {
        ResponseEntity<PageInfo<IamAppDTO>> iamAppPayLoadResponseEntity = null;
        try {
            iamAppPayLoadResponseEntity = baseServiceClient.getIamApplication(organizationId, code);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
        return iamAppPayLoadResponseEntity.getBody().getList().isEmpty() ? null : iamAppPayLoadResponseEntity.getBody().getList().get(0);
    }

    @Override
    public ProjectDTO createProject(Long organizationId, ProjectCreateVO projectCreateVO) {
        try {
            ProjectCreateDTO projectDTO = new ProjectCreateDTO();
            BeanUtils.copyProperties(projectCreateVO, projectDTO);
            ResponseEntity<ProjectDTO> projectDTORes = baseServiceClient
                    .createProject(organizationId, projectDTO);
            return projectDTORes.getBody();
        } catch (FeignException e) {
            LOGGER.error("error.create.iam.project");
            return null;
        }
    }

    @Override
    public PageInfo<OrganizationSimplifyVO> getAllOrgs(Integer page, Integer size) {
        try {
            ResponseEntity<PageInfo<OrganizationSimplifyVO>> simplifyDTOs = baseServiceClient
                    .getAllOrgs(page, size);
            return simplifyDTOs.getBody();
        } catch (FeignException e) {
            LOGGER.error("error.get.all.organization");
            return null;
        }
    }
}
