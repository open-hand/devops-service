package io.choerodon.devops.domain.application.repository;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.domain.application.entity.DevopsProjectConfigE;

import java.util.List;

/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/11
 */
public interface DevopsProjectConfigRepository {
    DevopsProjectConfigE create(DevopsProjectConfigE devopsProjectConfigE);

    Boolean checkNameWithProjectUniqueness(DevopsProjectConfigE devopsProjectConfigE);

    DevopsProjectConfigE updateByPrimaryKeySelective(DevopsProjectConfigE devopsProjectConfigE);

    DevopsProjectConfigE queryByPrimaryKey(Long id);

    DevopsProjectConfigE queryByName(Long projectId, String name);

    DevopsProjectConfigE queryByNameWithNullProject(String name);

    PageInfo<DevopsProjectConfigE> listByOptions(Long projectId, PageRequest pageRequest, String params);

    void delete(Long id);

    List<DevopsProjectConfigE> queryByIdAndType(Long projectId, String type);

    void checkName(Long projectId, String name);

    Boolean checkIsUsed(Long checkIsUsed);
}
