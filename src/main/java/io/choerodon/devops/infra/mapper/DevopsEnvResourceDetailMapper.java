package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsEnvResourceDetailDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by younger on 2018/4/24.
 */
public interface DevopsEnvResourceDetailMapper extends BaseMapper<DevopsEnvResourceDetailDTO> {
    List<DevopsEnvResourceDetailDTO> listByMessageIds(@Param("resourceDetailIds") Set<Long> resourceDetailIds);
}
