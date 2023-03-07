package io.choerodon.devops.app.service.impl.config;

import io.choerodon.devops.api.vo.template.CiTemplateJobVO;

public abstract class TemplateJobConfigService {
    /**
     * 配置 返回配置的主键Id
     * @param ciTemplateJobVO
     * @return
     */
    public abstract Long baseInsert(CiTemplateJobVO ciTemplateJobVO);

    public abstract void fillCdJobConfig(CiTemplateJobVO ciTemplateJobVO);

    public abstract void baseDelete(CiTemplateJobVO ciTemplateJobVO);
}
