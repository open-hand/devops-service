package io.choerodon.devops.infra.feign.operator;

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.exception.ExceptionResponse;
import io.choerodon.core.exception.FeignException;
import io.choerodon.devops.api.vo.RoleAssignmentSearchVO;
import io.choerodon.devops.api.vo.iam.AppDownloadDevopsReqVO;
import io.choerodon.devops.api.vo.iam.ProjectWithRoleVO;
import io.choerodon.devops.api.vo.iam.RemoteTokenAuthorizationVO;
import io.choerodon.devops.api.vo.kubernetes.ProjectCreateDTO;
import io.choerodon.devops.infra.dto.iam.*;
import io.choerodon.devops.infra.enums.LabelType;
import io.choerodon.devops.infra.enums.OrgPublishMarketStatus;
import io.choerodon.devops.infra.feign.BaseServiceClient;
import io.choerodon.devops.infra.util.FeignParamUtils;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.mybatis.autoconfigure.CustomPageRequest;

/**
 * Created by Sheep on 2019/7/11.
 */

@Component
public class BaseServiceClientOperator {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseServiceClientOperator.class);
    private static final Gson gson = new Gson();

    private static final String LOGIN_NAME = "loginName";
    private static final String REAL_NAME = "realName";


    @Autowired
    private BaseServiceClient baseServiceClient;

    public ProjectDTO queryIamProjectById(Long projectId) {
        ResponseEntity<ProjectDTO> projectDTOResponseEntity = baseServiceClient.queryIamProject(projectId);
        if (!projectDTOResponseEntity.getStatusCode().is2xxSuccessful()) {
            throw new CommonException("error.project.query.by.id", projectId);
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

    public List<ProjectDTO> listIamProjectByOrgId(Long organizationId) {
        return listIamProjectByOrgId(organizationId, null, null, null);
    }


    public List<ProjectDTO> listIamProjectByOrgId(Long organizationId, String name, String code, String params) {
        CustomPageRequest customPageRequest = CustomPageRequest.of(0, 0);
        ResponseEntity<PageInfo<ProjectDTO>> pageResponseEntity =
                baseServiceClient.pageProjectsByOrgId(organizationId, FeignParamUtils.encodePageRequest(customPageRequest), name, code, true, params);
        return Objects.requireNonNull(pageResponseEntity.getBody()).getList();
    }

    public PageInfo<ProjectDTO> pageProjectByOrgId(Long organizationId, int page, int size, Sort sort, String name, String code, String params) {
        CustomPageRequest pageable = CustomPageRequest.of(page, size, sort == null ? Sort.unsorted() : sort);
        try {
            ResponseEntity<PageInfo<ProjectDTO>> pageInfoResponseEntity = baseServiceClient.pageProjectsByOrgId(organizationId,
                    FeignParamUtils.encodePageRequest(pageable), name, code, true, params);
            return pageInfoResponseEntity.getBody();
        } catch (FeignException e) {
            throw new CommonException(e);
        }
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
                        .listUsersByIds(ids.toArray(newIds), false).getBody();
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

    public List<IamUserDTO> queryUsersByUserIds(List<Long> ids) {
        return this.listUsersByIds(ids);
    }

    public List<IamUserDTO> listUsersWithGitlabLabel(Long projectId,
                                                     RoleAssignmentSearchVO roleAssignmentSearchVO,
                                                     String labelName) {
        try {
            return baseServiceClient
                    .listUsersWithGitlabLabel(projectId, roleAssignmentSearchVO, labelName).getBody();
        } catch (FeignException e) {
            throw new CommonException("error.user.get.byGitlabLabel");
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

            return baseServiceClient.queryRoleIdByCode(roleCode).getBody().getList().get(0).getId();
        } catch (FeignException e) {
            LOGGER.error("get role id by code {} error", roleCode);
            return null;
        }
    }

    public List<Long> getAllMemberIdsWithoutOwner(Long projectId) {
        // 项目下所有项目成员
        List<Long> memberIds =
                this.listUsersWithGitlabLabel(projectId, new RoleAssignmentSearchVO(), LabelType.GITLAB_PROJECT_DEVELOPER.getValue())
                        .stream().map(IamUserDTO::getId).collect(Collectors.toList());
        // 项目下所有项目所有者
        List<Long> ownerIds =
                this.listUsersWithGitlabLabel(projectId, new RoleAssignmentSearchVO(), LabelType.GITLAB_PROJECT_OWNER.getValue())
                        .stream().map(IamUserDTO::getId).collect(Collectors.toList());
        return memberIds.stream().filter(e -> !ownerIds.contains(e)).collect(Collectors.toList());
    }

    //获得所有项目所有者id
    public List<Long> getAllOwnerIds(Long projectId) {
        // 项目下所有项目所有者
        return this.listUsersWithGitlabLabel(projectId, new RoleAssignmentSearchVO(), LabelType.GITLAB_PROJECT_OWNER.getValue())
                .stream().filter(IamUserDTO::getEnabled).map(IamUserDTO::getId).collect(Collectors.toList());
    }

    public List<IamUserDTO> getAllMember(Long projectId, String params) {
        // 项目下所有项目成员

        RoleAssignmentSearchVO roleAssignmentSearchVO = new RoleAssignmentSearchVO();
        roleAssignmentSearchVO.setEnabled(true);
        Map<String, Object> searchParamMap;
        List<String> paramList;
        // 处理搜索参数
        if (!StringUtils.isEmpty(params)) {
            Map maps = gson.fromJson(params, Map.class);
            searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
            paramList = TypeUtil.cast(maps.get(TypeUtil.PARAMS));
            roleAssignmentSearchVO.setParam(paramList == null ? null : paramList.toArray(new String[0]));
            if (searchParamMap != null) {
                if (searchParamMap.get(LOGIN_NAME) != null) {
                    String loginName = TypeUtil.objToString(searchParamMap.get(LOGIN_NAME));
                    roleAssignmentSearchVO.setLoginName(loginName);
                }
                if (searchParamMap.get(REAL_NAME) != null) {
                    String realName = TypeUtil.objToString(searchParamMap.get(REAL_NAME));
                    roleAssignmentSearchVO.setRealName(realName);
                }
            }
        }

        // 查出项目下的所有成员
        List<IamUserDTO> list = this.listUsersWithGitlabLabel(projectId, new RoleAssignmentSearchVO(), LabelType.GITLAB_PROJECT_DEVELOPER.getValue());
        List<Long> memberIds = list.stream().filter(IamUserDTO::getEnabled).map(IamUserDTO::getId).collect(Collectors.toList());
        // 项目下所有项目所有者
        this.listUsersWithGitlabLabel(projectId, new RoleAssignmentSearchVO(), LabelType.GITLAB_PROJECT_OWNER.getValue())
                .stream().filter(IamUserDTO::getEnabled).forEach(t -> {
            if (!memberIds.contains(t.getId())) {
                list.add(t);
            }
        });
        return list;
    }

    public Boolean isGitlabProjectOwner(Long userId, Long projectId) {
        Boolean isGitlabProjectOwner;
        try {
            isGitlabProjectOwner = baseServiceClient.checkIsGitlabProjectOwner(userId, projectId).getBody();
        } catch (FeignException e) {
            throw new CommonException(e);
        }
        return isGitlabProjectOwner;
    }

    public Boolean isGitLabOrgOwner(Long userId, Long projectId) {
        Boolean isGitLabOrgOwner;
        try {
            isGitLabOrgOwner = baseServiceClient.checkIsGitlabOrgOwner(userId, projectId).getBody();
        } catch (FeignException e) {
            throw new CommonException(e);
        }
        return isGitLabOrgOwner;
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


    public ApplicationDTO queryAppById(Long id) {
        try {
            return baseServiceClient.queryAppById(id).getBody();
        } catch (Exception e) {
            return null;
        }
    }

    public void publishFail(Long projectId, Long mktAppVersionId, String code, Boolean fixFlag) {
        try {
            baseServiceClient.publishFail(projectId, mktAppVersionId, code, fixFlag).getBody();
        } catch (Exception e) {
            throw new CommonException("error.insert.failed.message", e.getMessage());
        }
    }


    public void completeDownloadApplication(Long publishAppVersionId, Long appVersionId, Long organizationId, List<AppDownloadDevopsReqVO> appDownloadDevopsReqVOS) {
        try {
            ResponseEntity<String> responseEntity = baseServiceClient.completeDownloadApplication(publishAppVersionId, appVersionId, organizationId, appDownloadDevopsReqVOS);
            if (responseEntity != null && responseEntity.getBody() != null) {
                ExceptionResponse exceptionResponse = gson.fromJson(responseEntity.getBody(), ExceptionResponse.class);
                if (exceptionResponse.getFailed()) {
                    throw new CommonException("error.application.download.complete");
                }
            }
        } catch (Exception e) {
            throw new CommonException("error.application.download.complete", e.getMessage());
        }
    }

    public void failToDownloadApplication(Long publishAppVersionId, Long appVersionId, Long organizationId) {
        try {
            ResponseEntity<String> responseEntity = baseServiceClient.failToDownloadApplication(publishAppVersionId, appVersionId, organizationId);
            if (responseEntity != null && responseEntity.getBody() != null) {
                ExceptionResponse exceptionResponse = gson.fromJson(responseEntity.getBody(), ExceptionResponse.class);
                if (exceptionResponse.getFailed()) {
                    throw new CommonException("error.application.download.failed");
                }
            }
        } catch (Exception e) {
            throw new CommonException("error.application.download.failed", e.getMessage());
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

    public List<ProjectDTO> queryProjectsByIds(Set<Long> ids) {
        try {
            return baseServiceClient.queryByIds(ids).getBody();
        } catch (Exception e) {
            // TODO 是否考虑抛异常，暂时没时间看为什么返回null
            return null;
        }
    }


    /**
     * 根据组织id和项目code查询项目
     *
     * @param projectCode    项目code
     * @param organizationId 组织id
     * @return 项目信息(可能为空)
     */
    @Nullable
    public ProjectDTO queryProjectByCodeAndOrganizationId(@Nonnull String projectCode, @Nonnull Long organizationId) {
        try {
            ResponseEntity<ProjectDTO> resp = baseServiceClient.queryProjectByCodeAndOrgId(organizationId, projectCode);
            return resp == null ? null : resp.getBody();
        } catch (Exception ex) {
            LOGGER.info("Exception occurred when querying project by code {} and organization id {}", projectCode, organizationId);
            return null;
        }
    }

    public List<Long> listServicesForMarket(@Nonnull Long organizationId, Boolean deployOnly) {
        String status = deployOnly != null && deployOnly ? OrgPublishMarketStatus.DEPLOY_ONLY.getType() : OrgPublishMarketStatus.DOWNLOAD_ONLY.getType();
        try {
            ResponseEntity<Set<Long>> resp = baseServiceClient.listService(organizationId, status);

            return resp == null || resp.getBody() == null ? null : new ArrayList<>(resp.getBody());
        } catch (Exception ex) {
            return null;
        }
    }

    public List<Long> listServiceVersionsForMarket(@Nonnull Long organizationId, Boolean deployOnly) {
        String status = deployOnly != null && deployOnly ? OrgPublishMarketStatus.DEPLOY_ONLY.getType() : OrgPublishMarketStatus.DOWNLOAD_ONLY.getType();
        try {
            ResponseEntity<Set<Long>> resp = baseServiceClient.listSvcVersion(organizationId, status);
            return resp == null || resp.getBody() == null ? null : new ArrayList<>(resp.getBody());
        } catch (Exception ex) {
            return null;
        }
    }

    public List<ProjectWithRoleVO> listProjectWithRole(Long userId, int page, int size) {
        try {
            ResponseEntity<PageInfo<ProjectWithRoleVO>> pageInfoResponseEntity = baseServiceClient.listProjectWithRole(userId, page, size);
            return (pageInfoResponseEntity.getBody() == null) ? Collections.emptyList() : pageInfoResponseEntity.getBody().getList();
        } catch (Exception ex) {
            return Collections.emptyList();
        }

    }

    public ClientDTO createClient(Long organizationId, ClientVO clientVO) {
        try {
            ClientDTO client = baseServiceClient.createClient(organizationId, clientVO).getBody();
            if (client == null) {
                throw new CommonException("error.create.client");
            }
            return client;
        } catch (Exception ex) {
            throw new CommonException("error.create.client");
        }
    }

    public void deleteClient(Long organizationId, Long clientId) {
        try {
            baseServiceClient.deleteClient(organizationId, clientId);
        } catch (Exception ex) {
            throw new CommonException("error.delete.client");
        }
    }

    public ClientDTO queryClientBySourceId(Long organizationId, Long sourceId) {
        try {
            ResponseEntity<ClientDTO> responseEntity = baseServiceClient.queryClientBySourceId(organizationId, sourceId);
            return responseEntity.getBody();
        } catch (Exception ex) {
            throw new CommonException("error.query.client");
        }
    }

    /**
     * 通过登录名查询用户
     *
     * @param loginName 登录名
     * @return 用户信息
     */
    public IamUserDTO queryUserByLoginName(String loginName) {
        try {
            ResponseEntity<IamUserDTO> responseEntity = baseServiceClient.queryByLoginName(loginName);
            IamUserDTO iamUserDTO = responseEntity.getBody();
            if (iamUserDTO == null || iamUserDTO.getId() == null) {
                throw new CommonException("error.query.user.by.login.name", loginName);
            }
            return iamUserDTO;
        } catch (Exception ex) {
            throw new CommonException("error.query.user.by.login.name", loginName);
        }
    }

    public List<IamUserDTO> queryAllRootUsers() {
        ResponseEntity<List<IamUserDTO>> responseEntity = baseServiceClient.queryAllAdminUsers();
        return responseEntity == null ? Collections.emptyList() : responseEntity.getBody();
    }

    /**
     * 查询所有的组织管理员
     *
     * @return
     */
    public List<IamUserDTO> queryAllOrgRoot() {
        ResponseEntity<List<IamUserDTO>> responseEntity = baseServiceClient.queryAllOrgRoot();
        return responseEntity == null ? Collections.emptyList() : responseEntity.getBody();
    }
}
