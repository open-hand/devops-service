package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.DevopsCustomizeResourceContentDTO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:10 2019/7/12
 * Description:
 */
public interface DevopsCustomizeResourceContentService {
    DevopsCustomizeResourceContentDTO baseCreate(DevopsCustomizeResourceContentDTO devopsCustomizeResourceContentVO);

    DevopsCustomizeResourceContentDTO baseQuery(Long contentId);

    void baseUpdate(DevopsCustomizeResourceContentDTO devopsCustomizeResourceContentVO);

    void baseDelete(Long contentId);
}
