package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.CiVariableVO;

import java.util.List;

public interface DevopsCiVariableService {
    List<CiVariableVO> listGlobalVariable(Long projectId);

    List<CiVariableVO> listAppServiceVariable(Long projectId, Long appServiceId);
}
