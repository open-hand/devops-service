package io.choerodon.devops.infra.persistence.impl;

import static io.choerodon.core.iam.InitRoleCode.PROJECT_MEMBER;
import static io.choerodon.core.iam.InitRoleCode.PROJECT_OWNER;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.exception.FeignException;
import io.choerodon.devops.api.dto.RoleAssignmentSearchDTO;
import io.choerodon.devops.api.dto.iam.*;
import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.domain.application.event.IamAppPayLoad;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.devops.infra.dataobject.iam.OrganizationDO;
import io.choerodon.devops.infra.dataobject.iam.ProjectDO;
import io.choerodon.devops.infra.dataobject.iam.UserDO;
import io.choerodon.devops.infra.feign.IamServiceClient;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Created by younger on 2018/3/29.
 */
@Component
public class IamRepositoryImpl implements IamRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(IamRepositoryImpl.class);

    private IamServiceClient iamServiceClient;

    public IamRepositoryImpl(IamServiceClient iamServiceClient) {
        this.iamServiceClient = iamServiceClient;
    }

    @Override
    public ProjectE queryIamProject(Long projectId) {
        ResponseEntity<ProjectDO> projectDO = iamServiceClient.queryIamProject(projectId);
        if (!projectDO.getStatusCode().is2xxSuccessful()) {
            throw new CommonException("error.project.get");
        }
        return ConvertHelper.convert(projectDO.getBody(), ProjectE.class);
    }

    @Override
    public Organization queryOrganization() {
        ResponseEntity<OrganizationDO> organization = iamServiceClient.queryOrganization();
        if (organization.getStatusCode().is2xxSuccessful()) {
            return ConvertHelper.convert(organization.getBody(), Organization.class);
        } else {
            throw new CommonException("error.organization.get");
        }
    }

    @Override
    public Organization queryOrganizationById(Long organizationId) {
        ResponseEntity<OrganizationDO> organization = iamServiceClient.queryOrganizationById(organizationId);
        if (organization.getStatusCode().is2xxSuccessful()) {
            return ConvertHelper.convert(organization.getBody(), Organization.class);
        } else {
            throw new CommonException("error.organization.get");
        }
    }

    @Override
    public UserE queryByLoginName(String userName) {
        try {
            ResponseEntity<UserDO> responseEntity = iamServiceClient.queryByLoginName(userName);
            return ConvertHelper.convert(responseEntity.getBody(), UserE.class);
        } catch (FeignException e) {
            LOGGER.error("get user by longin name {} error", userName);
            return null;
        }
    }

    @Override
    public List<ProjectE> listIamProjectByOrgId(Long organizationId, String name, String[] params) {
        List<ProjectE> returnList = new ArrayList<>();
        int page = 0;
        int size = 200;
        ResponseEntity<Page<ProjectDO>> pageResponseEntity =
                iamServiceClient.queryProjectByOrgId(organizationId, page, size, name, null);
        Page<ProjectDO> projectDOPage = pageResponseEntity.getBody();
        List<ProjectE> projectEList = ConvertHelper.convertList(projectDOPage.getContent(), ProjectE.class);
        if (ConvertHelper.convertList(projectDOPage.getContent(), ProjectE.class) != null) {
            returnList.addAll(projectEList);
        }
        int totalPages = projectDOPage.getTotalPages();
        if (totalPages > 1) {
            for (int i = 1; i < totalPages; i++) {
                page = i;
                ResponseEntity<Page<ProjectDO>> entity = iamServiceClient
                        .queryProjectByOrgId(organizationId, page, size, name, null);
                if (entity != null) {
                    Page<ProjectDO> project = entity.getBody();
                    List<ProjectE> projectE = ConvertHelper.convertList(project.getContent(), ProjectE.class);
                    if (projectE != null) {
                        returnList.addAll(projectE);
                    }
                }
            }
        }
        return returnList;
    }

    @Override
    public Page<ProjectDO> queryProjectByOrgId(Long organizationId, int page, int size, String name, String[] params) {
        try {
            return iamServiceClient.queryProjectByOrgId(organizationId, page, size, name, params).getBody();
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    @Override
    public List<ProjectWithRoleDTO> listProjectWithRoleDTO(Long userId) {
        List<ProjectWithRoleDTO> returnList = new ArrayList<>();
        int page = 0;
        int size = 200;
        ResponseEntity<Page<ProjectWithRoleDTO>> pageResponseEntity =
                iamServiceClient.listProjectWithRole(userId, page, size);
        Page<ProjectWithRoleDTO> projectWithRoleDTOPage = pageResponseEntity.getBody();
        if (projectWithRoleDTOPage.getContent() != null) {
            returnList.addAll(projectWithRoleDTOPage.getContent());
        }
        int totalPages = projectWithRoleDTOPage.getTotalPages();
        if (totalPages > 1) {
            for (int i = 1; i < totalPages; i++) {
                page = i;
                ResponseEntity<Page<ProjectWithRoleDTO>> entity = iamServiceClient
                        .listProjectWithRole(userId, page, size);
                if (entity.getBody() != null) {
                    Page<ProjectWithRoleDTO> projectWithRolePageNew = entity.getBody();
                    if (!projectWithRolePageNew.getContent().isEmpty()) {
                        returnList.addAll(projectWithRolePageNew.getContent());
                    }
                }
            }
        }
        return returnList;
    }

    @Override
    public List<UserE> listUsersByIds(List<Long> ids) {
        List<UserE> userES = new ArrayList<>();
        if (ids != null && !ids.isEmpty()) {
            Long[] newIds = new Long[ids.size()];
            try {
                userES = ConvertHelper.convertList(iamServiceClient
                        .listUsersByIds(ids.toArray(newIds)).getBody(), UserE.class);
            } catch (Exception e) {
                throw new CommonException("error.users.get", e);
            }
        }
        return userES;
    }

    @Override
    public UserE queryUserByUserId(Long id) {
        List<Long> ids = new ArrayList<>();
        ids.add(id);
        List<UserE> userES = this.listUsersByIds(ids);
        if (userES != null && !userES.isEmpty()) {
            return userES.get(0);
        }
        return null;
    }

    @Override
    public Page<UserDTO> pagingQueryUsersByRoleIdOnProjectLevel(PageRequest pageRequest,
                                                                RoleAssignmentSearchDTO roleAssignmentSearchDTO,
                                                                Long roleId, Long projectId, Boolean doPage) {
        try {
            return iamServiceClient
                    .pagingQueryUsersByRoleIdOnProjectLevel(pageRequest.getPage(), pageRequest.getSize(), roleId,
                            projectId, doPage, roleAssignmentSearchDTO).getBody();
        } catch (FeignException e) {
            LOGGER.error("get users by role id {} and project id {} error", roleId, projectId);
        }
        return null;
    }

    @Override
    public Page<UserWithRoleDTO> queryUserPermissionByProjectId(Long projectId, PageRequest pageRequest,
                                                                Boolean doPage) {
        try {
            RoleAssignmentSearchDTO roleAssignmentSearchDTO = new RoleAssignmentSearchDTO();
            ResponseEntity<Page<UserWithRoleDTO>> userEPageResponseEntity = iamServiceClient
                    .queryUserByProjectId(projectId,
                            pageRequest.getPage(), pageRequest.getSize(), doPage, roleAssignmentSearchDTO);
            return userEPageResponseEntity.getBody();
        } catch (FeignException e) {
            LOGGER.error("get user permission by project id {} error", projectId);
            return null;
        }
    }

    @Override
    public UserE queryByEmail(Long projectId, String email) {
        try {
            ResponseEntity<Page<UserDO>> userDOResponseEntity = iamServiceClient
                    .listUsersByEmail(projectId, 0, 10, email);
            if (userDOResponseEntity.getBody().getContent().isEmpty()) {
                return null;
            }
            return ConvertHelper.convert(userDOResponseEntity.getBody().getContent().get(0), UserE.class);
        } catch (FeignException e) {
            LOGGER.error("get user by email {} error", email);
            return null;
        }
    }

    @Override
    public Long queryRoleIdByCode(String roleCode) {
        try {
            RoleSearchDTO roleSearchDTO = new RoleSearchDTO();
            roleSearchDTO.setCode(roleCode);
            return iamServiceClient.queryRoleIdByCode(roleSearchDTO).getBody().getContent().get(0).getId();
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
                this.pagingQueryUsersByRoleIdOnProjectLevel(new PageRequest(), new RoleAssignmentSearchDTO(), memberId,
                        projectId, false).getContent().stream().map(UserDTO::getId).collect(Collectors.toList());
        // 项目下所有项目所有者
        List<Long> ownerIds =
                this.pagingQueryUsersByRoleIdOnProjectLevel(new PageRequest(), new RoleAssignmentSearchDTO(), ownerId,
                        projectId, false).getContent().stream().map(UserDTO::getId).collect(Collectors.toList());
        return memberIds.stream().filter(e -> !ownerIds.contains(e)).collect(Collectors.toList());
    }

    @Override
    public Boolean isProjectOwner(Long userId, ProjectE projectE) {
        List<ProjectWithRoleDTO> projectWithRoleDTOList = listProjectWithRoleDTO(userId);
        List<RoleDTO> roleDTOS = new ArrayList<>();
        projectWithRoleDTOList.stream().filter(projectWithRoleDTO ->
                projectWithRoleDTO.getName().equals(projectE.getName())).forEach(projectWithRoleDTO ->
                roleDTOS.addAll(projectWithRoleDTO.getRoles()
                        .stream().filter(roleDTO -> roleDTO.getCode().equals(PROJECT_OWNER))
                        .collect(Collectors.toList())));
        return !roleDTOS.isEmpty();
    }

    @Override
    public IamAppPayLoad createIamApp(Long organizationId, IamAppPayLoad iamAppPayLoad) {
        ResponseEntity<IamAppPayLoad> iamAppPayLoadResponseEntity = null;
        try {
            iamAppPayLoadResponseEntity = iamServiceClient.createIamApplication(organizationId, iamAppPayLoad);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
        return iamAppPayLoadResponseEntity.getBody();
    }

    @Override
    public IamAppPayLoad updateIamApp(Long organizationId, Long id, IamAppPayLoad iamAppPayLoad) {
        ResponseEntity<IamAppPayLoad> iamAppPayLoadResponseEntity = null;
        try {
            iamAppPayLoadResponseEntity = iamServiceClient.updateIamApplication(organizationId, id, iamAppPayLoad);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
        return iamAppPayLoadResponseEntity.getBody();
    }

    @Override
    public void disabledIamApp(Long organizationId, Long id) {
        try {
            iamServiceClient.disableIamApplication(organizationId, id);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    @Override
    public void enableIamApp(Long organizationId, Long id) {
        try {
            iamServiceClient.enableIamApplication(organizationId, id);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }
}
