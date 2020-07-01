package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.api.vo.LatestAppServiceVO;
import io.choerodon.devops.infra.dto.DevopsBranchDTO;
import io.choerodon.mybatis.common.BaseMapper;

import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface DevopsBranchMapper extends BaseMapper<DevopsBranchDTO> {

    DevopsBranchDTO queryByAppAndBranchName(@Param("appServiceId") Long appServiceId, @Param("branchName") String name);

    List<DevopsBranchDTO> list(@Param("appServiceId") Long appServiceId,
                               @Param("sortString") String sortString,
                               @Param("searchParam") Map<String, Object> searchParam,
                               @Param("params") List<String> params);


    void deleteByIsDelete();

    void deleteDuplicateBranch();

    void deleteByAppServiceId(@Param("appServiceId") Long appServiceId);

    void updateBranchById(@Param("devopsBranchDTO") DevopsBranchDTO devopsBranchDTO);

    List<LatestAppServiceVO> listLatestUseAppServiceIdAndDate(@Param("projectIds") List<Long> projectIds,
                                                              @Param("userId") Long userId,
                                                              @Param("time") Date time);
}
