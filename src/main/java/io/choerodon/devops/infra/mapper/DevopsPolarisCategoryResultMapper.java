package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.ClusterPolarisSummaryItemVO;
import io.choerodon.devops.infra.dto.DevopsPolarisCategoryResultDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * @author zmf
 * @since 2/17/20
 */
public interface DevopsPolarisCategoryResultMapper extends BaseMapper<DevopsPolarisCategoryResultDTO> {
    void batchInsert(@Param("items") List<DevopsPolarisCategoryResultDTO> items);

    List<ClusterPolarisSummaryItemVO> queryPolarisSummary(@Param("recordId") Long recordId);
}
