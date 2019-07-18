package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.DevopsEnvGroupVO;
import io.choerodon.devops.infra.dto.DevopsEnvGroupDTO;

public interface DevopsEnvGroupService {
    /**
     * 项目下创建环境组
     *
     * @param name
     * @param projectId
     * @return
     */
    DevopsEnvGroupVO create(String name, Long projectId);

    /**
     * 项目下更新环境组
     *
     * @param devopsEnvGroupDTO
     * @param projectId
     * @return
     */
    DevopsEnvGroupVO update(DevopsEnvGroupVO devopsEnvGroupDTO, Long projectId);

    /**
     * 项目下查询环境组
     *
     * @param projectId
     * @return
     */
    List<DevopsEnvGroupVO> listByProject(Long projectId);

    /**
     * 校验环境组名唯一性
     *
     * @param name
     * @param projectId
     * @return
     */
    Boolean checkName(String name, Long projectId);

    /**
     * 环境组删除
     *
     * @param id
     */
    void delete(Long id);

    DevopsEnvGroupDTO baseCreate(DevopsEnvGroupDTO devopsEnvGroupDTO);

    DevopsEnvGroupDTO baseUpdate(DevopsEnvGroupDTO devopsEnvGroupDTO);

    List<DevopsEnvGroupDTO> baseListByProjectId(Long projectId);

    DevopsEnvGroupDTO baseQuery(Long id);

    Boolean baseCheckUniqueInProject(Long id, String name, Long projectId);

    Boolean baseCheckUniqueInProject(String name, Long projectId);

    void baseDelete(Long id);

}
