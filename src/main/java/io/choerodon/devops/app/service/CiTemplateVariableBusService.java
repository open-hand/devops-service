package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.template.CiTemplateVariableVO;

/**
 * Created by wangxiang on 2021/12/22
 */
public interface CiTemplateVariableBusService {
    List<CiTemplateVariableVO> queryCiVariableByPipelineTemplateId(Long sourceId, Long ciPipelineTemplateId);

    void deleteByTemplatePipelineId(Long ciTemplatePipelineId);

}
