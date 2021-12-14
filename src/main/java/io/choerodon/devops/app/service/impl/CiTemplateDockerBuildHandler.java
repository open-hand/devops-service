package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.DevopsCiStepVO;
import io.choerodon.devops.app.service.AbstractCiTemplateStepHandler;
import io.choerodon.devops.app.service.CiTemplateDockerService;
import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/12/14 16:16
 */
@Service
public class CiTemplateDockerBuildHandler extends AbstractCiTemplateStepHandler {

    @Autowired
    private CiTemplateDockerService ciTemplateDockerService;

    @Override
    protected void fillConfigInfo(DevopsCiStepVO devopsCiStepVO) {
        ciTemplateDockerService.queryByStepId(devopsCiStepVO.getId());
    }

    @Override
    public String getType() {
        return DevopsCiStepTypeEnum.DOCKER_BUILD.value();
    }
}
