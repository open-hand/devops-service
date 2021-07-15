package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.DevopsEnvResourceCountVO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentInfoDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentViewDTO;
import io.choerodon.devops.infra.dto.DevopsResourceEnvOverviewDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by younger on 2018/4/9.
 */
public interface DevopsEnvironmentMapper extends BaseMapper<DevopsEnvironmentDTO> {
    List<DevopsEnvironmentDTO> listEnvWithInstancesByClusterIdForAgent(@Param("clusterId") Long clusterId);

    void updateDevopsEnvGroupIdNullByProjectIdAndGroupId(@Param("project_id") Long projectId,
                                                         @Param("env_group_id") Long envGroupId);

    void updateDevopsEnvGroupId(@Param("envId") Long envId);

    DevopsEnvironmentDTO queryByToken(@Param("token") String token);

    DevopsEnvironmentDTO queryByTokenWithClusterCode(@Param("token") String token);

    DevopsEnvironmentDTO queryByIdWithClusterCode(@Param("id") Long id);

    void updateSagaSyncEnvCommit(@Param("envId") Long envId, @Param("sagaSyncCommit") Long sagaSyncCommit);

    void updateDevopsSyncEnvCommit(@Param("envId") Long envId, @Param("devopsSyncCommit") Long devopsSyncCommit);

    void updateAgentSyncEnvCommit(@Param("envId") Long envId, @Param("agentSyncCommit") Long agentSyncCommit);

    void updateOptions(@Param("gitlabEnvProjectId") Long gitlabEnvProjectId, @Param("hookId") Long hookId, @Param("isSynchro") Boolean isSynchro, @Param("envId") Long envId);

    /**
     * 项目下，查询实例视图的环境及其应用及实例作为树形目录（所有环境，以项目所有者的权限）
     *
     * @param projectId 项目id
     * @return 树形目录
     */
    List<DevopsEnvironmentViewDTO> listAllInstanceEnvTree(@Param("projectId") Long projectId);

    /**
     * 项目下，查询实例视图的环境及其应用及实例作为树形目录（以项目成员的权限）
     *
     * @param projectId 项目id
     * @param memberId  项目成员id
     * @return 树形目录
     */
    List<DevopsEnvironmentViewDTO> listMemberInstanceEnvTree(@Param("projectId") Long projectId, @Param("memberId") Long memberId);

    /**
     * 项目下，查询资源视图的环境及其下资源作为树形目录所有环境，以项目所有者的权限）
     *
     * @param projectId 项目id
     * @return 树形目录
     */
    List<DevopsResourceEnvOverviewDTO> listAllResourceEnvTree(@Param("projectId") Long projectId);

    /**
     * 项目下，查询资源视图的环境及其下资源作为树形目录（以项目成员的权限）
     *
     * @param projectId 项目id
     * @param memberId  项目成员id
     * @return 树形目录
     */
    List<DevopsResourceEnvOverviewDTO> listMemberResourceEnvTree(@Param("projectId") Long projectId, @Param("memberId") Long memberId);

    /**
     * 查询单个环境及其集群信息
     *
     * @param envId 环境id
     * @return 环境及其集群信息
     */
    DevopsEnvironmentInfoDTO queryInfoById(@Param("envId") Long envId);


    /**
     * 查询环境下相关资源的数量
     *
     * @param envId 环境id
     * @return 环境下相关资源的数量
     */
    DevopsEnvResourceCountVO queryEnvResourceCount(@Param("envId") Long envId);

    List<DevopsEnvironmentDTO> listByIds(@Param("envIds") List<Long> envIds);

    List<DevopsEnvironmentDTO> listByProjectId(@Param("projectId") Long projectId);

    /**
     * 查询指定分组的相关环境
     *
     * @param projectId 项目id
     * @param groupId   分组id
     * @return
     */
    List<DevopsEnvironmentDTO> listByProjectIdAndGroupId(@Param("projectId") Long projectId, @Param("groupId") Long groupId);

    int updateIsSynchroToTrueWhenFailed();

    int updateIsActiveNullToTrue();

    List<Long> listGitlabProjectIdByEnvPermission(@Param("gitlabGroupId") Long gitlabGroupId, @Param("iamUserId") Long iamUserId);

    List<DevopsEnvironmentDTO> listByProjectIdAndName(@Param("projectId") Long projectId, @Param("envName") String envName);

    Integer queryEnvConutByEnvIds(@Param("envIds") List<Long> envIds);

    int countByOptions(@Param("clusterId") Long clusterId,
                       @Param("projectId") Long projectId,
                       @Param("isFailed") Boolean isFailed,
                       @Param("type") String type);

    /**
     * 查询指定项目下的所有的环境
     *
     * @param projectIds 项目ID列表
     * @return 环境
     */
    List<DevopsEnvironmentDTO> listByProject(@Param("projectIds") List<Long> projectIds);
}
