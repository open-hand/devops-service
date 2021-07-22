package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsHostCommandDTO;
import io.choerodon.mybatis.common.BaseMapper;

import java.util.List;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/28 14:44
 */
public interface DevopsHostCommandMapper extends BaseMapper<DevopsHostCommandDTO> {
    DevopsHostCommandDTO selectLatestByInstanceId(@Param("instanceId") Long id);

    List<DevopsHostCommandDTO> listByHostId(Long hostId);

    void deleteByHostId(@Param("hostId") Long hostId);
}
