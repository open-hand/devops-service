package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Set;

import io.choerodon.devops.infra.dto.DevopsEnvResourceDetailDTO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:14 2019/7/15
 * Description:
 */
public interface DevopsEnvResourceDetailService {
    DevopsEnvResourceDetailDTO baseCreate(DevopsEnvResourceDetailDTO devopsEnvResourceDetailDTO);

    DevopsEnvResourceDetailDTO baseQueryByResourceDetailId(Long resourceDetailId);

    void baseUpdate(DevopsEnvResourceDetailDTO devopsEnvResourceDetailDTO);

    /**
     * 批量查询 DevopsEnvResourceDetailDTO 根据 resourceDetailIds
     * @param resourceDetailIds
     * @return
     */
    List<DevopsEnvResourceDetailDTO> listByResourceDetailsIds(Set<Long> resourceDetailIds);
}
