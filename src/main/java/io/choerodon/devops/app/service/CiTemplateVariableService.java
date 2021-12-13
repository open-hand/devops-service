package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.CiTemplateVariableDTO;

/**
 * 流水线模板配置的CI变量(CiTemplateVariable)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:22
 */
public interface CiTemplateVariableService {

    List<CiTemplateVariableDTO> listByTemplateId(Long templateId);

}

