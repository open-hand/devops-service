package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.eventhandler.payload.EnvGitlabProjectPayload;
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
     * @param projectId              项目Id
     * @param devopsEnvironmentReqVO 环境信息
     */
    void create(Long projectId, DevopsEnvironmentReqVO devopsEnvironmentReqVO);

    /**
     * 项目下环境流水线查询环境
     *
     * @param projectId 项目id
     * @param active    是否可用
     * @return List
     */
    List<DevopsEnvGroupEnvsVO> listDevopsEnvGroupEnvs(Long projectId, Boolean active);

    /**
     * 项目下根据分组查看环境详情
     *
     * @param projectId 项目id
     * @param groupId   分组id
     * @return
     */
    List<DevopsEnvironmentRepVO> listByGroup(Long projectId, Long groupId);

    /**
     * 项目下查询环境
     *
     * @param projectId 项目id
     * @param active    是否可用
     * @return List
     */
    List<DevopsEnvironmentRepVO> listByProjectIdAndActive(Long projectId, Boolean active);

    /**
     * 项目下环境配置树形目录
     *
     * @param projectId 项目id
     * @return 按组划分的环境
     */
    List<DevopsEnvGroupEnvsVO> listEnvTreeMenu(Long projectId);

    /**
     * 实例视图查询项目下环境及其应用及实例
     *
     * @param projectId 项目id
     * @return 实例视图树形目录层次数据
     */
    List<DevopsEnvironmentViewVO> listInstanceEnvTree(Long projectId);

    /**
     * 资源视图查询项目下环境及其下各种资源的基本信息
     *
     * @param projectId 项目id
     * @return 资源视图树形目录层次数据
     */
    List<DevopsResourceEnvOverviewVO> listResourceEnvTree(Long projectId);

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
    DevopsEnvironmentInfoVO queryInfoById(Long projectId, Long environmentId);

    /**
     * 查询环境下相关资源的数量
     *
     * @param environmentId 环境id
     * @return 环境下相关资源的数量
     */
    DevopsEnvResourceCountVO queryEnvResourceCount(Long environmentId);

    /**
     * 项目下更新环境
     *
     * @param devopsEnvironmentUpdateDTO 环境信息
     * @param projectId                  项目Id
     * @return DevopsEnvironmentUpdateDTO
     */
    DevopsEnvironmentUpdateVO update(DevopsEnvironmentUpdateVO devopsEnvironmentUpdateDTO, Long projectId);


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
    List<DevopsEnvironmentRepVO> listByProjectId(Long projectId, Long appServiceId);

    /**
     * 创建环境saga事件
     *
     * @param gitlabProjectPayload env saga payload
     */
    void handleCreateEnvSaga(EnvGitlabProjectPayload gitlabProjectPayload);

    EnvSyncStatusVO queryEnvSyncStatus(Long projectId, Long envId);

    /**
     * 分页查询环境下用户权限
     *
     * @param projectId   项目id
     * @param pageable 分页参数
     * @param envId       环境id
     * @return page
     */
    PageInfo<DevopsUserPermissionVO> pageUserPermissionByEnvId(Long projectId, Pageable pageable,
                                                               String params, Long envId);

    /**
     * 查询项目下所有与该环境未分配权限的项目成员
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @param params    搜索参数
     * @return 所有项目成员
     */
    List<DevopsEnvUserVO> listNonRelatedMembers(Long projectId, Long envId, String params);

    /**
     * 删除环境下该用户的权限
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @param userId    用户id
     */
    void deletePermissionOfUser(Long projectId, Long envId, Long userId);


    /**
     * 获取环境下所有用户权限
     *
     * @param envId 环境id
     * @return baseList
     */
    List<DevopsEnvUserVO> listAllUserPermission(Long envId);

    /**
     * 环境下为用户分配权限
     *
     * @param devopsEnvPermissionUpdateVO 权限更新信息
     */
    void updateEnvUserPermission(DevopsEnvPermissionUpdateVO devopsEnvPermissionUpdateVO);

    /**
     * 删除已停用或失败的环境
     *
     * @param envId     环境id
     * @param projectId 项目id
     */
    void deleteDeactivatedOrFailedEnvironment(Long projectId, Long envId);

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
     * @param data      {@link GitlabProjectPayload} 类型的数据
     * @param projectId 可为空
     */
    void setEnvErrStatus(String data, Long projectId);

    /**
     * 根据集群id和环境code查询环境
     *
     * @param clusterId 集群id
     * @param code      环境code
     * @return 环境信息
     */
    DevopsEnvironmentRepVO queryByCode(Long clusterId, String code);


    /**
     * 重试GitOps同步
     *
     * @param envId 环境id
     */
    void retryGitOps(Long envId);

    /**
     * @param devopsEnvironmentDTO
     * @param userAttrDTO
     */
    void checkEnv(DevopsEnvironmentDTO devopsEnvironmentDTO, UserAttrDTO userAttrDTO);

    /**
     * 检查环境是否可以删除
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @return
     */
    Boolean deleteCheck(Long projectId, Long envId);


    /**
     * 检查资源是否存在
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @param objectId  其他对象id
     * @param type      其他对象类型
     * @return boolean
     */
    EnvironmentMsgVO checkExist(Long projectId, Long envId, Long objectId, String type);

    DevopsEnvironmentDTO baseCreate(DevopsEnvironmentDTO devopsEnvironmentDTO);

    DevopsEnvironmentDTO baseQueryById(Long id);

    DevopsEnvironmentDTO baseUpdate(DevopsEnvironmentDTO devopsEnvironmentDTO);

    void baseCheckCode(DevopsEnvironmentDTO devopsEnvironmentDTO);

    List<DevopsEnvironmentDTO> baseListByProjectId(Long projectId);

    List<DevopsEnvironmentDTO> baseListByProjectIdAndActive(Long projectId, Boolean active);

    DevopsEnvironmentDTO baseQueryByClusterIdAndCode(Long clusterId, String code);

    DevopsEnvironmentDTO baseQueryByProjectIdAndCode(Long projectId, String code);

    DevopsEnvironmentDTO baseQueryByToken(String token);

    void baseUpdateSagaSyncEnvCommit(DevopsEnvironmentDTO devopsEnvironmentDTO);

    void baseUpdateDevopsSyncEnvCommit(DevopsEnvironmentDTO devopsEnvironmentDTO);

    void baseUpdateAgentSyncEnvCommit(DevopsEnvironmentDTO devopsEnvironmentDTO);

    void baseDeleteById(Long id);

    List<DevopsEnvironmentDTO> baseListUserEnvByClusterId(Long clusterId);

    List<DevopsEnvironmentDTO> baseListByIds(List<Long> envIds);

    void deleteEnvSaga(Long envId);

    /**
     * 创建集群的配置库
     *
     * @param clusterId 集群id
     * @return 集群对应的环境id
     */
    DevopsEnvironmentDTO createSystemEnv(Long clusterId);

    /**
     * 删除集群的配置库
     *
     * @param projectId   项目id
     * @param clusterId   集群id
     * @param clusterCode 集群code
     * @param envId       集群的配置库id
     */
    void deleteSystemEnv(Long projectId, Long clusterId, String clusterCode, Long envId);

    DevopsEnvironmentDTO queryByTokenWithClusterCode(@Param("token") String token);
}
