package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.asgard.saga.feign.SagaClient;
import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.*;
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;
import io.choerodon.devops.domain.application.event.GitlabProjectPayload;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by younger on 2018/4/9.
 */
public interface DevopsEnvironmentService {
    /**
     * 项目下创建环境
     *
     * @param projectId           项目Id
     * @param devopsEnviromentDTO 环境信息
     * @return String
     */
    void create(Long projectId, DevopsEnviromentDTO devopsEnviromentDTO);

    /**
     * 项目下环境流水线查询环境
     *
     * @param projectId 项目id
     * @param active    是否可用
     * @return List
     */
    List<DevopsEnvGroupEnvsDTO> listDevopsEnvGroupEnvs(Long projectId, Boolean active);

    /**
     * 项目下查询环境
     *
     * @param projectId 项目id
     * @param active    是否可用
     * @return List
     */
    List<DevopsEnviromentRepDTO> listByProjectIdAndActive(Long projectId, Boolean active);

    /**
     * 项目下查询环境
     *
     * @param projectId 项目id
     * @return List
     */
    List<DevopsEnviromentRepDTO> listDeployed(Long projectId);

    /**
     * 项目下启用停用环境
     *
     * @param environmentId 环境id
     * @param active        是否可用
     * @param projectId     项目id
     * @return Boolean
     */
    Boolean activeEnvironment(Long projectId, Long environmentId, Boolean active);

    /**
     * 项目下查询单个环境
     *
     * @param environmentId 环境id
     * @return DevopsEnvironmentUpdateDTO
     */
    DevopsEnvironmentUpdateDTO query(Long environmentId);

    /**
     * 项目下更新环境
     *
     * @param devopsEnvironmentUpdateDTO 环境信息
     * @param projectId                  项目Id
     * @return DevopsEnvironmentUpdateDTO
     */
    DevopsEnvironmentUpdateDTO update(DevopsEnvironmentUpdateDTO devopsEnvironmentUpdateDTO, Long projectId);

    /**
     * 项目下环境流水线排序
     *
     * @param environmentIds 环境列表
     * @return List
     */
    DevopsEnvGroupEnvsDTO sort(Long[] environmentIds);

    /**
     * 创建环境校验名称是否存在
     *
     * @param projectId 项目id
     * @param name      应用name
     */
    void checkName(Long projectId, Long clusterId, String name);

    /**
     * 创建环境校验编码是否存在
     *
     * @param projectId 项目ID
     * @param code      应用code
     */
    void checkCode(Long projectId, Long clusterId, String code);

    /**
     * 项目下查询有正在运行实例的环境
     *
     * @param projectId 项目id
     * @return List
     */
    List<DevopsEnviromentRepDTO> listByProjectId(Long projectId, Long appId);

    /**
     * 创建环境saga事件
     *
     * @param gitlabProjectPayload env saga payload
     */
    void handleCreateEnvSaga(GitlabProjectPayload gitlabProjectPayload);

    EnvSyncStatusDTO queryEnvSyncStatus(Long projectId, Long envId);

    String handDevopsEnvGitRepository(DevopsEnvironmentE devopsEnvironmentE);

    /**
     * 分页查询项目下用户权限
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param envId       环境id
     * @return page
     */
    Page<DevopsEnvUserPermissionDTO> listUserPermissionByEnvId(Long projectId, PageRequest pageRequest,
                                                               String params, String envId);

    /**
     * 获取环境下所有用户权限
     *
     * @param envId 环境id
     * @return list
     */
    List<DevopsEnvUserPermissionDTO> listAllUserPermission(Long envId);

    /**
     * 环境下为用户分配权限
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @param userIds   有权限的用户ids
     */
    Boolean updateEnvUserPermission(Long projectId, Long envId, List<Long> userIds);

    /**
     * 删除已停用的环境
     *
     * @param envId 环境id
     */
    void deleteDeactivatedEnvironment(Long envId);

    /**
     * 项目下查询集群信息
     *
     * @param projectId 项目id
     * @return List
     */
    List<DevopsClusterRepDTO> listDevopsCluster(Long projectId);

    void initMockService(SagaClient sagaClient);

}
