package io.choerodon.devops.app.service.impl;

import java.util.Set;

import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.DevopsCiStepVO;
import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.devops.app.service.AbstractDevopsCiStepHandler;
import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/12/16 14:39
 */
@Service
public class DevopsCiGoBuildStepHandler extends AbstractDevopsCiStepHandler {
    @Override
    public void fillTemplateStepConfigInfo(CiTemplateStepVO ciTemplateStepVO) {

    }

    @Override
    public void fillTemplateStepConfigInfo(DevopsCiStepVO devopsCiStepVO) {

    }

    @Override
    public void fillStepConfigInfo(DevopsCiStepVO devopsCiStepVO) {

    }

    @Override
    protected void saveConfig(Long stepId, DevopsCiStepVO devopsCiStepVO) {

    }

    @Override
    protected void batchDeleteConfig(Set<Long> stepIds) {

    }

    @Override
    public DevopsCiStepTypeEnum getType() {
        return DevopsCiStepTypeEnum.GO_BUILD;
    }
}