package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsIssueRelDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsIssueRelMapper extends BaseMapper<DevopsIssueRelDTO> {
    void batchDeleteByBranchIdAndIssueIds(@Param("branchId") Long branchId, @Param("issueIds") List<Long> issueIds);

    Set<Long> listIssueIdsByObjectTypeAndObjectId(@Param("objectIds") Set<Long> objectIds, @Param("object") String object);

    List<DevopsIssueRelDTO> listIssueIdsByObjectTypeAndObjectIds(Set<Long> objectIds, String object);
}
