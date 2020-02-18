package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.ClusterPolarisSummaryItemVO;
import io.choerodon.devops.infra.dto.DevopsPolarisItemDTO;
import io.choerodon.mybatis.common.Mapper;

/**
 * @author zmf
 * @since 2/17/20
 */
public interface DevopsPolarisItemMapper extends Mapper<DevopsPolarisItemDTO> {
    void batchInsert(@Param("items") List<DevopsPolarisItemDTO> items);

    List<ClusterPolarisSummaryItemVO> queryPolarisSummary(@Param("recordId") Long recordId);
}
