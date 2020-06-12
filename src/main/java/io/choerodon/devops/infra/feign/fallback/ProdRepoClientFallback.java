package io.choerodon.devops.infra.feign.fallback;

import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.dto.repo.NexusMavenRepoDTO;
import io.choerodon.devops.infra.feign.ProdRepoClient;

/**
 * @author zmf
 * @since 2020/6/12
 */
public class ProdRepoClientFallback implements ProdRepoClient {

    @Override
    public ResponseEntity<List<NexusMavenRepoDTO>> getRepoByProject(Long organizationId, Long projectId, String repoType, String type) {
        throw new CommonException("error.query.nexus.repo.list", projectId, repoType, type);
    }

    @Override
    public ResponseEntity<List<NexusMavenRepoDTO>> getRepoUserByProject(Long organizationId, Long projectId, Set<Long> repositoryIds) {
        throw new CommonException("error.query.nexus.repo.user.list", projectId, repositoryIds);
    }
}
