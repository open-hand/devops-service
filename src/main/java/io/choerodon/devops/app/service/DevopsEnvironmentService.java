package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.asgard.saga.feign.SagaClient;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.eventhandler.payload.GitlabProjectPayload;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.UserAttrDTO;


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
    void create(Long projectId, DevopsEnviromentVO devopsEnviromentDTO);

    /**
     * 项目下环境流水线查询环境
     *
     * @param projectId 项目id
     * @param active    是否可用
     * @return List
     */
    List<DevopsEnvGroupEnvsVO> listDevopsEnvGroupEnvs(Long projectId, Boolean active);

    /**
     * 项目下查询环境
     *
     * @param projectId 项目id
     * @param active    是否可用
     * @return List
     */
    List<DevopsEnviromentRepVO> listByProjectIdAndActive(Long projectId, Boolean active);

    /**
     * 项目下查询环境
     *
     * @param projectId 项目id
     * @return List
     */
    List<DevopsEnviromentRepVO> listDeployed(Long projectId);

    /**
     * 实例视图查询项目下环境及其应用及实例
     *
     * @param projectId 项目id
     * @return 实例视图树形目录层次数据
     */
    List<DevopsEnvironmentViewVO> listEnvTree(Long projectId);

    /**
     * 项目下启用停用环境
     *
     * @param environmentId 环境id
     * @param active        是否可用
     * @param projectId     项目id
     * @return Boolean
     */
    Boolean updateActive(Long projectId, Long environmentId, Boolean active);

    /**
     * 项目下查询单个环境
     *
     * @param environmentId 环境id
     * @return DevopsEnvironmentUpdateDTO
     */
    DevopsEnvironmentUpdateVO query(Long environmentId);

    /**
     * 项目下查询单个环境及其相关信息
     *
     * @param environmentId 环境id
     * @return 环境及其相关信息
     */
    DevopsEnvironmentInfoVO queryInfoById(Long environmentId);

    /**
     * 项目下更新环境
     *
     * @param devopsEnvironmentUpdateDTO 环境信息
     * @param projectId                  项目Id
     * @return DevopsEnvironmentUpdateDTO
     */
    DevopsEnvironmentUpdateVO update(DevopsEnvironmentUpdateVO devopsEnvironmentUpdateDTO, Long projectId);

    /**
     * 项目下环境流水线排序
     *
     * @param environmentIds 环境列表
     * @return List
     */
    DevopsEnvGroupEnvsVO sort(Long[] environmentIds);

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
    List<DevopsEnviromentRepVO> listByProjectId(Long projectId, Long appId);

    /**
     * 创建环境saga事件
     *
     * @param gitlabProjectPayload env saga payload
     */
    void handleCreateEnvSaga(GitlabProjectPayload gitlabProjectPayload);

    EnvSyncStatusVO queryEnvSyncStatus(Long projectId, Long envId);

    /**
     * 分页查询项目下用户权限
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param envId       环境id
     * @return page
     */
    PageInfo<DevopsEnvUserPermissionVO> listUserPermissionByEnvId(Long projectId, PageRequest pageRequest,
                                                                  String params, Long envId);

    /**
     * 获取环境下所有用户权限
     *
     * @param envId 环境id
     * @return baseList
     */
    List<DevopsEnvUserPermissionVO> listAllUserPermission(Long envId);

    /**
     * 环境下为用户分配权限
     *
     * @param envId   环境id
     * @param userIds 有权限的用户ids
     */
    Boolean updateEnvUserPermission(Long envId, List<Long> userIds);

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
    List<DevopsClusterRepVO> listDevopsCluster(Long projectId);

    /**
     * 设置环境状态为错误
     *
     * @param data      数据
     * @param projectId 可为空
     */
    void setEnvErrStatus(String data, Long projectId);

    /**
     * @param clusterId
     * @param code
     * @return
     */
    DevopsEnviromentRepVO queryByCode(Long clusterId, String code);


    /**
     * @param envId
     */
    void retryGitOps(Long envId);

    /**
     *
     * @param devopsEnvironmentDTO
     * @param userAttrDTO
     */
    void checkEnv(DevopsEnvironmentDTO devopsEnvironmentDTO, UserAttrDTO userAttrDTO);


    DevopsEnvironmentDTO baseCreate(DevopsEnvironmentDTO devopsEnvironmentDTO);

    DevopsEnvironmentDTO baseQueryById(Long id);

    Boolean baseUpdateActive(Long environmentId, Boolean active);

    DevopsEnvironmentDTO baseUpdate(DevopsEnvironmentDTO devopsEnvironmentDTO);

    void baseCheckCode(DevopsEnvironmentDTO devopsEnvironmentDTO);

    List<DevopsEnvironmentDTO> baseListByProjectId(Long projectId);

    List<DevopsEnvironmentDTO> baseListByProjectIdAndActive(Long projectId, Boolean active);

    DevopsEnvironmentDTO baseQueryByClusterIdAndCode(Long clusterId, String code);

    DevopsEnvironmentDTO baseQueryByProjectIdAndCode(Long projectId, String code);

    DevopsEnvironmentDTO baseQueryByToken(String token);

    List<DevopsEnvironmentDTO> baseListAll();

    void baseUpdateSagaSyncEnvCommit(DevopsEnvironmentDTO devopsEnvironmentDTO);

    void baseUpdateDevopsSyncEnvCommit(DevopsEnvironmentDTO devopsEnvironmentDTO);

    void baseUpdateAgentSyncEnvCommit(DevopsEnvironmentDTO devopsEnvironmentDTO);

    void baseDeleteById(Long id);

    List<DevopsEnvironmentDTO> baseListByClusterId(Long clusterId);
}
