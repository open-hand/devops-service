package io.choerodon.devops.app.eventhandler.pipeline.exec;

import org.springframework.stereotype.Component;

import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.enums.CiCommandTypeEnum;

@Component
public class HostDeployCommandHandler extends AbstractCiCommandHandler {
    @Override
    public CiCommandTypeEnum getType() {
        return CiCommandTypeEnum.HOST_DEPLOY;
    }

    @Override
    protected Object execute(AppServiceDTO appServiceDTO, Long gitlabPipelineId, Long gitlabJobId, Long configId, StringBuilder log) {
        // todo lihao 主机部署
        return null;
    }
}
