package io.choerodon.devops.app.service.impl;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.DevopsCiStepVO;
import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.devops.app.service.AbstractDevopsCiStepHandler;
import io.choerodon.devops.app.service.CiTemplateNodeJsBuildService;
import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;

/**
 * Created by wangxiang on 2021/12/20
 */
@Service
public class DevopsNodeJsBuildStepHandler  extends AbstractDevopsCiStepHandler {

    @Autowired
    private CiTemplateNodeJsBuildService ciTemplateNodeJsBuildService;


    @Override
    public void fillTemplateStepConfigInfo(CiTemplateStepVO ciTemplateStepVO) {

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
        return DevopsCiStepTypeEnum.NODE_JS_BUILD;
    }
}
