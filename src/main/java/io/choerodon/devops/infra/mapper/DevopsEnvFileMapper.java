package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsEnvFileDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsEnvFileMapper extends BaseMapper<DevopsEnvFileDTO> {

    DevopsEnvFileDTO queryLatestByEnvAndPath(@Param("envId") Long envId, @Param("filePath") String filePath);
}
