package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;
import io.choerodon.devops.infra.dataobject.DevopsEnvironmentViewDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.DevopsEnvironmentDO;

import java.util.List;

/**
 * Created by younger on 2018/4/9.
 */
public interface DevopsEnvironmentMapper extends Mapper<DevopsEnvironmentDO> {

    void updateDevopsEnvGroupId(@Param("envId") Long envId);

    DevopsEnvironmentDO queryByToken(@Param("token") String token);

    void updateSagaSyncEnvCommit(@Param("envId") Long envId, @Param("sagaSyncCommit") Long sagaSyncCommit);

    void updateDevopsSyncEnvCommit(@Param("envId") Long envId, @Param("devopsSyncCommit") Long devopsSyncCommit);

    void updateAgentSyncEnvCommit(@Param("envId") Long envId, @Param("agentSyncCommit") Long agentSyncCommit);

    void updateOptions(@Param("gitlabEnvProjectId") Long gitlabEnvProjectId, @Param("hookId") Long hookId, @Param("isSynchro") Boolean isSynchro, @Param("envId") Long envId);

    /**
     * 项目下，查询实例视图的环境及其应用及实例作为树形目录
     * @param projectId 项目id
     * @return 树形目录
     */
    List<DevopsEnvironmentViewDTO> listEnvTree(@Param("projectId") Long projectId);
}
