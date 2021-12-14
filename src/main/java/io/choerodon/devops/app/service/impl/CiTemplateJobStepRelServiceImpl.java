package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.devops.app.service.CiTemplateJobStepRelService;
import io.choerodon.devops.infra.mapper.CiTemplateJobStepRelMapper;

/**
 * 流水线任务模板与步骤模板关系表(CiTemplateJobStepRel)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:17
 */
@Service
public class CiTemplateJobStepRelServiceImpl implements CiTemplateJobStepRelService {
    @Autowired
    private CiTemplateJobStepRelMapper ciTemplateJobStepRelmapper;


    @Override
    public List<CiTemplateStepVO> listByJobIds(Set<Long> jobIds) {
        return ciTemplateJobStepRelmapper.listByJobIds(jobIds);
    }
}

