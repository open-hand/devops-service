package io.choerodon.devops.infra.mapper;

import java.util.List;

import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsEnvFileErrorDO;

public interface DevopsEnvFileErrorMapper extends Mapper<DevopsEnvFileErrorDO> {

    DevopsEnvFileErrorDO queryLatestByEnvAndPath(@Param("envId") Long envId, @Param("filePath") String filePath);

    DevopsEnvFileErrorDO queryByEnvAndPathAndCommits(@Param("envId") Long envId,
                                                     @Param("filePath") String filePath,
                                                     @Param("commits") List<String> commits);

    long queryErrorFileCountByEnvId(@Param("envId") Long envId);
}
