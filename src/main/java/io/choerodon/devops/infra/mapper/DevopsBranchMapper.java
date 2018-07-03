package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.DevopsBranchDO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsBranchMapper extends BaseMapper<DevopsBranchDO> {

    DevopsBranchDO queryByAppAndBranchName(@Param("appId") Long appId, @Param("branchName") String name);
}
