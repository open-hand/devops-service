package io.choerodon.devops.infra.mapper;

import java.util.List;

import io.choerodon.devops.api.vo.DevopsEnvResourceCountVO;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.mybatis.common.Mapper;

import org.apache.ibatis.annotations.Param;

/**
 * Created by younger on 2018/4/9.
 */
public interface DevopsEnvironmentMapper extends Mapper<DevopsEnvironmentDTO> {

    void updateDevopsEnvGroupId(@Param("envId") Long envId);

    DevopsEnvironmentDTO queryByToken(@Param("token") String token);

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

    /**
     * 查询指定分组的相关环境
     *
     * @param projectId 项目id
     * @param groupId   分组id
     * @param active    是否可用
     * @return
     */
    List<DevopsEnvironmentDTO> listByProjectIdAndGroupIdAndActive(@Param("projectId") Long projectId, @Param("groupId") Long groupId, @Param("active") Boolean active);
}
