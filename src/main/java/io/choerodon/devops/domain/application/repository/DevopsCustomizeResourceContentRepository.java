package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.domain.application.entity.DevopsCustomizeResourceContentE;

/**
 * Created by Sheep on 2019/6/26.
 */
public interface DevopsCustomizeResourceContentRepository {


    DevopsCustomizeResourceContentE create(DevopsCustomizeResourceContentE devopsCustomizeResourceContentE);


    DevopsCustomizeResourceContentE query(Long contentId);

    void update(DevopsCustomizeResourceContentE devopsCustomizeResourceContentE);


    void delete(Long contentId);
}
