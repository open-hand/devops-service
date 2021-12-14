package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.DevopsCiStepVO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/12/14 16:10
 */
public abstract class AbstractCiTemplateStepHandler {

    protected abstract void fillConfigInfo(DevopsCiStepVO devopsCiStepVO);

    public abstract String getType();
}
