package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsHostAppDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/7/1 9:25
 */
public interface DevopsHostAppMapper extends BaseMapper<DevopsHostAppDTO> {

    List<DevopsHostAppDTO> listByHostId(Long hostId);

    void deleteByHostId(@Param("hostId") Long hostId);
}
