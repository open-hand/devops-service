package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Set;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.template.CiTemplateJobVO;
import io.choerodon.devops.api.vo.template.CiTemplateStepCategoryVO;
import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by wangxiang on 2021/12/14
 */
public interface CiTemplateStepBusService {
    Page<CiTemplateStepVO> pageTemplateStep(Long sourceId, String sourceType, PageRequest pageRequest, String name, Long categoryId, Boolean builtIn, String params);

    CiTemplateStepVO updateTemplateStep(Long sourceId, CiTemplateStepVO ciTemplateStepVO);

    void deleteTemplateStep(Long sourceId, String sourceType, Long ciStepTemplateId);

    CiTemplateStepVO createTemplateStep(Long sourceId, CiTemplateStepVO ciTemplateStepVO);

    List<CiTemplateStepVO> queryStepTemplateByJobId(Long sourceId, Long templateJobId);

    CiTemplateStepVO queryStepTemplateByStepId(Long sourceId, Long templateStepId);

    Boolean checkStepTemplateByStepId(Long sourceId, Long templateStepId);

    Boolean checkTemplateStepName(Long sourceId, String name, Long templateStepId);

    List<CiTemplateStepVO> templateStepList(Long sourceId, String sourceType, String name);

    List<CiTemplateStepCategoryVO> listStepWithCategory(Long sourceId, String sourceType);

    void deleteTemplateStepByIds(Long projectId, Set<Long> ciStepTemplateId);

    void createNonVisibilityStep(Long sourceId, CiTemplateJobVO ciTemplateJobVO);

    void initNonVisibilityStep(Long sourceId, CiTemplateStepVO ciTemplateStepVO);

    boolean checkName(Long projectId, String newName);
}
