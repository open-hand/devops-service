package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.DevopsEnvFileErrorDO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsEnvFileErrorMapper extends BaseMapper<DevopsEnvFileErrorDO> {

    DevopsEnvFileErrorDO queryLatestByEnvAndPath(@Param("envId") Long envId, @Param("filePath") String filePath);

    DevopsEnvFileErrorDO queryByEnvAndPathAndCommits(@Param("envId") Long envId,
                                                     @Param("filePath") String filePath,
                                                     @Param("commits") List<String> commits);
}
