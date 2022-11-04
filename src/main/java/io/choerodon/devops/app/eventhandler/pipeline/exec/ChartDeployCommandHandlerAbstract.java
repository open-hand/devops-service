package io.choerodon.devops.app.eventhandler.pipeline.exec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.CiChartDeployConfigService;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.CiChartDeployConfigDTO;
import io.choerodon.devops.infra.enums.CiCommandTypeEnum;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/4 16:03
 */
@Service
public class ChartDeployCommandHandlerAbstract extends AbstractCiCommandHandler {

    @Autowired
    private CiChartDeployConfigService ciChartDeployConfigService;

    @Override
    public CiCommandTypeEnum getType() {
        return CiCommandTypeEnum.CHART_DEPLOY;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected Object execute(AppServiceDTO appServiceDTO, Long gitlabPipelineId, Long gitlabJobId, Long configId, StringBuilder log) {
        CiChartDeployConfigDTO ciChartDeployConfigDTO = ciChartDeployConfigService.queryConfigById(configId);
        if (ciChartDeployConfigDTO == null) {
            throw new CommonException("devops.chart.deploy.config.not.found");
        }
        return null;
    }
}
