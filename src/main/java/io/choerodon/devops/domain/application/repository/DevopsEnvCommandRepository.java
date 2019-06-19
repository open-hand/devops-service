package io.choerodon.devops.domain.application.repository;

import java.util.Date;
import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.domain.application.entity.DevopsEnvCommandE;

public interface DevopsEnvCommandRepository {

    DevopsEnvCommandE create(DevopsEnvCommandE devopsEnvCommandE);

    DevopsEnvCommandE queryByObject(String objectType, Long objectId);

    DevopsEnvCommandE update(DevopsEnvCommandE devopsEnvCommandE);

    DevopsEnvCommandE query(Long id);

    List<DevopsEnvCommandE> listByEnvId(Long envId);

    List<DevopsEnvCommandE> queryInstanceCommand(String objectType, Long objectId);

    PageInfo<DevopsEnvCommandE> listByObject(PageRequest pageRequest, String objectType, Long objectId, Date startTime, Date endTime);

    void deleteById(Long commandId);

    List<DevopsEnvCommandE> listByObjectAll(String objectType, Long objectId);

    void deleteCommandById(DevopsEnvCommandE commandE);
}
