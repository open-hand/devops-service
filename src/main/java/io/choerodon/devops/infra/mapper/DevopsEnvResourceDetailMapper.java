package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dto.DevopsEnvResourceDetailDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * Created by younger on 2018/4/24.
 */
public interface DevopsEnvResourceDetailMapper extends Mapper<DevopsEnvResourceDetailDTO> {
    List<DevopsEnvResourceDetailDTO> listByMessageIds(@Param("resourceDetailIds") Set<Long> resourceDetailIds);
}
