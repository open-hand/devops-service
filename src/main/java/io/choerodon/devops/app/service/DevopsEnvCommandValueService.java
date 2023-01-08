package io.choerodon.devops.app.service;

import java.util.Set;

import io.choerodon.devops.infra.dto.DevopsEnvCommandValueDTO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:47 2019/7/12
 * Description:
 */
public interface DevopsEnvCommandValueService {
    DevopsEnvCommandValueDTO baseCreate(DevopsEnvCommandValueDTO devopsEnvCommandValueDTO);

    DevopsEnvCommandValueDTO baseQueryById(Long id);

    void baseDeleteById(Long commandId);

    void baseUpdateById(Long valueId, String value);

    void batchDeleteByIds(Set<Long> ids);
}
