package io.choerodon.devops.infra.mapper;

import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.DevopsEnvCommandLogDO;

/**
 *
 * @author younger
 * @date 2018/4/24
 */
public interface DevopsEnvCommandLogMapper extends Mapper<DevopsEnvCommandLogDO> {

    /**
     * 删除实例历史Command Log
     * @param instanceId 实例Id
     * @return 删除行数
     */
    int deletePreInstanceCommandLog(@Param("instanceId") Long instanceId);
}
