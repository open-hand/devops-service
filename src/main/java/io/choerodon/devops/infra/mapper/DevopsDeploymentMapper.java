package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.DevopsDeploymentVO;
import io.choerodon.devops.infra.dto.DevopsDeploymentDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/6/8 11:14
 */
public interface DevopsDeploymentMapper extends BaseMapper<DevopsDeploymentDTO> {

    List<DevopsDeploymentVO> listByEnvId(@Param("envId") Long envId,
                                         @Param("name") String name);
}
