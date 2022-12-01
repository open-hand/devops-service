package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsCiHostDeployInfoDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsCiHostDeployInfoMapper extends BaseMapper<DevopsCiHostDeployInfoDTO> {
    List<DevopsCiHostDeployInfoDTO> selectByHostAppId(@Param("hostAppId") Long hostAppId);
}
