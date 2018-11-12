package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.DevopsEnvFileDO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsEnvFileMapper extends BaseMapper<DevopsEnvFileDO> {

    DevopsEnvFileDO queryLatestByEnvAndPath(@Param("envId") Long envId, @Param("filePath") String filePath);

    DevopsEnvFileDO queryByEnvAndPathAndCommits(@Param("envId") Long envId, @Param("filePath") String filePath, @Param("commits") List<String> commits);
}
