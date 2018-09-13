package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.DevopsEnvironmentDO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by younger on 2018/4/9.
 */
public interface DevopsEnvironmentMapper extends BaseMapper<DevopsEnvironmentDO> {

    void updateDevopsEnvGroupId(@Param("envId") Long envId);

    void updateDevopsEnvCommit(@Param("envId") Long envId,@Param("gitCommit") Long gitCommit,@Param("devopsSyncCommit") Long devopsSyncCommit,@Param("agentSyncCommit") Long agentSyncCommit);

}
