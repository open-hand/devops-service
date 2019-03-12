package io.choerodon.devops.domain.application.repository;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.domain.application.entity.DevopsProjectConfigE;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/11
 */
public interface DevopsProjectConfigRepository {
    DevopsProjectConfigE create(DevopsProjectConfigE devopsProjectConfigE);

    DevopsProjectConfigE updateByPrimaryKeySelective(DevopsProjectConfigE devopsProjectConfigE);

    DevopsProjectConfigE queryByPrimaryKey(Long id);

    Page<DevopsProjectConfigE> listByOptions(Long projectId, PageRequest pageRequest, String params);

    void delete(Long id);
}
