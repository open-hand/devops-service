package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.host.DevopsDockerInstanceVO;
import io.choerodon.devops.infra.dto.DevopsDockerInstanceDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/30 14:12
 */
public interface DevopsDockerInstanceMapper extends BaseMapper<DevopsDockerInstanceDTO> {

    List<DevopsDockerInstanceDTO> listByHostId(Long hostId);

    void deleteByHostId(@Param("hostId") Long hostId);

    List<DevopsDockerInstanceVO> listByAppId(@Param("appId") Long appId);
}
