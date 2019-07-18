package io.choerodon.devops.domain.application.repository;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.iam.entity.DevopsCustomizeResourceE;
import io.choerodon.devops.infra.dto.DevopsCustomizeResourceDTO;

/**
 * Created by Sheep on 2019/6/26.
 */
public interface DevopsCustomizeResourceRepository {


    DevopsCustomizeResourceDTO baseCreate(DevopsCustomizeResourceDTO devopsCustomizeResourceDTO);

    DevopsCustomizeResourceDTO baseQuery(Long resourceId);

    void baseUpdate(DevopsCustomizeResourceDTO devopsCustomizeResourceDTO);

    void baseDelete(Long resourceId);

    List<DevopsCustomizeResourceDTO> listByEnvAndFilePath(Long envId, String filePath);

    DevopsCustomizeResourceDTO queryByEnvIdAndKindAndName(Long envId, String kind, String name);

    DevopsCustomizeResourceDTO queryDetail(Long resourceId);

    PageInfo<DevopsCustomizeResourceDTO> pageDevopsCustomizeResourceE(Long envId, PageRequest pageRequest, String params);

    void checkExist(Long envId, String kind, String name);
}
