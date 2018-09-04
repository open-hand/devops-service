package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.DevopsEnvGroupDO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Creator: Runge
 * Date: 2018/9/4
 * Time: 14:17
 * Description:
 */
public interface DevopsEnvGroupMapper extends BaseMapper<DevopsEnvGroupDO> {
    void sortGroupInProject(@Param("projectId") Long projectId, @Param("envGroupIds") List<Long> envGroupIds);
}
