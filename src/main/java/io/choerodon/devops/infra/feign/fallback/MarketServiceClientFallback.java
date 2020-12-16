package io.choerodon.devops.infra.feign.fallback;



import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.market.RepoConfigVO;
import io.choerodon.devops.infra.feign.MarketServiceClient;


@Component
public class MarketServiceClientFallback implements MarketServiceClient {

    @Override
    public ResponseEntity<RepoConfigVO> queryRepoConfig(Long projectId, Long appId, Long appServiceVersionId) {
        throw new CommonException("error.query.repo.config");
    }
}
