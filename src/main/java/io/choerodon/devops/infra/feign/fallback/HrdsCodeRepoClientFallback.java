package io.choerodon.devops.infra.feign.fallback;

import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.hrdsCode.MemberPrivilegeViewDTO;
import io.choerodon.devops.api.vo.hrdsCode.RepositoryPrivilegeViewDTO;
import io.choerodon.devops.infra.dto.repo.RdmMemberQueryDTO;
import io.choerodon.devops.infra.dto.repo.RdmMemberViewDTO;
import io.choerodon.devops.infra.feign.HrdsCodeRepoClient;

/**
 * @author scp
 * @date 2020/6/11
 * @description
 */
@Component
public class HrdsCodeRepoClientFallback implements HrdsCodeRepoClient {
    @Override
    public ResponseEntity<List<MemberPrivilegeViewDTO>> selfPrivilege(Long organizationId, Long projectId, Set<Long> repositoryIds) {
        throw new CommonException("error.get.gitlab.accessLevel");
    }

    @Override
    public ResponseEntity<List<RepositoryPrivilegeViewDTO>> listRepositoriesByPrivilege(Long organizationId, Long projectId, Set<Long> userIds) {
        throw new CommonException("error.get.gitlab.project.appService");
    }

    @Override
    public ResponseEntity<List<RdmMemberViewDTO>> listMembers(Long organizationId, Long projectId, RdmMemberQueryDTO query) {
        throw new CommonException("error.list.code.users.appService");
    }
}
