package io.choerodon.devops.app.service;

import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

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
    Boolean checkName(String name, Long projectId, Long groupId);


    /**
     * 校验环境组存在，且在该项目下
     *
     * @param groupId   组id 可为空，为空不校验
     * @param projectId 项目id
     * @throws io.choerodon.core.exception.CommonException 当环境组ID不为空时，环境组id不存在，
     *                                                     或者环境组id存在但不在该项目下
     */
    void checkGroupIdInProject(@Nullable Long groupId, @NotNull Long projectId);

    /**
     * 环境组删除
     *
     * @param id
     */
    void delete(Long id);

    /**
     * 检查环境组是否存在
     *
     * @param id
     */
    Boolean checkExist(Long id);

    DevopsEnvGroupDTO baseCreate(DevopsEnvGroupDTO devopsEnvGroupDTO);

    DevopsEnvGroupDTO baseUpdate(DevopsEnvGroupDTO devopsEnvGroupDTO);

    List<DevopsEnvGroupDTO> baseListByProjectId(Long projectId);

    DevopsEnvGroupDTO baseQuery(Long id);

    Boolean baseCheckUniqueInProject(Long id, String name, Long projectId);

    Boolean baseCheckUniqueInProject(String name, Long projectId);

    void baseDelete(Long id);

}
