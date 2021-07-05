package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dto.DevopsDockerInstanceDTO;
import io.choerodon.mybatis.common.BaseMapper;

import java.util.List;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/30 14:12
 */
public interface DevopsDockerInstanceMapper extends BaseMapper<DevopsDockerInstanceDTO> {

    List<DevopsDockerInstanceDTO> listByHostId(Long hostId);
}
