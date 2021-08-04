package io.choerodon.devops.infra.feign.operator;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.utils.FeignClientUtils;
import io.choerodon.devops.api.vo.market.MarketAppSubscribeRelVO;
import io.choerodon.devops.api.vo.market.MarketAppUseRecordDTO;
import io.choerodon.devops.api.vo.market.MarketServiceDeployObjectVO;
import io.choerodon.devops.api.vo.market.MarketServiceVO;
import io.choerodon.devops.infra.dto.market.MarketAppVersionDTO;
import io.choerodon.devops.infra.dto.market.MarketApplicationDTO;
import io.choerodon.devops.infra.dto.market.MarketChartValueDTO;
import io.choerodon.devops.infra.feign.MarketServiceClient;
import io.choerodon.devops.infra.util.CommonExAssertUtil;

/**
 * Created by wangxiang on 2020/12/15
 */
@Component
public class MarketServiceClientOperator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarketServiceClientOperator.class);
    @Autowired
    private MarketServiceClient marketServiceClient;

    public void createUseRecord(MarketAppUseRecordDTO marketAppUseRecordDTO) {
        marketServiceClient.createUseRecord(marketAppUseRecordDTO).getBody();
    }

    public MarketServiceDeployObjectVO queryDeployObject(Long projectId, Long deployObjectId) {
        return FeignClientUtils.doRequest(() -> marketServiceClient.queryDeployObject(Objects.requireNonNull(projectId), false, Objects.requireNonNull(deployObjectId)), MarketServiceDeployObjectVO.class);
    }

    public MarketServiceDeployObjectVO queryDeployObjectWithValues(Long projectId, Long deployObjectId) {
        return FeignClientUtils.doRequest(() -> marketServiceClient.queryDeployObject(Objects.requireNonNull(projectId), true, Objects.requireNonNull(deployObjectId)), MarketServiceDeployObjectVO.class);
    }

    public MarketChartValueDTO queryValues(Long projectId, Long deployObjectId) {
        CommonExAssertUtil.assertNotNull(projectId, "error.project.id.null");
        CommonExAssertUtil.assertNotNull(deployObjectId, "error.deploy.object.id.null");
        return FeignClientUtils.doRequest(() -> marketServiceClient.queryValuesForDeployObject(projectId, deployObjectId), MarketChartValueDTO.class);
    }

    public MarketServiceVO queryMarketService(Long projectId, Long marketServiceId) {
        return FeignClientUtils.doRequest(() -> marketServiceClient.queryMarketService(Objects.requireNonNull(projectId), Objects.requireNonNull(marketServiceId)), MarketServiceVO.class);
    }

    public MarketServiceDeployObjectVO queryDeployObjectByCodeAndVersion(Long projectId, String chartName, String chartVersion) {
        return FeignClientUtils.doRequest(() -> marketServiceClient.queryDeployObjectByCodeAndService(Objects.requireNonNull(projectId), Objects.requireNonNull(chartName), Objects.requireNonNull(chartVersion), false), MarketServiceDeployObjectVO.class);
    }

    public List<MarketServiceVO> queryMarketServiceByIds(Long projectId, Set<Long> ids) {
        CommonExAssertUtil.assertTrue(!CollectionUtils.isEmpty(ids), "error.ids.params.empty");
        try {
            return FeignClientUtils.doRequest(() -> marketServiceClient.queryMarketServiceByIds(projectId, ids), new TypeReference<List<MarketServiceVO>>() {
            });
        } catch (Exception ex) {
            LOGGER.warn("Failed to queryMarketServiceByIds due to ex with message {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    public List<MarketServiceDeployObjectVO> listDeployObjectsByIds(Long projectId, Set<Long> deployObjectIds) {
        CommonExAssertUtil.assertTrue(!CollectionUtils.isEmpty(deployObjectIds), "error.ids.params.empty");
        try {
            return FeignClientUtils.doRequest(() -> marketServiceClient.listDeployObjectsByIds(projectId, deployObjectIds), new TypeReference<List<MarketServiceDeployObjectVO>>() {
            });
        } catch (Exception ex) {
            LOGGER.warn("Failed to listDeployObjectsByIds due to ex with message {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    public MarketAppSubscribeRelVO subscribeApplication(Long marketAppId, Long userId) {
        MarketAppSubscribeRelVO marketAppSubscribeRelVO = marketServiceClient.subscribeApplication(Objects.requireNonNull(marketAppId), Objects.requireNonNull(userId)).getBody();
        return marketAppSubscribeRelVO;
    }

    public List<MarketServiceDeployObjectVO> queryUpgradeDeployObjects(Long projectId, Long marketServiceId, Long currentDeployObjectId) {
        CommonExAssertUtil.assertNotNull(projectId, "error.project.id.null");
        CommonExAssertUtil.assertNotNull(marketServiceId, "error.market.service.id.null");
        CommonExAssertUtil.assertNotNull(currentDeployObjectId, "error.deploy.object.id.null");
        return FeignClientUtils.doRequest(() -> marketServiceClient.queryUpgradeMarketService(projectId, marketServiceId, currentDeployObjectId), new TypeReference<List<MarketServiceDeployObjectVO>>() {
        });
    }

    public MarketServiceDeployObjectVO getMiddlewareServiceReleaseInfo(String appName, String mode, String version) {
        return FeignClientUtils.doRequest(() -> marketServiceClient.getMiddlewareServiceReleaseInfo(appName, mode, version), MarketServiceDeployObjectVO.class);
    }

    public List<MarketServiceDeployObjectVO> queryDeployObjectByMarketServiceId(Long projectId, Long marketServiceId) {
        return FeignClientUtils.doRequest(() -> marketServiceClient.queryDeployObjectByMarketServiceId(projectId, marketServiceId), new TypeReference<List<MarketServiceDeployObjectVO>>() {
        });
    }

    public List<MarketServiceVO> queryMarketServiceAndDeployObjAndCategoryByMarketServiceId(Long projectId, Set<Long> marketServiceIds) {
        return FeignClientUtils.doRequest(() -> marketServiceClient.queryMarketServiceAndDeployObjAndCategoryByMarketServiceId(projectId, marketServiceIds), new TypeReference<List<MarketServiceVO>>() {
        });
    }

    public MarketApplicationDTO queryApplication(Long applicationId) {
        return FeignClientUtils.doRequest(() -> marketServiceClient.queryApplication(applicationId), MarketApplicationDTO.class);
    }

    public MarketAppVersionDTO queryAppVersionById(Long applicationId, Long appVersionId) {
        return FeignClientUtils.doRequest(() -> marketServiceClient.queryAppVersionById(applicationId, appVersionId), MarketAppVersionDTO.class);
    }
}
