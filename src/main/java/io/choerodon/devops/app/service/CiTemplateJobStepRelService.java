package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Set;

import io.choerodon.devops.api.vo.template.CiTemplateJobStepRelVO;

/**
 * 流水线任务模板与步骤模板关系表(CiTemplateJobStepRel)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:17
 */
public interface CiTemplateJobStepRelService {

    List<CiTemplateJobStepRelVO> listByJobIds(Set<Long> jobIds);
}

