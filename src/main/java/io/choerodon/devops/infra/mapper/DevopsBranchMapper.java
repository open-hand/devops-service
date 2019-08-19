package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsBranchDTO;

public interface DevopsBranchMapper extends Mapper<DevopsBranchDTO> {

    DevopsBranchDTO queryByAppAndBranchName(@Param("appServiceId") Long appServiceId, @Param("branchName") String name);

    List<DevopsBranchDTO> list(@Param("appServiceId") Long appServiceId,
                               @Param("searchParam") Map<String, Object> searchParam,
                               @Param("params") List<String> params);

    void  deleteByIsDelete();

    void  deleteDuplicateBranch();

}
