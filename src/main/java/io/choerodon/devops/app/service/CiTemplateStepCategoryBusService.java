package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.template.CiTemplateStepCategoryVO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by wangxiang on 2021/12/2
 */
public interface CiTemplateStepCategoryBusService {
    Page<CiTemplateStepCategoryVO> pageTemplateStepCategory(Long sourceId, PageRequest pageRequest, String searchParam);

    CiTemplateStepCategoryVO updateTemplateStepCategory(Long sourceId, CiTemplateStepCategoryVO ciTemplateStepCategoryVO);

    void deleteTemplateStepCategory(Long sourceId, Long ciTemplateCategoryId);

    CiTemplateStepCategoryVO createTemplateStepCategory(Long sourceId, CiTemplateStepCategoryVO ciTemplateStepCategoryVO);

    Boolean checkTemplateStepCategory(Long sourceId, String name, Long ciTemplateCategoryId);
}

