package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.template.CiTemplateJobGroupVO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * 流水线任务模板分组(CiTemplateJobGroup)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:16
 */
public interface CiTemplateJobGroupBusService {

    Page<CiTemplateJobGroupVO> pageTemplateJobGroup(Long sourceId, PageRequest pageRequest, String searchParam);

    CiTemplateJobGroupVO createTemplateJobGroup(Long sourceId, CiTemplateJobGroupVO ciTemplateJobGroupVO);

    CiTemplateJobGroupVO updateTemplateJobGroup(Long sourceId, CiTemplateJobGroupVO ciTemplateJobGroupVO);

    void deleteTemplateJobGroup(Long sourceId, Long ciTemplateJobGroupId);

    Page<CiTemplateJobGroupVO> pageTemplateJobGroupByCondition(Long sourceId, PageRequest pageRequest);

    List<CiTemplateJobGroupVO> listTemplateJobGroup(Long sourceId, String name);

    Boolean checkTemplateJobGroupName(Long sourceId, String name, Long templateJobId);


}

