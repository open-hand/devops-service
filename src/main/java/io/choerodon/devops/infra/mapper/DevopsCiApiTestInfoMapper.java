package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsCiApiTestInfoDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsCiApiTestInfoMapper extends BaseMapper<DevopsCiApiTestInfoDTO> {
    DevopsCiApiTestInfoDTO selectById(@Param("id") Long id);
}
