package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.CiVariableVO;

import java.util.List;
import java.util.Map;

/**
 * @author lihao
 */
public interface DevopsCiVariableService {
    /**
     * 列出所有的key
     *
     * @param projectId    项目id
     * @param level        层级
     * @param appServiceId 应用id
     * @return List<CiVariableVO>
     */
    Map<String, List<CiVariableVO>> listKeys(Long projectId, Long appServiceId);

    /**
     * 列出所有的key，包括values
     *
     * @param projectId    项目id
     * @param level        层级
     * @param appServiceId 应用id
     * @return List<CiVariableVO>
     */
    List<CiVariableVO> listValues(Long projectId, String level, Long appServiceId);
}
