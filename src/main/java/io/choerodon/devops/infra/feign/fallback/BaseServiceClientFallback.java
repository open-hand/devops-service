package io.choerodon.devops.infra.feign.fallback;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.OrgAdministratorVO;
import io.choerodon.devops.api.vo.ResourceLimitVO;
import io.choerodon.devops.api.vo.RoleAssignmentSearchVO;
import io.choerodon.devops.api.vo.iam.AppDownloadDevopsReqVO;
import io.choerodon.devops.api.vo.iam.UserVO;
import io.choerodon.devops.infra.dto.iam.*;
import io.choerodon.devops.infra.feign.BaseServiceClient;

/**
 * Created by younger on 2018/3/29.
 */
@Component
public class BaseServiceClientFallback implements BaseServiceClient {
    private static final String ERROR_PROJECT_GET = "error.project.get";
    private static final String ERROR_PROJECT_QUERY_BY_ID = "error.project.query.by.id";

    @Override
    public ResponseEntity<ProjectDTO> queryIamProject(Long projectId, Boolean withCategoryInfo, Boolean withUserInfo, Boolean withAgileInfo) {
        throw new CommonException(ERROR_PROJECT_QUERY_BY_ID, projectId);
    }

    @Override
    public ResponseEntity<List<RoleDTO>> getRoleByCode(Long organizationId, String code, String labelName) {
        throw new CommonException("error.organization.role.id.get", code);
    }

    @Override
    public ResponseEntity<ResourceLimitVO> queryResourceLimit() {
        throw new CommonException("error.query.resource.limit");
    }

    @Override
    public ResponseEntity<Tenant> queryOrganizationById(Long organizationId,Boolean withMoreInfo) {
        throw new CommonException("error.organization.get", organizationId);
    }

    @Override
    public ResponseEntity<List<Tenant>> queryOrgByIds(Set<Long> ids) {
        throw new CommonException("error.organization.get", ids == null ? "null" : ids.toString());
    }

    @Override
    public ResponseEntity<IamUserDTO> queryByLoginName(String loginName) {
        throw new CommonException("error.user.get.byLoginName");
    }

    @Override
    public ResponseEntity<IamUserDTO> queryById(Long id) {
        throw new CommonException("error.user.get.byId");
    }

    @Override
    public ResponseEntity<List<IamUserDTO>> listUsersByIds(Long[] ids, Boolean onlyEnabled) {
        throw new CommonException("error.user.get.byIds");
    }

    @Override
    public ResponseEntity<Page<IamUserDTO>> listUsersByEmail(Long projectId, int page, int size, String email) {
        throw new CommonException("error.user.get.byEmail");
    }

    @Override
    public ResponseEntity<List<IamUserDTO>> listUsersWithGitlabLabel(Long projectId, RoleAssignmentSearchVO roleAssignmentSearchVO, String labelName) {
        throw new CommonException("error.user.get.byGitlabLabel");
    }

    @Override
    public ResponseEntity<Page<ProjectDTO>> pageProjectsByOrgId(Long organizationId, Map<String, Object> pageable, String name, String code, Boolean enabled, String params) {
        throw new CommonException("error.page.projects.byOrgId");
    }

    public ResponseEntity<String> completeDownloadApplication(Long publishAppVersionId, Long appVersionId, Long organizationId, List<AppDownloadDevopsReqVO> appDownloadDevopsReqVOS) {
        throw new CommonException("error.application.download.complete");
    }

    @Override
    public ResponseEntity<List<ProjectDTO>> queryByIds(Set<Long> ids) {
        throw new CommonException(ERROR_PROJECT_GET);
    }

    @Override
    public ResponseEntity<ProjectDTO> queryProjectByCodeAndOrgId(Long organizationId, String projectCode) {
        throw new CommonException(ERROR_PROJECT_GET);
    }

    @Override
    public ResponseEntity<ClientVO> createClient(Long organizationId, @Valid ClientVO clientVO) {
        throw new CommonException("error.create.client");
    }

    @Override
    public ResponseEntity deleteClient(Long organizationId, Long clientId) {
        throw new CommonException("error.delete.client");
    }

    @Override
    public ResponseEntity<ClientVO> queryClientBySourceId(Long organizationId, Long sourceId) {
        throw new CommonException("error.query.client");
    }

    @Override
    public ResponseEntity<Boolean> checkIsGitlabProjectOwner(Long id, Long projectId) {
        throw new CommonException("error.check.project.permission");
    }

    @Override
    public ResponseEntity<Boolean> checkIsGitlabOrgOwner(Long userId, Long projectId) {
        throw new CommonException("error.check.org.permission");
    }

    @Override
    public ResponseEntity<Boolean> checkIsRoot(Long id) {
        throw new CommonException("error.check.is.root");
    }

    @Override
    public ResponseEntity<Boolean> checkIsOrgRoot(Long organizationId, Long userId) {
        throw new CommonException("error.check.is.org.root");
    }

    @Override
    public ResponseEntity<Boolean> checkIsProjectOwner(Long id, Long projectId) {
        throw new CommonException("error.check.is.project.owner");
    }

    @Override
    public ResponseEntity<List<IamUserDTO>> listProjectOwnerByProjectId(Long projectId) {
        throw new CommonException("error.check.is.project.owner");
    }

    @Override
    public ResponseEntity<Boolean> checkOrganizationIsRegister(Long organizationId) {
        throw new CommonException("error.check.organization.is.new");
    }

    @Override
    public ResponseEntity<Page<OrgAdministratorVO>> listOrgAdministrator(Long organizationId, Integer size) {
        throw new CommonException("error.query.org.by.id");
    }

    @Override
    public ResponseEntity<ClientVO> queryClientByName(Long organizationId, String clientName) {
        throw new CommonException("error.get.client");
    }

    @Override
    public ResponseEntity<List<UserProjectLabelVO>> listRoleLabelsForUserInTheProject(Long userId, Set<Long> projectIds) {
        throw new CommonException("error.get.user.labels", userId, projectIds == null ? null : projectIds.toString());
    }

    @Override
    public ResponseEntity<List<ProjectDTO>> listOwnedProjects(Long organizationId, Long userId) {
        throw new CommonException("error.query.project");
    }

    @Override
    public ResponseEntity<List<UserVO>> listUserByCreationDate() {
        throw new CommonException("error.list.user");
    }

    @Override
    public ResponseEntity<List<IamUserDTO>> queryUserByProjectId(Long projectId) {
       throw new CommonException("error.query.user.by.project.id");
    }
}