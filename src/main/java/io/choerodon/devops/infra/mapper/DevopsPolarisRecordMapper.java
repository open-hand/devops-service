package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsPolarisRecordDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * @author zmf
 * @since 2/17/20
 */
public interface DevopsPolarisRecordMapper extends BaseMapper<DevopsPolarisRecordDTO> {
    DevopsPolarisRecordDTO queryRecordByScopeIdAndScope(@Param("scopeId") Long scopeId, @Param("scope") String scope);
}
