package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dto.DevopsEnvFileDTO;
import org.apache.ibatis.annotations.Param;

public interface DevopsEnvFileMapper extends BaseMapper<DevopsEnvFileDTO> {

    DevopsEnvFileDTO queryLatestByEnvAndPath(@Param("envId") Long envId, @Param("filePath") String filePath);
}
