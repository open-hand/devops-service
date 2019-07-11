package io.choerodon.devops.infra.feign.fallback;

import java.util.List;
import javax.validation.Valid;

import com.github.pagehelper.PageInfo;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.ProjectReqVO;
import io.choerodon.devops.api.vo.RoleAssignmentSearchDTO;
import io.choerodon.devops.api.vo.iam.*;
import io.choerodon.devops.app.eventhandler.payload.IamAppPayLoad;
import io.choerodon.devops.domain.application.valueobject.MemberRoleV;
import io.choerodon.devops.domain.application.valueobject.OrganizationSimplifyDTO;
import io.choerodon.devops.domain.application.valueobject.ProjectCreateDTO;
import io.choerodon.devops.infra.dto.iam.OrganizationDO;
import io.choerodon.devops.infra.dto.iam.ProjectDO;
import io.choerodon.devops.infra.dto.iam.UserDO;
import io.choerodon.devops.infra.feign.IamServiceClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Created by younger on 2018/3/29.
 */
@Component
public class IamServiceClientFallback implements IamServiceClient {

    @Override
    public ResponseEntity<ProjectDO> queryIamProject(Long projectId) {
        throw new CommonException("error.project.get");
    }

    @Override
    public ResponseEntity<OrganizationDO> queryOrganization() {
        throw new CommonException("error.organization.get");
    }

    @Override
    public ResponseEntity<OrganizationDO> queryOrganizationById(Long organizationId) {
        throw new CommonException("error.organization.get");
    }

    @Override
    public ResponseEntity<MemberRoleV> addMemberRole(Long projectId, MemberRoleV memberRoleVo) {
        throw new CommonException("error.memberRole.add");
    }

    @Override
    public ResponseEntity<UserDO> queryByLoginName(String loginName) {
        throw new CommonException("error.user.get.byLoginName");
    }

    @Override
    public ResponseEntity<UserDO> queryById(Long id) {
        throw new CommonException("error.user.get.byId");
    }

    @Override
    public ResponseEntity<PageInfo<UserDO>> queryInProjectById(Long projectId, Long id) {
        throw new CommonException("error.userInProject.get");
    }

    @Override
    public ResponseEntity<PageInfo<ProjectDO>> queryProjectByOrgId(Long id, int page, int size, String name, String[] params) {
        throw new CommonException("error.project.get");
    }

    @Override
    public ResponseEntity<List<UserDO>> listUsersByIds(Long[] ids) {
        throw new CommonException("error.user.get.byIds");
    }

    @Override
    public ResponseEntity<PageInfo<UserDO>> listUsersByEmail(Long projectId, int page, int size, String email) {
        return new ResponseEntity("error.user.get.byEmail", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<RoleDTO>> listRolesWithUserCountOnProjectLevel(
            Long projectId, RoleAssignmentSearchDTO roleAssignmentSearchDTO) {
        throw new CommonException("error.roles.get.byProjectId");
    }

    @Override
    public ResponseEntity<PageInfo<UserDTO>> pagingQueryUsersByRoleIdOnProjectLevel(int page, int size, Long roleId,
                                                                                Long sourceId,
                                                                                Boolean doPage,
                                                                                RoleAssignmentSearchDTO roleAssignmentSearchDTO) {
        throw new CommonException("error.user.get.byRoleId");
    }

    @Override
    public ResponseEntity<PageInfo<UserWithRoleDTO>> queryUserByProjectId(Long projectId, int page, int size,
                                                                      Boolean doPage, RoleAssignmentSearchDTO roleAssignmentSearchDTO) {
        throw new CommonException("error.user.get.byProjectId");
    }

    @Override
    public ResponseEntity<com.github.pagehelper.PageInfo<ProjectWithRoleDTO>> listProjectWithRole(Long id, int page, int size) {
        throw new CommonException("error.project.role.get");
    }

    @Override
    public ResponseEntity<PageInfo<RoleDTO>> queryRoleIdByCode(RoleSearchDTO roleSearchDTO) {
        throw new CommonException("error.roleId.get");
    }

    @Override
    public ResponseEntity<IamAppPayLoad> createIamApplication(Long organizationId, IamAppPayLoad iamAppPayLoad) {
        throw new CommonException("error.iam.app.create");
    }

    @Override
    public ResponseEntity<IamAppPayLoad> updateIamApplication(Long organizationId, Long id, IamAppPayLoad iamAppPayLoad) {
        throw new CommonException("error.iam.app.update");
    }

    @Override
    public ResponseEntity<IamAppPayLoad> disableIamApplication(Long organizationId, Long id) {
        throw new CommonException("error.iam.app.disable");
    }

    @Override
    public ResponseEntity<IamAppPayLoad> enableIamApplication(Long organizationId, Long id) {
        throw new CommonException("error.iam.app.enabled");
    }

    @Override
    public ResponseEntity<PageInfo<IamAppPayLoad>> getIamApplication(Long organizationId, String code) {
        throw new CommonException("error.iam.app.get");
    }

    @Override
    public ResponseEntity<ProjectReqVO> createProject(Long organizationId, @Valid ProjectCreateDTO projectCreateDTO) {
        throw new CommonException("error.iam.project.create");
    }

    @Override
    public ResponseEntity<PageInfo<OrganizationSimplifyDTO>> getAllOrgs(int page, int size) {
        throw new CommonException("error.get.all.organizations");
    }
}
