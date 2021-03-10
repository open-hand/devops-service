package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import io.choerodon.devops.api.vo.AppServiceInstanceVO;
import io.choerodon.devops.api.vo.MarketInstanceCreationRequestVO;
import io.choerodon.devops.api.vo.MiddlewareRedisDeployVO;
import io.choerodon.devops.api.vo.market.MarketServiceDeployObjectVO;
import io.choerodon.devops.app.service.AppServiceInstanceService;
import io.choerodon.devops.app.service.DevopsMiddlewareService;
import io.choerodon.devops.infra.feign.operator.MarketServiceClientOperator;
import io.choerodon.devops.infra.util.ConvertUtils;

@Service
public class DevopsMiddlewareServiceImpl implements DevopsMiddlewareService {

    private static final String PVC_TEMPLATE = "# 如果是 pvc,优先级最高\n" +
            "persistence:\n" +
            "  existingClaim: %s";
    private static final String MIDDLEWARE_REDIS_NAME = "Redis";

    @Autowired
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private MarketServiceClientOperator marketServiceClientOperator;

    /**
     * 中间件的环境部署逻辑和市场应用的部署逻辑完全一样，只是需要提前构造values
     *
     * @param projectId
     * @param middlewareRedisDeployVO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppServiceInstanceVO deployForRedis(Long projectId, MiddlewareRedisDeployVO middlewareRedisDeployVO) {

        // 根据部署模式
        MarketServiceDeployObjectVO middlewareServiceReleaseInfo = marketServiceClientOperator.getMiddlewareServiceReleaseInfo(MIDDLEWARE_REDIS_NAME, middlewareRedisDeployVO.getMode(), middlewareRedisDeployVO.getVersion());

        middlewareRedisDeployVO.setMarketDeployObjectId(middlewareServiceReleaseInfo.getId());
        middlewareRedisDeployVO.setMarketAppServiceId(middlewareServiceReleaseInfo.getMarketServiceId());


        // 构造value
        // TODO 优化values构造逻辑
        String values = middlewareRedisDeployVO.getValues();
        if (!StringUtils.isEmpty(middlewareRedisDeployVO.getPvcName())) {
            values = values + String.format(PVC_TEMPLATE, middlewareRedisDeployVO.getPvcName());
        }

        middlewareRedisDeployVO.setValues(values);

        MarketInstanceCreationRequestVO marketInstanceCreationRequestVO = ConvertUtils.convertObject(middlewareRedisDeployVO, MarketInstanceCreationRequestVO.class);

        return appServiceInstanceService.createOrUpdateMarketInstance(projectId, marketInstanceCreationRequestVO);
    }
}
