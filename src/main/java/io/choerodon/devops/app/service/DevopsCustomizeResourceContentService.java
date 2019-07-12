package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.iam.entity.DevopsCustomizeResourceContentVO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:10 2019/7/12
 * Description:
 */
public interface DevopsCustomizeResourceContentService {
    DevopsCustomizeResourceContentVO baseCreate(DevopsCustomizeResourceContentVO devopsCustomizeResourceContentVO);

    DevopsCustomizeResourceContentVO baseQuery(Long contentId);

    void baseUpdate(DevopsCustomizeResourceContentVO devopsCustomizeResourceContentVO);

    void baseDelete(Long contentId);
}
