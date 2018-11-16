package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.DevopsBranchDO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsBranchMapper extends BaseMapper<DevopsBranchDO> {

    DevopsBranchDO queryByAppAndBranchName(@Param("appId") Long appId, @Param("branchName") String name);

    List<DevopsBranchDO> list(@Param("appId") Long appId,
                              @Param("searchParam") Map<String, Object> searchParam,
                              @Param("param") String param);
}
