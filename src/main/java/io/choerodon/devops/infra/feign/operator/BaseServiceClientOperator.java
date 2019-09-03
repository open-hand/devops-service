package io.choerodon.devops.infra.feign.operator;

import static io.choerodon.core.iam.InitRoleCode.PROJECT_MEMBER;
import static io.choerodon.core.iam.InitRoleCode.PROJECT_OWNER;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.exception.FeignException;
import io.choerodon.devops.api.vo.OrganizationSimplifyVO;
import io.choerodon.devops.api.vo.RoleAssignmentSearchVO;
import io.choerodon.devops.api.vo.iam.*;
import io.choerodon.devops.api.vo.kubernetes.ProjectCreateDTO;
import io.choerodon.devops.infra.dto.iam.*;
import io.choerodon.devops.infra.feign.BaseServiceClient;
import io.choerodon.devops.infra.util.FeignParamUtils;

/**
 * Created by Sheep on 2019/7/11.
 */

@Component
public class BaseServiceClientOperator {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseServiceClientOperator.class);

    @Autowired
    private BaseServiceClient baseServiceClient;

    public ProjectDTO queryIamProjectById(Long projectId) {
        ResponseEntity<ProjectDTO> projectDTOResponseEntity = baseServiceClient.queryIamProject(projectId);
        if (!projectDTOResponseEntity.getStatusCode().is2xxSuccessful()) {
            throw new CommonException("error.project.get");
        }
        return projectDTOResponseEntity.getBody();
    }

    public OrganizationDTO queryOrganizationById(Long organizationId) {
        ResponseEntity<OrganizationDTO> organizationDTOResponseEntity = baseServiceClient.queryOrganizationById(organizationId);
        if (organizationDTOResponseEntity.getStatusCode().is2xxSuccessful()) {
            return organizationDTOResponseEntity.getBody();
        } else {
            throw new CommonException("error.organization.get");
        }
    }

    public List<ProjectDTO> listIamProjectByOrgId(Long organizationId, String name, String[] params) {
        int page = 0;
        int size = 0;
        ResponseEntity<PageInfo<ProjectDTO>> pageResponseEntity =
                baseServiceClient.queryProjectByOrgId(organizationId, page, size, name, null);
        return pageResponseEntity.getBody().getList();
    }

    public PageInfo<ProjectDTO> pageProjectByOrgId(Long organizationId, int page, int size, String name, String[] params) {
        try {
            ResponseEntity<PageInfo<ProjectDTO>> pageInfoResponseEntity = baseServiceClient.queryProjectByOrgId(organizationId, page, size, name, params);
            return pageInfoResponseEntity.getBody();
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    public List<ApplicationDTO> listAppsByOrgId(Long orgId, String name) {
        ResponseEntity<List<ApplicationDTO>> apps;
        try {
            apps = baseServiceClient.queryAppsByOrgId(orgId, false, 1, 0, name, null, null, null);
        } catch (Exception e) {
            throw new CommonException(e);
        }
        return apps.getBody();
    }

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

    public List<IamUserDTO> listUsersByIds(List<Long> ids) {
        List<IamUserDTO> userDTOS = new ArrayList<>();
        if (ids != null && !ids.isEmpty()) {
            Long[] newIds = new Long[ids.size()];
            try {
                userDTOS = baseServiceClient
                        .listUsersByIds(ids.toArray(newIds)).getBody();

            } catch (Exception e) {
                throw new CommonException("error.users.get", e);
            }
        }
        return userDTOS;
    }

    public IamUserDTO queryUserByUserId(Long id) {
        List<Long> ids = new ArrayList<>();
        ids.add(id);
        List<IamUserDTO> userES = this.listUsersByIds(ids);
        if (userES != null && !userES.isEmpty()) {
            return userES.get(0);
        }
        return null;
    }

    public PageInfo<IamUserDTO> pagingQueryUsersByRoleIdOnProjectLevel(PageRequest pageRequest,
                                                                       RoleAssignmentSearchVO roleAssignmentSearchVO,
                                                                       Long roleId, Long projectId, Boolean doPage) {
        try {
            return baseServiceClient
                    .pagingQueryUsersByRoleIdOnProjectLevel(pageRequest.getPage(), pageRequest.getSize(), roleId,
                            projectId, doPage, roleAssignmentSearchVO).getBody();
        } catch (FeignException e) {
            LOGGER.error("get users by role id {} and project id {} error", roleId, projectId);
        }
        return null;
    }

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

    public IamAppDTO createIamApp(Long organizationId, IamAppDTO appDTO) {
        ResponseEntity<IamAppDTO> appDTOResponseEntity = null;
        try {
            appDTOResponseEntity = baseServiceClient.createIamApplication(organizationId, appDTO);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
        IamAppDTO result = appDTOResponseEntity.getBody();
        if (result == null || result.getProjectId() == null) {
            throw new CommonException("error.code.exist");
        }
        return result;
    }

    public IamAppDTO updateIamApp(Long organizationId, Long id, IamAppDTO appDTO) {
        ResponseEntity<IamAppDTO> appDTOResponseEntity = null;
        try {
            appDTOResponseEntity = baseServiceClient.updateIamApplication(organizationId, id, appDTO);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
        return appDTOResponseEntity.getBody();
    }

    public IamAppDTO queryIamAppByCode(Long organizationId, String code) {
        ResponseEntity<PageInfo<IamAppDTO>> pageInfoResponseEntity = null;
        try {
            pageInfoResponseEntity = baseServiceClient.getIamApplication(organizationId, code);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
        return pageInfoResponseEntity.getBody().getList().isEmpty() ? null : pageInfoResponseEntity.getBody().getList().get(0);
    }

    public ProjectDTO createProject(Long organizationId, ProjectCreateDTO projectCreateDTO) {
        try {
            ResponseEntity<ProjectDTO> projectDTOResponseEntity = baseServiceClient
                    .createProject(organizationId, projectCreateDTO);
            return projectDTOResponseEntity.getBody();
        } catch (FeignException e) {
            LOGGER.error("error.create.iam.project");
            return null;
        }
    }

    public PageInfo<ProjectDTO> listProject(Long organizationId, PageRequest pageRequest, String[] params) {
        try {
            ResponseEntity<PageInfo<ProjectDTO>> projectDTOResponseEntity = baseServiceClient
                    .listProject(organizationId, FeignParamUtils.encodePageRequest(pageRequest), null, null, null, true, null, params);
            return projectDTOResponseEntity.getBody();
        } catch (FeignException e) {
            LOGGER.error("error.create.iam.project");
            return null;
        }
    }

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

    public ProjectDTO queryProjectByAppId(Long id) {
        try {
            return baseServiceClient.queryProjectByAppId(id).getBody();
        } catch (Exception e) {
            return null;
        }
    }

    public ApplicationDTO queryAppById(Long id) {
        try {
            return baseServiceClient.queryAppById(id).getBody();
        } catch (Exception e) {
            return null;
        }
    }

    public void publishFail(Long projectId, Long mktAppId, Long mktAppVersionId, String code, Boolean fixFlag) {
        try {
            baseServiceClient.publishFail(projectId, mktAppId, mktAppVersionId, code, fixFlag).getBody();
        } catch (Exception e) {
            throw new CommonException("error.insert.failed.message", e.getMessage());
        }
    }

    public PageInfo<ProjectDTO> pagingProjectByOptions(Long organizationId, int page, int size, String[] params) {
        try {
            return baseServiceClient.pagingProjectByOptions(organizationId, false, page, size, params).getBody();
        } catch (Exception e) {
            return null;
        }
    }

    public void completeDownloadApplication(Long publishAppVersionId, Set<Long> serviceVersionIds) {
        try {
            baseServiceClient.completeDownloadApplication(publishAppVersionId, serviceVersionIds);
        } catch (Exception e) {
            throw new CommonException("error.application.download.message", e.getMessage());
        }
    }

    public String checkLatestToken() {
        try {
            RemoteTokenAuthorizationVO remoteTokenAuthorizationVO = baseServiceClient.checkLatestToken().getBody();
            if (remoteTokenAuthorizationVO != null) {
                return remoteTokenAuthorizationVO.getRemoteToken();
            }
        } catch (Exception e) {
            throw new CommonException("error.remote.token.authorization", e.getMessage());
        }
        return null;
    }
}
