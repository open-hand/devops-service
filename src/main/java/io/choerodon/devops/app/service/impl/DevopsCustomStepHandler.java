package io.choerodon.devops.app.service.impl;

import java.util.Set;

import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.DevopsCiStepVO;
import io.choerodon.devops.app.service.AbstractDevopsCiStepHandler;
import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/29 17:46
 */
@Service
public class DevopsCustomStepHandler extends AbstractDevopsCiStepHandler {
    protected DevopsCiStepTypeEnum type = DevopsCiStepTypeEnum.CUSTOM;


    @Override
    public void fillConfigInfo(DevopsCiStepVO devopsCiStepVO) {

    }

    @Override
    protected void saveConfig(Long stepId, DevopsCiStepVO devopsCiStepVO) {

    }

    @Override
    protected void batchDeleteConfig(Set<Long> stepIds) {

    }

    @Override
    public String getType() {
        return type.value();
    }
}
