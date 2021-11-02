package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsCommandEventDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * @author crcokitwood
 */
public interface DevopsCommandEventMapper extends BaseMapper<DevopsCommandEventDTO> {

    /**
     * 删除实例Command Event记录
     * @param instanceId 实例Id
     * @return 删除行数
     */
    int deletePreInstanceCommandEvent(@Param("instanceId") Long instanceId);

    List<DevopsCommandEventDTO> listByCommandIdsAndType(@Param("commandIds") Set<Long> commandIds, @Param("type") String type);

    void batchDeleteByCommandIds(@Param("commandIds") Set<Long> commandIds);
}
