package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.CiVariableVO;

import java.util.List;
import java.util.Map;

/**
 * @author lihao
 */
public interface DevopsCiVariableService {

    /**
     * 列出项目层的所有ci变量
     *
     * @param projectId 项目id
     * @return 变量数组
     */
    List<CiVariableVO> listKeysOnProject(Long projectId);

    /**
     * 列出应用层的所有ci变量
     *
     * @param appServiceId 应用id
     * @return 变量数组
     */
    List<CiVariableVO> listKeysOnApp(Long appServiceId);

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

    /**
     * 批量创建/更新ci变量
     *
     * @param projectId        项目id
     * @param level            层级
     * @param appServiceId     应用id
     * @param ciVariableVOList 变量列表
     * @return 变量列表
     */
    List<CiVariableVO> batchUpdate(Long projectId, String level, Long appServiceId, List<CiVariableVO> ciVariableVOList);

    /**
     * 批量删除ci变量
     *
     * @param projectId    项目id
     * @param level        层级
     * @param appServiceId 应用id
     * @param keys         键列表
     */
    void batchDelete(Long projectId, String level, Long appServiceId, List<String> keys);

    void save(Long projectId, String level, Long appServiceId, List<CiVariableVO> ciVariableVOList);
}
