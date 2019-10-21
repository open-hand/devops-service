package io.choerodon.devops.infra.feign.fallback;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.Valid;

import com.github.pagehelper.PageInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.OrganizationSimplifyVO;
import io.choerodon.devops.api.vo.RoleAssignmentSearchVO;
import io.choerodon.devops.api.vo.iam.*;
import io.choerodon.devops.api.vo.kubernetes.MemberRoleVO;
import io.choerodon.devops.api.vo.kubernetes.ProjectCreateDTO;
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
    public ResponseEntity<List<ApplicationDTO>> queryAppsByOrgId(Long organizationId, Boolean doPage, int page, int size, String name, String code, String type, String[] params) {
        throw new CommonException("error.app.query.by.org.id.and.app.name");
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
    public ResponseEntity<PageInfo<IamUserDTO>> pagingQueryUsersByRoleIdOnProjectLevel(int page, int size, Long roleId,
                                                                                       Long sourceId,
                                                                                       Boolean doPage,
                                                                                       RoleAssignmentSearchVO roleAssignmentSearchVO) {
        throw new CommonException("error.user.get.byRoleId");
    }

    @Override
    public ResponseEntity<PageInfo<UserWithRoleVO>> queryUserByProjectId(Long projectId, int page, int size,
                                                                         Boolean doPage, RoleAssignmentSearchVO roleAssignmentSearchVO) {
        throw new CommonException("error.user.get.byProjectId");
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
    public ResponseEntity<IamAppDTO> createIamApplication(Long organizationId, @Valid IamAppDTO appDTO) {
        throw new CommonException("error.iam.app.create");
    }

    @Override
    public ResponseEntity<IamAppDTO> updateIamApplication(Long organizationId, Long id, @Valid IamAppDTO appDTO) {
        throw new CommonException("error.iam.app.update");
    }

    @Override
    public ResponseEntity<IamAppDTO> disableIamApplication(Long organizationId, Long id) {
        throw new CommonException("error.iam.app.disable");
    }

    @Override
    public ResponseEntity<IamAppDTO> enableIamApplication(Long organizationId, Long id) {
        throw new CommonException("error.iam.app.enabled");
    }

    @Override
    public ResponseEntity<PageInfo<IamAppDTO>> getIamApplication(Long organizationId, String code) {
        throw new CommonException("error.iam.app.get");
    }

    @Override
    public ResponseEntity<ProjectDTO> createProject(Long organizationId, @Valid ProjectCreateDTO projectCreateDTO) {
        throw new CommonException("error.iam.project.create");
    }

    @Override
    public ResponseEntity<PageInfo<ProjectDTO>> pageProjectsByOrgId(Long organizationId, Map<String, Object> pageRequest, String name, String code, Boolean enabled, String params) {
        return null;
    }

    @Override
    public ResponseEntity<PageInfo<OrganizationSimplifyVO>> getAllOrgs(int page, int size) {
        throw new CommonException("error.get.all.organizations");
    }

    @Override
    public ResponseEntity<ProjectDTO> queryProjectByAppId(Long id) {
        throw new CommonException(ERROR_PROJECT_GET);
    }

    @Override
    public ResponseEntity<ApplicationDTO> queryAppById(Long id) {
        throw new CommonException("error.application.get");
    }

    @Override
    public ResponseEntity<List<ApplicationDTO>> getAppByIds(Set<Long> appIds) {
        throw new CommonException("error.application.get");
    }

    @Override
    public ResponseEntity<Boolean> publishFail(Long projectId, Long id, String errorCode, Boolean fixFlag) {
        throw new CommonException("error.publishFail.status.get");
    }

    @Override
    public ResponseEntity<PageInfo<ProjectDTO>> pagingProjectByOptions(Long organizationId, Boolean doPage, int page, int size, String[] params) {
        throw new CommonException(ERROR_PROJECT_GET);
    }

    @Override
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
    public ResponseEntity<List<ApplicationDTO>> listApplicationInfoByAppIds(Long projectId, Set<Long> serviceIds) {
        throw new CommonException("error.application.query");
    }

    @Override
    public ResponseEntity<Set<Long>> listAppServiceByAppId(Long projectId, Long applicationId) {
        throw new CommonException("error.app.service.id.query");
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
    public ResponseEntity<PageInfo<IamUserDTO>> pagingQueryUsersWithRolesOnOrganizationLevel(Long organizationId, int page, int size, String loginName, String realName, String roleName, Boolean enabled, Boolean locked, String params) {
        throw new CommonException("error.query.organization.owner");
    }

    @Override
    public ResponseEntity<List<MemberRoleDTO>> assignUsersRolesOnProjectLevel(Long projectId, List<MemberRoleDTO> memberRoleDTOS) {
        throw new CommonException("error.assign.user.roles.on.project.level");
    }
}