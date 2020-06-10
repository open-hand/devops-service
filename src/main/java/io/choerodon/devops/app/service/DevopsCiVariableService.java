package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.CiVariableVO;

import java.util.List;

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
    List<CiVariableVO> listKeys(Long projectId, String level, Long appServiceId);

    /**
     * 列出指定key的value
     *
     * @param projectId    项目id
     * @param level        层级
     * @param appServiceId 应用id
     * @param keys         key列表
     * @return List<CiVariableVO>
     */
    List<CiVariableVO> listValues(Long projectId, String level, Long appServiceId, List<String> keys);
}
