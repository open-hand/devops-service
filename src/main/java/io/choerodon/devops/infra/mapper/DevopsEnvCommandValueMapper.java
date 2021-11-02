package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.Set;

import io.choerodon.devops.infra.dto.DevopsEnvCommandValueDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsEnvCommandValueMapper extends BaseMapper<DevopsEnvCommandValueDTO> {

    void batchDeleteByIds(@Param("ids") Set<Long> ids);
}
