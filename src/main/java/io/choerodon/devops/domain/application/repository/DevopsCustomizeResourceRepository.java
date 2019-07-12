package io.choerodon.devops.domain.application.repository;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.iam.entity.DevopsCustomizeResourceE;

/**
 * Created by Sheep on 2019/6/26.
 */
public interface DevopsCustomizeResourceRepository {


    DevopsCustomizeResourceE baseCreate(DevopsCustomizeResourceE devopsCustomizeResourceE);

    DevopsCustomizeResourceE baseQuery(Long resourceId);

    void baseUpdate(DevopsCustomizeResourceE devopsCustomizeResourceE);

    void baseDelete(Long resourceId);

    List<DevopsCustomizeResourceE> listByEnvAndFilePath(Long envId, String filePath);

    DevopsCustomizeResourceE queryByEnvIdAndKindAndName(Long envId, String kind, String name);

    DevopsCustomizeResourceE queryDetail(Long resourceId);

    PageInfo<DevopsCustomizeResourceE> pageDevopsCustomizeResourceE(Long envId, PageRequest pageRequest, String params);

    void checkExist(Long envId, String kind, String name);
}
