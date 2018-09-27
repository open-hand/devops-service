package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.DevopsCommandEventDO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * @author crcokitwood
 */
public interface DevopsCommandEventMapper extends BaseMapper<DevopsCommandEventDO> {

    /**
     * 删除实例Command Event记录
     * @param instanceId 实例Id
     * @return 删除行数
     */
    int deletePreInstanceCommandEvent(@Param("instanceId") Long instanceId);
}
