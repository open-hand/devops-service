package io.choerodon.devops.domain.application.repository;

import java.util.Date;
import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvCommandVO;

public interface DevopsEnvCommandRepository {

    DevopsEnvCommandVO baseCreate(DevopsEnvCommandVO devopsEnvCommandVO);

    DevopsEnvCommandVO baseQueryByObject(String objectType, Long objectId);

    DevopsEnvCommandVO baseUpdate(DevopsEnvCommandVO devopsEnvCommandE);

    DevopsEnvCommandVO baseQuery(Long id);

    List<DevopsEnvCommandVO> baseListByEnvId(Long envId);

    List<DevopsEnvCommandVO> baseListInstanceCommand(String objectType, Long objectId);

    PageInfo<DevopsEnvCommandVO> basePageByObject(PageRequest pageRequest, String objectType, Long objectId, Date startTime, Date endTime);

    void baseDelete(Long commandId);

    List<DevopsEnvCommandVO> baseListByObject(String objectType, Long objectId);

    void baseDeleteByEnvCommandId(DevopsEnvCommandVO commandE);
}
