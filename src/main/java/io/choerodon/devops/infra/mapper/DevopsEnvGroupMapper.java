package io.choerodon.devops.infra.mapper;

import java.util.List;

import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsEnvGroupDTO;

/**
 * Creator: Runge
 * Date: 2018/9/4
 * Time: 14:17
 * Description:
 */
public interface DevopsEnvGroupMapper extends Mapper<DevopsEnvGroupDTO> {
    void sortGroupInProject(@Param("projectId") Long projectId, @Param("envGroupIds") List<Long> envGroupIds);
}
