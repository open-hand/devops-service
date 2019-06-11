package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.DevopsEnvironmentDO;

/**
 * Created by younger on 2018/4/9.
 */
public interface DevopsEnvironmentMapper extends Mapper<DevopsEnvironmentDO> {

    void updateDevopsEnvGroupId(@Param("envId") Long envId);

    DevopsEnvironmentDO queryByToken(@Param("token") String token);

    void updateSagaSyncEnvCommit(@Param("envId") Long envId, @Param("sagaSyncCommit") Long sagaSyncCommit);

    void updateDevopsSyncEnvCommit(@Param("envId") Long envId, @Param("devopsSyncCommit") Long devopsSyncCommit);

    void updateAgentSyncEnvCommit(@Param("envId") Long envId, @Param("agentSyncCommit") Long agentSyncCommit);
}
