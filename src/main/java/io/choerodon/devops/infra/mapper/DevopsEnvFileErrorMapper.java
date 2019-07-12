package io.choerodon.devops.infra.mapper;

import java.util.List;

import io.choerodon.devops.infra.dto.DevopsEnvFileErrorDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

public interface DevopsEnvFileErrorMapper extends Mapper<DevopsEnvFileErrorDTO> {

    DevopsEnvFileErrorDTO queryLatestByEnvAndPath(@Param("envId") Long envId, @Param("filePath") String filePath);

    DevopsEnvFileErrorDTO queryByEnvAndPathAndCommits(@Param("envId") Long envId,
                                                      @Param("filePath") String filePath,
                                                      @Param("commits") List<String> commits);
}
