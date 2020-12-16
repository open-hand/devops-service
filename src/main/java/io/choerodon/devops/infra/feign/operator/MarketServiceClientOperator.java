package io.choerodon.devops.infra.feign.operator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.market.RepoConfigVO;
import io.choerodon.devops.infra.feign.MarketServiceClient;

/**
 * Created by wangxiang on 2020/12/15
 */
@Component
public class MarketServiceClientOperator {
    @Autowired
    private MarketServiceClient marketServiceClient;


    public RepoConfigVO queryRepoConfig(Long projectId, Long appId, Long appServiceVersionId) {
        RepoConfigVO repoConfigVO = marketServiceClient.queryRepoConfig(projectId, appId, appServiceVersionId).getBody();
        if (repoConfigVO == null) {
            throw new CommonException("error.query.repo.config");
        }
        return repoConfigVO;
    }
}
