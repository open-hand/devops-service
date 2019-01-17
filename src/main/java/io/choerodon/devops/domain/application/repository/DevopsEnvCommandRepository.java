package io.choerodon.devops.domain.application.repository;

import java.util.Date;
import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.domain.application.entity.DevopsEnvCommandE;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

public interface DevopsEnvCommandRepository {

    DevopsEnvCommandE create(DevopsEnvCommandE devopsEnvCommandE);

    DevopsEnvCommandE queryByObject(String objectType, Long objectId);

    DevopsEnvCommandE update(DevopsEnvCommandE devopsEnvCommandE);

    DevopsEnvCommandE query(Long id);

    List<DevopsEnvCommandE> listByEnvId(Long envId);

    List<DevopsEnvCommandE> queryInstanceCommand(String objectType, Long objectId);

    Page<DevopsEnvCommandE> listByObject(PageRequest pageRequest, String objectType, Long objectId, Date startTime, Date endTime);
}
