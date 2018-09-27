package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.DevopsEnvCommandLogDO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 *
 * @author younger
 * @date 2018/4/24
 */
public interface DevopsEnvCommandLogMapper extends BaseMapper<DevopsEnvCommandLogDO> {

    /**
     * 删除实例历史Command Log
     * @param instanceId 实例Id
     * @return 删除行数
     */
    int deletePreInstanceCommandLog(@Param("instanceId") Long instanceId);
}
