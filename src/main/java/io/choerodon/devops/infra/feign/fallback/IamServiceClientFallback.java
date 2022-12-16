package io.choerodon.devops.infra.feign.fallback;

import static io.choerodon.devops.infra.constant.ExceptionConstants.PublicCode.DEVOPS_ORGANIZATION_GET;
import static io.choerodon.devops.infra.constant.ExceptionConstants.PublicCode.DEVOPS_ORGANIZATION_ROLE_ID_GET;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.ExternalTenantVO;
import io.choerodon.devops.api.vo.OrgAdministratorVO;
import io.choerodon.devops.api.vo.ResourceLimitVO;
import io.choerodon.devops.api.vo.RoleAssignmentSearchVO;
import io.choerodon.devops.infra.dto.iam.*;
import io.choerodon.devops.infra.feign.BaseServiceClient;
import io.choerodon.devops.infra.feign.IamServiceClient;

/**
 * Created by younger on 2018/3/29.
 */
@Component
public class IamServiceClientFallback implements IamServiceClient {
    private static final String ERROR_PROJECT_GET = "devops.project.get";
    private static final String ERROR_PROJECT_QUERY_BY_ID = "devops.project.query.by.id";

    @Override
    public ResponseEntity<Tenant> queryOrganizationById(Long organizationId, Boolean withMoreInfo) {
        throw new CommonException(DEVOPS_ORGANIZATION_GET, organizationId);
    }

    @Override
    public ResponseEntity<List<Tenant>> queryOrgByIds(Set<Long> ids) {
        throw new CommonException(DEVOPS_ORGANIZATION_GET, ids == null ? "null" : ids.toString());
    }

    @Override
    public ResponseEntity<IamUserDTO> queryByLoginName(String loginName) {
        throw new CommonException("devops.user.get.byLoginName");
    }

    @Override
    public ResponseEntity<IamUserDTO> queryById(Long id) {
        throw new CommonException("devops.user.get.byId");
    }

    @Override
    public ResponseEntity<String> listUsersByIds(Long[] ids, Boolean onlyEnabled) {
        throw new CommonException("devops.user.get.byIds");
    }

    @Override
    public ResponseEntity<ClientVO> createClient(Long organizationId, @Valid ClientVO clientVO) {
        throw new CommonException("devops.create.client");
    }

    @Override
    public ResponseEntity deleteClient(Long organizationId, Long clientId) {
        throw new CommonException("devops.delete.client");
    }

    @Override
    public ResponseEntity<ClientVO> queryClientBySourceId(Long organizationId, Long sourceId) {
        throw new CommonException("devops.query.client");
    }

    @Override
    public ResponseEntity<Boolean> checkIsRoot(Long id) {
        throw new CommonException("devops.check.is.root");
    }

    @Override
    public ResponseEntity<Boolean> checkIsOrgRoot(Long organizationId, Long userId) {
        throw new CommonException("devops.check.is.org.root");
    }

    @Override
    public ResponseEntity<Page<OrgAdministratorVO>> listOrgAdministrator(Long organizationId, Integer size) {
        throw new CommonException("devops.query.org.by.id");
    }

    @Override
    public ResponseEntity<ClientVO> queryClientByName(Long organizationId, String clientName) {
        throw new CommonException("devops.get.client");
    }

    @Override
    public ResponseEntity<List<IamUserDTO>> queryRoot() {
        throw new CommonException("devops.query.root.user");
    }

    @Override
    public ResponseEntity<String> countAllUsers() {
        throw new CommonException("devops.count.all.users");
    }

    @Override
    public ResponseEntity<ExternalTenantVO> queryTenantByIdWithExternalInfo(Long organizationId) {
        throw new CommonException("devops.query.tenant", organizationId);
    }

    @Override
    public ResponseEntity<Boolean> platformAdministratorOrAuditor(Long userId) {
        throw new CommonException("devops.check.user.site.access");
    }

}