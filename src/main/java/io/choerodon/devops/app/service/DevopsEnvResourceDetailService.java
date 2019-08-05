package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.DevopsEnvResourceDetailDTO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:14 2019/7/15
 * Description:
 */
public interface DevopsEnvResourceDetailService {
    DevopsEnvResourceDetailDTO baseCreate(DevopsEnvResourceDetailDTO devopsEnvResourceDetailDTO);

    DevopsEnvResourceDetailDTO baesQueryByMessageId(Long messageId);

    void baseUpdate(DevopsEnvResourceDetailDTO devopsEnvResourceDetailDTO);
}
