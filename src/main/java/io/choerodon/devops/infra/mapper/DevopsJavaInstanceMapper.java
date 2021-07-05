package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dto.DevopsJavaInstanceDTO;
import io.choerodon.mybatis.common.BaseMapper;

import java.util.List;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/7/1 9:25
 */
public interface DevopsJavaInstanceMapper extends BaseMapper<DevopsJavaInstanceDTO> {

    List<DevopsJavaInstanceDTO> listByHostId(Long hostId);
}
