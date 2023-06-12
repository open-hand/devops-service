package io.choerodon.devops.infra.feign.fallback;

import static io.choerodon.devops.infra.constant.ExceptionConstants.PublicCode.DEVOPS_ORGANIZATION_GET;
import static io.choerodon.devops.infra.constant.ExceptionConstants.PublicCode.DEVOPS_ORGANIZATION_ROLE_ID_GET;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.ResourceLimitVO;
import io.choerodon.devops.api.vo.RoleAssignmentSearchVO;
import io.choerodon.devops.infra.dto.iam.*;
import io.choerodon.devops.infra.feign.BaseServiceClient;

/**
 * Created by younger on 2018/3/29.
 */
@Component
public class BaseServiceClientFallback implements BaseServiceClient {
    private static final String ERROR_PROJECT_GET = "devops.project.get";
    private static final String ERROR_PROJECT_QUERY_BY_ID = "devops.project.query.by.id";

    @Override
    public ResponseEntity<String> immutableProjectInfoById(Long id) {
        throw new CommonException(ERROR_PROJECT_QUERY_BY_ID, id);
    }

    @Override
    public ResponseEntity<String> queryIamProject(Long projectId, Boolean withCategoryInfo, Boolean withUserInfo, Boolean withAgileInfo, Boolean withWorkGroup, Boolean withProjectClassfication) {
        throw new CommonException(ERROR_PROJECT_QUERY_BY_ID, projectId);
    }

    @Override
    public ResponseEntity<String> queryIamProjectBasicInfo(Long projectId) {
        throw new CommonException("devops.query.project");
    }

    @Override
    public ResponseEntity<List<RoleDTO>> getRoleByCode(Long organizationId, String code, String labelName) {
        throw new CommonException(DEVOPS_ORGANIZATION_ROLE_ID_GET, code);
    }

    @Override
    public ResponseEntity<ResourceLimitVO> queryResourceLimit(Long organizationId) {
        throw new CommonException("devops.query.resource.limit");
    }

    @Override
    public ResponseEntity<Tenant> queryOrganizationBasicInfoWithCache(Long organizationId) {
        throw new CommonException(DEVOPS_ORGANIZATION_GET);
    }

    @Override
    public ResponseEntity<String> listUsersByEmail(Long projectId, int page, int size, String email) {
        throw new CommonException("devops.user.get.byEmail");
    }

    @Override
    public ResponseEntity<List<IamUserDTO>> listUsersWithGitlabLabel(Long projectId, RoleAssignmentSearchVO roleAssignmentSearchVO, String labelName) {
        throw new CommonException("devops.user.get.byGitlabLabel");
    }

    @Override
    public ResponseEntity<Page<ProjectDTO>> pageProjectsByOrgId(Long organizationId, Map<String, Object> pageable, String name, String code, Boolean enabled, String params) {
        throw new CommonException("devops.page.projects.byOrgId");
    }

    @Override
    public ResponseEntity<Page<IamUserDTO>> pagingQueryUsersWithRolesOnProjectLevel(Long projectId, int page, int size, String params) {
        throw new CommonException("devops.page.projects.user");
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
    public ResponseEntity<Boolean> checkIsGitlabProjectOwner(Long id, Long projectId) {
        throw new CommonException("devops.check.project.permission");
    }

    @Override
    public ResponseEntity<Map<Long, Boolean>> checkUsersAreGitlabProjectOwner(Set<Long> id, Long projectId) {
        throw new CommonException("devops.check.project.permission");
    }

    @Override
    public ResponseEntity<Boolean> checkIsGitlabOrgOwner(Long userId, Long projectId) {
        throw new CommonException("devops.check.org.permission");
    }

    @Override
    public ResponseEntity<Boolean> checkIsOrgOrProjectGitlabOwner(Long id, Long projectId) {
        throw new CommonException("devops.check.org.project.owner");
    }

    @Override
    public ResponseEntity<Boolean> checkIsProjectOwner(Long id, Long projectId) {
        throw new CommonException("devops.check.is.project.owner");
    }

    @Override
    public ResponseEntity<List<IamUserDTO>> listProjectOwnerByProjectId(Long projectId) {
        throw new CommonException("devops.check.is.project.owner");
    }

    @Override
    public ResponseEntity<Boolean> checkOrganizationIsRegister(Long organizationId) {
        throw new CommonException("devops.check.organization.is.new");
    }

    @Override
    public ResponseEntity<List<UserProjectLabelVO>> listRoleLabelsForUserInTheProject(Long userId, Set<Long> projectIds) {
        throw new CommonException("devops.get.user.labels", userId, projectIds == null ? null : projectIds.toString());
    }

    @Override
    public ResponseEntity<List<ProjectDTO>> listOwnedProjects(Long organizationId, Long userId) {
        throw new CommonException("devops.query.project");
    }

    @Override
    public ResponseEntity<String> listManagedProjects(Long organizationId) {
        throw new CommonException("devops.query.project");
    }

    @Override
    public ResponseEntity<List<IamUserDTO>> queryUserByProjectId(Long projectId) {
        throw new CommonException("devops.query.user.by.project.id");
    }

    @Override
    public ResponseEntity<String> listAllUserIds() {
        throw new CommonException("devops.list.all.user.ids");
    }

    @Override
    public ResponseEntity<List<String>> listProjectCategoryById(Long projectId) {
        throw new CommonException("devops.list.project.category");
    }

    @Override
    public ResponseEntity<String> listProjectIdsInOrg(Long tenantId) {
        throw new CommonException("devops.list.projectIds.in.org", tenantId);
    }

    @Override
    public ResponseEntity<List<ProjectDTO>> listProjectsByUserId(Long organizationId, Long userId) {
        throw new CommonException("devops.list.projectIds.in.org", organizationId);
    }

    @Override
    public ResponseEntity<List<IamUserDTO>> listProjectUsersByProjectIdAndRoleLabel(Long projectId, String roleLable) {
        throw new CommonException("devops.query.gitlab.owner.user");
    }

    @Override
    public ResponseEntity<String> listUsersUnderRoleByIds(Long projectId, String roleIds) {
        throw new CommonException("devops.list.users.by.role.ids");
    }
}