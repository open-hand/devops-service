package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsHostAppInstanceDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/9/3 15:50
 */
public interface DevopsHostAppInstanceMapper extends BaseMapper<DevopsHostAppInstanceDTO> {
    void updateKillCommand(@Param("id") Long id, @Param("killCommand") String killCommand);

    void updateHealthProb(@Param("id") Long id, @Param("healthProb") String healthProb);
}
