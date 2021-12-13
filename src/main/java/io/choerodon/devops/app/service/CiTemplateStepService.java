package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Set;

import io.choerodon.devops.api.vo.template.CiTemplateStepVO;

/**
 * 流水线步骤模板(CiTemplateStep)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:21
 */
public interface CiTemplateStepService {

    List<CiTemplateStepVO> listByJobIds(Set<Long> jobIds);
}

