package io.choerodon.devops.infra.feign.operator;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.dto.repo.NexusMavenRepoDTO;
import io.choerodon.devops.infra.feign.ProdRepoClient;

/**
 * @author zmf
 * @since 2020/6/12
 */
@Component
public class ProdRepoClientOperator {
    @Autowired
    private ProdRepoClient prodRepoClient;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;

    /**
     * CI-流水线-获取项目下仓库列表-包含用户信息
     *
     * @param organizationId 组织id
     * @param projectId      项目id
     * @param repositoryIds  仓库id集合
     * @return 仓库信息
     */
    public List<NexusMavenRepoDTO> getRepoUserByProject(@Nullable Long organizationId, Long projectId, Set<Long> repositoryIds) {
        if (CollectionUtils.isEmpty(repositoryIds)) {
            return Collections.emptyList();
        }
        if (organizationId == null) {
            organizationId = baseServiceClientOperator.queryIamProjectById(Objects.requireNonNull(projectId))
                    .getOrganizationId();
        }
        ResponseEntity<List<NexusMavenRepoDTO>> response = prodRepoClient.getRepoUserByProject(
                Objects.requireNonNull(organizationId), projectId, repositoryIds);
        if (response == null || response.getBody() == null) {
            throw new CommonException("error.query.nexus.repo.user.list", projectId, repositoryIds);
        }
        return response.getBody();
    }
}
