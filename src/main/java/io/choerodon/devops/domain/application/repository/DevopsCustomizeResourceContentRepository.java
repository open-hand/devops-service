package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.api.vo.iam.entity.DevopsCustomizeResourceContentVO;

/**
 * Created by Sheep on 2019/6/26.
 */
public interface DevopsCustomizeResourceContentRepository {


    DevopsCustomizeResourceContentVO baseCreate(DevopsCustomizeResourceContentVO devopsCustomizeResourceContentE);


    DevopsCustomizeResourceContentVO baseQuery(Long contentId);

    void baseUpdate(DevopsCustomizeResourceContentVO devopsCustomizeResourceContentE);


    void baseDelete(Long contentId);
}
