package io.choerodon.devops.infra.mapper;

import java.util.List;

import io.choerodon.devops.infra.dto.DevopsEnvFileErrorDTO;
import org.apache.ibatis.annotations.Param;

public interface DevopsEnvFileErrorMapper extends BaseMapper<DevopsEnvFileErrorDTO> {

    DevopsEnvFileErrorDTO queryLatestByEnvAndPath(@Param("envId") Long envId, @Param("filePath") String filePath);

    DevopsEnvFileErrorDTO queryByEnvAndPathAndCommits(@Param("envId") Long envId,
                                                      @Param("filePath") String filePath,
                                                      @Param("commits") List<String> commits);

    long queryErrorFileCountByEnvId(@Param("envId") Long envId);
}
