package io.choerodon.devops.infra.mapper;

import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsCommandEventDTO;

/**
 * @author crcokitwood
 */
public interface DevopsCommandEventMapper extends Mapper<DevopsCommandEventDTO> {

    /**
     * 删除实例Command Event记录
     * @param instanceId 实例Id
     * @return 删除行数
     */
    int deletePreInstanceCommandEvent(@Param("instanceId") Long instanceId);
}
