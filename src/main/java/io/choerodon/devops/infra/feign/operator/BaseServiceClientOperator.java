package io.choerodon.devops.infra.feign.operator;

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.Gson;
import org.hzero.core.util.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.OrgAdministratorVO;
import io.choerodon.devops.api.vo.ResourceLimitVO;
import io.choerodon.devops.api.vo.RoleAssignmentSearchVO;
import io.choerodon.devops.infra.dto.iam.*;
import io.choerodon.devops.infra.enums.LabelType;
import io.choerodon.devops.infra.feign.BaseServiceClient;
import io.choerodon.devops.infra.util.FeignParamUtils;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;

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

    /**
     * @param organizationId 组织id
     * @param code           角色标签
     * @return 角色id
     */
    public Long getRoleId(Long organizationId, String code, String labelName) {
        ResponseEntity<List<RoleDTO>> roleResponseEntity = baseServiceClient.getRoleByCode(organizationId, code, labelName);
        List<RoleDTO> roleDTOList = roleResponseEntity.getBody();
        if (roleResponseEntity.getStatusCode().is2xxSuccessful() && roleDTOList != null && roleDTOList.size() != 0) {
            return roleDTOList.get(0).getId();
        } else {
            throw new CommonException("error.organization.role.id.get", code);
        }
    }

    public ProjectDTO queryIamProjectById(Long projectId) {
        return queryIamProjectById(projectId, true, true, true);
    }

    public ProjectDTO queryIamProjectById(Long projectId, Boolean withCategory, Boolean withUserInfo, Boolean withAgileInfo) {
        ResponseEntity<ProjectDTO> projectDTOResponseEntity = baseServiceClient.queryIamProject(Objects.requireNonNull(projectId), withCategory, withUserInfo, withAgileInfo);
        ProjectDTO projectDTO = projectDTOResponseEntity.getBody();
        // 判断id是否为空是因为可能会返回 CommonException 但是也会被反序列化为  ProjectDTO
        if (projectDTO == null || projectDTO.getId() == null) {
            throw new CommonException("error.project.query.by.id", projectId);
        }
        return projectDTO;
    }

    public Tenant queryOrganizationById(Long organizationId) {
        return queryOrganizationById(organizationId, true);
    }

    public Tenant queryOrganizationById(Long organizationId, Boolean withMoreInfo) {
        ResponseEntity<Tenant> organizationDTOResponseEntity = baseServiceClient.queryOrganizationById(organizationId, withMoreInfo);
        if (organizationDTOResponseEntity.getStatusCode().is2xxSuccessful()) {
            Tenant tenant = organizationDTOResponseEntity.getBody();
            if (tenant != null && tenant.getTenantId() != null) {
                return tenant;
            }
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("queryOrganizationById: unexpected result: {}", JSONObject.toJSONString(tenant));
            }
        }
        throw new CommonException("error.organization.get", organizationId);
    }

    public List<Tenant> listOrganizationByIds(Set<Long> organizationIds) {
        if (CollectionUtils.isEmpty(organizationIds)) {
            return Collections.emptyList();
        }
        ResponseEntity<List<Tenant>> organizationDTOResponseEntity = baseServiceClient.queryOrgByIds(organizationIds);
        if (organizationDTOResponseEntity.getStatusCode().is2xxSuccessful()) {
            return organizationDTOResponseEntity.getBody();
        } else {
            throw new CommonException("error.organization.get", organizationIds.toString());
        }
    }

    public List<ProjectDTO> listIamProjectByOrgId(Long organizationId) {
        return listIamProjectByOrgId(organizationId, null, null, null);
    }

    public List<ProjectDTO> listOwnedProjects(Long organizationId, Long userId) {
        return baseServiceClient.listOwnedProjects(organizationId, userId).getBody();
    }


    public List<ProjectDTO> listIamProjectByOrgId(Long organizationId, String name, String code, String params) {
        PageRequest customPageRequest = new PageRequest(0, 0);
        ResponseEntity<Page<ProjectDTO>> pageResponseEntity =
                baseServiceClient.pageProjectsByOrgId(organizationId, FeignParamUtils.encodePageRequest(customPageRequest), name, code, true, params);
        return Objects.requireNonNull(pageResponseEntity.getBody()).getContent();
    }

    public Page<ProjectDTO> pageProjectByOrgId(Long organizationId, int page, int size, Sort sort, String name, String code, String params) {
        PageRequest pageable = new PageRequest(page, size, sort);
        try {
            ResponseEntity<Page<ProjectDTO>> pageInfoResponseEntity = baseServiceClient.pageProjectsByOrgId(organizationId,
                    FeignParamUtils.encodePageRequest(pageable), name, code, true, params);
            return pageInfoResponseEntity.getBody();
        } catch (Exception e) {
            throw new CommonException(e);
        }
    }

    public List<IamUserDTO> listUsersByIds(List<Long> ids) {
        List<IamUserDTO> userDTOS = new ArrayList<>();
        if (ids != null && !ids.isEmpty()) {
            Long[] newIds = new Long[ids.size()];
            try {
                userDTOS = ResponseUtils.getResponse(baseServiceClient
                        .listUsersByIds(ids.toArray(newIds), false), new TypeReference<List<IamUserDTO>>() {});
                if (userDTOS == null) {
                    userDTOS = Collections.emptyList();
                }
            } catch (Exception e) {
                throw new CommonException("error.users.get", e);
            }
        }
        return userDTOS;
    }

    public List<IamUserDTO> listUsersByIds(Long[] ids, boolean onlyEnabled) {
        try {
            List<IamUserDTO> userDTOS = ResponseUtils.getResponse(baseServiceClient
                    .listUsersByIds(ids, onlyEnabled), new TypeReference<List<IamUserDTO>>() {});
            if (userDTOS == null) {
                userDTOS = Collections.emptyList();
            }
            return userDTOS;
        } catch (Exception e) {
            throw new CommonException("error.users.get", e);
        }
    }

    public IamUserDTO queryUserByUserId(Long id) {
        List<Long> ids = new ArrayList<>();
        if (id == null) {
            return null;
        }
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
        } catch (Exception e) {
            throw new CommonException("error.user.get.byGitlabLabel");
        }
    }

    public IamUserDTO queryByEmail(Long projectId, String email) {
        try {
            ResponseEntity<Page<IamUserDTO>> userDOResponseEntity = baseServiceClient
                    .listUsersByEmail(projectId, 0, 0, email);
            if (userDOResponseEntity.getBody().getContent().isEmpty()) {
                return null;
            }
            return userDOResponseEntity.getBody().getContent().get(0);
        } catch (Exception e) {
            LOGGER.error("get user by email {} error", email);
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
        List<IamUserDTO> list = this.listUsersWithGitlabLabel(projectId, roleAssignmentSearchVO, LabelType.GITLAB_PROJECT_DEVELOPER.getValue());
        List<Long> memberIds = list.stream().filter(IamUserDTO::getEnabled).map(IamUserDTO::getId).collect(Collectors.toList());
        // 项目下所有项目所有者
        this.listUsersWithGitlabLabel(projectId, roleAssignmentSearchVO, LabelType.GITLAB_PROJECT_OWNER.getValue())
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
        } catch (Exception e) {
            throw new CommonException(e);
        }
        return isGitlabProjectOwner;
    }

    public Boolean isGitLabOrgOwner(Long userId, Long projectId) {
        Boolean isGitLabOrgOwner;
        try {
            isGitLabOrgOwner = baseServiceClient.checkIsGitlabOrgOwner(userId, projectId).getBody();
        } catch (Exception e) {
            throw new CommonException(e);
        }
        return isGitLabOrgOwner;
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

    public ClientVO createClient(Long organizationId, ClientVO clientVO) {
        try {
            ClientVO client = baseServiceClient.createClient(organizationId, clientVO).getBody();
            if (client == null || client.getId() == null) {
                throw new CommonException("error.create.client");
            }
            return client;
        } catch (Exception ex) {
            throw new CommonException("error.create.client");
        }
    }

    public ClientVO queryClientByName(Long organization, String name) {
        try {
            return baseServiceClient.queryClientByName(organization, name).getBody();
        } catch (Exception ex) {
            throw new CommonException("error.get.client");
        }
    }

    public void deleteClient(Long organizationId, Long clientId) {
        try {
            baseServiceClient.deleteClient(organizationId, clientId);
        } catch (Exception ex) {
            throw new CommonException("error.delete.client");
        }
    }

    public ClientVO queryClientBySourceId(Long organizationId, Long clientId) {
        try {
            ResponseEntity<ClientVO> responseEntity = baseServiceClient.queryClientBySourceId(organizationId, clientId);
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

    /**
     * 判断用户是否是root用户
     *
     * @param userId
     * @return
     */
    public Boolean isRoot(Long userId) {
        ResponseEntity<Boolean> responseEntity = baseServiceClient.checkIsRoot(userId);
        return responseEntity == null ? false : responseEntity.getBody();
    }

    /**
     * 判段用户是否是组织root用户
     *
     * @param organizationId
     * @param userId
     * @return
     */
    public Boolean isOrganzationRoot(Long userId, Long organizationId) {
        ResponseEntity<Boolean> responseEntity = baseServiceClient.checkIsOrgRoot(organizationId, userId);
        return responseEntity == null ? false : responseEntity.getBody();
    }

    /**
     * 校验用户是否是项目所有者
     *
     * @param userId    用户id
     * @param projectId 项目id
     * @return true表示是
     */
    public Boolean isProjectOwner(Long userId, Long projectId) {
        ResponseEntity<Boolean> responseEntity = baseServiceClient.checkIsProjectOwner(userId, projectId);
        return responseEntity == null ? false : responseEntity.getBody();
    }

    /**
     * 查询项目下的项目所有者，用于发送通知
     *
     * @param projectId
     * @return
     */
    public List<IamUserDTO> listProjectOwnerByProjectId(Long projectId) {
        ResponseEntity<List<IamUserDTO>> responseEntity = baseServiceClient.listProjectOwnerByProjectId(projectId);
        return responseEntity == null ? Collections.emptyList() : responseEntity.getBody();
    }

    /**
     * 判断组织是否是注册组织
     *
     * @param organizationId
     * @return
     */
    public Boolean checkOrganizationIsRegistered(Long organizationId) {
        ResponseEntity<Boolean> responseEntity = baseServiceClient.checkOrganizationIsRegister(organizationId);
        return responseEntity.getBody();
    }

    public Page<OrgAdministratorVO> listOrgAdministrator(Long organizationId) {
        ResponseEntity<Page<OrgAdministratorVO>> pageInfoResponseEntity = baseServiceClient.listOrgAdministrator(organizationId, 0);
        Page<OrgAdministratorVO> body = pageInfoResponseEntity.getBody();
        return body;
    }

    public ResourceLimitVO queryResourceLimit() {
        ResponseEntity<ResourceLimitVO> resourceLimitVOResponseEntity = baseServiceClient.queryResourceLimit();
        return resourceLimitVOResponseEntity.getBody();
    }

    public List<UserProjectLabelVO> listRoleLabelsForUserInTheProject(Long userId, Set<Long> projectIds) {
        if (CollectionUtils.isEmpty(projectIds)) {
            return Collections.emptyList();
        }
        ResponseEntity<List<UserProjectLabelVO>> labels = baseServiceClient.listRoleLabelsForUserInTheProject(Objects.requireNonNull(userId), projectIds);
        return labels.getBody();
    }

    public List<IamUserDTO> queryUserByProjectId(Long projectId) {
        ResponseEntity<List<IamUserDTO>> labels = baseServiceClient.queryUserByProjectId(Objects.requireNonNull(projectId));
        return labels.getBody();
    }

    public List<IamUserDTO> queryRoot() {
        ResponseEntity<List<IamUserDTO>> labels = baseServiceClient.queryRoot();
        return labels.getBody();
    }

    public long queryAllUserCount() {
        // TODO
        return 0L;
    }

    public Set<Long> queryAllUserIds() {
        // TODO
        return Collections.emptySet();
    }
}
