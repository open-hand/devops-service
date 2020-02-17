package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsPolarisInstanceResultDTO;
import io.choerodon.mybatis.common.Mapper;

/**
 * @author zmf
 * @since 2/17/20
 */
public interface DevopsPolarisInstanceResultMapper extends Mapper<DevopsPolarisInstanceResultDTO> {
    void batchInsert(@Param("items") List<DevopsPolarisInstanceResultDTO> items);

}
