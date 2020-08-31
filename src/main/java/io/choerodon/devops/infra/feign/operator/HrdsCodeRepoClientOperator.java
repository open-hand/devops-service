package io.choerodon.devops.infra.feign.operator;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.dto.repo.RdmMemberQueryDTO;
import io.choerodon.devops.infra.dto.repo.RdmMemberViewDTO;
import io.choerodon.devops.infra.feign.HrdsCodeRepoClient;

/**
 * @author scp
 * @date 2020/8/28
 * @description
 */
@Component
public class HrdsCodeRepoClientOperator {
    @Autowired
    private HrdsCodeRepoClient rdupmClient;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;

    /**
     * 根据应用服务id 获取团队成员
     *
     * @param organizationId 组织id
     * @param projectId      项目id
     * @param queryDTO       仓库id集合
     * @return 仓库信息
     */
    public List<RdmMemberViewDTO> listMembers(@Nullable Long organizationId, Long projectId, RdmMemberQueryDTO queryDTO) {
        if (organizationId == null) {
            organizationId = baseServiceClientOperator.queryIamProjectById(Objects.requireNonNull(projectId))
                    .getOrganizationId();
        }
        ResponseEntity<List<RdmMemberViewDTO>> response = rdupmClient.listMembers(
                Objects.requireNonNull(organizationId), projectId, queryDTO.getRepositoryIds(), queryDTO.getRepositoryName(),
                queryDTO.getRealName(), queryDTO.getLoginName(), queryDTO.getParams(), true, true, true);
        if (response == null) {
            throw new CommonException("error.list.code.app.user.list", projectId, queryDTO);
        }
        return response.getBody();
    }

}
