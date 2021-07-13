package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dto.DevopsNormalInstanceDTO;
import io.choerodon.mybatis.common.BaseMapper;

import java.util.List;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/7/1 9:25
 */
public interface DevopsNormalInstanceMapper extends BaseMapper<DevopsNormalInstanceDTO> {

    List<DevopsNormalInstanceDTO> listByHostId(Long hostId);
}
