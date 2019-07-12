package io.choerodon.devops.infra.mapper;

import java.util.List;

import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsEnvFileDTO;

public interface DevopsEnvFileMapper extends Mapper<DevopsEnvFileDTO> {

    DevopsEnvFileDTO queryLatestByEnvAndPath(@Param("envId") Long envId, @Param("filePath") String filePath);

    DevopsEnvFileDTO queryByEnvAndPathAndCommits(@Param("envId") Long envId, @Param("filePath") String filePath, @Param("commits") List<String> commits);
}
