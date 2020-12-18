package io.choerodon.devops.infra.feign.operator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.market.MarketAppUseRecordDTO;
import io.choerodon.devops.api.vo.market.MarketServiceDeployObjectVO;
import io.choerodon.devops.api.vo.market.RepoConfigVO;
import io.choerodon.devops.infra.dto.market.MarketChartValueDTO;
import io.choerodon.devops.infra.dto.market.MarketServiceDTO;
import io.choerodon.devops.infra.dto.market.MarketServiceVersionDTO;
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

    public void createUseRecord(MarketAppUseRecordDTO marketAppUseRecordDTO) {
        marketServiceClient.createUseRecord(marketAppUseRecordDTO).getBody();
    }

    public MarketServiceDeployObjectVO queryDeployObject(Long projectId, Long deployObjectId) {
        MarketServiceDeployObjectVO marketServiceDeployObjectVO = marketServiceClient.queryDeployObject(projectId, deployObjectId).getBody();
        if (marketServiceDeployObjectVO == null) {
            throw new CommonException("error.query.deploy.object.config");

        }
        return marketServiceDeployObjectVO;
    }


    public MarketServiceDTO queryMarketService(Long marketServiceId) {
        // TODO
        return new MarketServiceDTO();
    }

    public MarketServiceVersionDTO queryVersion(Long marketServiceVersionId) {
        // TODO
        return new MarketServiceVersionDTO();
    }

    public MarketChartValueDTO queryValues(Long projectId, Long deployObjectId) {
        ResponseEntity<MarketChartValueDTO> result = marketServiceClient.queryValuesForDeployObject(projectId, deployObjectId);
        if (!result.getStatusCode().is2xxSuccessful() || result.getBody() == null || result.getBody().getValues() == null) {
            throw new CommonException("error.query.values.from.market");
        }
        return result.getBody();
    }
}
