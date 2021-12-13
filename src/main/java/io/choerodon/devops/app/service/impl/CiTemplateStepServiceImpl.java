package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.devops.app.service.CiTemplateStepService;
import io.choerodon.devops.infra.mapper.CiTemplateStepMapper;

/**
 * 流水线步骤模板(CiTemplateStep)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:21
 */
@Service
public class CiTemplateStepServiceImpl implements CiTemplateStepService {
    @Autowired
    private CiTemplateStepMapper ciTemplateStepmapper;


    @Override
    public List<CiTemplateStepVO> listByJobIds(Set<Long> jobIds) {
        return ciTemplateStepmapper.listByJobIds(jobIds);
    }
}

