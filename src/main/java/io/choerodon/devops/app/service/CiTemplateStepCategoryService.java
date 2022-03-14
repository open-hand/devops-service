package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Set;

import io.choerodon.devops.api.vo.template.CiTemplateStepCategoryVO;

/**
 * 流水线步骤模板分类(CiTemplateStepCategory)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:21
 */
public interface CiTemplateStepCategoryService {

    List<CiTemplateStepCategoryVO> listByIds(Set<Long> cids);
}

