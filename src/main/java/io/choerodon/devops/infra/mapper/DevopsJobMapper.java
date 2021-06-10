package io.choerodon.devops.infra.mapper;

import java.util.List;

import io.choerodon.devops.api.vo.DevopsJobVO;
import io.choerodon.devops.infra.dto.DevopsJobDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/6/8 11:16
 */
public interface DevopsJobMapper extends BaseMapper<DevopsJobDTO> {

    List<DevopsJobVO> listByEnvId(Long envId, String name, Boolean fromInstance);
}
