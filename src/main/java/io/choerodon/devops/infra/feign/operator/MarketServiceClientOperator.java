package io.choerodon.devops.infra.feign.operator;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import org.hzero.core.util.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.market.MarketAppUseRecordDTO;
import io.choerodon.devops.api.vo.market.MarketServiceDeployObjectVO;
import io.choerodon.devops.api.vo.market.MarketServiceVO;
import io.choerodon.devops.api.vo.market.RepoConfigVO;
import io.choerodon.devops.infra.dto.market.MarketChartValueDTO;
import io.choerodon.devops.infra.feign.MarketServiceClient;
import io.choerodon.devops.infra.util.CommonExAssertUtil;

/**
 * Created by wangxiang on 2020/12/15
 */
@Component
public class MarketServiceClientOperator {
    @Autowired
    private MarketServiceClient marketServiceClient;

    public void createUseRecord(MarketAppUseRecordDTO marketAppUseRecordDTO) {
        marketServiceClient.createUseRecord(marketAppUseRecordDTO).getBody();
    }

    public MarketServiceDeployObjectVO queryDeployObject(Long projectId, Long deployObjectId) {
        MarketServiceDeployObjectVO marketServiceDeployObjectVO = marketServiceClient.queryDeployObject(Objects.requireNonNull(projectId), Objects.requireNonNull(deployObjectId))
                .getBody();
        if (marketServiceDeployObjectVO == null) {
            throw new CommonException("error.query.deploy.object.config");

        }
        return marketServiceDeployObjectVO;
    }

    public MarketChartValueDTO queryValues(Long projectId, Long deployObjectId) {
        ResponseEntity<MarketChartValueDTO> result = marketServiceClient.queryValuesForDeployObject(Objects.requireNonNull(projectId), Objects.requireNonNull(deployObjectId));
        if (!result.getStatusCode().is2xxSuccessful() || result.getBody() == null || result.getBody().getValue() == null) {
            throw new CommonException("error.query.values.from.market");
        }
        return result.getBody();
    }

    public MarketServiceVO queryMarketService(Long projectId, Long marketServiceId) {
        ResponseEntity<MarketServiceVO> result = marketServiceClient.queryMarketService(Objects.requireNonNull(projectId), Objects.requireNonNull(marketServiceId));
        if (!result.getStatusCode().is2xxSuccessful() || result.getBody() == null || result.getBody().getId() == null) {
            throw new CommonException("error.query.market.service");
        }
        return result.getBody();
    }

    public MarketServiceDeployObjectVO queryDeployObjectByCodeAndVersion(Long projectId, String chartName, String chartVersion) {
        ResponseEntity<MarketServiceDeployObjectVO> deployObject = marketServiceClient.queryDeployObjectByCodeAndService(Objects.requireNonNull(projectId), Objects.requireNonNull(chartName), Objects.requireNonNull(chartVersion), false);
        if (!deployObject.getStatusCode().is2xxSuccessful() || deployObject.getBody() == null || deployObject.getBody().getId() == null) {
            throw new CommonException("error.query.deploy.object.by.code.and.service");
        }
        return deployObject.getBody();
    }

    public List<MarketServiceVO> queryMarketServiceByIds(Long projectId, Set<Long> ids) {
        CommonExAssertUtil.assertTrue(!CollectionUtils.isEmpty(ids), "error.ids.params.empty");
        ResponseEntity<List<MarketServiceVO>> result = marketServiceClient.queryMarketServiceByIds(projectId, ids);
        if (!result.getStatusCode().is2xxSuccessful() || result.getBody() == null) {
            throw new CommonException("error.list.market.service.by.ids");
        }
        return result.getBody();
    }

    public List<MarketServiceDeployObjectVO> listDeployObjectsByIds(Long projectId, Set<Long> deployObjectIds) {
        CommonExAssertUtil.assertTrue(!CollectionUtils.isEmpty(deployObjectIds), "error.ids.params.empty");
        return ResponseUtils.getResponse(marketServiceClient.listDeployObjectsByIds(projectId, deployObjectIds), new TypeReference<List<MarketServiceDeployObjectVO>>() {
        });
    }
}
