package io.choerodon.devops.infra.feign.fallback;

import java.util.List;
import javax.validation.Valid;

import com.github.pagehelper.PageInfo;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.OrganizationSimplifyVO;
import io.choerodon.devops.api.vo.ProjectCreateVO;
import io.choerodon.devops.api.vo.RoleAssignmentSearchVO;
import io.choerodon.devops.api.vo.iam.ProjectWithRoleVO;
import io.choerodon.devops.api.vo.iam.RoleSearchVO;
import io.choerodon.devops.api.vo.iam.RoleVO;
import io.choerodon.devops.api.vo.iam.UserWithRoleVO;
import io.choerodon.devops.api.vo.kubernetes.MemberRoleVO;
import io.choerodon.devops.api.vo.kubernetes.ProjectCreateDTO;
import io.choerodon.devops.infra.dto.DevopsProjectDTO;
import io.choerodon.devops.infra.dto.iam.IamAppDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
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
    public ResponseEntity<ProjectDTO> queryIamProject(Long projectId) {
        throw new CommonException("error.project.get");
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
    public ResponseEntity<PageInfo<ProjectDTO>> queryProjectByOrgId(Long id, int page, int size, String name, String[] params) {
        throw new CommonException("error.project.get");
    }

    @Override
    public ResponseEntity<List<IamUserDTO>> listUsersByIds(Long[] ids) {
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
    public ResponseEntity<PageInfo<RoleVO>> queryRoleIdByCode(RoleSearchVO roleSearchVO) {
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
    public ResponseEntity<PageInfo<OrganizationSimplifyVO>> getAllOrgs(int page, int size) {
        throw new CommonException("error.get.all.organizations");
    }
}
