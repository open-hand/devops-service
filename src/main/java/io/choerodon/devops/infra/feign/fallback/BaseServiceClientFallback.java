package io.choerodon.devops.infra.feign.fallback;

import com.github.pagehelper.PageInfo;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.OrganizationSimplifyVO;
import io.choerodon.devops.api.vo.RoleAssignmentSearchVO;
import io.choerodon.devops.api.vo.iam.*;
import io.choerodon.devops.api.vo.kubernetes.MemberRoleVO;
import io.choerodon.devops.api.vo.kubernetes.ProjectCreateDTO;
import io.choerodon.devops.infra.dto.iam.*;
import io.choerodon.devops.infra.feign.BaseServiceClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by younger on 2018/3/29.
 */
@Component
public class BaseServiceClientFallback implements BaseServiceClient {
    private static final String ERROR_PROJECT_GET = "error.project.get";
    private static final String ERROR_PROJECT_QUERY_BY_ID = "error.project.query.by.id";

    @Override
    public ResponseEntity<ProjectDTO> queryIamProject(Long projectId) {
        throw new CommonException(ERROR_PROJECT_QUERY_BY_ID, projectId);
    }

    @Override
    public ResponseEntity<OrganizationDTO> queryOrganization() {
        throw new CommonException("error.organization.get");
    }

    @Override
    public ResponseEntity<OrganizationDTO> queryOrganizationById(Long organizationId) {
        throw new CommonException("error.organization.get");
    }

    @Override
    public ResponseEntity<PageInfo<OrganizationDTO>> listOrganizations(Integer page, Integer size) {
        throw new CommonException("error.organization.get");
    }

    @Override
    public ResponseEntity<MemberRoleVO> addMemberRole(Long projectId, MemberRoleVO memberRoleVo) {
        throw new CommonException("error.memberRole.add");
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
    public ResponseEntity<PageInfo<IamUserDTO>> queryInProjectById(Long projectId, Long id) {
        throw new CommonException("error.userInProject.get");
    }

    @Override
    public ResponseEntity<List<IamUserDTO>> listUsersByIds(Long[] ids, Boolean onlyEnabled) {
        throw new CommonException("error.user.get.byIds");
    }

    @Override
    public ResponseEntity<PageInfo<IamUserDTO>> listUsersByEmail(Long projectId, int page, int size, String email) {
        return new ResponseEntity("error.user.get.byEmail", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<RoleVO>> listRolesWithUserCountOnProjectLevel(
            Long projectId, RoleAssignmentSearchVO roleAssignmentSearchVO) {
        throw new CommonException("error.roles.get.byProjectId");
    }

    @Override
    public ResponseEntity<List<IamUserDTO>> listUsersWithGitlabLabel(Long projectId, RoleAssignmentSearchVO roleAssignmentSearchVO, String labelName) {
        throw new CommonException("error.user.get.byGitlabLabel");
    }

    @Override
    public ResponseEntity<com.github.pagehelper.PageInfo<ProjectWithRoleVO>> listProjectWithRole(Long id, int page, int size) {
        throw new CommonException("error.project.role.get");
    }

    @Override
    public ResponseEntity<PageInfo<RoleVO>> queryRoleIdByCode(String code) {
        throw new CommonException("error.roleId.get");
    }


    @Override
    public ResponseEntity<ProjectDTO> createProject(Long organizationId, @Valid ProjectCreateDTO projectCreateDTO) {
        throw new CommonException("error.iam.project.create");
    }

    @Override
    public ResponseEntity<PageInfo<ProjectDTO>> pageProjectsByOrgId(Long organizationId, Map<String, Object> pageable, String name, String code, Boolean enabled, String params) {
        return null;
    }

    @Override
    public ResponseEntity<ApplicationDTO> queryAppById(Long id) {
        throw new CommonException("error.application.get");
    }

    @Override
    public ResponseEntity<Boolean> publishFail(Long projectId, Long id, String errorCode, Boolean fixFlag) {
        throw new CommonException("error.publishFail.status.get");
    }

    public ResponseEntity<String> completeDownloadApplication(Long publishAppVersionId, Long appVersionId, Long organizationId, List<AppDownloadDevopsReqVO> appDownloadDevopsReqVOS) {
        throw new CommonException("error.application.download.complete");
    }

    @Override
    public ResponseEntity<String> failToDownloadApplication(Long publishAppVersionId, Long appVersionId, Long organizationId) {
        throw new CommonException("error.application.download.failed");
    }

    @Override
    public ResponseEntity<RemoteTokenAuthorizationVO> checkLatestToken() {
        throw new CommonException("error.remote.token.authorization");
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
    public ResponseEntity<Set<Long>> listService(Long organizationId, String appType) {
        throw new CommonException("error.app.service.market.list");
    }

    @Override
    public ResponseEntity<Set<Long>> listSvcVersion(Long organizationId, String appType) {
        throw new CommonException("error.app.service.version.market.list");
    }

    @Override
    public ResponseEntity<ClientDTO> createClient(Long organizationId, @Valid ClientVO clientVO) {
        throw new CommonException("error.create.client");
    }

    @Override
    public ResponseEntity deleteClient(Long organizationId, Long clientId) {
        throw new CommonException("error.delete.client");
    }

    @Override
    public ResponseEntity<ClientDTO> queryClientBySourceId(Long organizationId, Long sourceId) {
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
    public ResponseEntity<List<IamUserDTO>> queryAllAdminUsers() {
        throw new CommonException("error.query.all.admins");
    }

    @Override
    public ResponseEntity<List<IamUserDTO>> queryAllOrgRoot() {
        throw new CommonException("error.query.all.org.admin");
    }

    @Override
    public ResponseEntity<IamUserDTO> query(String loginName) {
        throw new CommonException("error.query.user.by.login.name", loginName);
    }

}