package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dto.DevopsEnvFileDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

public interface DevopsEnvFileMapper extends Mapper<DevopsEnvFileDTO> {

    DevopsEnvFileDTO queryLatestByEnvAndPath(@Param("envId") Long envId, @Param("filePath") String filePath);
}
